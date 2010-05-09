using System;
using System.Collections;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using mCubed.Classes;
using System.Windows.Input;
using System.Windows.Controls.Primitives;

namespace mCubed.Controls
{
	/// <summary>
	/// Interaction logic for TagEditor.xaml
	/// </summary>
	public partial class TagEditor : UserControl, IListener<TagEditorPic>
	{
		#region Dependency Properties

		private static void MediaFilesChanged(DependencyObject sender, DependencyPropertyChangedEventArgs e)
		{
			TagEditor te = sender as TagEditor;
			if (te != null)
				te.MediaFilesChanged();
		}

		public static readonly DependencyProperty MediaFilesProperty =
			DependencyProperty.Register("MediaFiles", typeof(IEnumerable<MediaFile>), typeof(TagEditor), new UIPropertyMetadata(null, new PropertyChangedCallback(MediaFilesChanged)));
		public static readonly DependencyProperty PicListProperty =
			DependencyProperty.Register("PicList", typeof(IEnumerable<TagEditorPic>), typeof(TagEditor), new UIPropertyMetadata(null));
		public static readonly DependencyProperty IsMultiFilesProperty =
			DependencyProperty.Register("IsMultiFiles", typeof(bool), typeof(TagEditor), new UIPropertyMetadata(false));
		public static readonly DependencyProperty MediaFilesSelectorProperty =
			DependencyProperty.Register("MediaFilesSelector", typeof(Selector), typeof(TagEditor), new UIPropertyMetadata(null));

		public IEnumerable<MediaFile> MediaFiles
		{
			get { return (IEnumerable<MediaFile>)GetValue(MediaFilesProperty); }
			set { SetValue(MediaFilesProperty, value); }
		}
		public IEnumerable<TagEditorPic> PicList
		{
			get { return (IEnumerable<TagEditorPic>)GetValue(PicListProperty); }
			set { SetValue(PicListProperty, value); }
		}
		public bool IsMultiFiles
		{
			get { return (bool)GetValue(IsMultiFilesProperty); }
			set { SetValue(IsMultiFilesProperty, value); }
		}
		public Selector MediaFilesSelector
		{
			get { return (Selector)GetValue(MediaFilesSelectorProperty); }
			set { SetValue(MediaFilesSelectorProperty, value); }
		}

		#endregion

		#region Properties

		private bool IsDupsHidden { get { return Duplicates.IsChecked.Value; } }
		private string Directory { get { return MediaFiles.Select(file => System.IO.Path.GetDirectoryName(file.MetaData.FilePath)).FirstOrDefault() ?? Environment.CurrentDirectory; } }
		private IEnumerable<TagEditorInfo> TEI { get { return Track.Children.OfType<TagEditorInfo>().Union(ArtistAlbum.Children.OfType<TagEditorInfo>()); } }
		private IEnumerable<TagEditorPic> _picsChanging;

		#endregion

		#region Constructor

		/// <summary>
		/// Create the tag editor control
		/// </summary>
		public TagEditor()
		{
			InitializeComponent();
			AddHandler(TagEditor.KeyDownEvent, new KeyEventHandler(TagEditor_KeyDown), true);
		}

		#endregion

		#region Event Handlers

		private void TagEditor_KeyDown(object sender, KeyEventArgs e)
		{
			if ((Keyboard.Modifiers & ModifierKeys.Control) == ModifierKeys.Control && (e.Key == Key.Enter || e.Key == Key.Return))
			{
				Button_Save_Click(null, null);
				if (MediaFilesSelector != null)
				{
					int index = MediaFilesSelector.SelectedIndex;
					index = IsMultiFiles ? index : index + ((Keyboard.Modifiers & ModifierKeys.Shift) == ModifierKeys.Shift ? -1 : 1 );
					index = index < 0 || index >= MediaFilesSelector.Items.Count ? 0 : index;
					MediaFilesSelector.SelectedIndex = -1;
					MediaFilesSelector.SelectedIndex = index;
				}
			}
		}

		/// <summary>
		/// Event that handles when the tag information is saved
		/// </summary>
		/// <param name="sender">Sender object</param>
		/// <param name="e">Event arguments</param>
		private void Button_Save_Click(object sender, RoutedEventArgs e)
		{
			// Pre-stuff
			List<TagEditorInfo> tei = TEI.Where(t => t.TagHeader.IsChecked).ToList();
			IEnumerable<TagEditorPic> pics = PicListControl.ItemsSource.OfType<TagEditorPic>();

			// Loop through all the selected files and update the information
			foreach (MediaFile file in MediaFiles)
			{
				// Update all the properties
				tei.ForEach(t => t.Update(file));

				// Update the pictures
				if(PicsHeader.IsChecked)
					file.MediaTag.Pics = pics;

				// Save the file
				if (tei.Count > 0 || PicsHeader.IsChecked)
				{
					//file.Save();
					var obj = file.Parent.MediaObject.UnlockFile(file);
					file.MediaTag.Save();
					file.Parent.MediaObject.RestoreState(obj);
				}
			}
		}

		/// <summary>
		/// Event that handles when the changes are canceled
		/// </summary>
		/// <param name="sender">Sender object</param>
		/// <param name="e">Event arguments</param>
		private void Button_Cancel_Click(object sender, RoutedEventArgs e)
		{
			MediaFilesChanged();
		}

		/// <summary>
		/// Event that handles when duplicates should be shown or hidden
		/// </summary>
		/// <param name="sender">Sender object</param>
		/// <param name="e">Event arguments</param>
		private void Button_ToggleDups_Click(object sender, RoutedEventArgs e)
		{
			UpdatePicSource();
		}

		#endregion

		#region TagEditorPic Event Handlers

		/// <summary>
		/// Event that handles when a picture is added
		/// </summary>
		/// <param name="sender">Sender object</param>
		/// <param name="e">Event arguments</param>
		private void TagEditorPicAdd(object sender, RoutedEventArgs e)
		{
			TagEditorPic pic = new TagEditorPic() { EventListener = this };
			if (pic.BrowseDialog())
			{
				PicList = PicList.Concat(new TagEditorPic[] { pic }).ToArray();
				UpdatePicSource();
			}
		}

		/// <summary>
		/// Event than handles when a picture should be deleted
		/// </summary>
		/// <param name="pic">The picture that should be deleted</param>
		public void OnSpeakerDeleting(TagEditorPic pic)
		{
			// Check if there are hidden duplicates and ask permission to delete them
			int count = PicList.Count(p => pic.Equals(p)) - 1;
			if (!IsDupsHidden || count <= 0 ||
			    MessageBox.Show("This will remove " + count.ToString() + " duplicate(s) as well, continue?", "Remove Duplicates", MessageBoxButton.YesNo, MessageBoxImage.Warning, MessageBoxResult.No) == MessageBoxResult.Yes)
			{
				// Remove duplicates
				PicList = GetPicList(pic, false);
				UpdatePicSource();
			}
		}

		public void OnSpeakerDeleted(TagEditorPic pic) { }

		/// <summary>
		/// Event that handles when a picture is changing
		/// </summary>
		/// <param name="pic">The picture that is being changed</param>
		public void OnSpeakerChanging(TagEditorPic pic)
		{
			_picsChanging = GetPicList(pic, true);
		}

		/// <summary>
		/// Event that handles when a picture has changed
		/// </summary>
		/// <param name="pic">The picture that actually changed</param>
		public void OnSpeakerChanged(TagEditorPic pic)
		{
			if (_picsChanging != null)
				_picsChanging.CopyEachFrom(pic);
			_picsChanging = null;
			UpdatePicSource();
		}

		#endregion

		#region Members

		/// <summary>
		/// Get the list of pictures to update when one picture is updated
		/// </summary>
		/// <param name="pic">The picture to get the equivalent pictures of</param>
		/// <param name="equal">True if the pictures that are equivalent should be returned, or false if all non-equivalent pictures should be returned</param>
		/// <returns>A list of pictures that will receive the update</returns>
		private IEnumerable<TagEditorPic> GetPicList(TagEditorPic pic, bool equal)
		{
			return PicList.Where(p => p != pic && (IsDupsHidden && p.Equals(pic)) == equal).ToArray();
		}

		/// <summary>
		/// The logic to run when the media file changes
		/// </summary>
		private void MediaFilesChanged()
		{
			// Add pictures to the list if a file exists and update the display
			if (MediaFiles != null)
			{
				IsMultiFiles = MediaFiles.Count() > 1;
				PicsHeader.IsChecked = !IsMultiFiles;
				TEI.ToList().ForEach(t => t.MediaFilesChanged(MediaFiles));
				PicList = MediaFiles.SelectMany(file => file.MediaTag.Pics).ToArray();
				PicList.ToList().ForEach(p => p.EventListener = this);
				UpdatePicSource();
			}
		}

		/// <summary>
		/// Updates the picture source information
		/// </summary>
		private void UpdatePicSource()
		{
			PicListControl.ItemsSource = (IsDupsHidden) ? PicList.DistinctEquals() : PicList;
		}

		#endregion
	}
}