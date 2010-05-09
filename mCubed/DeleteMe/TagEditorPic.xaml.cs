using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using System.ComponentModel;
using System.IO;
using System.Text.RegularExpressions;
using System.Windows.Resources;
using mCubed.Classes;

namespace mCubed.Controls
{
	public enum TagEditorPicResource { Default, InvalidFormat }

	/// <summary>
	/// Interaction logic for TagEditorPic.xaml
	/// </summary>
	public partial class TagEditorPic : UserControl, INotifyPropertyChanged, IEquatable<TagEditorPic>, ICopiable<TagEditorPic>
	{
		#region Static Members

		/// <summary>
		/// Static property returning the available picture types
		/// </summary>
		public static IEnumerable<string> PictureTypes
		{
			get
			{
				return Enum.GetNames(typeof(TagLib.PictureType)).
				    Where(str => str != "FileIcon" && str != "OtherFileIcon").
				    Select(str => PictureTypeToString(str)).
				    OrderBy(str => str);
			}
		}

		/// <summary>
		/// Converts a picture type to a string
		/// </summary>
		/// <param name="type">The picture type to convert</param>
		/// <returns>The string for the given picture type</returns>
		public static string PictureTypeToString(TagLib.PictureType type)
		{
			return TagEditorPic.PictureTypeToString(type.ToString());
		}

		/// <summary>
		/// Converts a picture type to a string
		/// </summary>
		/// <param name="type">The picture type to convert as a string</param>
		/// <returns>The string for the given picture type</returns>
		public static string PictureTypeToString(string type)
		{
			return Regex.Replace(type, "([A-Z][a-z]*)", "$1 ").Trim();
		}

		/// <summary>
		/// Converts a string to a picture type
		/// </summary>
		/// <param name="type">The string to convert</param>
		/// <returns>The picture type for the given string</returns>
		public static TagLib.PictureType StringToPictureType(string type)
		{
			return (TagLib.PictureType)Enum.Parse(typeof(TagLib.PictureType), type.Replace(" ", ""));
		}

		/// <summary>
		/// Retrieve the specified resource picture
		/// </summary>
		/// <param name="pic">The resource picture to retrieve</param>
		/// <returns>A byte array containing the specified resource picture</returns>
		public static byte[] GetPicture(TagEditorPicResource pic)
		{
			StreamResourceInfo sri = Application.GetResourceStream(new Uri("pack://application:,,,/Images/" + pic.ToString() + ".gif", UriKind.Absolute));
			byte[] picData = new byte[sri.Stream.Length];
			sri.Stream.Read(picData, 0, picData.Length);
			return picData;
		}

		#endregion

		#region Event Declarations

		public event PropertyChangedEventHandler PropertyChanged;
		private event Action<TagEditorPic> PicDelete;
		private event Action<TagEditorPic> PicChanging;
		private event Action<TagEditorPic> PicChanged;

		/// <summary>
		/// Invoke the property changed event
		/// </summary>
		/// <param name="property">The name of the property being changed</param>
		private void OnPropertyChanged(string property)
		{
			if (PropertyChanged != null)
				PropertyChanged(this, new PropertyChangedEventArgs(property));
		}

		/// <summary>
		/// Invoke the proprety changed event if the value chanaged
		/// </summary>
		/// <typeparam name="T">The type for the property that will be changing</typeparam>
		/// <param name="property">A reference to the property that will be changing its value</param>
		/// <param name="propertyValue">The value the property should be set to</param>
		/// <param name="propertyChanged">A list of the properties that will change as a result of the property change</param>
		private void OnPictureChanged<T>(ref T property, T propertyValue, params string[] propertyChanged)
		{
			// Check if the property value changed
			if(!Object.ReferenceEquals(property, propertyValue))
			{
				// Invoke the picture changing event
				OnPicChanging();

				// Change the property value
				property = propertyValue;
				propertyChanged.ToList().ForEach(p => OnPropertyChanged(p));

				// Invoke the picture changed event
				OnPicChanged();
			}
		}

		/// <summary>
		/// Invoke the picture changing event if necessary
		/// </summary>
		private void OnPicChanging()
		{
			if (_invokePicEvents++ == 0 && PicChanging != null)
				PicChanging(this);
		}

		/// <summary>
		/// Invoke the picture changed event if necessary
		/// </summary>
		private void OnPicChanged()
		{
			if (_invokePicEvents-- == 1 && PicChanged != null)
				PicChanged(this);
		}

		#endregion

		#region Properties Implementing Property Changed

		private TagLib.ByteVector _data;
		public TagLib.ByteVector Data
		{
			get { return _data; }
			set { OnPictureChanged(ref _data, value, "Data", "Image"); }
		}
		private string _description;
		public string Description
		{
			get { return _description; }
			set { OnPictureChanged(ref _description, value, "Description"); }
		}
		private string _mimeType;
		public string MimeType
		{
			get { return _mimeType; }
			set { OnPictureChanged(ref _mimeType, value, "MimeType"); }
		}
		private TagLib.PictureType _type;
		public TagLib.PictureType Type
		{
			get { return _type; }
			set { OnPictureChanged(ref _type, value, "Type", "ReadableType"); }
		}
		public BitmapSource Image
		{
			get
			{
				try
				{
					BitmapImage bm = new BitmapImage();
					bm.BeginInit();
					bm.StreamSource = new MemoryStream(ByteData);
					bm.EndInit();
					return bm;
				}
				catch
				{
					ByteData = GetPicture(TagEditorPicResource.InvalidFormat);
					return Image;
				}
			}
		}
		public string ReadableType
		{
			get { return TagEditorPic.PictureTypeToString(Type); }
			set { Type = TagEditorPic.StringToPictureType(value); }
		}

		#endregion

		#region Properties

		public TagLib.IPicture Picture
		{
			get
			{
				return new TagLib.Id3v2.AttachedPictureFrame()
				{
					TextEncoding = TagLib.StringType.Latin1,
					Description = Description ?? string.Empty,
					Type = Type,
					MimeType = MimeType,
					Data = Data
				};
			}
			set
			{
				OnPicChanging();
				Data = value.Data;
				Description = value.Description;
				MimeType = value.MimeType;
				Type = value.Type;
				OnPicChanged();
			}
		}
		public byte[] ByteData
		{
			private get { return Data.Data; }
			set
			{
				OnPicChanging();
				TagLib.IPicture pic = new TagLib.Picture(new TagLib.ByteVector(value, value.Length));
				Data = pic.Data;
				MimeType = pic.MimeType;
				OnPicChanged();
			}
		}
		public string FilePath { set { ByteData = new TagLib.Picture(value).Data.Data; } }
		public IListener<TagEditorPic> EventListener { set { PicDelete = value.OnSpeakerDeleting; PicChanging = value.OnSpeakerChanging; PicChanged = value.OnSpeakerChanged; } }
		private int _invokePicEvents = 0;

		#endregion

		#region Constructors

		/// <summary>
		/// Create a tag editor picture object
		/// </summary>
		public TagEditorPic()
		{
			InitializeComponent();
			Type = TagLib.PictureType.FrontCover;
		}

		#endregion

		#region Members

		/// <summary>
		/// Check if two objects are equivalent
		/// </summary>
		/// <param name="y">The object to compare to</param>
		/// <returns>Returns true if they are equivalent, false otherwise</returns>
		public bool Equals(TagEditorPic y)
		{
			return y != null && Description == y.Description && MimeType == y.MimeType && Type.ToString() == y.Type.ToString() && Data == y.Data;
		}

		/// <summary>
		/// Browse for a picture to replace the current picture
		/// </summary>
		public bool BrowseDialog()
		{
			// Create an open dialog and show
			var dlg = new Microsoft.Win32.OpenFileDialog
			{
				DefaultExt = "jpg",
				InitialDirectory = Utilities.MainSettings.DirectoryPicDefault
			};
			dlg.Filter = "JPEG Image (.jpg, jpeg)|*.jpg;*.jpeg|GIF Image (.gif)|*.gif|PNG Image(.png)|*.png";
			bool result = dlg.ShowDialog() ?? false;

			// Update the picture
			if (result)
				FilePath = dlg.FileName;

			// Now return
			return result;
		}

		/// <summary>
		/// Update the picture without invoking the picture changed events
		/// </summary>
		/// <param name="pic">The picture to update to</param>
		public void UpdatePicture(TagLib.IPicture pic)
		{
			_invokePicEvents++;
			Picture = pic;
			_invokePicEvents--;
		}

		#endregion

		#region Commands / Command Handlers / Event Handlers

		public static RoutedCommand PicSaveCommand = new RoutedCommand();
		public static RoutedCommand PicDeleteCommand = new RoutedCommand();
		public static RoutedCommand PicBrowseCommand = new RoutedCommand();

		/// <summary>
		/// Command handler that always returns true for the can execute property
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void AlwaysCanExecute(object sender, CanExecuteRoutedEventArgs e)
		{
			e.CanExecute = true;
		}

		/// <summary>
		/// Command handler that saves the selected picture
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void Command_PicSave(object sender, ExecutedRoutedEventArgs e)
		{
			// Create the save dialog and show
			var dlg = new Microsoft.Win32.SaveFileDialog
			{
				AddExtension = true,
				DefaultExt = MimeType.Split('/')[1].Replace("jpeg", "jpg"),
				InitialDirectory = Utilities.MainSettings.DirectoryPicDefault
			};
			dlg.Filter = "Image file (*." + dlg.DefaultExt + ")|*." + dlg.DefaultExt + "|All files (*.*)|*.*";

			// Check if save filename exists
			if (dlg.ShowDialog().Value)
			{
				// Save the picture to the save filename
				File.WriteAllBytes(dlg.FileName, Data.Data);
			}
		}

		/// <summary>
		/// Command handler that deletes the selected picture
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void Command_PicDelete(object sender, ExecutedRoutedEventArgs e)
		{
			if (PicDelete != null)
				PicDelete(this);
		}

		/// <summary>
		/// Command handler that browses for a new picture to replace the current picture
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void Command_PicBrowse(object sender, ExecutedRoutedEventArgs e)
		{
			BrowseDialog();
		}

		#endregion

		#region ICopiable<TagEditorPic> Members

		public void CopyFrom(TagEditorPic obj)
		{
			UpdatePicture(obj.Picture);
		}

		#endregion
	}
}
