using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Collections.Specialized;
using System.ComponentModel;
using System.IO;
using System.Linq;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Input;
using mCubed.Core;
using mCubed.MetaData;

namespace mCubed.Controls {
	public partial class LibraryViewer : UserControl, IExternalNotifyPropertyChanged, IExternalNotifyPropertyChanging {
		#region IExternalNotifyPropertyChanged Members

		public PropertyChangedEventHandler PropertyChangedHandler {
			get { return PropertyChanged; }
		}

		public event PropertyChangedEventHandler PropertyChanged;

		#endregion

		#region IExternalNotifyPropertyChanging Members

		public PropertyChangingEventHandler PropertyChangingHandler {
			get { return PropertyChanging; }
		}

		public event PropertyChangingEventHandler PropertyChanging;

		#endregion

		#region Dependency Property: GroupSortHeight

		public static readonly DependencyProperty GroupSortHeightProperty =
			DependencyProperty.Register("GroupSortHeight", typeof(double), typeof(LibraryViewer), new UIPropertyMetadata(0d));

		/// <summary>
		/// Get/set the height for the group by and sort by options [Bindable]
		/// </summary>
		public double GroupSortHeight {
			get { return (double)GetValue(GroupSortHeightProperty); }
			set { SetValue(GroupSortHeightProperty, value); }
		}

		#endregion

		#region Dependency Property: Library

		/// <summary>
		/// Event that handles when the library changed
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private static void OnLibraryChanged(DependencyObject sender, DependencyPropertyChangedEventArgs e) {
			var viewer = sender as LibraryViewer;
			if (viewer != null) {
				var oldVal = e.OldValue as Library;
				var newVal = e.NewValue as Library;
				viewer.OnLibraryChanged(oldVal, newVal);
			}
		}

		public static readonly DependencyProperty LibraryProperty =
			DependencyProperty.Register("Library", typeof(Library), typeof(LibraryViewer), new UIPropertyMetadata(null, new PropertyChangedCallback(OnLibraryChanged)));

		/// <summary>
		/// Get/set the library that is currently being viewed by this viewer
		/// </summary>
		public Library Library {
			get { return (Library)GetValue(LibraryProperty); }
			set { SetValue(LibraryProperty, value); }
		}

		#endregion

		#region Data Store

		private bool _isColumnCollectionChanging, _itemsChanging;
		private ObservableCollection<ColumnVector> _displayColumns;
		private GridLength _prevMDIHeight = new GridLength(.4, GridUnitType.Star);

		#endregion

		#region Properties

		/// <summary>
		/// Get the collection of column details that represents the displayed columns and its ordering [Bindable]
		/// </summary>
		public ObservableCollection<ColumnVector> DisplayColumns {
			get { return _displayColumns; }
			private set { this.SetAndNotify(ref _displayColumns, value, null, OnDisplayColumnsChanged, "DisplayColumns"); }
		}

		/// <summary>
		/// Get the collection of currently selected items
		/// </summary>
		public IEnumerable<MediaFile> SelectedItems {
			get { return SelectedMedia.SelectedItems.OfType<MediaFile>(); }
			set {
				_itemsChanging = true;
				SelectedMedia.SelectedItems.Clear();
				foreach (var file in value)
					SelectedMedia.SelectedItems.Add(file);
				_itemsChanging = false;
				OnSelectedMediaChanged(SelectedMedia, null);
			}
		}

		#endregion

		#region Constructor

		public LibraryViewer() {
			// Set up event handlers
			Utilities.MainSettings.ShowMDIManagerChanged += new Action(OnShowMDIManagerChanged);
			Loaded += new RoutedEventHandler(OnLoaded);

			// Initialize
			InitializeComponent();

			// Setup column reorder event after initilization
			LibraryGridView.Columns.CollectionChanged += new NotifyCollectionChangedEventHandler(OnColumnCollectionChanged);
		}

		#endregion

		#region Media Event Handlers

		/// <summary>
		/// Event that handles when the media should be played directly
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnMediaFilePlay(object sender, RoutedEventArgs e) {
			OnMediaFilePlay(sender, (MouseButtonEventArgs)null);
		}

		/// <summary>
		/// Event that handles when the media should be played directly
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnMediaFilePlay(object sender, MouseButtonEventArgs e) {
			var ele = sender as FrameworkElement;
			var file = ele == null ? null : ele.DataContext as MediaFile;
			if (file != null)
				file.Play();
		}

		/// <summary>
		/// Event that handles when the media file should be shown in Windows Explorer
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnMediaFileShowExplorer(object sender, RoutedEventArgs e) {
			var ele = sender as FrameworkElement;
			var file = ele == null ? null : ele.DataContext as MediaFile;
			if (file != null) {
				System.Diagnostics.Process.Start("explorer.exe", "/select,\"" + file.MetaData.FilePath + "\"");
			}
		}

		/// <summary>
		/// Event that handles when a command should be executed with the selected media
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnMediaExecuteCommand(object sender, RoutedEventArgs e) {
			// Grab the command
			var ele = sender as MenuItem;
			var command = ele == null ? null : ele.DataContext as Command;
			if (command != null) {
				// Generate the %c placeholder value
				string currentPath = "";
				var par = ele.CommandParameter as FrameworkElement;
				var file = par == null ? null : par.DataContext as MediaFile;
				if (file != null) {
					currentPath = "\"" + file.MetaData.FilePath + "\"";
				}

				// Generate the %s placeholder value
				string selectedPath = "";
				var files = SelectedItems.ToArray();
				if (files.Length > 0) {
					selectedPath = files.Select(f => "\"" + f.MetaData.FilePath + "\"").Aggregate((s1, s2) => s1 += "," + s2);
				}

				// Execute the command
				Func<string, string> replace = s => s.Replace("%c", currentPath).Replace("%s", selectedPath);
				string value = command.Value;
				int index = value.IndexOf(' ');
				if (index > -1) {
					System.Diagnostics.Process.Start(replace(value.Substring(0, index)), replace(value.Substring(index + 1)));
				} else {
					System.Diagnostics.Process.Start(replace(value));
				}
			}
		}

		/// <summary>
		/// Event that handles when the selected media should be copied to a new location
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnMediaCopyTo(object sender, RoutedEventArgs e) {
			string destDir = GetDestinationDirectory(sender);
			if (destDir != null) {
				Library destLib = GetDestinationLibrary(sender);
				if (destLib != null) {
					var items = SelectedItems.ToArray();
					Utilities.MainProcessManager.AddProcess(process =>
					{
						foreach (var item in items) {
							FileUtilities.Copy(item, destLib, destDir);
							process.CompletedCount++;
						}
					}, "Copying files", items.Length);
				}
			}
		}

		/// <summary>
		/// Event that handles when the selected media should be moved to a new location
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnMediaMoveTo(object sender, RoutedEventArgs e) {
			string destDir = GetDestinationDirectory(sender);
			if (destDir != null) {
				Library destLib = GetDestinationLibrary(sender);
				if (destLib != null) {
					var items = SelectedItems.ToArray();
					Utilities.MainProcessManager.AddProcess(process =>
					{
						foreach (var item in items) {
							FileUtilities.Move(item, destLib, destDir);
							process.CompletedCount++;
						}
					}, "Moving files", items.Length);
				}
			}
		}

		/// <summary>
		/// Event that handles when media should be removed from the library
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnMediaRemove(object sender, RoutedEventArgs e) {
			var items = SelectedItems.ToArray();
			Library.RemoveMedia(items);
		}

		/// <summary>
		/// Event that handles when media should be removed from the library and the computer
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnMediaRemoveComputer(object sender, RoutedEventArgs e) {
			var items = SelectedItems.ToArray();
			if (items.Length > 0 && mCubedError.ShowConfirm(string.Format("Are you sure you want to permanently remove the {0} selected file(s)?", items.Length))) {
				Library.RemoveMedia(items);
				foreach (var item in items) {
					FileUtilities.Delete(item);
				}
			}
		}

		/// <summary>
		/// Event that handles when media should be added to the library
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnMediaAdd(object sender, RoutedEventArgs e) {
			Library.AddMedia(Library.GenerateMedia());
		}

		/// <summary>
		/// Event that handles when the current library should be reloaded
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnMediaReload(object sender, RoutedEventArgs e) {
			Library.Reload();
		}

		/// <summary>
		/// Event that handles when the now playing media should be scrolled into view
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnMediaSeekToNowPlaying(object sender, RoutedEventArgs e) {
			var nowPlaying = Library.MediaFileCurrent;
			if (nowPlaying != null) {
				SelectedMedia.ScrollIntoView(nowPlaying);
				SelectedMedia.SelectedItem = nowPlaying;
			}
		}

		/// <summary>
		/// Event that handles when the selected items' track numbers should be auto-populated from 1 to N, where N is the number of selected items
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnMediaAutoPopulateTrack(object sender, RoutedEventArgs e) {
			var items = SelectedItems.ToArray();
			Utilities.MainProcessManager.AddProcess(process =>
			{
				uint currentTrack = 1;
				foreach (var item in items) {
					item.Parent.MediaFiles.BeginTransaction();
					item.MetaData.Track = currentTrack++;
					item.MetaData.Save();
					item.Parent.MediaFiles.EndTransaction();
					process.CompletedCount++;
				}
			}, "Auto-populating track numbers", items.Length);
		}

		/// <summary>
		/// Event that handles when the selected items should be auto-renamed and located in their proper location
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnMediaAutoRename(object sender, RoutedEventArgs e) {
			var items = SelectedItems.ToArray();
			Utilities.MainProcessManager.AddProcess(process =>
			{
				foreach (var item in items) {
					FileUtilities.Rename(item);
					process.CompletedCount++;
				}
			}, "Auto-renaming files", items.Length);
		}

		/// <summary>
		/// Event that handles when the selected media should be loaded into the meta-data manager
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnMediaLoaded(object sender, RoutedEventArgs e) {
			SetMDIManager("Manually Loaded Media", SelectedItems.Select(mf => mf.MetaData));
		}

		#endregion

		#region Event Handlers

		/// <summary>
		/// Event that handles when the viewer has loaded
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnLoaded(object sender, RoutedEventArgs e) {
			if (Utilities.MainSettings.LibraryCurrent != null)
				OnNowPlayingChanged(Utilities.MainSettings.LibraryCurrent.MediaFileCurrent);
			Utilities.MainSettings.NowPlayingChanged += new Action<MediaFile>(OnNowPlayingChanged);
		}

		/// <summary>
		/// Event that handles when the library being viewed changed
		/// </summary>
		/// <param name="oldLibrary">The previous library value</param>
		/// <param name="newLibrary">The new library value</param>
		private void OnLibraryChanged(Library oldLibrary, Library newLibrary) {
			// Unregister the old library
			if (oldLibrary != null) {
				DisplayColumns.CollectionChanged -= new NotifyCollectionChangedEventHandler(OnDisplayCollectionChanged);
			}

			// Register the new library
			if (newLibrary != null) {
				DisplayColumns = newLibrary.ColumnSettings.Display;
				DisplayColumns.CollectionChanged += new NotifyCollectionChangedEventHandler(OnDisplayCollectionChanged);
			}
		}

		/// <summary>
		/// Event that handles when media is dragged and dropped into the library viewer
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnMediaFileDrop(object sender, DragEventArgs e) {
			// Read the data
			var data = e.Data.GetData(DataFormats.FileDrop);
			var files = data as string[];
			if (files == null) {
				var tempFiles = data as FileInfo[];
				if (tempFiles != null) {
					files = tempFiles.Select(f => f.FullName).ToArray();
				}
			}

			// Generate the media
			if (files != null) {
				Library.GenerateMediaFromDragDrop(files);
			}
		}

		/// <summary>
		/// Event that handles when the now playing media has changed
		/// </summary>
		/// <param name="file">The media that is now playing</param>
		private void OnNowPlayingChanged(MediaFile file) {
			SetMDIManager("Now Playing", file == null ? null : new[] { file.MetaData });
		}

		/// <summary>
		/// Event that handles when the selected media has changed
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnSelectedMediaChanged(object sender, SelectionChangedEventArgs e) {
			if (!_itemsChanging)
				SetMDIManager("Selected Media", SelectedItems.Select(mf => mf.MetaData));
		}

		/// <summary>
		/// Event that handles when the visiblity of the MDI manager should change
		/// </summary>
		private void OnShowMDIManagerChanged() {
			// Show or hide the MDI manager according
			if (Utilities.MainSettings.ShowMDIManager) {
				MDIRow.Height = _prevMDIHeight;
				MDISplitter.Visibility = Visibility.Visible;
				MetaDataManager.Visibility = Visibility.Visible;
			} else {
				_prevMDIHeight = MDIRow.Height;
				MDIRow.Height = new GridLength();
				MDISplitter.Visibility = Visibility.Collapsed;
				MetaDataManager.Visibility = Visibility.Collapsed;
			}
		}

		/// <summary>
		/// Event that handles when a media file has been selected
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnMediaFileSelected(object sender, MouseButtonEventArgs e) {
			var element = sender as FrameworkElement;
			var group = element == null ? null : element.DataContext as GroupList<MediaFile>;
			if (group != null) {
				if ((Keyboard.Modifiers & ModifierKeys.Control) == ModifierKeys.Control) {
					SelectedItems = SelectedItems.ToArray().Union(group);
				} else {
					SelectedItems = group;
				}
				e.Handled = true;
			}
		}

		#endregion

		#region Column Event Handlers

		/// <summary>
		/// Event that handles when a grid view column header is clicked
		/// </summary>
		/// <param name="sender">The object sending the request</param>
		/// <param name="e">The arguments for the request</param>
		private void OnGridViewColumnHeaderClicked(object sender, RoutedEventArgs e) {
			var headerClicked = e.OriginalSource as GridViewColumnHeader;
			if (headerClicked != null && headerClicked.Role != GridViewColumnHeaderRole.Padding)
				DisplayColumnSelector.IsOpen = true;
		}

		/// <summary>
		/// Event that handles when the grid view column collection itself changes
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnColumnCollectionChanged(object sender, NotifyCollectionChangedEventArgs e) {
			if (e.Action == NotifyCollectionChangedAction.Move) {
				if (e.OldStartingIndex == 0 || e.NewStartingIndex == 0) {
					var item = LibraryGridView.Columns[e.NewStartingIndex];
					LibraryGridView.Columns.RemoveAt(e.NewStartingIndex);
					LibraryGridView.Columns.Insert(e.OldStartingIndex, item);
				} else {
					_isColumnCollectionChanging = true;
					DisplayColumns.Move(e.OldStartingIndex - 1, e.NewStartingIndex - 1);
					_isColumnCollectionChanging = false;
				}
			}
		}

		/// <summary>
		/// Event that handles when the collection of columns being displayed changes to a new collection
		/// </summary>
		private void OnDisplayColumnsChanged() {
			OnDisplayCollectionChanged(null, null);
		}

		/// <summary>
		/// Event that handles when the display column collection has changed
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnDisplayCollectionChanged(object sender, NotifyCollectionChangedEventArgs e) {
			// Ignore this event if it came from the grid view itself
			if (_isColumnCollectionChanging)
				return;

			// Clear the original columns
			while (LibraryGridView.Columns.Count > 1)
				LibraryGridView.Columns.RemoveAt(1);

			// Add all the additional columns
			foreach (var columnInfo in DisplayColumns) {
				// Create the column
				var column = new GridViewColumn();
				var header = new GridViewColumnHeader();
				column.Header = header;

				// Setup the data template
				var factory = new FrameworkElementFactory(typeof(TextBlock));
				if (columnInfo.ColumnDetail.Type == ColumnType.Property) {
					var binding = new Binding { Path = new PropertyPath("MetaData." + columnInfo.ColumnDetail.Key) };
					factory.SetBinding(TextBlock.TextProperty, binding);
				} else {
					Formula.BindFormula(factory, columnInfo.ColumnDetail.Formula, new Binding(), TextBlock.TextProperty);
				}
				column.CellTemplate = new DataTemplate { VisualTree = factory };

				// Setup the bindings
				BindingOperations.SetBinding(column, GridViewColumn.WidthProperty, new Binding { Source = columnInfo, Mode = BindingMode.TwoWay, Path = new PropertyPath("Width") });
				BindingOperations.SetBinding(header, GridViewColumnHeader.ContentProperty, new Binding { Source = columnInfo.ColumnDetail, Path = new PropertyPath("Display") });
				header.Tag = "MetaData." + columnInfo.ColumnDetail.Key;

				// Add the column
				LibraryGridView.Columns.Add(column);
			}
		}

		/// <summary>
		/// Event that handles when a display column has been deselected
		/// </summary>
		/// <param name="column">The column that has been deselected</param>
		private void OnDisplayColumnDeselected(ColumnDetail column) {
			var vectors = Library.ColumnSettings.Display.Where(vector => vector.ColumnDetail == column).ToArray();
			foreach (var vector in vectors)
				Library.ColumnSettings.Display.Remove(vector);
			DisplayColumnSelector.IsOpen = false;
		}

		/// <summary>
		/// Event that handles when a display column has been selected
		/// </summary>
		/// <param name="column">The column that has been selected</param>
		private void OnDisplayColumnSelected(ColumnDetail column) {
			Library.ColumnSettings.Display.Add(new ColumnVector(column));
			DisplayColumnSelector.IsOpen = false;
		}

		#endregion

		#region Members

		/// <summary>
		/// Retrieves the selected destination directory from the selected menu item
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <returns>The selected destination directory from the selected menu item</returns>
		private string GetDestinationDirectory(object sender) {
			// Retrieve the menu item
			var menu = sender as MenuItem;
			if (menu == null || menu.Role != MenuItemRole.SubmenuItem) {
				return null;
			}

			// Return the data context if it's a string
			var dir = menu.DataContext as string;
			if (dir != null) {
				return dir;
			}

			// Return the library's first directory
			var lib = menu.DataContext as Library;
			if (lib != null) {
				return lib.Directories.FirstOrDefault();
			}
			return null;
		}

		/// <summary>
		/// Retrieves the selected destination library from the selected menu item
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <returns>The selected destination library from the selected menu item</returns>
		private Library GetDestinationLibrary(object sender) {
			// Retrieve the menu item
			var menu = sender as MenuItem;
			if (menu == null || menu.Role != MenuItemRole.SubmenuItem) {
				return null;
			}

			// Return the data context if it's a library
			var lib = menu.DataContext as Library;
			if (lib != null) {
				return lib;
			}

			// Return the tag if it's a library
			return menu.Tag as Library;
		}

		/// <summary>
		/// Set a collection of meta-data information in the meta-data manager
		/// </summary>
		/// <param name="key">The key for the collection to belong to</param>
		/// <param name="info">The collection of meta-data information</param>
		private void SetMDIManager(string key, IEnumerable<MetaDataInfo> info) {
			if (Dispatcher.CheckAccess()) {
				var manager = FindName("MetaDataManager") as MDIManager;
				if (manager != null)
					manager[key] = info;
			} else {
				Dispatcher.Invoke(new Action<string, IEnumerable<MetaDataInfo>>(SetMDIManager), key, info);
			}
		}

		#endregion
	}
}