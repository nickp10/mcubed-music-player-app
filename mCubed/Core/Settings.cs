using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Collections.Specialized;
using System.ComponentModel;
using System.IO;
using System.Linq;
using System.Windows;
using System.Xml.Linq;

namespace mCubed.Core {
	public class Settings : IExternalNotifyPropertyChanged, IExternalNotifyPropertyChanging, IDisposable {
		#region Data Store

		private string _path = Path.Combine(Utilities.ExecutionDirectory, "mCubed.xml");
		private readonly ObservableCollection<ColumnDetail> _allColumns = new ObservableCollection<ColumnDetail>();
		private readonly ObservableCollection<Command> _commands = new ObservableCollection<Command>();
		private string _directoryMediaDefault = Environment.CurrentDirectory;
		private string _directoryPictureDefault = Environment.CurrentDirectory;
		private XDocument _document;
		private bool _isLoaded;
		private IEnumerable<Library> _libraries = Enumerable.Empty<Library>();
		private Library _libraryCurrent;
		private Library _librarySelected;
		private Point? _miniLocation;
		private int _selectedTab = (int)TabOption.Help;
		private bool _showMDIManager = true;
		private bool _showMini;
		private readonly ObservableCollection<MetaDataFormula> _formulas = new ObservableCollection<MetaDataFormula>()
		{
			new MetaDataFormula {
				FallbackValue = "Music Meta-data Manager",
				Formula = "%Track% - %Title% [%Playback.State%] [%Playback.Position%/%Playback.Length%]",
				Type = MetaDataFormulaType.Title
			},
			new MetaDataFormula {
				Formula = "%FileName%",
				Type = MetaDataFormulaType.MetaDataLoader
			}
		};

		#endregion

		#region Bindable Properties

		/// <summary>
		/// Get a collection of all the column details that are available to be chosen from [Bindable]
		/// </summary>
		public ObservableCollection<ColumnDetail> AllColumns { get { return _allColumns; } }

		/// <summary>
		/// Get a collection of all the commands that may be executed as a separate process [Bindable]
		/// </summary>
		public ObservableCollection<Command> Commands { get { return _commands; } }

		/// <summary>
		/// Get/set the default directory for media browsing
		/// </summary>
		public string DirectoryMediaDefault {
			get { return _directoryMediaDefault; }
			set { this.SetAndNotify(ref _directoryMediaDefault, value, "DirectoryMediaDefault"); }
		}

		/// <summary>
		/// Get/set the default directory for picture browsing [Bindable]
		/// </summary>
		public string DirectoryPictureDefault {
			get { return _directoryPictureDefault; }
			set { this.SetAndNotify(ref _directoryPictureDefault, value, "DirectoryPictureDefault"); }
		}

		/// <summary>
		/// Get the collection of formulas that may be used throughout the application [Bindable]
		/// </summary>
		public ObservableCollection<MetaDataFormula> Formulas { get { return _formulas; } }

		/// <summary>
		/// Get whether or not the settings have loaded or not [Bindable]
		/// </summary>
		public bool IsLoaded {
			get { return _isLoaded; }
			private set { this.SetAndNotify(ref _isLoaded, value, null, OnLoaded, "IsLoaded"); }
		}

		/// <summary>
		/// Get the current collection of libraries [Bindable]
		/// </summary>
		public IEnumerable<Library> Libraries {
			get { return _libraries; }
			private set { this.SetAndNotify(ref _libraries, (value ?? Enumerable.Empty<Library>()).ToArray(), "Libraries"); }
		}

		/// <summary>
		/// Get the current loaded library [Bindable]
		/// </summary>
		public Library LibraryCurrent {
			get { return _libraryCurrent; }
			set { this.SetAndNotify(ref _libraryCurrent, value, OnLibraryCurrentChanging, OnLibraryCurrentChanged, "LibraryCurrent"); }
		}

		/// <summary>
		/// Get/set the current selected library for visual purposes [Bindable]
		/// </summary>
		public Library LibrarySelected {
			get { return _librarySelected; }
			set { this.SetAndNotify(ref _librarySelected, value, "LibrarySelected"); }
		}

		/// <summary>
		/// Get/set the location for the mini player for startup and closing purposes only [Bindable]
		/// </summary>
		public Point? MiniLocation {
			get { return _miniLocation; }
			set { this.SetAndNotify(ref _miniLocation, value, "MiniLocation"); }
		}

		/// <summary>
		/// Get/set the selected tab within this window [Bindable]
		/// </summary>
		public int SelectedTab {
			get { return _selectedTab; }
			set { this.SetAndNotify(ref _selectedTab, value, "SelectedTab", "SelectedTabEnum"); }
		}

		/// <summary>
		/// Get/set the selected tab within this window [Bindable]
		/// </summary>
		public TabOption SelectedTabEnum {
			get { return (TabOption)SelectedTab; }
			set { SelectedTab = (int)value; }
		}

		/// <summary>
		/// Get whether or not the MDI manager should be visible [Bindable]
		/// </summary>
		public bool ShowMDIManager {
			get { return _showMDIManager; }
			set { this.SetAndNotify(ref _showMDIManager, value, null, OnShowMDIManagerChanged, "ShowMDIManager"); }
		}

		/// <summary>
		/// Get whether or not the application is in minituare mode [Bindable]
		/// </summary>
		public bool ShowMini {
			get { return _showMini; }
			set { this.SetAndNotify(ref _showMini, value, null, OnShowMiniChanged, "ShowMini"); }
		}

		#endregion

		#region Events

		public event Action<Library> LibraryMediaCollectionChanged;
		private event Action Loaded;
		public event Action<MediaFile, string> MediaFilePropertyChanged;
		public event Action<MediaObject> MediaObjectChanged;
		public event Action<MediaFile> NowPlayingChanged;
		public event Action ShowMDIManagerChanged;
		public event Action ShowMiniChanged;

		#endregion

		#region XML To mCubed Members

		/// <summary>
		/// Generate from a XML column detail to a mCubed column detail
		/// </summary>
		/// <param name="element">The XML column detail to generate from</param>
		/// <returns>The mCubed column detail that is generated</returns>
		private ColumnDetail GenerateColumn(XElement element) {
			// Attempt to reuse a created object
			string key = element.Parse("Key", "");
			ColumnType type = element.Parse("Type", ColumnType.Property);
			ColumnDetail detail = AllColumns.FirstOrDefault(c => c.Key == key && c.Type == type);

			// Create the instance if it wasn't found
			if (detail == null) {
				try {
					detail = new ColumnDetail(type, key);
				} catch {
					return null;
				}
			}

			// Put the instance in the dictionary and modify the details accordingly
			detail.XMLID = element.Parse<int>("ID", 0);
			detail.Display = element.Parse("Display", detail.Display);
			return detail;
		}

		/// <summary>
		/// Generate from a XML command to a mCubed command
		/// </summary>
		/// <param name="element">The XML command to generate from</param>
		/// <returns>The mCubed command that is generated</returns>
		private Command GenerateCommand(XElement element) {
			return new Command
			{
				DisplayName = element.Parse<string>("DisplayName"),
				Value = element.Parse<string>("Value")
			};
		}

		/// <summary>
		/// Generate from a XML formula to a mCubed formula
		/// </summary>
		/// <param name="element">The XML formula to generate from</param>
		/// <returns>The mCubed formula that is generated</returns>
		private MetaDataFormula GenerateFormula(XElement element) {
			// Attempt to reuse a created object
			MetaDataFormulaType type = element.Name.LocalName.Parse(MetaDataFormulaType.Custom);
			MetaDataFormula formula = null;
			if (type != MetaDataFormulaType.Custom) {
				formula = Formulas.FirstOrDefault(f => f.Type == type);
			}

			// Create an object otherwise
			if (formula == null)
				formula = new MetaDataFormula { Type = type };

			// Fill it in
			formula.Formula = element.Parse("Formula", formula.Formula);
			formula.FallbackValue = element.Parse("FallbackValue", formula.FallbackValue);
			formula.Name = element.Parse("Name", formula.Name);
			return formula;
		}

		/// <summary>
		/// Generate from a XML library to a mCubed library
		/// </summary>
		/// <param name="element">The XML library to generate from</param>
		/// <returns>The mCubed library that is generated</returns>
		private Library GenerateLibrary(XElement element) {
			var library = new Library
			{
				DisplayName = element.Parse<string>("DisplayName"),
				IsLoaded = element.Parse("IsLoaded", false),
				IsShuffled = element.Parse("IsShuffled", false),
				RepeatStatus = element.Parse("RepeatStatus", MediaRepeat.NoRepeat)
			};
			library.AutoRenameOnUpdates = element.Parse("AutoRenameOnUpdates", library.AutoRenameOnUpdates);
			library.FilenameFormula = element.Parse("FilenameFormula", library.FilenameFormula);
			library.LoadOnStartup = element.Parse("LoadOnStartup", library.LoadOnStartup);
			library.MediaObject.Balance = element.Parse("MOBalance", library.MediaObject.Balance);
			library.MediaObject.PlaybackSpeed = element.Parse("MOPlaybackSpeed", library.MediaObject.PlaybackSpeed);
			library.MediaObject.Volume = element.Parse("MOVolume", library.MediaObject.Volume);
			if (library.LoadOnStartup)
				library.AddDirectory(element.Elements("Directory").Select(ele => ele.Value));
			else
				library.AddDirectories(element.Elements("Directory").Select(ele => ele.Value).Distinct());
			library.ColumnSettings.GenerateCollections(element);
			return library;
		}

		/// <summary>
		/// Generate from XML settings to the main settings
		/// </summary>
		/// <param name="element">The XML settings to generate from</param>
		private void GenerateRoot(XElement element) {
			if (element.Element("Formulas") != null)
				element.Element("Formulas").Elements().Select(GenerateFormula).Where(e => e != null && !Formulas.Contains(e)).Perform(e => Formulas.Add(e));
			if (element.Element("Columns") != null)
				element.Element("Columns").Elements().Select(GenerateColumn).Where(c => c != null && !AllColumns.Contains(c)).Perform(c => AllColumns.Add(c));
			if (element.Element("Libraries") != null)
				element.Element("Libraries").Elements().Select(GenerateLibrary).Where(l => l != null && !Libraries.Contains(l)).Perform(l => AddLibrary(l));
			if (element.Element("Commands") != null)
				element.Element("Commands").Elements().Select(GenerateCommand).Where(c => c != null && !Commands.Contains(c)).Perform(c => Commands.Add(c));
			ShowMDIManager = element.Parse("ShowMDIManager", true);
			ShowMini = element.Parse("ShowMini", false);
			MiniLocation = element.Parse<Point?>("MiniLocation", null);
			DirectoryMediaDefault = element.Parse("DirectoryMediaDefault", Environment.CurrentDirectory);
			DirectoryPictureDefault = element.Parse("DirectoryPictureDefault", Environment.CurrentDirectory);
			SelectedTabEnum = element.Parse("SelectedTab", SelectedTabEnum);
		}

		#endregion

		#region mCubed To XML Members

		/// <summary>
		/// Generate from a mCubed column detail to a XML column detail
		/// </summary>
		/// <param name="column">The mCubed column detail to generate from</param>
		/// <param name="id">The numerical identification for the given column detail</param>
		/// <returns>The XML column detail that is generated</returns>
		private XElement GenerateColumn(ColumnDetail column, int id) {
			return new XElement("Column",
				new XAttribute("ID", id),
				new XAttribute("Display", column.Display),
				new XAttribute("Key", column.Key),
				new XAttribute("Type", column.Type)
			);
		}

		/// <summary>
		/// Generate from a mCubed command to a XML command
		/// </summary>
		/// <param name="command">The mCubed command to generate from</param>
		/// <returns>The XML command that is generated</returns>
		private XElement GenerateCommand(Command command) {
			return new XElement("Command",
				new XAttribute("DisplayName", command.DisplayName ?? ""),
				new XAttribute("Value", command.Value ?? ""));
		}

		/// <summary>
		/// Generate from a mCubed formula to a XML formula
		/// </summary>
		/// <param name="library">The mCubed formula to generate from</param>
		/// <returns>The XML formula that is generated</returns>
		private XElement GenerateFormula(MetaDataFormula formula) {
			return new XElement(formula.Type.ToString(),
				new XAttribute("Formula", formula.Formula ?? ""),
				new XAttribute("FallbackValue", formula.FallbackValue ?? ""),
				new XAttribute("Name", formula.Name ?? "")
			);
		}

		/// <summary>
		/// Generate from a mCubed library to a XML library
		/// </summary>
		/// <param name="library">The mCubed library to generate from</param>
		/// <returns>The XML library that is generated</returns>
		private XElement GenerateLibrary(Library library) {
			return new XElement("Library",
				new XAttribute("AutoRenameOnUpdates", library.AutoRenameOnUpdates),
				new XAttribute("DisplayName", library.DisplayName ?? ""),
				new XAttribute("FilenameFormula", library.FilenameFormula ?? ""),
				new XAttribute("IsLoaded", library.IsLoaded),
				new XAttribute("IsShuffled", library.IsShuffled),
				new XAttribute("LoadOnStartup", library.LoadOnStartup),
				new XAttribute("RepeatStatus", library.RepeatStatus),
				new XAttribute("MOBalance", library.MediaObject.Balance),
				new XAttribute("MOPlaybackSpeed", library.MediaObject.PlaybackSpeed),
				new XAttribute("MOVolume", library.MediaObject.Volume),
				library.ColumnSettings.GenerateCollections(),
				library.Directories.Select(str => new XElement("Directory", str))
			);
		}

		/// <summary>
		/// Generate from the main settings to XML settings
		/// </summary>
		/// <returns>The XML settings that are generated</returns>
		private XElement GenerateRoot() {
			int id = 1;
			return new XElement("mCubed",
				new XAttribute("ShowMDIManager", ShowMDIManager),
				new XAttribute("ShowMini", ShowMini),
				new XAttribute("MiniLocation", ((object)MiniLocation ?? "").ToString()),
				new XAttribute("DirectoryMediaDefault", DirectoryMediaDefault ?? ""),
				new XAttribute("DirectoryPictureDefault", DirectoryPictureDefault ?? ""),
				new XAttribute("SelectedTab", SelectedTabEnum.ToString() ?? ""),
				new XElement("Formulas", Formulas.Select(f => GenerateFormula(f))),
				new XElement("Libraries", Libraries.Where(l => l.SaveLibrary).Select(l => GenerateLibrary(l))),
				new XElement("Columns", AllColumns.Select(c => GenerateColumn(c, id++))),
				new XElement("Commands", Commands.Select(c => GenerateCommand(c)))
			);
		}

		#endregion

		#region File I/O Members

		/// <summary>
		/// Test to see if the settings file is valid
		/// </summary>
		/// <returns>True if the settings file is valid, or false otherwise</returns>
		private bool ValidSettingsFile() {
			try {
				_document = XDocument.Load(_path);
				return _document.Root != null;
			} catch {
				return false;
			}
		}

		/// <summary>
		/// Load the settings file
		/// </summary>
		public void Load() {
			// Setup the columns
			foreach (var property in MetaDataFormula.MetaDataProperties) {
				AllColumns.Add(new ColumnDetail(property));
			}

			// Register to event handlers
			Formulas.CollectionChanged += new NotifyCollectionChangedEventHandler(OnFormulasCollectionChanged);
			AllColumns.CollectionChanged += new NotifyCollectionChangedEventHandler(OnAllColumnsCollectionChanged);

			// Attempt to load the file
			if (ValidSettingsFile())
				GenerateRoot(_document.Root);

			// Add at least one default library
			if (Libraries.Count() == 0)
				AddLibrary(GenerateDefaultLibrary());

			// Load at least one library first
			if (LibraryCurrent == null)
				LibraryCurrent = Libraries.FirstOrDefault();

			// Select the current library
			if (LibrarySelected == null)
				LibrarySelected = LibraryCurrent;

			// The settings have loaded
			Utilities.MainProcessManager.AddProcess(p =>
			{
				IsLoaded = true;
				p.CompletedCount++;
			}, "Finalizing startup...", 1);
		}

		/// <summary>
		/// Save the settings file
		/// </summary>
		public void Save() {
			_document = new XDocument { Declaration = new XDeclaration("1.0", "utf8", "yes") };
			_document.Add(GenerateRoot());
			_document.Save(_path);
		}

		#endregion

		#region Action Members

		/// <summary>
		/// Perform an action on the current playing media object
		/// </summary>
		/// <param name="action">The action to perform on the media object</param>
		public void PerformAction(MediaAction action) {
			switch (action) {
				case MediaAction.Play:
					LibraryCurrent.MediaObject.State = MediaState.Play;
					break;
				case MediaAction.Pause:
					LibraryCurrent.MediaObject.State = MediaState.Pause;
					break;
				case MediaAction.PlayPause:
					LibraryCurrent.MediaObject.State = LibraryCurrent.MediaObject.State == MediaState.Play ? MediaState.Pause : MediaState.Play;
					break;
				case MediaAction.Stop:
					LibraryCurrent.MediaObject.State = MediaState.Stop;
					break;
				case MediaAction.Prev:
					LibraryCurrent.Select(MediaSelect.Previous);
					break;
				case MediaAction.Next:
					LibraryCurrent.Select(MediaSelect.Next);
					break;
				case MediaAction.Restart:
					LibraryCurrent.MediaObject.Seek(0);
					break;
				case MediaAction.ToggleMini:
					ShowMini = !ShowMini;
					break;
				case MediaAction.ToggleMDI:
					ShowMDIManager = !ShowMDIManager;
					break;
				case MediaAction.ToggleMediaOrder:
					LibraryCurrent.ToggleMediaOrders(true);
					break;
				case MediaAction.ToggleRepeat:
					LibraryCurrent.ToggleRepeat(true);
					break;
				case MediaAction.ToggleShuffle:
					LibraryCurrent.IsShuffled = !LibraryCurrent.IsShuffled;
					break;
			}
		}

		/// <summary>
		/// Performs the specified action when the settings have been loaded, or is already loaded
		/// </summary>
		/// <param name="action">The action to perform when the settings are loaded</param>
		public void PerformWhenLoaded(Action action) {
			if (action != null) {
				if (IsLoaded) {
					action();
				} else {
					Loaded += action;
				}
			}
		}

		#endregion

		#region Collection Members

		/// <summary>
		/// Add a library to the collection of libraries
		/// </summary>
		/// <param name="library">The library to add to the collection of libraries</param>
		public void AddLibrary(Library library) {
			if (library != null) {
				library.MediaFiles.PropertyChanged += (s, e) => OnLibraryMediaCollectionChanged(library, s, e);
				library.MediaFilePropertyChanged += OnMediaFilePropertyChanged;
				Libraries = Libraries.Concat(new[] { library });
			}
		}

		/// <summary>
		/// Generate a default library that may be used
		/// </summary>
		/// <returns>A default library that may be used</returns>
		public Library GenerateDefaultLibrary() {
			Library library = new Library { DisplayName = "Default Library" };
			library.AddDirectory(Environment.GetFolderPath(Environment.SpecialFolder.MyMusic));
			return library;
		}

		/// <summary>
		/// Remove a library from the collection of libraries
		/// </summary>
		/// <param name="library">The library to remove from the collection</param>
		public void RemoveLibrary(Library library) {
			if (library != null) {
				var newLibraryCurrent = Libraries.OrderByDescending(l => l.IsLoaded).FirstOrDefault(l => l != library);
				if (newLibraryCurrent == null)
					AddLibrary(newLibraryCurrent = GenerateDefaultLibrary());
				if (library.IsLoaded)
					newLibraryCurrent.IsLoaded = true;
				Libraries = Libraries.Where(l => l != library);
				library.Dispose();
			}
		}

		#endregion

		#region Event Handlers

		/// <summary>
		/// Invoke the show MDI manager and mini changed event handlers
		/// </summary>
		public void InvokeEvents() {
			OnShowMDIManagerChanged();
			OnShowMiniChanged();
		}

		/// <summary>
		/// Event that handles when the current loaded library is changing
		/// </summary>
		private void OnLibraryCurrentChanging() {
			if (LibraryCurrent != null)
				LibraryCurrent.IsLoaded = false;
		}

		/// <summary>
		/// Event that handles when the current loaded library has changed
		/// </summary>
		private void OnLibraryCurrentChanged() {
			// Load the new library
			if (LibraryCurrent != null) {
				LibraryCurrent.IsLoaded = true;

				// Send some events
				OnMediaObjectChanged(LibraryCurrent.MediaObject);
				OnNowPlayingChanged(LibraryCurrent.MediaFileCurrent);
			} else {
				// Send some events
				OnMediaObjectChanged(null);
				OnNowPlayingChanged(null);
			}
		}

		/// <summary>
		/// Event that handles when a library's media files collection has changed
		/// </summary>
		/// <param name="sender">The library whose media files property has changed</param>
		/// <param name="library">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnLibraryMediaCollectionChanged(Library library, object sender, PropertyChangedEventArgs e) {
			var tempHandler = LibraryMediaCollectionChanged;
			if (tempHandler != null && library.MediaFiles == sender && e.PropertyName == "Items") {
				tempHandler(library);
			}
		}

		/// <summary>
		/// Event that handles when a media file's property has changed
		/// </summary>
		/// <param name="file">The file whose property has changed</param>
		/// <param name="property">The name of the property that has changed</param>
		private void OnMediaFilePropertyChanged(MediaFile file, string property) {
			var tempHandler = MediaFilePropertyChanged;
			if (tempHandler != null) {
				tempHandler(file, property);
			}
		}

		/// <summary>
		/// Event that handles when the settings have loaded
		/// </summary>
		private void OnLoaded() {
			var tempHandler = Loaded;
			if (IsLoaded && tempHandler != null)
				tempHandler();
			Loaded = null;
		}

		/// <summary>
		/// Event that handles when the collection of all the columns changes
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnAllColumnsCollectionChanged(object sender, NotifyCollectionChangedEventArgs e) {
			this.OnPropertyChanged("AllColumns");
		}

		/// <summary>
		/// Event that handles when the formulas collection changes so that the columns change accordingly
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnFormulasCollectionChanged(object sender, NotifyCollectionChangedEventArgs e) {
			// Remove each of the removed formula columns from each of the column collections
			if (e.Action == NotifyCollectionChangedAction.Remove) {
				foreach (var formula in e.OldItems.OfType<MetaDataFormula>()) {
					var column = AllColumns.FirstOrDefault(c => c.Type == ColumnType.Formula && c.Key == formula.Name);
					if (column != null) {
						column.Dispose();
						AllColumns.Remove(column);
						foreach (var library in Libraries) {
							library.ColumnSettings.Remove(column);
						}
					}
				}
			}

			// Add a formula column for each of the added items
			else if (e.Action == NotifyCollectionChangedAction.Add) {
				foreach (var formula in e.NewItems.OfType<MetaDataFormula>()) {
					AllColumns.Add(new ColumnDetail(formula));
				}
			}
		}

		/// <summary>
		/// Event that handles when the media object has changed
		/// </summary>
		/// <param name="mediaObject">The new media object</param>
		private void OnMediaObjectChanged(MediaObject mediaObject) {
			if (MediaObjectChanged != null)
				MediaObjectChanged(mediaObject);
		}

		/// <summary>
		/// Event that handles when the now playing media has changed
		/// </summary>
		/// <param name="file">The media that is now playing</param>
		public void OnNowPlayingChanged(MediaFile file) {
			if (NowPlayingChanged != null)
				NowPlayingChanged(file);
		}

		/// <summary>
		/// Event that handles when the MDI manager should be shown or hidden
		/// </summary>
		private void OnShowMDIManagerChanged() {
			if (ShowMDIManagerChanged != null)
				ShowMDIManagerChanged();
		}

		/// <summary>
		/// Event that handles when the mini-player should be shown or hidden
		/// </summary>
		private void OnShowMiniChanged() {
			if (ShowMiniChanged != null)
				ShowMiniChanged();
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
		/// Dispose of the settings properly
		/// </summary>
		public void Dispose() {
			// Unsubscribe others from its events
			PropertyChanged = null;
			PropertyChanging = null;
			LibraryMediaCollectionChanged = null;
			Loaded = null;
			MediaFilePropertyChanged = null;
			MediaObjectChanged = null;
			NowPlayingChanged = null;
			ShowMDIManagerChanged = null;
			ShowMiniChanged = null;

			// Unsubscribe from delegates
			AllColumns.CollectionChanged -= new NotifyCollectionChangedEventHandler(OnAllColumnsCollectionChanged);
			Formulas.CollectionChanged -= new NotifyCollectionChangedEventHandler(OnFormulasCollectionChanged);

			// Dispose all disposable references it created
			foreach (Library library in Libraries)
				library.Dispose();
			foreach (MetaDataFormula formula in Formulas)
				formula.Dispose();
		}

		#endregion
	}

	public class ColumnSettings : IExternalNotifyPropertyChanged, IExternalNotifyPropertyChanging, IDisposable {
		#region Data Store

		private readonly Dictionary<string, ObservableCollection<ColumnVector>> _collections = new Dictionary<string, ObservableCollection<ColumnVector>>();

		#endregion

		#region Bindable Properties

		/// <summary>
		/// Get the collection of all the column details that are available to be chosen from [Bindable]
		/// </summary>
		public ObservableCollection<ColumnDetail> AllColumns { get { return Utilities.MainSettings.AllColumns; } }

		/// <summary>
		/// Get the collection of the columns in order to display [Bindable]
		/// </summary>
		public ObservableCollection<ColumnVector> Display { get { return _collections["Display"]; } }

		/// <summary>
		/// Get the collection of the columns in order to group by [Bindable]
		/// </summary>
		public ObservableCollection<ColumnVector> GroupBy { get { return _collections["GroupBy"]; } }

		/// <summary>
		/// Get the collection of the columns in order to sort by [Bindable]
		/// </summary>
		public ObservableCollection<ColumnVector> SortBy { get { return _collections["SortBy"]; } }

		#endregion

		#region Indexer

		/// <summary>
		/// Get the collection of columns at the given group name
		/// </summary>
		/// <param name="group">The name of the group to retrieve the columns for</param>
		/// <returns>The collection of columns at the given group name</returns>
		public ObservableCollection<ColumnVector> this[string group] {
			get { return _collections[group]; }
		}

		#endregion

		#region Constructor

		/// <summary>
		/// Construct a new column settings
		/// </summary>
		public ColumnSettings() {
			// Create the collections and register to their collection changed event
			foreach (string key in new[] { "Display", "GroupBy", "SortBy" }) {
				var value = new ObservableCollection<ColumnVector>();
				value.CollectionChanged += new NotifyCollectionChangedEventHandler(OnCollectionChanged);
				_collections.Add(key, value);
			}

			// Register to the collection changed event for AllColumns
			Utilities.MainSettings.PropertyChanged += new PropertyChangedEventHandler(OnAllColumnsChanged);

			// Default the Display columns
			new[] { "OrderKey", "Track", "Title", "JoinedPerformers", "Album", "LengthString", "JoinedGenres", "FileName" }.
				Select(s => AllColumns.FirstOrDefault(c => c.Type == ColumnType.Property && c.Key == s)).
				Where(s => s != null).
				Perform(s => Display.Add(new ColumnVector(s)));
		}

		#endregion

		#region Event Handlers

		/// <summary>
		/// Event that handles when all the collection of all the columns changed
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnAllColumnsChanged(object sender, PropertyChangedEventArgs e) {
			if (e.PropertyName == "AllColumns")
				this.OnPropertyChanged("AllColumns");
		}

		/// <summary>
		/// Event that handles when a collection has changed for the column vectors
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnCollectionChanged(object sender, NotifyCollectionChangedEventArgs e) {
			// Get the key for the collection that changed
			var pair = _collections.SingleOrDefault(c => c.Value == sender);
			if (!string.IsNullOrEmpty(pair.Key))
				this.OnPropertyChanged(pair.Key);
		}

		#endregion

		#region XML to mCubed Members

		/// <summary>
		/// Generate the mCubed column collections from XML
		/// </summary>
		/// <param name="element">The XML column collections to generate from</param>
		public void GenerateCollections(XElement element) {
			// Load up all the column collections
			foreach (var collection in _collections) {
				// Find the collection's list of columns
				bool hasLoaded = false;
				XElement ele = element.Element(collection.Key);
				if (ele != null) {
					// Generate a column vector for each of the columns in the collection
					foreach (var column in ele.Elements()) {
						int id = column.Parse<int>("ID", 0);
						var detail = AllColumns.FirstOrDefault(c => c.XMLID == id);
						if (id != 0 && detail != null) {
							var vector = new ColumnVector(detail);
							vector.Direction = column.Parse("Direction", vector.Direction);
							vector.Width = column.Parse("Width", vector.Width);
							if (!hasLoaded) {
								hasLoaded = true;
								collection.Value.Clear();
							}
							collection.Value.Add(vector);
						}
					}
				}
			}
		}

		#endregion

		#region mCubed to XML Members

		/// <summary>
		/// Generate the XML column collections from mCubed
		/// </summary>
		/// <returns>The XML column collections that were generated</returns>
		public IEnumerable<XElement> GenerateCollections() {
			foreach (var collection in _collections) {
				XElement element = new XElement(collection.Key);
				foreach (var column in collection.Value) {
					int id = AllColumns.IndexOf(column.ColumnDetail);
					if (id == -1) {
						id = AllColumns.Count;
						AllColumns.Add(column.ColumnDetail);
					}
					element.Add(new XElement("Column",
						new XAttribute("ID", id + 1),
						new XAttribute("Direction", column.Direction),
						new XAttribute("Width", column.Width.ToString())
					));
				}
				yield return element;
			}
		}

		#endregion

		#region Members

		/// <summary>
		/// Remove a given column detail from each of the column detail collections
		/// </summary>
		/// <param name="column">The column details to remove from each of the column detail collections</param>
		public void Remove(ColumnDetail column) {
			foreach (var collection in _collections) {
				foreach (var item in collection.Value.Where(c => c.ColumnDetail == column).ToArray()) {
					item.Dispose();
					collection.Value.Remove(item);
				}
			}
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
		/// Dispose of the column settings appropriately
		/// </summary>
		public void Dispose() {
			// Unsubscribe others from its events
			PropertyChanged = null;
			PropertyChanging = null;

			// Dispose of the resources it created while unregistering from events
			foreach (var collection in _collections) {
				foreach (var item in collection.Value) {
					item.Dispose();
				}
				collection.Value.CollectionChanged -= new NotifyCollectionChangedEventHandler(OnCollectionChanged);
			}

			// Finish unregistering from events
			Utilities.MainSettings.PropertyChanged -= new PropertyChangedEventHandler(OnAllColumnsChanged);
		
			// Ensure there are no cyclic references
			_collections.Clear();
		}

		#endregion
	}
}