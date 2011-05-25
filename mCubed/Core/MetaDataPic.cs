using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.IO;
using System.Linq;
using System.Text.RegularExpressions;
using System.Windows;
using System.Windows.Media.Imaging;

namespace mCubed.Core {
	public class MetaDataPic : IEquatable<MetaDataPic>, ICopiable<MetaDataPic>, IExternalNotifyPropertyChanged, IExternalNotifyPropertyChanging, IDisposable {
		#region Data Store

		private static IEnumerable<string> _pictureTypes =
			Enum.GetNames(typeof(TagLib.PictureType)).
			Where(str => str != "FileIcon" && str != "OtherFileIcon").
			Select(str => str.ToReadableString()).
			OrderBy(str => str).ToArray();
		private byte[] _data;
		private string _description = string.Empty;
		private IListener<MetaDataPic> _eventListener;
		private int _invokePicEvents;
		private TagLib.PictureType _type = TagLib.PictureType.FrontCover;

		#endregion

		#region Binadable Properties

		/// <summary>
		/// Get/set the picture data [Bindable]
		/// </summary>
		public byte[] Data {
			get { return _data; }
			set { this.SetAndNotify(ref _data, value, OnPicChanging, OnPicChanged, "Data", "Image", "MimeType"); }
		}

		/// <summary>
		/// Get/set the description for the picture [Bindable]
		/// </summary>
		public string Description {
			get { return _description; }
			set { this.SetAndNotify(ref _description, value ?? string.Empty, OnPicChanging, OnPicChanged, "Description"); }
		}

		/// <summary>
		/// Get/set the event listener for any changes on this picture [Bindable]
		/// </summary>
		public IListener<MetaDataPic> EventListener {
			get { return _eventListener; }
			set { this.SetAndNotify(ref _eventListener, value, "EventListener"); }
		}

		/// <summary>
		/// Get the bitmap version of the picture for displaying purposes [Bindable]
		/// </summary>
		public BitmapSource Image {
			get {
				try {
					var bm = new BitmapImage();
					bm.BeginInit();
					bm.StreamSource = new MemoryStream(Data);
					bm.EndInit();
					return bm;
				} catch {
					UpdatePicture(MetaDataPicResource.InvalidFormat);
					return Image;
				}
			}
		}

		/// <summary>
		/// Get the mime-type of the picture [Bindable]
		/// </summary>
		public string MimeType { get { return Data.MimeTypeFromBytes(); } }

		/// <summary>
		/// Get a collection of all the available picture types [Bindable]
		/// </summary>
		public IEnumerable<string> PictureTypes {
			get { return MetaDataPic._pictureTypes; }
		}

		/// <summary>
		/// Get/set the picture type [Bindable]
		/// </summary>
		public TagLib.PictureType Type {
			get { return _type; }
			set { this.SetAndNotify(ref _type, value, OnPicChanging, OnPicChanged, "Type", "TypeString"); }
		}

		/// <summary>
		/// Get/set the picture type in a human-readable format [Bindable]
		/// </summary>
		public string TypeString {
			get { return Type.ToReadableString(); }
			set { Type = value.ToEnumType<TagLib.PictureType>(); }
		}

		#endregion

		#region Constructors

		/// <summary>
		/// Create a new metadata picture information object from a default image
		/// </summary>
		public MetaDataPic() {
			UpdatePicture(MetaDataPicResource.Default);
		}

		/// <summary>
		/// Create a new meta-data picture information object as a clone of an existing meta-data picture object
		/// </summary>
		/// <param name="picture">The existing meta-data picture object to clonse</param>
		public MetaDataPic(MetaDataPic picture) {
			UpdatePicture(picture);
		}

		/// <summary>
		/// Create a new meta-data picture information object from a TagLib picture
		/// </summary>
		/// <param name="picture">The TagLib picture to create from</param>
		public MetaDataPic(TagLib.IPicture picture) {
			UpdatePicture(picture);
		}

		#endregion

		#region Event Handlers

		/// <summary>
		/// Invoke the picture changing event if necessary
		/// </summary>
		protected void OnPicChanging() {
			if (_invokePicEvents++ == 0 && EventListener != null)
				EventListener.OnSpeakerChanging(this);
		}

		/// <summary>
		/// Invoke the picture changed event if necessary
		/// </summary>
		protected void OnPicChanged() {
			if (_invokePicEvents-- == 1 && EventListener != null)
				EventListener.OnSpeakerChanged(this);
		}

		/// <summary>
		/// Invoke the picture deleting event if necessary
		/// </summary>
		protected void OnPicDeleting() {
			if (EventListener != null)
				EventListener.OnSpeakerDeleting(this);
		}

		/// <summary>
		/// Invoke the picture deleted event if necessary
		/// </summary>
		protected void OnPicDeleted() {
			if (EventListener != null)
				EventListener.OnSpeakerDeleted(this);
		}

		#endregion

		#region File I/O Members

		/// <summary>
		/// Browse for a picture to replace the current picture
		/// </summary>
		/// <returns>True if a file was selected, or false otherwise</returns>
		public bool BrowseDialog() {
			// Create an open dialog and show
			var dlg = new Microsoft.Win32.OpenFileDialog
			{
				DefaultExt = "jpg",
				Filter = Utilities.FilterImage,
				FilterIndex = 3,
				InitialDirectory = Utilities.MainSettings.DirectoryPictureDefault
			};
			bool result = dlg.ShowDialog() ?? false;

			// Update the picture
			if (result && File.Exists(dlg.FileName))
				Data = File.ReadAllBytes(dlg.FileName);

			// Now return
			return result;
		}

		/// <summary>
		/// Save the meta-data picture information to a file
		/// </summary>
		public void SaveToFile() {
			// Create the save dialog and show
			var dlg = new Microsoft.Win32.SaveFileDialog
			{
				AddExtension = true,
				DefaultExt = MimeType.Split('/')[1].Replace("jpeg", "jpg"),
				Filter = Utilities.FilterImage,
				FilterIndex = 5,
				InitialDirectory = Utilities.MainSettings.DirectoryPictureDefault
			};

			// Check if save filename exists
			if (dlg.ShowDialog().Value) {
				// Save the picture to the save filename
				File.WriteAllBytes(dlg.FileName, Data);
			}
		}

		#endregion

		#region Members

		/// <summary>
		/// Copy the information from an object into this object
		/// </summary>
		/// <param name="obj">The object to copy the information from</param>
		public void CopyFrom(MetaDataPic obj) {
			UpdatePicture(obj);
		}

		/// <summary>
		/// Delete the picture by invoking the picture deleting and deleted events
		/// </summary>
		public void Delete() {
			OnPicDeleting();
			OnPicDeleted();
		}

		/// <summary>
		/// Check if the given meta-data picture is equivalent to the current one
		/// </summary>
		/// <param name="obj">The meta-data picture to compare to</param>
		/// <returns>Returns true if the two objects are equivalent, or false otherwise</returns>
		public bool Equals(MetaDataPic other) {
			return other != null && Description == other.Description && Type == other.Type && Data.SequenceEqual(other.Data);
		}

		/// <summary>
		/// Update the picture information without invoking the picture changed events
		/// </summary>
		/// <param name="picture">The picture to update to</param>
		public void UpdatePicture(MetaDataPic picture) {
			UpdatePicture(picture, false);
		}

		/// <summary>
		/// Update the picture information while choosing whether or not the picture changed events are invoked
		/// </summary>
		/// <param name="picture">The picture to update to</param>
		/// <param name="invokeEvents">True to invoke the picture changed events, or false otherwise</param>
		public void UpdatePicture(MetaDataPic picture, bool invokeEvents) {
			// Check the picture first
			if (picture != null) {
				// Invoke the changing event
				if (invokeEvents)
					OnPicChanging();
				else
					_invokePicEvents++;

				// Update the information
				Data = picture.Data;
				Description = picture.Description;
				Type = picture.Type;

				// Invoke the changed event
				if (invokeEvents)
					OnPicChanged();
				else
					_invokePicEvents--;
			}
		}

		/// <summary>
		/// Update the picture information from a predefined list of picture resources
		/// </summary>
		/// <param name="pictureResource">The picture resource to update to</param>
		public void UpdatePicture(MetaDataPicResource pictureResource) {
			var stream = Application.GetResourceStream(new Uri("pack://application:,,,/Images/" + pictureResource.ToString() + ".png", UriKind.Absolute)).Stream;
			byte[] bytes = new byte[stream.Length];
			stream.Read(bytes, 0, bytes.Length);
			Data = bytes;
		}

		#endregion

		#region TagLib Members

		/// <summary>
		/// Update the picture information from a TagLib picture
		/// </summary>
		/// <param name="picture">The picture to update to</param>
		public void UpdatePicture(TagLib.IPicture picture) {
			// Check the picture first
			if (picture != null) {
				// Update the information
				OnPicChanging();
				Data = picture.Data.Data;
				Description = picture.Description;
				Type = picture.Type;
				OnPicChanged();
			}
		}

		/// <summary>
		/// Generate a TagLib picture from the picture information
		/// </summary>
		/// <returns></returns>
		public TagLib.IPicture GenerateTagLib() {
			return new TagLib.Id3v2.AttachedPictureFrame()
			{
				TextEncoding = TagLib.StringType.Latin1,
				Description = Description ?? string.Empty,
				Type = Type,
				MimeType = MimeType,
				Data = Data
			};
		}

		#endregion

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

		#region IDisposable Members

		/// <summary>
		/// Dispose of the meta-data picture properly
		/// </summary>
		public void Dispose() {
			// Unsubscribe others from its events
			PropertyChanged = null;
			PropertyChanging = null;
		}

		#endregion
	}
}