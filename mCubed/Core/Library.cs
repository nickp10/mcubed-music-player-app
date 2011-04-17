using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.IO;
using System.Linq;

namespace mCubed.Core {
	public class Library : IExternalNotifyPropertyChanged, IExternalNotifyPropertyChanging, IDisposable {
		#region Static Members

		/// <summary>
		/// Generate the media for the given paths and places them in the "Now Playing" library
		/// </summary>
		/// <param name="paths">The paths to generate the media for</param>
		public static void GenerateMediaFromCommandLine(IEnumerable<string> paths) {
			var destLibrary = Utilities.MainSettings.Libraries.FirstOrDefault(l => l.DisplayName == "Now Playing");
			GenerateMediaFromPaths(paths, destLibrary, false);
			var slctLibrary = Utilities.MainSettings.Libraries.FirstOrDefault(l => l.DisplayName == "Now Playing");
			if (slctLibrary != null) {
				Utilities.MainSettings.LibrarySelected = slctLibrary;
			}
		}

		/// <summary>
		/// Generate the media for the given paths and places them in the selected library (using drag & drop business logic)
		/// </summary>
		/// <param name="paths">The paths to generate media for</param>
		public static void GenerateMediaFromDragDrop(IEnumerable<string> paths) {
			GenerateMediaFromDragDrop(paths, Utilities.MainSettings.LibrarySelected);
		}

		/// <summary>
		/// Generate the media for the given paths and places them in the destination library (using drag & drop business logic)
		/// </summary>
		/// <param name="paths">The paths to generate media for</param>
		/// <param name="destLibrary">The destination library for the media, or null to create a "Now Playing" library</param>
		public static void GenerateMediaFromDragDrop(IEnumerable<string> paths, Library destLibrary) {
			GenerateMediaFromPaths(paths, destLibrary, true);
		}

		/// <summary>
		/// Generates the media for the given paths and places them in the destination library
		/// </summary>
		/// <param name="paths">The paths to generate media for</param>
		/// <param name="destLibrary">The destination library for the meida, or null to create a "Now Playing" library</param>
		/// <param name="copyTo">True if the files should be copied to the library's first directory, or false otherwise</param>
		private static void GenerateMediaFromPaths(IEnumerable<string> paths, Library destLibrary, bool copyTo) {
			// Ensure we have paths first
			if (paths == null) {
				return;
			}
			paths = paths.Where(s => !string.IsNullOrEmpty(s));
			if (!paths.Any()) {
				return;
			}

			// Fix the library so one exists
			var addedLibrary = false;
			if (destLibrary == null) {
				destLibrary = new Library
				{
					DisplayName = "Now Playing",
					SaveLibrary = false
				};
				Utilities.MainSettings.AddLibrary(destLibrary);
				addedLibrary = true;
			}

			// Copy the files, or just add them
			var addedMedia = false;
			var addMedia = new List<MediaFile>();
			var files = paths.Select(p => destLibrary.GenerateMedia(p, 0)).Where(p => p != null);
			if (copyTo && destLibrary.Directories.Any()) {
				// Setup the copy
				var destDirectory = destLibrary.Directories.First();

				// Copy all the files, and add all the files that aren't added by the Copy utility
				foreach (var file in files) {
					var location = FileUtilities.Copy(file, destLibrary, destDirectory);
					if (FileUtilities.FileEquals(file.MetaData.FilePath, location)) {
						addMedia.Add(file);
					} else {
						destLibrary.PrepareRemove(file);
						addedMedia = true;
					}
				}
			} else {
				addMedia.AddRange(files);
			}

			// Check to make sure media was added
			if (addedMedia || addMedia.Any()) {
				// Add the media
				destLibrary.AddMedia(destLibrary.GenerateMedia(addMedia));

				// Load the library now
				destLibrary.IsLoaded = true;

				// Change the state
				if (destLibrary.MediaObject.State == MediaState.Stop) {
					destLibrary.MediaObject.State = MediaState.Play;
				}
			}

			// Otherwise, remove the library if we added it
			else if (addedLibrary) {
				Utilities.MainSettings.RemoveLibrary(destLibrary);
			}
		}

		#endregion

		#region Data Store

		private static IEnumerable<string> _repeatTypes =
			Enum.GetNames(typeof(MediaRepeat)).
			Select(s => s.ToReadableString()).ToArray();
		private bool _autoRenameOnUpdates;
		private readonly ColumnSettings _columnSettings = new ColumnSettings();
		private string _displayName;
		private IEnumerable<string> _directories = Enumerable.Empty<string>();
		private string _filenameForumla = "%FirstAlbumPerformer?FirstPerformer%" + Path.DirectorySeparatorChar + "%Album%" + Path.DirectorySeparatorChar + "%Track:pad-left,2,0% %Title%";
		private bool _isLoaded;
		private bool _isShuffled;
		private bool _loadOnStartup = true;
		private readonly GroupList<MediaFile> _mediaFiles = new GroupList<MediaFile>();
		private MediaFile _mediaFileCurrent;
		private readonly MediaObject _mediaObject = new MediaObject();
		private IEnumerable<MediaOrder> _mediaOrders = Enumerable.Empty<MediaOrder>();
		private MediaOrder _mediaOrderCurrent;
		private int _nextMediaIndex = 1;
		private MediaRepeat _repeatStatus = MediaRepeat.NoRepeat;
		private bool _saveLibrary = true;
		private bool _selectNextExists;
		private bool _selectPrevExists;

		#endregion

		#region Bindable Properties

		/// <summary>
		/// Get/set whether or not updates to media files within this library will auto-rename the associated file [Bindable]
		/// </summary>
		public bool AutoRenameOnUpdates {
			get { return _autoRenameOnUpdates; }
			set { this.SetAndNotify(ref _autoRenameOnUpdates, value, "AutoRenameOnUpdates"); }
		}

		/// <summary>
		/// Get the column settings for this library for how the media will be grouped, sorted, and displayed [Bindable]
		/// </summary>
		public ColumnSettings ColumnSettings { get { return _columnSettings; } }

		/// <summary>
		/// Get the list of directories that make up this library [Bindable]
		/// </summary>
		public IEnumerable<string> Directories {
			get { return _directories; }
			private set { this.SetAndNotify(ref _directories, (value ?? Enumerable.Empty<string>()).ToArray(), "Directories"); }
		}

		/// <summary>
		/// Get/set the display name for this library [Bindable]
		/// </summary>
		public string DisplayName {
			get { return _displayName; }
			set { this.SetAndNotify(ref _displayName, value, "DisplayName"); }
		}

		/// <summary>
		/// Get/set the formula that will be used to generate the filename for the media files in the library [Bindable]
		/// </summary>
		public string FilenameFormula {
			get { return _filenameForumla; }
			set { this.SetAndNotify(ref _filenameForumla, value, "FilenameFormula"); }
		}

		/// <summary>
		/// Get/set whether or not this library is currently loaded [Bindable]
		/// </summary>
		public bool IsLoaded {
			get { return _isLoaded; }
			set { this.SetAndNotify(ref _isLoaded, value, null, OnLibraryLoadedChanged, "IsLoaded"); }
		}

		/// <summary>
		/// Get/set whether or not the current media of this library is shuffled [Bindable]
		/// </summary>
		public bool IsShuffled {
			get { return _isShuffled; }
			set { this.SetAndNotify(ref _isShuffled, value, null, OnShuffleChanged, "IsShuffled"); }
		}

		/// <summary>
		/// Get/set whether or not the library should be loaded on startup of the application [Bindable]
		/// </summary>
		public bool LoadOnStartup {
			get { return _loadOnStartup; }
			set { this.SetAndNotify(ref _loadOnStartup, value, "LoadOnStartup"); }
		}

		/// <summary>
		/// Get the collection of media files contained within this library [Bindable]
		/// </summary>
		public GroupList<MediaFile> MediaFiles { get { return _mediaFiles; } }

		/// <summary>
		/// Get/set the current loaded media file [Bindable]
		/// </summary>
		public MediaFile MediaFileCurrent {
			get { return _mediaFileCurrent; }
			set { this.SetAndNotify(ref _mediaFileCurrent, value, OnMediaFileCurrentChanging, OnMediaFileCurrentChanged, "MediaFileCurrent"); }
		}

		/// <summary>
		/// Get the media object that is responsible for playing the media for this library [Bindable]
		/// </summary>
		public MediaObject MediaObject { get { return _mediaObject; } }

		/// <summary>
		/// Get the current list of media orders available for this library [Bindable]
		/// </summary>
		public IEnumerable<MediaOrder> MediaOrders {
			get { return _mediaOrders; }
			private set { this.SetAndNotify(ref _mediaOrders, (value ?? Enumerable.Empty<MediaOrder>()).ToArray(), "MediaOrders"); }
		}

		/// <summary>
		/// Get/set the media order that is currently being used for this library [Bindable]
		/// </summary>
		public MediaOrder MediaOrderCurrent {
			get { return _mediaOrderCurrent; }
			set {
				if (value != null && MediaOrders.Contains(value))
					this.SetAndNotify(ref _mediaOrderCurrent, value, OnMediaOrderCurrentChanging, OnMediaOrderCurrentChanged, "MediaOrderCurrent");
			}
		}

		/// <summary>
		/// Get the index for the next media file that is added to the library [Bindable]
		/// </summary>
		public int NextMediaIndex {
			get { return _nextMediaIndex; }
			private set { this.SetAndNotify(ref _nextMediaIndex, value, "NextMediaIndex"); }
		}

		/// <summary>
		/// Get/set the repeat status for this library [Bindable]
		/// </summary>
		public MediaRepeat RepeatStatus {
			get { return _repeatStatus; }
			set { this.SetAndNotify(ref _repeatStatus, value, null, OnRepeatStatusChanged, "RepeatStatus", "RepeatStatusString"); }
		}

		/// <summary>
		/// Get/set the repeat status for this library as a readable string [Bindable]
		/// </summary>
		public string RepeatStatusString {
			get { return RepeatStatus.ToReadableString(); }
			set { RepeatStatus = value.ToEnumType<MediaRepeat>(); }
		}

		/// <summary>
		/// Get a collection of all the available repeat status types [Bindable]
		/// </summary>
		public IEnumerable<string> RepeatTypes {
			get { return Library._repeatTypes; }
		}

		/// <summary>
		/// Get/set whether or not the library will be persisted when the application opens up again [Bindable]
		/// </summary>
		public bool SaveLibrary {
			get { return _saveLibrary; }
			set { this.SetAndNotify(ref _saveLibrary, value, "SaveLibrary"); }
		}

		/// <summary>
		/// Get whether or not selecting next media exists [Bindable]
		/// </summary>
		public bool SelectNextExists {
			get { return _selectNextExists; }
			private set { this.SetAndNotify(ref _selectNextExists, value, "SelectNextExists"); }
		}

		/// <summary>
		/// Get whether or not selecting previous media exists [Bindable]
		/// </summary>
		public bool SelectPrevExists {
			get { return _selectPrevExists; }
			private set { this.SetAndNotify(ref _selectPrevExists, value, "SelectPrevExists"); }
		}

		#endregion

		#region Events

		public event Action<MediaFile, string> MediaFilePropertyChanged;

		#endregion

		#region Constructor

		public Library() {
			// Initializations
			MediaOrders = new[] { new MediaOrder { Parent = this, Type = MediaOrderType.Sequential }, new MediaOrder { Parent = this, Type = MediaOrderType.Shuffle } };
			MediaOrderCurrent = MediaOrders.First();
			MediaFiles.SubscribeGroupBy(ColumnSettings.GroupBy);
			MediaFiles.SubscribeSortBy(ColumnSettings.SortBy);

			// Set up event handlers
			MediaObject.MediaEnded += () => Select(MediaSelect.Next, true, true);
			MediaObject.MediaFailed += new Action<string>(OnPlaybackError);
			MediaFiles.PropertyChanged += delegate(object sender, PropertyChangedEventArgs e)
			{
				if (sender == MediaFiles && e.PropertyName == "Structure")
					OnMediaFilesChanged();
			};
		}

		#endregion

		#region Event Handlers

		/// <summary>
		/// Event that handles when the library has been loaded or unloaded
		/// </summary>
		private void OnLibraryLoadedChanged() {
			// Update the current media file
			if (MediaFileCurrent != null)
				MediaFileCurrent.IsLoaded = IsLoaded;

			// Update the current media order
			if (MediaOrderCurrent != null)
				MediaOrderCurrent.IsLoaded = IsLoaded;

			// Update the media object
			if (IsLoaded) {
				Utilities.MainSettings.LibraryCurrent = this;
				MediaObject.RestoreState();
			} else {
				MediaObject.SaveState();
			}
		}

		/// <summary>
		/// Event that handles when a property on a media file has changed
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnMediaFilePropertyChanged(object sender, PropertyChangedEventArgs e) {
			// Retrieve the info that changed
			MetaDataInfo info = sender as MetaDataInfo;
			if (info != null && info.Parent != null) {
				// Ensure the property that changed is a valid-meta-data property
				if (MetaDataFormula.MetaDataProperties.Any(p => p.Path == "MetaData" && p.Property.Name == e.PropertyName)) {
					// Reset the file in the collection
					MediaFiles.Reset(info.Parent);
				}

				// Send a notification to anyone listening
				var tempHandler = MediaFilePropertyChanged;
				if (tempHandler != null) {
					tempHandler(info.Parent, e.PropertyName);
				}
			}
		}

		/// <summary>
		/// Event that handles when the media files list changed
		/// </summary>
		private void OnMediaFilesChanged() {
			// Update the source, so the source is not null
			if (MediaFileCurrent == null && MediaFiles.Count > 0)
				MediaFileCurrent = FindMedia(MediaOrderCurrent.MediaIndexForOrderKey(0));

			// Update the media order
			OnMediaOrderCurrentChanged();

			// Update the selects
			UpdateSelects();
		}

		/// <summary>
		/// Event that handles when the current media file is changing
		/// </summary>
		private void OnMediaFileCurrentChanging() {
			if (MediaFileCurrent != null)
				MediaFileCurrent.IsLoaded = false;
		}

		/// <summary>
		/// Event that handles when the current media file has changed
		/// </summary>
		private void OnMediaFileCurrentChanged() {
			// Update the properties
			if (MediaFileCurrent != null) {
				MediaFileCurrent.IsLoaded = IsLoaded;
				MediaObject.Path = MediaFileCurrent.MetaData.FilePath;
			} else {
				MediaObject.Path = null;
			}

			// Send the now playing changed event
			if (IsLoaded)
				Utilities.MainSettings.OnNowPlayingChanged(MediaFileCurrent);

			// Update the selects
			UpdateSelects();
		}

		/// <summary>
		/// Event that handles when the current media order is changing
		/// </summary>
		private void OnMediaOrderCurrentChanging() {
			if (MediaOrderCurrent != null)
				MediaOrderCurrent.IsLoaded = false;
		}

		/// <summary>
		/// Event that handles when the current media order has channged
		/// </summary>
		private void OnMediaOrderCurrentChanged() {
			// Load the order
			MediaOrderCurrent.IsLoaded = IsLoaded;
			MediaFiles.BeginTransaction();

			// Move things around
			foreach (MediaFile file in MediaFiles) {
				file.OrderKey = MediaOrderCurrent.OrderKeyForMediaIndex(file.Index) + 1;
			}

			// Update the view
			MediaFiles.EndTransaction();

			// Update the selects
			UpdateSelects();
		}

		/// <summary>
		/// Event that handles when a playback error has occurred
		/// </summary>
		/// <param name="error">The playback error</param>
		private void OnPlaybackError(string error) {
			var batchLog = new BatchLog(LogLevel.Error, LogType.Playback, "The following file(s) have failed to play and have been automatically skipped:\n", 5, TimeSpan.FromSeconds(2));
			Logger.BeginBatch(batchLog);
			Logger.Log(LogLevel.Error, LogType.Playback, MediaFileCurrent.MetaData.FileName);
			if (!Logger.IsBatchLimitReached(batchLog)) {
				Select(MediaSelect.Next, RepeatStatus == MediaRepeat.RepeatMedia ? MediaRepeat.NoRepeat : RepeatStatus, true, true);
			} else {
				MediaObject.State = MediaState.Stop;
				Logger.EndBatch(batchLog);
			}
		}

		/// <summary>
		/// Event that handles when the repeat status of the library changed
		/// </summary>
		private void OnRepeatStatusChanged() {
			UpdateSelects();
		}

		/// <summary>
		/// Event that handles when the shuffle status of the libray changed
		/// </summary>
		private void OnShuffleChanged() {
			MediaOrderType type = IsShuffled ? MediaOrderType.Shuffle : MediaOrderType.Sequential;
			MediaOrderCurrent = MediaOrders.FirstOrDefault(m => m.Type == type);
		}

		#endregion

		#region Update Members

		/// <summary>
		/// Update the select previous and next properties accordingly
		/// </summary>
		public void UpdateSelects() {
			SelectPrevExists = Select(MediaSelect.Previous, false, false);
			SelectNextExists = Select(MediaSelect.Next, false, false);
		}

		#endregion

		#region Select Members

		/// <summary>
		/// Check to see if media can be loaded in the specified select direction and load it
		/// </summary>
		/// <param name="select">The select direction to check</param>
		/// <returns>True if media existed in the select direction, or false otherwise</returns>
		public bool Select(MediaSelect select) {
			return Select(select, RepeatStatus, true, false);
		}

		/// <summary>
		/// Check to see if media can be loaded in the specified select direction and load it if desired
		/// </summary>
		/// <param name="select">The select direction to check</param>
		/// <param name="load">True if the found media should be loaded, or false otherwise</param>
		/// <param name="stop">True if the current media should be stopped if no media is found, or false otherwise</param>
		/// <returns>True if media existed in the select direction, or false otherwise</returns>
		public bool Select(MediaSelect select, bool load, bool stop) {
			return Select(select, RepeatStatus, load, stop);
		}

		/// <summary>
		/// Check to see if media can be loaded in the specified select direction and load it if desired
		/// </summary>
		/// <param name="select">The select direction to check</param>
		/// <param name="repeat">The repeat status to abide by when selectingg</param>
		/// <param name="load">True if the found media should be loaded, or false otherwise</param>
		/// <param name="stop">True if the current media should be stopped if no media is found, or false otherwise</param>
		/// <returns>True if media existed in the select direction, or false otherwise</returns>
		public bool Select(MediaSelect select, MediaRepeat repeat, bool load, bool stop) {
			// If no current file or media order, then selection is impossible
			if (MediaOrderCurrent == null || MediaFileCurrent == null)
				return false;

			// Calculate the new order key
			int moveCount = repeat == MediaRepeat.RepeatMedia ? 0 : (select == MediaSelect.Next ? 1 : -1);
			int newOrderKey = MediaOrderCurrent.OrderKeyForMediaIndex(MediaFileCurrent.Index) + moveCount;
			if (repeat == MediaRepeat.RepeatLibrary && !MediaOrderCurrent.OrderKeyExists(newOrderKey) && MediaOrderCurrent.Count > 0) {
				while (newOrderKey >= MediaOrderCurrent.Count)
					newOrderKey -= MediaOrderCurrent.Count;
				while (newOrderKey < 0)
					newOrderKey += MediaOrderCurrent.Count;
			}

			// Retrieve the media and load, if necessary
			bool selectExists = MediaOrderCurrent.OrderKeyExists(newOrderKey);
			if (selectExists && load) {
				var file = FindMedia(MediaOrderCurrent.MediaIndexForOrderKey(newOrderKey));
				if (MediaFileCurrent != file)
					MediaFileCurrent = file;
				else
					MediaObject.Seek(0);
			} else if (!selectExists && stop) {
				MediaObject.State = MediaState.Stop;
			}
			return selectExists;
		}

		#endregion

		#region Directory Members

		/// <summary>
		/// Adds the given directory to the list of directories for the library
		/// </summary>
		/// <param name="directory">The directory to add to the library</param>
		public void AddDirectory(string directory) {
			AddDirectory(new[] { directory });
		}

		/// <summary>
		/// Adds the given collection of directories to the list of directories for the library
		/// </summary>
		/// <param name="directories">The collection of directories to add</param>
		public void AddDirectory(IEnumerable<string> directories) {
			// Ensure directories are being added
			if (directories != null) {
				// Start a process
				var tempDirs = directories.Distinct().Where(d => !Directories.Contains(d)).ToArray();
				Utilities.MainProcessManager.AddProcess(process =>
				{
					// Setup
					var addDirs = new List<string>();
					MediaFiles.BeginTransaction();

					// Iterate over the directories
					foreach (var directory in tempDirs) {
						// Generate the media
						var items = GenerateDirectoryMedia(directory);
						if (items != null) {
							// Add the directory
							addDirs.Add(directory);

							// Add the media
							foreach (var item in items) {
								MediaFiles.Add(item);
							}
						}
						process.CompletedCount++;
					}

					// Finalize
					MediaFiles.EndTransaction();
					Directories = Directories.Union(addDirs);
				}, string.Format("Updating library \"{0}\"", DisplayName), tempDirs.Length);
			}
		}

		/// <summary>
		/// Adds the given collection of directories to the directory list without generating media for it
		/// </summary>
		/// <param name="directories">The directories that should be added without generating media</param>
		public void AddDirectories(IEnumerable<string> directories) {
			Directories = Directories.Union(directories);
		}

		/// <summary>
		/// Removes the given directory from the list of directories for the library
		/// </summary>
		/// <param name="directory">The directory to remove from the library</param>
		public void RemoveDirectory(string directory) {
			RemoveDirectory(new[] { directory });
		}

		/// <summary>
		/// Removes the given collection of directories from the list of directories for the library
		/// </summary>
		/// <param name="directories">The collection of directories to remove</param>
		public void RemoveDirectory(IEnumerable<string> directories) {
			// Ensure directories are being removed
			if (directories != null) {
				// Start a process
				var tempDirs = directories.Where(d => Directories.Contains(d)).ToArray();
				Utilities.MainProcessManager.AddProcess(process =>
				{
					// Setup
					var removeDirs = new List<string>();
					MediaFiles.BeginTransaction();

					// Iterate over the directories
					foreach (var directory in tempDirs) {
						// Remove the media
						var items = PrepareRemoveDirectory(MediaFiles, directory);
						if (items != null) {
							// Remove the directory
							removeDirs.Add(directory);

							// Remove the media
							foreach (var item in items) {
								MediaFiles.Remove(item);
							}
						}
						process.CompletedCount++;
					}

					// Finalize
					MediaFiles.EndTransaction();
					Directories = Directories.Except(removeDirs);
				}, string.Format("Updating library \"{0}\"", DisplayName), tempDirs.Length);
			}
		}

		/// <summary>
		/// Adds a directory to the library and its associated media files
		/// </summary>
		/// <param name="directory">The directory to add</param>
		private IEnumerable<MediaFile> GenerateDirectoryMedia(string directory) {
			// Check if the directory exists first
			IEnumerable<MediaFile> items = null;
			if (!String.IsNullOrEmpty(directory) && Directory.Exists(directory)) {
				// Add all the directories recursively
				items = Directory.GetDirectories(directory).
					Select(GenerateDirectoryMedia).
					Where(i => i != null).
					SelectMany(i => i);

				// Add all the files in the current directory
				string[] validExtensions = Utilities.ExtensionsMusic;
				items = items.Union(
					Directory.GetFiles(directory).
					Where(file => validExtensions.Contains(Path.GetExtension(file).ToLower())).
					Select(file => GenerateMedia(file)).
					Where(file => file != null)
				);
			}
			return items;
		}

		/// <summary>
		/// Removes a directory from the library and its associated media files
		/// </summary>
		/// <param name="list">The items in which to search which items to be removing</param>
		/// <param name="directory">The directory to remove</param>
		/// <returns>The list of items that need to be removed from the collection</returns>
		private IEnumerable<MediaFile> PrepareRemoveDirectory(IEnumerable<MediaFile> list, string directory) {
			// Check the directory first
			var items = Enumerable.Empty<MediaFile>();
			if (!String.IsNullOrEmpty(directory)) {
				// Prepare the media
				items = list.Where(mf => mf.MetaData.FilePath.StartsWith(directory));
				PrepareRemove(items);
			}
			return items;
		}

		#endregion

		#region Media File Members

		/// <summary>
		/// Find the media to play based on the media index
		/// </summary>
		/// <param name="mediaIndex">The media index for the media to find</param>
		/// <returns>The media object based on the index location</returns>
		public MediaFile FindMedia(int mediaIndex) {
			return MediaFiles.FirstOrDefault(mediaFile => mediaFile.Index == mediaIndex);
		}

		/// <summary>
		/// Generate a list of new media files from an open file dialog box
		/// </summary>
		/// <returns>A list of media files that were selected</returns>
		public IEnumerable<MediaFile> GenerateMedia() {
			// Open file dialog box to browse for file
			var dlg = new Microsoft.Win32.OpenFileDialog
			{
				Multiselect = true,
				DefaultExt = "mp3",
				Filter = Utilities.FilterMusic,
				FilterIndex = 4,
				InitialDirectory = Utilities.MainSettings.DirectoryMediaDefault
			};

			// Process open file dialog box results
			return (dlg.ShowDialog().Value) ? GenerateMedia(dlg.FileNames) : Enumerable.Empty<MediaFile>();
		}

		/// <summary>
		/// Generate a list of media files for use in the library
		/// </summary>
		/// <param name="files">The files from an outside library to use</param>
		/// <returns>A list of media files for use in the library</returns>
		public IEnumerable<MediaFile> GenerateMedia(IEnumerable<MediaFile> files) {
			return GenerateMedia(files.Select(file => file.MetaData.FilePath));
		}

		/// <summary>
		/// Generate a list of media files for use in the library
		/// </summary>
		/// <param name="files">The paths to generate media for the library to use</param>
		/// <returns>A list of media files for use in the library</returns>
		public IEnumerable<MediaFile> GenerateMedia(IEnumerable<string> paths) {
			return paths.Select(p => GenerateMedia(p)).Where(p => p != null).ToArray();
		}

		/// <summary>
		/// Generate a new media file for use in the library
		/// </summary>
		/// <param name="path">The path to the file to generate from</param>
		/// <returns>The media file that is generated for the library</returns>
		public MediaFile GenerateMedia(string path) {
			// Check if the file exists first
			MediaFile file = null;
			if (File.Exists(path = Path.GetFullPath(path))) {
				// Add the media to the media orders
				foreach (MediaOrder mediaOrder in MediaOrders) {
					mediaOrder.AddMediaIndex(NextMediaIndex);
				}

				// Generate the file
				file = GenerateMedia(path, NextMediaIndex);

				// Ensure one was created
				if (file == null) {
					foreach (MediaOrder mediaOrder in MediaOrders) {
						mediaOrder.RemoveMediaIndex(NextMediaIndex);
					}
				} else {
					file.MetaData.PropertyChanged += new PropertyChangedEventHandler(OnMediaFilePropertyChanged);
					NextMediaIndex++;
				}
			}
			return file;
		}

		/// <summary>
		/// Generate a new media file for use in the library with the given media index
		/// </summary>
		/// <param name="path">The path to the file to generate from</param>
		/// <param name="nextMediaIndex">The media index for the given file</param>
		/// <returns>The media file that is generated for the library</returns>
		private MediaFile GenerateMedia(string path, int mediaIndex) {
			try {
				return new MediaFile(path, mediaIndex, this);
			} catch (Exception e) {
				Logger.Log(LogLevel.Error, LogType.Library, e, "This media file is corrupt. It cannot be played, nor " +
					"can the meta-data information be modified; therefore, this file will not " +
					"be added. The file that caused this error:\n\n\t" + path);
				return null;
			}
		}

		/// <summary>
		/// Add media to the current library
		/// </summary>
		/// <param name="media">The media that should be added to the library</param>
		public void AddMedia(IEnumerable<MediaFile> media) {
			MediaFiles.BeginTransaction();
			foreach (MediaFile file in media) {
				MediaFiles.Add(file);
			}
			MediaFiles.EndTransaction();
		}

		/// <summary>
		/// Remove media from the current library
		/// </summary>
		/// <param name="media">The media that should be removed from the library</param>
		public void RemoveMedia(IEnumerable<MediaFile> media) {
			MediaFiles.BeginTransaction();
			PrepareRemove(media);
			foreach (MediaFile file in media)
				MediaFiles.Remove(file);
			MediaFiles.EndTransaction();
		}

		/// <summary>
		/// Prepare media to be removed from the library
		/// </summary>
		/// <param name="media">The media file to remove</param>
		private void PrepareRemove(MediaFile media) {
			// Check if this media is playing
			if (media == MediaFileCurrent) {
				// Switch to the next or previous media
				if (!Select(MediaSelect.Next, MediaRepeat.NoRepeat, true, false) && !Select(MediaSelect.Previous, MediaRepeat.NoRepeat, true, false))
					MediaFileCurrent = null;
			}

			// Remove the media from the orders
			foreach (MediaOrder order in MediaOrders) {
				order.RemoveMediaIndex(media.Index);
			}

			// Dispose the media
			media.Dispose();
		}

		/// <summary>
		/// Prepare media to be removed from the library
		/// </summary>
		/// <param name="files">A list of media files to remove</param>
		private void PrepareRemove(IEnumerable<MediaFile> files) {
			// Reorder the media and prepare to remove them all
			files = files.OrderBy(file => file == MediaFileCurrent);
			foreach (MediaFile file in files) {
				PrepareRemove(file);
			}
		}

		/// <summary>
		/// Clear the media contents
		/// </summary>
		public void ClearMedia() {
			// Clear all the media orders
			foreach (MediaOrder mediaOrder in MediaOrders) {
				mediaOrder.Clear();
			}

			// Clear the media
			foreach (MediaFile file in MediaFiles)
				file.Dispose();
			MediaFiles.Clear();

			// Update the current media file
			MediaFileCurrent = null;
		}

		#endregion

		#region Members

		/// <summary>
		/// Reloads the library, by re-adding all the directories in the library
		/// </summary>
		public void Reload() {
			// Setup
			var tempDirs = Directories.ToArray();

			// Add a process
			Utilities.MainProcessManager.AddProcess(process =>
			{
				// Cache the current state
				var file = MediaFileCurrent;
				var state = file == null ? null : file.UnlockFile();

				// Clear the media
				ClearMedia();

				// Add all the media again
				foreach (var directory in Directories) {
					var items = GenerateDirectoryMedia(directory);
					if (items != null) {
						AddMedia(items);
					}
					process.CompletedCount++;
				}

				// Reload the state
				if (state != null) {
					file = MediaFiles.FirstOrDefault(f => f.MetaData.FilePath == state.Path);
					if (file != null) {
						MediaFileCurrent = file;
						file.RestoreState(state);
					}
				}
			}, string.Format("Reloading library \"{0}\"", DisplayName), tempDirs.Length);
		}

		/// <summary>
		/// Reshuffle all the media orders marked with the type of shuffle
		/// </summary>
		public void Reshuffle() {
			foreach (var order in MediaOrders)
				Reshuffle(order);
		}

		/// <summary>
		/// Reshuffle the given media order only if its type is shuffle
		/// </summary>
		/// <param name="order">The media order to reshuffle</param>
		public void Reshuffle(MediaOrder order) {
			if (order.Type == MediaOrderType.Shuffle) {
				order.Shuffle();
				if (order == MediaOrderCurrent)
					OnMediaOrderCurrentChanged();
			}
		}

		/// <summary>
		/// Toggle through the media orders for this library
		/// </summary>
		/// <param name="forward">True if the next media order should be loaded, or false for the previous one</param>
		public void ToggleMediaOrders(bool forward) {
			MediaOrderCurrent = forward ? MediaOrders.ElementAfter(MediaOrderCurrent, true) : MediaOrders.ElementBefore(MediaOrderCurrent, true);
		}

		/// <summary>
		/// Toggle through the repeat status options for this library
		/// </summary>
		/// <param name="forward">True if the the next repeat status should be used, or false if the previous status should be used</param>
		public void ToggleRepeat(bool forward) {
			var types = Enum.GetValues(typeof(MediaRepeat)).Cast<MediaRepeat>();
			RepeatStatus = forward ? types.ElementAfter(RepeatStatus, true) : types.ElementBefore(RepeatStatus, true);
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
		/// Dispose of the library properly
		/// </summary>
		public void Dispose() {
			// Unsubscribe others from its events
			MediaFilePropertyChanged = null;
			PropertyChanged = null;
			PropertyChanging = null;

			// Dispose all disposable references it created
			foreach (MediaOrder order in MediaOrders)
				order.Dispose();
			foreach (MediaFile file in MediaFiles)
				file.Dispose();			
			MediaFiles.Dispose();
			ColumnSettings.Dispose();
			MediaObject.Dispose();
		}

		#endregion
	}
}