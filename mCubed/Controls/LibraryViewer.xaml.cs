using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Input;
using mCubed.Core;
using mCubed.MetaData;

namespace mCubed.Controls {
	public partial class LibraryViewer : UserControl {
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
				if (oldVal != null)
					oldVal.Refreshed -= new Action(viewer.OnRefreshed);
				var newVal = e.NewValue as Library;
				if (newVal != null)
					newVal.Refreshed += new Action(viewer.OnRefreshed);
			}
		}
		public static readonly DependencyProperty LibraryProperty =
			DependencyProperty.Register("Library", typeof(Library), typeof(LibraryViewer), new UIPropertyMetadata(null, new PropertyChangedCallback(OnLibraryChanged)));
		public Library Library {
			get { return (Library)GetValue(LibraryProperty); }
			set { SetValue(LibraryProperty, value); }
		}

		#endregion

		#region Data Store

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