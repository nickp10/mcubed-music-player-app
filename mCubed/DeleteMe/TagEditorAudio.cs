using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.IO;
using System.Linq;
using System.Reflection;
using mCubed.Controls;

namespace mCubed.Classes
{
	public class TagEditorAudio : INotifyPropertyChanged
	{
		#region Property Changed

		public event PropertyChangedEventHandler PropertyChanged;

		/// <summary>
		/// Call the property changed event
		/// </summary>
		/// <param name="property">The name of the property being changed</param>
		private void PropChanged(string property)
		{
			if (null != PropertyChanged)
			{
				PropertyChanged(this, new PropertyChangedEventArgs(property));
			}
		}

		#endregion

		#region Properties Implementing Property Changed

		public IEnumerable<TagEditorPic> Pics
		{
			get { return Tag.Pictures.Select(pic => new TagEditorPic() { Picture = pic }).ToList(); }
			set { Tag.Pictures = value.Select(pic => pic.Picture).ToArray(); PropChanged("Pics"); }
		}

		#endregion

		#region Properties

		public string FilePath { get { return Path.GetFullPath(TLFile.Name); } }
		public string FileName { get { return Path.GetFileName(TLFile.Name); } }
		public TimeSpan Duration { get { return TLFile.Properties.Duration; } }
		public string Length { get { return Duration.Format(); } }
		public int AudioBitrate { get { return TLFile.Properties.AudioBitrate; } }
		public int AudioSampleRate { get { return TLFile.Properties.AudioSampleRate; } }
		public TagLib.Tag Tag { get { return TLFile.Tag; } }
		private TagLib.File TLFile { get; set; }

		#endregion

		#region Constructor

		/// <summary>
		/// Create the tag edtior audio object with all the tag information
		/// </summary>
		/// <param name="filePath">The path to the file for the metadata</param>
		public TagEditorAudio(string filePath)
		{
			TLFile = TagLib.File.Create(filePath);
		}

		#endregion

		#region Members

		/// <summary>
		/// Sets a property declaration to the dictionary
		/// </summary>
		/// <param name="property">The name for the property/accessor</param>
		/// <param name="value">The value to be assigned to the property/accessor</param>
		public void SetProp(string property, object value)
		{
			PropertyInfo prop = Tag.GetType().GetProperty(property);
			if (prop != null) {
				if (value == null) {
					prop.SetValue(Tag, null, null);
				} else {
					// Change the value if its of the appropriate type
					value = value.TryParse(prop.PropertyType);
					if (value != null && value.GetType() == prop.PropertyType)
						prop.SetValue(Tag, value, null);
				}
			}
		}

		/// <summary>
		/// Save the information from the dictionary to the file
		/// </summary>
		public void Save()
		{
			TLFile.Save();
			PropChanged("Tag");
		}

		#endregion
	}
}