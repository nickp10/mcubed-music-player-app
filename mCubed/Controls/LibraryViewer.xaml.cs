using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Collections.Specialized;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Input;
using System.Windows.Markup;
using mCubed.Core;
using mCubed.MetaData;

namespace mCubed.Controls {
	public partial class LibraryViewer : UserControl, INotifyPropertyChanged {
		#region INotifyPropertyChanged Members

		public event PropertyChangedEventHandler PropertyChanged;

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

		private ObservableCollection<ColumnVector> _displayColumns;
		private GridLength _prevMDIHeight = new GridLength(.4, GridUnitType.Star);

		#endregion

		#region Properties

		/// <summary>
		/// Get the collection view source that represents the items being displayed
		/// </summary>
		public CollectionViewSource CollectionItems {
			get {
				var ele = Content as FrameworkElement;
				return ele == null ? null : ele.FindResource("CollectionItems") as CollectionViewSource;
			}
		}

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
		public IEnumerable<MediaFile> SelectedItems { get { return SelectedMedia.SelectedItems.OfType<MediaFile>(); } }

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
				oldLibrary.Refreshed -= new Action(OnRefreshed);
				DisplayColumns.CollectionChanged -= new NotifyCollectionChangedEventHandler(OnDisplayCollectionChanged);
			}

			// Register the new library
			if (newLibrary != null) {
				newLibrary.Refreshed += new Action(OnRefreshed);
				DisplayColumns = newLibrary.ColumnSettings.Display;
				DisplayColumns.CollectionChanged += new NotifyCollectionChangedEventHandler(OnDisplayCollectionChanged);
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
		/// Event that handles when media should be removed from the library
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnMediaRemove(object sender, RoutedEventArgs e) {
			Library.RemoveMedia(SelectedItems);
		}

		/// <summary>
		/// Event that handles when the selected media should be loaded into the meta-data manager
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnMediaLoaded(object sender, RoutedEventArgs e) {
			SetMDIManager("Manually Loaded Media", SelectedItems.Select(mf => mf.MetaData));
		}

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
		/// Event that handles when the library view should be refreshed
		/// </summary>
		private void OnRefreshed() {
			if (CollectionItems != null)
				CollectionItems.View.Refresh();
		}

		/// <summary>
		/// The sorting method that handles sorting a grid view
		/// </summary>
		/// <param name="header">The header that was clicked</param>
		private void Sort(GridViewColumnHeader header) {
			ICollectionView view = CollectionItems == null ? null : CollectionItems.View;
			if (String.IsNullOrEmpty(header.Tag as string) || view == null)
				return;
			SortDescription ascending = new SortDescription(header.Tag as string, ListSortDirection.Ascending);
			SortDescription descending = new SortDescription(header.Tag as string, ListSortDirection.Descending);
			if (view.SortDescriptions.Contains(ascending)) {
				int index = view.SortDescriptions.IndexOf(ascending);
				view.SortDescriptions.RemoveAt(index);
				view.SortDescriptions.Insert(index, descending);
				header.ContentTemplate = Resources["GridViewHeaderDesc"] as DataTemplate;
			} else if (view.SortDescriptions.Contains(descending)) {
				view.SortDescriptions.Remove(descending);
				header.ContentTemplate = null;
			} else {
				view.SortDescriptions.Add(ascending);
				header.ContentTemplate = Resources["GridViewHeaderAsc"] as DataTemplate;
			}
			view.Refresh();
		}

		/// <summary>
		/// The action to be taken when a gridview header is clicked
		/// </summary>
		/// <param name="sender">The object sending the request</param>
		/// <param name="e">The arguments for the request</param>
		private void GridViewColumnHeaderClickedHandler(object sender, RoutedEventArgs e) {
			GridViewColumnHeader headerClicked = e.OriginalSource as GridViewColumnHeader;
			if (headerClicked != null && headerClicked.Role != GridViewColumnHeaderRole.Padding) {
				Sort(headerClicked);
			}
		}

		#endregion

		#region Column Event Handlers

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
					DisplayColumns.Move(e.OldStartingIndex - 1, e.NewStartingIndex - 1);
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
				StringBuilder xaml = new StringBuilder("<DataTemplate");
				xaml.Append(" xmlns=\"http://schemas.microsoft.com/winfx/2006/xaml/presentation\"");
				xaml.Append(" xmlns:x=\"http://schemas.microsoft.com/winfx/2006/xaml\"");
				xaml.Append(" xmlns:controls=\"clr-namespace:mCubed.Controls;assembly=" + GetType().Assembly.GetName().Name + "\"");
				xaml.Append(">");
				if (columnInfo.ColumnDetail.Type == ColumnType.Property)
					xaml.Append("<TextBlock Text=\"{Binding MetaData." + columnInfo.ColumnDetail.Key + "}\"/>");
				else
					xaml.Append("<TextBlock Text=\"{controls:Formula Name=" + columnInfo.ColumnDetail.Key + ", File={Binding}}\"/>");
				xaml.Append("</DataTemplate>");
				column.CellTemplate = (DataTemplate)XamlReader.Parse(xaml.ToString());

				// Setup the bindings
				BindingOperations.SetBinding(column, GridViewColumn.WidthProperty, new Binding { Source = columnInfo, Mode = BindingMode.TwoWay, Path = new PropertyPath("Width") });
				BindingOperations.SetBinding(header, GridViewColumnHeader.ContentProperty, new Binding { Source = columnInfo.ColumnDetail, Path = new PropertyPath("Display") });
				header.Tag = "MetaData." + columnInfo.ColumnDetail.Key;

				// Add the column
				LibraryGridView.Columns.Add(column);
			}
		}

		#endregion

		#region Members

		/// <summary>
		/// Set a collection of meta-data information in the meta-data manager
		/// </summary>
		/// <param name="key">The key for the collection to belong to</param>
		/// <param name="info">The collection of meta-data information</param>
		private void SetMDIManager(string key, IEnumerable<MetaDataInfo> info) {
			var manager = FindName("MetaDataManager") as MDIManager;
			if (manager != null)
				manager[key] = info;
		}

		#endregion
	}
}