using System;
using System.Collections;
using System.ComponentModel;
using System.Diagnostics;
using System.Linq;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Navigation;
using mCubed.Core;

namespace mCubed {
	public partial class mCubedSecondary : Window {
		#region Data Store

		private static DateTime _releaseDate = new DateTime(2011, 2, 19);

		#endregion

		#region Bindable Properties

		/// <summary>
		/// Get the date this version was released [Bindable]
		/// </summary>
		public static DateTime ReleaseDate { get { return mCubedSecondary._releaseDate; } }

		/// <summary>
		/// Get the main application settings [Bindable]
		/// </summary>
		public Settings Settings { get { return Utilities.MainSettings; } }

		#endregion

		#region Constructor

		public mCubedSecondary() {
			// Set up event handlers
			Closing += new CancelEventHandler(OnClosing);
			
			// Initialize
			InitializeComponent();
		}

		#endregion

		#region Application Settings

		/// <summary>
		/// Event that handles when a directory should be changed in the application settings
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void ApplicationSettings_BrowseDirectory(object sender, RoutedEventArgs e) {
			var ele = sender as FrameworkElement;
			var tag = ele == null ? null : ele.Tag as string;
			if (tag != null) {
				var property = typeof(Settings).GetProperty(tag);
				var directory = OnBrowseDirectory(property.GetValue(Settings, null) as string);
				if (directory != null) {
					property.SetValue(Settings, directory, null);
				}
			}
		}

		/// <summary>
		/// Event that handles when a custom formula needs to be added
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void ApplicationSettings_AddFormula(object sender, RoutedEventArgs e) {
			var formula = new MetaDataFormula { Formula = "%Title%", Name = "New Formula", Type = MetaDataFormulaType.Custom };
			Settings.Formulas.Add(formula);
		}

		/// <summary>
		/// Event that handles when a custom formula needs to be removed
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void ApplicationSettings_RemoveFormula(object sender, RoutedEventArgs e) {
			var selection = FormulaListView.SelectedItem as MetaDataFormula;
			if (selection != null) {
				selection.Dispose();
				Settings.Formulas.Remove(selection);
			}
		}

		#endregion

		#region Advanced Settings

		/// <summary>
		/// Event that handles when a command needs to be added
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void ApplicationSettings_AddCommand(object sender, RoutedEventArgs e) {
			Settings.Commands.Add(new Command
			{
				DisplayName = "Show in Windows Explorer",
				Value = "explorer.exe /select,%c"
			});
		}

		/// <summary>
		/// Event that handles when a command needs to be removed
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void ApplicationSettings_RemoveCommand(object sender, RoutedEventArgs e) {
			var selection = CommandListView.SelectedItem as Command;
			if (selection != null) {
				Settings.Commands.Remove(selection);
			}
		}

		#endregion

		#region Library Settings

		/// <summary>
		/// Event that handles when a library should be added
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void LibrarySettings_AddLibrary(object sender, RoutedEventArgs e) {
			Settings.LibrarySelected = new Library { DisplayName = "New Library" };
			Settings.AddLibrary(Settings.LibrarySelected);
		}

		/// <summary>
		/// Event that handles when a library should be deleted
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void LibrarySettings_RemoveLibrary(object sender, RoutedEventArgs e) {
			Settings.RemoveLibrary(Settings.LibrarySelected);
			Settings.LibrarySelected = Settings.LibraryCurrent;
		}

		/// <summary>
		/// Event that handles when a library should be loaded
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void LibrarySettings_LoadLibrary(object sender, RoutedEventArgs e) {
			if (Settings.LibrarySelected != null)
				Settings.LibrarySelected.IsLoaded = true;
		}

		/// <summary>
		/// Event that handles when a directory should be added to a library
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void LibrarySettings_AddDirectory(object sender, RoutedEventArgs e) {
			if (Settings.LibrarySelected != null) {
				var directory = OnBrowseDirectory(Settings.DirectoryMediaDefault);
				if(directory != null)
					Settings.LibrarySelected.AddDirectory(directory);
			}
		}

		/// <summary>
		/// Event that handles when a directory should be deleted from a library
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void LibrarySettings_RemoveDirectory(object sender, RoutedEventArgs e) {
			if (Settings.LibrarySelected != null && DirectorySelection.SelectedItem is string)
				Settings.LibrarySelected.RemoveDirectory((string)DirectorySelection.SelectedItem);
		}

		/// <summary>
		/// Event that handles when all the library's shuffled media orders should be re-shuffled
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void LibrarySettings_ReshuffleAll(object sender, RoutedEventArgs e) {
			if (Settings.LibrarySelected != null)
				Settings.LibrarySelected.Reshuffle();
		}

		/// <summary>
		/// Event that handles when the selected media order should be re-shuffled
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void LibrarySettings_Reshuffle(object sender, RoutedEventArgs e) {
			var order = MediaOrdersListBox.SelectedItem as MediaOrder;
			if (order != null && order.Parent != null)
				order.Parent.Reshuffle(order);
		}

		/// <summary>
		/// Event that handles when the selected media order should be loaded
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void LibrarySettings_LoadMediaOrder(object sender, RoutedEventArgs e) {
			var order = MediaOrdersListBox.SelectedItem as MediaOrder;
			if (order != null)
				order.IsLoaded = true;
		}

		#endregion

		#region Keyboard Shortcuts

		private static readonly IEnumerable _shortcuts = new[] {
			new { Shortcut = "Tab / Shift + Tab", Description = "Gives the next element keyboard focus (with the shift key determining if the previous or next element should be given focus)." },
			new { Shortcut = "Enter / Shift + Enter", Description = "Same as Tab / Shift + Tab, but with 'smart' functionality within the meta-data manager." },
			new { Shortcut = "Left Arrow / Right Arrow", Description = "Seeks the current playing media by 5% backward or forward, respectfully, but only when the slider is focused." },
			new { Shortcut = "FN + Play", Description = "Toggles between the play and pause status for the current media." },
			new { Shortcut = "FN + Stop", Description = "Stops the playback for the current media." },
			new { Shortcut = "FN + Previous", Description = "Loads the previous media from the current media using the current media order." },
			new { Shortcut = "FN + Next", Description = "Loads the next media from the current media using the current media order." },
			new { Shortcut = "Escape", Description = "Closes the auto-complete drop-down option when modifying a value in the meta-data manager after it has been opened." },
			new { Shortcut = "Ctrl + Space", Description = "Opens the auto-complete drop-down option when modifying a value in the meta-data manager." },
			new { Shortcut = "Ctrl + A", Description = "Selects all the text within a textbox, or selects all the media within the library viewer." },
			new { Shortcut = "Ctrl + C", Description = "Copies the selected text within a textbox." },
			new { Shortcut = "Ctrl + D", Description = "Deletes the current value in the meta-data manager if the field supports multiple values, or clears the current value otherwise." },
			new { Shortcut = "Ctrl + E", Description = "Cancels or undo's all the changes within the meta-data manager." },
			//new { Shortcut = "Ctrl + F", Description = "Moves the keyboard focus to the search/find field to search/find media within the selected library." },
			new { Shortcut = "Ctrl + H", Description = "Toggles the shuffle status of the current library." },
			new { Shortcut = "Ctrl + I", Description = "Toggles the visibility of the meta-data manager." },
			new { Shortcut = "Ctrl + M", Description = "Toggles the manually marking of the current field in the meta-data manager." },
			new { Shortcut = "Ctrl + N", Description = "Loads the next meta-data information from the loaded list, unless more than one is currently loaded, in which the first one is loaded." },
			new { Shortcut = "Ctrl + O / Shift + Ctrl + O", Description = "Toggles through the media orders defined on the current library (with the shift key determining if the previous or next order should be loaded)." },
			new { Shortcut = "Ctrl + P", Description = "Loads the previous meta-data information from the loaded list, unless more than one is currently loaded, in which the last one is loaded." },
			//new { Shortcut = "Ctrl + Q", Description = "Toggles the visibility of the quick view." },
			new { Shortcut = "Ctrl + R", Description = "Restarts the current loaded media from the beginning." },
			new { Shortcut = "Ctrl + S", Description = "Saves all the changes within the meta-data manager." },
			new { Shortcut = "Ctrl + T / Shift + Ctrl + T", Description = "Toggles the repeat status of the current library (with the shift key determining if the previous or next repeat status should be selected)." },
			new { Shortcut = "Ctrl + U / Shift + Ctrl + U", Description = "Cancels or undo's the change to the current field within the meta-data manager (with the shift key determining if the previous or next field should be given focus)." },
			new { Shortcut = "Ctrl + V", Description = "Pastes the current text stored in the clipboard." },
			new { Shortcut = "Ctrl + X", Description = "Copies and deletes the selected text within a textbox." },
			new { Shortcut = "Ctrl + Z", Description = "Cancels or undo's the most recent change within a textbox." },
			new { Shortcut = "Alt + Left Arrow", Description = "Gives the field to left of the current field keyboard focus within the meta-data manager." },
			new { Shortcut = "Alt + Right Arrow", Description = "Gives the field to right of the current field keyboard focus within the meta-data manager." },
			new { Shortcut = "Alt + Up Arrow", Description = "Gives the field above the current field keyboard focus within the meta-data manager." },
			new { Shortcut = "Alt + Down Arrow", Description = "Gives the field below the current field keyboard focus within the meta-data manager." }
		};

		/// <summary>
		/// Get a collection of all the available keyboard shortcuts in mCubed [Bindable]
		/// </summary>
		public IEnumerable Shortcuts { get { return mCubedSecondary._shortcuts; } }

		#endregion

		#region Custom Formulas

		private static readonly IEnumerable _formulas = new [] {
			new { Items = MetaDataFormula.FormulaProperties.Where(f => f.Priority == 1), Title = "File", Priority = "1st",
				Description = "These are the formulas available to the individual media files. " },
			new { Items = MetaDataFormula.FormulaProperties.Where(f => f.Priority == 2), Title = "Playback", Priority = "2nd",
				Description = "These are the formulas available to the playback options for the library that the media file is in." }
		};

		/// <summary>
		/// Get a collection of all the available custom formulas in mCubed [Bindable]
		/// </summary>
		public IEnumerable Formulas { get { return mCubedSecondary._formulas; } }

		/// <summary>
		/// Event that handles when the formula name has changed
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void FormulaName_TextChanged(object sender, TextChangedEventArgs e) {
			TextBox textBox = sender as TextBox;
			if (textBox != null) {
				string baseText = textBox.Text;
				string text = baseText;
				int id = 1;
				int count = 1;
				while (string.IsNullOrEmpty(text) || Settings.Formulas.Count(f => f.Name == text) > count) {
					text = baseText + id;
					count = 0;
					id++;
				}
				if (text != textBox.Text)
					textBox.Text = text;
				BindingOperations.GetBindingExpression(textBox, TextBox.TextProperty).UpdateTarget();
			}
		}

		#endregion

		#region Credits

		private static readonly IEnumerable _credits = new[] {
			new { Author = "Nick Paddock", Project = "mCubed Core", Version = "Music Meta-data Manager", URL = "http://mcubed.sourceforge.net/" },
			new { Author = "Nick Paddock", Project = "mCubed UI", Version = "Music Meta-data Manager", URL = "http://mcubed.sourceforge.net/" },
			new { Author = "Mark James", Project = "Silk Icon Set", Version = "1.3", URL = "http://www.famfamfam.com/lab/icons/silk/" },
			new { Author = "Banshee Development Team", Project = "TagLib#", Version = "2.0.4.0", URL = "https://github.com/mono/taglib-sharp/" }
		};

		/// <summary>
		/// Get the credits associated with this version of mCubed [Bindable]
		/// </summary>
		public IEnumerable Credits { get { return mCubedSecondary._credits; } }

		#endregion

		#region Help

		private static readonly IEnumerable _help = new[] {
			new { Title = "How to add a library?", Description = "Start by clicking the Help/Settings button in the top right corner of the application. " +
				"From here, select the Library Settings tab. Click the Add Library button. From here, selecting the newly added library gives more options for it." },
			new { Title = "How to remove a library?", Description = "Select the library from the Library Settings tab. Click the Remove Library button." },
			new { Title = "How to load a library?", Description = "Loading is a common term throughout mCubed. Loaded means the current or active library in this case. " +
				"The loaded library will be the library that contains the currently loaded media. The currently loaded media is what is being played. To play media " +
				"in a different library, it must be loaded first. Select the library from the Library Settings tab. Click the Load Library button. Only one library " +
				"may be loaded at a time. Each library keeps track of its playback information individually, so enjoy the Easter Egg with library loading." },
			new { Title = "How to rename a library?", Description = "Select the library that will be renamed from the Library Settings tab. In the Display Name field, " +
				"type in a new name for the library. This change occurs instantly and, therefore, cannot be cancelled." },
			new { Title = "How to add media to a library?", Description = "Media is only added to libraries via directories. With this, select the library " +
				"that will receive the media from the Library Settings tab. Select the Directories tab. Click the Add Directory button. Select the directory that " +
				"contains the media to add (subdirectories are included recursively). Click OK and wait for the media to be added." },
			new { Title = "How to remove media from a library?", Description = "Select the library that the media will be removed from, from the Library Settings tab. " +
				"Select the Directories tab. Select the directory that contains the media to remove. Click the Remove Directory button and wait for the media to be removed." },
			new { Title = "How to shuffle the media?", Description = "mCubed uses a unique approach to shuffling. This approach is called media orders. Each library " +
				"technically supports an unlimited number of orders. An order consists of a media file mapped to the order in which it will be played. mCubed comes with " +
				"two media orders for each library, a sequential order and a shuffled order. To select the shuffled order: Method 1, Select the library in the Library " +
				"Settings tab. Select the Basic Information tab. Place a checkmark in the Is Shuffled box. Method 2, Click the S button in the player controls to " +
				"toggle between the sequential and the shuffled order. Method 3, Select the library in the Library Settings tab. Select the Media Orders tab. Select " +
				"the shuffled order. Click the Load Media Order button." },
			new { Title = "How to reshuffle the shuffled order?", Description = "Select the library that contains the media order in question from the Library Settings tab. " + 
				"Select the Media Orders tab. Click the Reshuffle All button to reshuffle EVERY media order that is a shuffled media order. Or select the specific media " +
				"order to shuffle. Click the Reshuffle Selected button to reshuffle ONLY the selected media order if it is a shuffled media order." },
			new { Title = "How to add a playlist?", Description = "mCubed is not a media player. It is not designed to replace WMP or iTunes. With this being said, " +
				"there is no support for playlists and there never will be. However, media orders accomplish the concept of playlists." },
			new { Title = "What are media orders?", Description = "Media orders is how mCubed determines which media will be played next or which media came previous. " +
				"One media order will map every file in the library to a number in which it will be played. The mCubed Core library supports an unlimited number of " +
				"media orders. This is, however, limited by the mCubed UI. Each library only has the sequential and the shuffled order and cannot be customized." },
			new { Title = "How to add or remove media orders?", Description = "This version of mCubed does not support adding or removing media orders." },
			new { Title = "What file types are supported for playback?", Description = "Playback is limited to the WMP file types. The exhaustive list of supported " +
				"playback types: .aac, .aif, .aifc, .aiff, .m4a, .mp3, .wav, .wma, .wv" },
			new { Title = "What file types are supported for viewing and modifying meta-data?", Description = "Meta-data is limited to the TagLib# file types. The " +
				"exhaustive list of supported meta-data types: .aac, .aif, .aifc, .aiff, .flac, .m4a, .mp3, .ogg, .wav, .wma, .wv" },
			new { Title = "What file types are supported for album art and pictures?", Description = "Pictures are limited to the support of TagLib#. The exhaustive " +
				"list of supported picture types: .bmp, .gif, .jpeg, .jpg, .png" },
			new { Title = "What is the difference between ## and #?", Description = "As previously stated, every media file receives a number in which it will be " +
				"played. This number is known as its media order number. The two pound signs (##) represents the media order. The single pound sign (#) represents " +
				"the track number from the meta-data information as expected." },
			new { Title = "How many Easter Eggs are there?", Description = "'Easter Eggs' is the technical term for hidden features. mCubed is filled with so many " +
				"little features, making it unrealistic to document every detail. These features are left as Easter Eggs, meaning they have to be found individually. " +
				"There are too many Easter Eggs to answer the original question." }
		};

		/// <summary>
		/// Get a collection of all the help items for mCubed [Bindable]
		/// </summary>
		public IEnumerable Help { get { return mCubedSecondary._help; } }

		#endregion

		#region Event Handlers

		/// <summary>
		/// Allow the user to browse for a directory, returning null if the user cancelled
		/// </summary>
		/// <param name="initialDirectory">The initiali directory that should be selected</param>
		/// <returns>The directory the user selected, or null if the user cancelled or selected an invalid directory</returns>
		private string OnBrowseDirectory(string initialDirectory) {
			var dlg = new System.Windows.Forms.FolderBrowserDialog { SelectedPath = initialDirectory };
			if (dlg.ShowDialog() == System.Windows.Forms.DialogResult.OK && System.IO.Directory.Exists(dlg.SelectedPath))
				return dlg.SelectedPath;
			return null;
		}

		/// <summary>
		/// Event that handles when the window is closing
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnClosing(object sender, CancelEventArgs e) {
			Utilities.MainSettings.Save();
		}

		/// <summary>
		/// Event that handles when the window should be closed
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnClosed(object sender, RoutedEventArgs e) {
			Close();
		}

		/// <summary>
		/// Event that handles when a hyperlink has been clicked
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnHyperlinkClicked(object sender, RequestNavigateEventArgs e) {
			var hyperlink = sender as Hyperlink;
			if (hyperlink != null) {
				string navigateUri = hyperlink.NavigateUri.ToString();
				System.Diagnostics.Process.Start(new ProcessStartInfo(navigateUri));
			}
		}

		#endregion
	}
}