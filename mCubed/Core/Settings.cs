using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.IO;
using System.Linq;
using System.Xml.Linq;
using System.Collections.ObjectModel;
using System.Text;

namespace mCubed.Core {
	public class Settings : INotifyPropertyChanged {
		#region INotifyPropertyChanged Members

		public event PropertyChangedEventHandler PropertyChanged;

		#endregion

		#region Data Store

		private string _path = Path.Combine(Utilities.ExecutionDirectory, "mCubed.xml");
		private ColumnSettings _columnSettings;
		private string _directoryMediaDefault = Environment.CurrentDirectory;
		private string _directoryPictureDefault = Environment.CurrentDirectory;
		private XDocument _document;
		private bool _isLoaded;
		private IEnumerable<Library> _libraries = Enumerable.Empty<Library>();
		private Library _libraryCurrent;
		private Library _librarySelected;
		private int _selectedTab = (int)TabOption.Help;
		private bool _showMDIManager = true;
		private bool _showMini;
		private IEnumerable<MetaDataFormula> _formulas = new[] {
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
		/// Get the column settings that are to be used [Bindable]
		/// </summary>
		public ColumnSettings ColumnSettings { get { return _columnSettings; } }

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
		public IEnumerable<MetaDataFormula> Formulas {
			get { return _formulas; }
			private set { this.SetAndNotify(ref _formulas, (value ?? Enumerable.Empty<MetaDataFormula>()).ToArray(), "Formulas"); }
		}

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

		#region Properties

		public event Action Loaded;
		public event Action<MediaObject> MediaObjectChanged;
		public event Action<MediaFile> NowPlayingChanged;
		public event Action<MediaFailure, string> Failure;
		public event Action ShowMDIManagerChanged;
		public event Action ShowMiniChanged;

		#endregion

		#region XML To mCubed Members

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
			library.MediaObject.Balance = element.Parse("MOBalance", library.MediaObject.Balance);
			library.MediaObject.PlaybackSpeed = element.Parse("MOPlaybackSpeed", library.MediaObject.PlaybackSpeed);
			library.MediaObject.Volume = element.Parse("MOVolume", library.MediaObject.Volume);
			foreach (var item in element.Elements("Directory").Select(ele => ele.Value))
				library.Directories.Add(item);
			return library;
		}

		/// <summary>
		/// Generate from XML settings to the main settings
		/// </summary>
		/// <param name="element">The XML settings to generate from</param>
		private void GenerateRoot(XElement element) {
			if (element.Element("Libraries") != null)
				Libraries = element.Element("Libraries").Elements().Select(e => GenerateLibrary(e));
			if (element.Element("Formulas") != null)
				Formulas = Formulas.Union(element.Element("Formulas").Elements().Select(e => GenerateFormula(e)));
			if (element.Element("Columns") != null)
				ColumnSettings.GenerateRoot(element.Element("Columns"));
			ShowMDIManager = element.Parse("ShowMDIManager", true);
			ShowMini = element.Parse("ShowMini", false);
			DirectoryMediaDefault = element.Parse("DirectoryMediaDefault", Environment.CurrentDirectory);
			DirectoryPictureDefault = element.Parse("DirectoryPictureDefault", Environment.CurrentDirectory);
			SelectedTabEnum = element.Parse("SelectedTab", SelectedTabEnum);
		}

		#endregion

		#region mCubed To XML Members

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
				new XAttribute("DisplayName", library.DisplayName ?? ""),
				new XAttribute("IsLoaded", library.IsLoaded),
				new XAttribute("IsShuffled", library.IsShuffled),
				new XAttribute("RepeatStatus", library.RepeatStatus),
				new XAttribute("MOBalance", library.MediaObject.Balance),
				new XAttribute("MOPlaybackSpeed", library.MediaObject.PlaybackSpeed),
				new XAttribute("MOVolume", library.MediaObject.Volume),
				library.Directories.Select(str => new XElement("Directory", str))
			);
		}

		/// <summary>
		/// Generate from the main settings to XML settings
		/// </summary>
		/// <returns>The XML settings that are generated</returns>
		private XElement GenerateRoot() {
			return new XElement("mCubed",
				new XAttribute("ShowMDIManager", ShowMDIManager),
				new XAttribute("ShowMini", ShowMini),
				new XAttribute("DirectoryMediaDefault", DirectoryMediaDefault ?? ""),
				new XAttribute("DirectoryPictureDefault", DirectoryPictureDefault ?? ""),
				new XAttribute("SelectedTab", SelectedTabEnum.ToString() ?? ""),
				new XElement("Formulas", Formulas.Select(f => GenerateFormula(f))),
				new XElement("Libraries", Libraries.Select(l => GenerateLibrary(l))),
				ColumnSettings.GenerateRoot()
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
			// Create the column settings first
			_columnSettings = new ColumnSettings();

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
			IsLoaded = true;
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

		#endregion

		#region Collection Members

		/// <summary>
		/// Add a library to the collection of libraries
		/// </summary>
		/// <param name="library">The library to add to the collection of libraries</param>
		public void AddLibrary(Library library) {
			if (library != null)
				Libraries = Libraries.Union(new[] { library });
		}

		/// <summary>
		/// Generate a default library that may be used
		/// </summary>
		/// <returns>A default library that may be used</returns>
		public Library GenerateDefaultLibrary() {
			Library library = new Library { DisplayName = "Default Library" };
			library.Directories.Add(Environment.GetFolderPath(Environment.SpecialFolder.MyMusic));
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
			}
		}

		/// <summary>
		/// Add a custom formula to the collection of formulas
		/// </summary>
		/// <param name="formula">The formula that will be added</param>
		public void AddFormula(MetaDataFormula formula) {
			if (formula != null && formula.Type == MetaDataFormulaType.Custom)
				Formulas = Formulas.Union(new[] { formula });
		}

		/// <summary>
		/// Remove a custom formula from the collection of formulas
		/// </summary>
		/// <param name="formula">The formula that will be removed</param>
		public void RemoveFormula(MetaDataFormula formula) {
			if (formula != null && formula.Type == MetaDataFormulaType.Custom)
				Formulas = Formulas.Where(f => f != formula);
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
		/// Event that handles when the settings have loaded
		/// </summary>
		private void OnLoaded() {
			if (IsLoaded && Loaded != null)
				Loaded();
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
		/// Event that handles when playback of media has failed
		/// </summary>
		/// <param name="failure">The failure type</param>
		/// <param name="error">The error message</param>
		public void OnFailure(MediaFailure failure, string error) {
			if (Failure != null)
				Failure(failure, error);
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
	}

	public class ColumnSettings : INotifyPropertyChanged {
		#region INotifyPropertyChanged Members

		public event PropertyChangedEventHandler PropertyChanged;

		#endregion

		#region Data Store

		private readonly ObservableCollection<ColumnDetail> _allColumns = new ObservableCollection<ColumnDetail>();
		private readonly ObservableCollection<ColumnDetail> _display = new ObservableCollection<ColumnDetail>();
		private readonly ObservableCollection<ColumnDetail> _groupBy = new ObservableCollection<ColumnDetail>();
		private readonly ObservableCollection<ColumnDetail> _sortBy = new ObservableCollection<ColumnDetail>();
		private readonly Dictionary<string, ObservableCollection<ColumnDetail>> _collections;

		#endregion

		#region Bindable Properties

		/// <summary>
		/// Get the collection of all the available columns to select from [Bindable]
		/// </summary>
		public ObservableCollection<ColumnDetail> AllColumns { get { return _allColumns; } }

		/// <summary>
		/// Get the collection of the columns in order to display [Bindable]
		/// </summary>
		public ObservableCollection<ColumnDetail> Display { get { return _display; } }

		/// <summary>
		/// Get the collection of the columns in order to group by [Bindable]
		/// </summary>
		public ObservableCollection<ColumnDetail> GroupBy { get { return _groupBy; } }

		/// <summary>
		/// Get the collection of the columns in order to sort by [Bindable]
		/// </summary>
		public ObservableCollection<ColumnDetail> SortBy { get { return _sortBy; } }

		#endregion

		#region Constructor

		/// <summary>
		/// Create a column settings instance with default column information
		/// </summary>
		public ColumnSettings() {
			_collections = new Dictionary<string, ObservableCollection<ColumnDetail>>()
			{
				{ "SortBy", SortBy },
				{ "GroupBy", GroupBy },
				{ "Display", Display }
			};
			Utilities.MainSettings.PropertyChanged += new PropertyChangedEventHandler(OnFormulasCollectionChanged);
			foreach (var property in MetaDataFormula.MetaDataProperties) {
				AllColumns.Add(new ColumnDetail(ColumnType.Property, property.Formula, property.Display));
			}
		}

		#endregion

		#region Event Handlers

		/// <summary>
		/// Event that handles when the formulas collection changes so that the columns change accordingly
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnFormulasCollectionChanged(object sender, PropertyChangedEventArgs e) {
			// Check if the formulas property changed
			if (e.PropertyName == null || e.PropertyName == "Formulas") {
				// Remove all the existing custom formula columns
				foreach (var column in AllColumns.Where(c => c.Type == ColumnType.Formula).ToArray()) {
					AllColumns.Remove(column);
					// Remove from SortBy, GroupBy, & SortBy
				}

				// Add columns for each of the new items
				foreach (var column in Utilities.MainSettings.Formulas.Where(f => f.Type == MetaDataFormulaType.Custom)) {
					AllColumns.Add(new ColumnDetail(ColumnType.Formula, column.Name, column.Name));
				}
			}
		}

		#endregion

		#region XML to mCubed Members

		/// <summary>
		/// Generate the mCubed column detail settings from XML
		/// </summary>
		/// <param name="element">The XML settings to generate from</param>
		public void GenerateRoot(XElement element) {
			// Load up all the indivdual column information first
			Dictionary<int, ColumnDetail> details = new Dictionary<int, ColumnDetail>();
			foreach (XElement ele in element.Elements()) {
				// Get the instance
				string key = ele.Parse("Key", "");
				ColumnType type = ele.Parse("Type", ColumnType.Property);
				ColumnDetail detail = AllColumns.FirstOrDefault(c => c.Key == key && c.Type == type);

				// Create the instance if it wasn't found
				if (detail == null) {
					detail = new ColumnDetail(type, key, null);
					AllColumns.Add(detail);
				}

				// Put the instance in the dictionary and modify the details accordingly
				int index = ele.Parse<int>("ID", 0);
				details.Add(index, detail);
				detail.Width = ele.Parse("Width", detail.Width);
				detail.Display = ele.Parse("Display", detail.Display);
			}

			// Load up all the column collections
			foreach (var collection in _collections) {
				// Find all the column details ID's
				string[] columns = element.Parse(collection.Key, "").Split(',');
				var ids = columns.Select(c => c.Parse(0)).Where(i => i != 0);

				// Find the column detail
				foreach (var id in ids.Where(i => details.ContainsKey(i)))
					collection.Value.Add(details[id]);
			}
		}

		#endregion

		#region mCubed to XML Members

		/// <summary>
		/// Generate the XML column detail settings from mCubed
		/// </summary>
		/// <returns>The XML settings that were generated</returns>
		public XElement GenerateRoot() {
			// Setup
			XElement root = new XElement("Columns");

			// Save all the column collections
			foreach (var collection in _collections) {
				StringBuilder builder = new StringBuilder();
				foreach (var column in collection.Value) {
					int id = AllColumns.IndexOf(column);
					if (id == -1) {
						id = AllColumns.Count;
						AllColumns.Add(column);
					}
					if (builder.Length != 0)
						builder.Append(",");
					builder.Append(id+1);
				}
				root.Add(new XAttribute(collection.Key, builder.ToString()));
			}

			// Save all the individual columns
			for (int i = 0; i < AllColumns.Count; i++) {
				var item = AllColumns[i];
				root.Add(new XElement("Column",
					new XAttribute("ID", i+1),
					new XAttribute("Display", item.Display),
					new XAttribute("Key", item.Key),
					new XAttribute("Type", item.Type),
					new XAttribute("Width", item.Width.ToString())
				));
			}
			return root;
		}

		#endregion
	}
}