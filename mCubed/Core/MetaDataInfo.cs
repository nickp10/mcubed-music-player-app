using System;
using System.Collections;
using System.Collections.Generic;
using System.ComponentModel;
using System.IO;
using System.Linq;
using System.Reflection;

namespace mCubed.Core {
	public abstract class MetaDataInfo : IExternalNotifyPropertyChanged, IExternalNotifyPropertyChanging, IDisposable {
		#region Data Store

		private string _album;
		private string[] _albumArtists;
		private int _audioBitrate;
		private int _audioSampleRate;
		private uint _beatsPerMinute;
		private string _comment;
		private string[] _composers;
		private string _conductor;
		private string _copyright;
		private uint _disc;
		private uint _discCount;
		private string _filePath;
		private long _fileSizeBytes;
		private string[] _genres;
		private string _grouping;
		private TimeSpan _length;
		private string _lyrics;
		private string[] _performers;
		private MetaDataPic[] _pictures;
		private MetaDataStatus _status;
		private string _title;
		private uint _track;
		private uint _trackCount;
		private uint _year;

		#endregion

		#region Bindable Properties

		/// <summary>
		/// Get/set the album title [Bindable]
		/// </summary>
		[MetaData("The album that the media is on.")]
		public string Album {
			get { return _album; }
			set { this.SetAndNotify(ref _album, value, "Album"); }
		}

		/// <summary>
		/// Get/set the artists/performers for the album [Bindable]
		/// </summary>
		public string[] AlbumArtists {
			get { return _albumArtists; }
			set { this.SetAndNotify(ref _albumArtists, value, "AlbumArtists", "FirstAlbumArtist", "JoinedAlbumArtists"); }
		}

		/// <summary>
		/// Get the audio bitrate [Bindable]
		/// </summary>
		[MetaData("The bitrate for the media measured in kilobits per seconds (kbps).")]
		public int AudioBitrate {
			get { return _audioBitrate; }
			protected set { this.SetAndNotify(ref _audioBitrate, value, "AudioBitrate"); }
		}

		/// <summary>
		/// Get the audio sample rate [Bindable]
		/// </summary>
		[MetaData("The sample rate for the media measured in samples per second (hertz).")]
		public int AudioSampleRate {
			get { return _audioSampleRate; }
			protected set { this.SetAndNotify(ref _audioSampleRate, value, "AudioSampleRate"); }
		}

		/// <summary>
		/// Get/set the beats per minute [Bindable]
		/// </summary>
		[MetaData("The number of beats every minute for the media.")]
		public uint BeatsPerMinute {
			get { return _beatsPerMinute; }
			set { this.SetAndNotify(ref _beatsPerMinute, value, "BeatsPerMinute"); }
		}

		/// <summary>
		/// Get/set the comment [Bindable]
		/// </summary>
		[MetaData("The comment for the media.")]
		public string Comment {
			get { return _comment; }
			set { this.SetAndNotify(ref _comment, value, "Comment"); }
		}

		/// <summary>
		/// Get/set the composers [Bindable]
		/// </summary>
		public string[] Composers {
			get { return _composers; }
			set { this.SetAndNotify(ref _composers, value, "Composers", "FirstComposer", "JoinedComposers"); }
		}

		/// <summary>
		/// Get/set the conductor [Bindable]
		/// </summary>
		[MetaData("The conductor for the media.")]
		public string Conductor {
			get { return _conductor; }
			set { this.SetAndNotify(ref _conductor, value, "Conductor"); }
		}

		/// <summary>
		/// Get/set the copyright information [Bindable]
		/// </summary>
		[MetaData("The copyright information for the media.")]
		public string Copyright {
			get { return _copyright; }
			set { this.SetAndNotify(ref _copyright, value, "Copyright"); }
		}

		/// <summary>
		/// Get/set the disc number [Bindable]
		/// </summary>
		[MetaData("The disc number for the media.")]
		public uint Disc {
			get { return _disc; }
			set { this.SetAndNotify(ref _disc, value, "Disc"); }
		}

		/// <summary>
		/// Get/set the total number of discs [Bindable]
		/// </summary>
		[MetaData("The total number of discs on the album that the media is on.")]
		public uint DiscCount {
			get { return _discCount; }
			set { this.SetAndNotify(ref _discCount, value, "DiscCount"); }
		}

		/// <summary>
		/// Get the filename out of the filepath [Bindable]
		/// </summary>
		[MetaData("The filename portion from the file path for the media.")]
		public string FileName { get { return System.IO.Path.GetFileName(FilePath); } }

		/// <summary>
		/// Get/set the filepath [Bindable]
		/// </summary>
		[MetaData("The full filepath to the media location.")]
		public string FilePath {
			get { return _filePath; }
			set { this.SetAndNotify(ref _filePath, value, "FilePath", "FileName"); }
		}

		/// <summary>
		/// Get the size of the file as a human-readable string [Bindable]
		/// </summary>
		[MetaData("The size of the file.")]
		public string FileSize { get { return Utilities.FormatBytesToString(FileSizeBytes); } }

		/// <summary>
		/// Get the size of the file calculated in bytes [Bindable]
		/// </summary>
		public long FileSizeBytes {
			get { return _fileSizeBytes; }
			protected set { this.SetAndNotify(ref _fileSizeBytes, value, "FileSizeBytes", "FileSize"); }
		}

		/// <summary>
		/// Get the first artist/performer listed in the album artists [Bindable]
		/// </summary>
		[MetaData("The first album artist/performer listed in the album artists/performers on the media.", Formula = "FirstAlbumPerformer")]
		public string FirstAlbumArtist { get { return AlbumArtists.FirstOrDefault() ?? string.Empty; } }

		/// <summary>
		/// Get the first composer listed in the composers [Bindable]
		/// </summary>
		[MetaData("The first composer listed in the composers on the media.")]
		public string FirstComposer { get { return Composers.FirstOrDefault() ?? string.Empty; } }

		/// <summary>
		/// Get the first genre listed in the genres [Bindable]
		/// </summary>
		[MetaData("The first genre listed in the genres for the media.")]
		public string FirstGenre { get { return Genres.FirstOrDefault() ?? string.Empty; } }

		/// <summary>
		/// Get the first artist/performer listed in the performers [Bindable]
		/// </summary>
		[MetaData("The first artist/perform listed in the artists/performers on the media.")]
		public string FirstPerformer { get { return Performers.FirstOrDefault() ?? string.Empty; } }

		/// <summary>
		/// Get/set the genres [Bindable]
		/// </summary>
		public string[] Genres {
			get { return _genres; }
			set { this.SetAndNotify(ref _genres, value, "Genres", "FirstGenre", "JoinedGenres"); }
		}

		/// <summary>
		/// Get/set the grouping or series [Bindable]
		/// </summary>
		[MetaData("The grouping that the media belongs to.")]
		public string Grouping {
			get { return _grouping; }
			set { this.SetAndNotify(ref _grouping, value, "Grouping"); }
		}

		/// <summary>
		/// Get the index identifier for this media file [Bindable]
		/// </summary>
		public int Index { get { return Parent.Index; } }

		/// <summary>
		/// Get whether or not this media file is currently loaded [Bindable]
		/// </summary>
		public bool IsLoaded { get { return Parent.IsLoaded; } }

		/// <summary>
		/// Get whether or not the file information is serializable back to the media it came from
		/// </summary>
		public abstract bool IsReadOnly { get; }

		/// <summary>
		/// Get the aggregated album artists separated by a ';' [Bindable]
		/// </summary>
		[MetaData("All of the album artists/performers on the media separtated by a semi-colon.", Formula = "AlbumPerformers")]
		public string JoinedAlbumArtists { get { return AlbumArtists.AggregateIfAny((s1, s2) => s1 += "; " + s2); } }

		/// <summary>
		/// Get the aggregated composers separated by a ';' [Bindable]
		/// </summary>
		[MetaData("All of the composers on the media separtated by a semi-colon.", Formula = "Composers")]
		public string JoinedComposers { get { return Composers.AggregateIfAny((s1, s2) => s1 += "; " + s2); } }

		/// <summary>
		/// Get the aggregated genres separated by a ';' [Bindable]
		/// </summary>
		[MetaData("All of the genres for the media separtated by a semi-colon.", Formula = "Genres")]
		public string JoinedGenres { get { return Genres.AggregateIfAny((s1, s2) => s1 += "; " + s2); } }

		/// <summary>
		/// Get the aggregated performers separated by a ';' [Bindable]
		/// </summary>
		[MetaData("All of the artists/performers on the media separtated by a semi-colon.", Formula = "Performers")]
		public string JoinedPerformers { get { return Performers.AggregateIfAny((s1, s2) => s1 += "; " + s2); } }

		/// <summary>
		/// Get the length/duration [Bindable]
		/// </summary>
		public TimeSpan Length {
			get { return _length; }
			protected set { this.SetAndNotify(ref _length, value, "Length", "LengthString"); }
		}

		/// <summary>
		/// Get the length/duration as a readable string [Bindable]
		/// </summary>
		[MetaData("The length of the media using the format that is used throughout the application.", Formula = "Length")]
		public string LengthString { get { return Length.Format(); } }

		/// <summary>
		/// Get/set the lyrics [Bindable]
		/// </summary>
		[MetaData("The lyrics to the media.")]
		public string Lyrics {
			get { return _lyrics; }
			set { this.SetAndNotify(ref _lyrics, value, "Lyrics"); }
		}

		/// <summary>
		/// Get the order key in which this media file will be played [Bindable]
		/// </summary>
		[MetaData("An integer describing the order of when the media file will be played.", Formula = "Order", ColumnAlias = "##")]
		public int OrderKey { get { return Parent.OrderKey; } }

		/// <summary>
		/// Get/set the artists/performers for the media [Bindable]
		/// </summary>
		public string[] Performers {
			get { return _performers; }
			set { this.SetAndNotify(ref _performers, value, "Performers", "FirstPerformer", "JoinedPerformers"); }
		}

		/// <summary>
		/// Get/set the album artwork/pictures [Bindable]
		/// </summary>
		public MetaDataPic[] Pictures {
			get { return _pictures; }
			set { this.SetAndNotify(ref _pictures, value, "Pictures"); }
		}

		/// <summary>
		/// Get/set the status of the meta data information [Bindable]
		/// </summary>
		public MetaDataStatus Status {
			get { return _status; }
			set { this.SetAndNotify(ref _status, value, "Status"); }
		}

		/// <summary>
		/// Get/set the title [Bindable]
		/// </summary>
		[MetaData("The title of the media.")]
		public string Title {
			get { return _title; }
			set { this.SetAndNotify(ref _title, value, "Title"); }
		}

		/// <summary>
		/// Get/set the track number [Bindable]
		/// </summary>
		[MetaData("The track number for the media.", ColumnAlias = "#")]
		public uint Track {
			get { return _track; }
			set { this.SetAndNotify(ref _track, value, "Track"); }
		}

		/// <summary>
		/// Get/set the total number of tracks [Bindable]
		/// </summary>
		[MetaData("The total number of tracks on the album that the media is on.")]
		public uint TrackCount {
			get { return _trackCount; }
			set { this.SetAndNotify(ref _trackCount, value, "TrackCount"); }
		}

		/// <summary>
		/// Get/set the year [Bindable]
		/// </summary>
		[MetaData("The year the media was released.")]
		public uint Year {
			get { return _year; }
			set { this.SetAndNotify(ref _year, value, "Year"); }
		}

		#endregion

		#region Properties

		/// <summary>
		/// Get/set the media file that the information is for
		/// </summary>
		public MediaFile Parent { get; set; }

		#endregion

		#region Members

		/// <summary>
		/// Save the information back to the media it was created from
		/// </summary>
		public void Save() {
			if (!IsReadOnly) {
				var state = Parent.UnlockFile();
				Save(null);
				if (Parent.Parent.AutoRenameOnUpdates) {
					string newLocation = FileUtilities.Rename(Parent);
					FilePath = newLocation;
					if (state != null) {
						state.Path = newLocation;
					}
				}
				Parent.RestoreState(state);
			}
		}

		/// <summary>
		/// Get a property value on this object via the property name
		/// </summary>
		/// <param name="propertyName">The property name of the value to retrieve</param>
		/// <returns>The property value on this object in the form of a string collection</returns>
		public IEnumerable<string> GetValue(string propertyName) {
			// Get the property
			PropertyInfo property = GetType().GetProperty(propertyName);
			if (property == null)
				return Enumerable.Empty<string>().ToArray();

			// Check the value
			object value = property.GetValue(this, null);
			if (value == null)
				return Enumerable.Empty<string>().ToArray();

			// Return the value
			return Utilities.IsTypeIEnumerable(value.GetType()) ? ((IEnumerable)value).OfType<object>().Where(o => o != null).Select(o => o.ToString()).ToArray() : new string[] { (value ?? "").ToString() };
		}

		/// <summary>
		/// Set a property value on this object
		/// </summary>
		/// <param name="propertyName">The name of the property to set</param>
		/// <param name="propertyValue">The value to set to</param>
		public void SetProperty(string propertyName, object propertyValue) {
			// Get the property
			PropertyInfo property = GetType().GetProperty(propertyName);
			if (property == null)
				return;

			// Get the set method
			MethodInfo setMethod = property.GetSetMethod();
			if (setMethod == null || setMethod.IsPrivate)
				return;

			// Set the property
			setMethod.Invoke(this, new object[] { propertyValue });
		}

		#endregion

		#region Abstract Members

		/// <summary>
		/// Save the information back to the media it was created from
		/// </summary>
		/// <param name="obj">This object will ALWAYS be null for sake of overloading purposes</param>
		protected abstract void Save(object obj);

		/// <summary>
		/// Load the information from the media
		/// </summary>
		protected abstract void Load();

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
		/// Dispose of the meta-data information object properly
		/// </summary>
		public void Dispose() {
			// Unsubscribe others from its events
			PropertyChanged = null;
			PropertyChanging = null;

			// Ensure no cyclic references
			Parent = null;
		}

		#endregion
	}

	public class MDITagLib : MetaDataInfo {
		#region Bindable Properties

		/// <summary>
		/// Get whether or not the file information is serializable back to the media it came from [Bindable]
		/// </summary>
		public override bool IsReadOnly { get { return false; } }

		#endregion

		#region Constructor

		/// <summary>
		/// Create a metadata information object from a specified file
		/// </summary>
		/// <param name="filePath">The path to the file to load the information from</param>
		public MDITagLib(string filePath) {
			FilePath = Path.GetFullPath(filePath);
			FileInfo info = new FileInfo(FilePath);
			if (info.Exists) {
				FileSizeBytes = info.Length;
				Load();
			} else {
				throw new IOException("The specifed file does not exist");
			}
		}

		#endregion

		#region Serialization Members

		/// <summary>
		/// Save the information back to the media it was created from
		/// </summary>
		/// <param name="obj">This object will ALWAYS be null for sake of overloading purposes</param>
		protected override void Save(object obj) {
			using (var tlFile = TagLib.File.Create(FilePath)) {
				tlFile.RemoveTags(TagLib.TagTypes.Id3v1);
				tlFile.RemoveTags(TagLib.TagTypes.Id3v2);
				tlFile.Save();
			}
			using (var tlFile = TagLib.File.Create(FilePath)) {
				tlFile.Tag.Album = Album;
				tlFile.Tag.AlbumArtists = AlbumArtists;
				tlFile.Tag.BeatsPerMinute = BeatsPerMinute;
				tlFile.Tag.Comment = Comment;
				tlFile.Tag.Composers = Composers;
				tlFile.Tag.Conductor = Conductor;
				tlFile.Tag.Copyright = Copyright;
				tlFile.Tag.Disc = Disc;
				tlFile.Tag.DiscCount = DiscCount;
				tlFile.Tag.Genres = Genres;
				tlFile.Tag.Grouping = Grouping;
				tlFile.Tag.Lyrics = Lyrics;
				tlFile.Tag.Performers = Performers;
				tlFile.Tag.Pictures = (Pictures ?? new MetaDataPic[0]).Select(p => p.GenerateTagLib()).ToArray();
				tlFile.Tag.Title = Title;
				tlFile.Tag.Track = Track;
				tlFile.Tag.TrackCount = TrackCount;
				tlFile.Tag.Year = Year;
				tlFile.Save();
			}
		}

		/// <summary>
		/// Load the information from the media
		/// </summary>
		protected override void Load() {
			using (var tlFile = TagLib.File.Create(FilePath)) {
				Album = tlFile.Tag.Album ?? string.Empty;
				AlbumArtists = tlFile.Tag.AlbumArtists ?? new string[0];
				AudioBitrate = tlFile.Properties.AudioBitrate;
				AudioSampleRate = tlFile.Properties.AudioSampleRate;
				BeatsPerMinute = tlFile.Tag.BeatsPerMinute;
				Comment = tlFile.Tag.Comment ?? string.Empty;
				Composers = tlFile.Tag.Composers ?? new string[0];
				Conductor = tlFile.Tag.Conductor ?? string.Empty;
				Copyright = tlFile.Tag.Copyright ?? string.Empty;
				Disc = tlFile.Tag.Disc;
				DiscCount = tlFile.Tag.DiscCount;
				Genres = tlFile.Tag.Genres ?? new string[0];
				Grouping = tlFile.Tag.Grouping ?? string.Empty;
				Length = tlFile.Properties.Duration;
				Lyrics = tlFile.Tag.Lyrics ?? string.Empty;
				Performers = tlFile.Tag.Performers ?? new string[0];
				Pictures = (tlFile.Tag.Pictures ?? new TagLib.IPicture[0]).Select(p => new MetaDataPic(p)).ToArray();
				Title = tlFile.Tag.Title ?? string.Empty;
				Track = tlFile.Tag.Track;
				TrackCount = tlFile.Tag.TrackCount;
				Year = tlFile.Tag.Year;
			}
		}

		#endregion
	}
}