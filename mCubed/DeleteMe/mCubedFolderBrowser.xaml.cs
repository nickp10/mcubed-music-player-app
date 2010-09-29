using System;
using System.Collections.Generic;
using System.IO;
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

namespace mCubed.Controls {
	/// <summary>
	/// Interaction logic for mCubedFolderBrowser.xaml
	/// </summary>
	public partial class mCubedFolderBrowser : UserControl {
		// Public Properties
		public string StartPath { get; set; }
		public double FolderWidth { get; set; }
		public int FoldersCount { get; set; }
		//public double SubFolderWidth { get; set; }
		//public int SubFolderCount { get; set; }

		/// <summary>
		/// Initialize a folder browser control
		/// </summary>
		/// <param name="path">The initial path to start the folder browesr at</param>
		public mCubedFolderBrowser() : this("", 100, 5/*, 200, 15*/) { InitializeComponent(); }
		public mCubedFolderBrowser(string startPath, double folderWidth, int foldersCount)//, double subFolderWidth, int subFolderCount)
		{
			// Initialize the control
			StartPath = startPath;
			FolderWidth = folderWidth;
			FoldersCount = foldersCount;
			//SubFolderWidth = subFolderWidth;
			//SubFolderCount = subFolderCount;
			InitializeComponent();
			Loaded += delegate
			{
				// Create the logical drives menu
				CreateLogicalDrives();

				// Create the menus from the path
				CreateMenuPath("C:\\Users\\npaddock");
			};
		}

		public void CreateLogicalDrives() {
			// Clear existing menus leaving 0 left
			ClearMenuItems(0);

			// Create the logical drives menu
			CreateMenuHeaderFromPath("");
		}

		public void CreateMenuPath(string path) {
			// Clear existing menus leaving 0 left
			ClearMenuItems(1);

			// Create local variables
			string tempPath = string.Empty;
			List<string> paths = new List<string>();

			// Check if the directory exists
			if (Directory.Exists(path) && IsDirectoryAccessible(path)) {
				// Create a Dictionary with header and path values
				string[] headersArray = path.Split('\\');
				foreach (string header in headersArray) {
					if (header == "")
						break;
					tempPath += header + "\\";
					paths.Add(tempPath);
				}

				// Show the last FoldersCount paths
				int dicIndex = 0;
				foreach (string tempPaths in paths) {
					if (dicIndex++ >= paths.Count - FoldersCount)
						CreateMenuHeaderFromPath(tempPaths);
				}
			}
		}

		public void CreateMenuHeaderFromPath(string path) {
			// Create menu header
			MenuItem tempMI = new MenuItem() { Width = FolderWidth, Header = HeaderFromPath(path) };
			MenuBrowser.Items.Add(tempMI);

			// Create submenu items
			string[] directories = (DirectoryHasItems(path)) ? Directory.GetDirectories(path) : (path == "" ? Directory.GetLogicalDrives() : new string[] { });
			foreach (string dir in directories) {
				CreateMenuItemFromPath(tempMI, dir);
			}
		}

		public void CreateMenuItemFromPath(MenuItem tempMI, string path) {
			// Check if the directory is accessible
			if (IsDirectoryAccessible(path)) {
				string tempDir = path;
				MenuItem tempSMI = new MenuItem() { /*Width = SubFolderWidth, */Header = HeaderFromPath(path) };
				tempSMI.Click += delegate { CreateMenuPath(tempDir); };
				tempMI.Items.Add(tempSMI);
			}
		}

		public void ClearMenuItems(int menusAfterClear) {
			// Clear the menu items
			while (MenuBrowser.Items.Count > menusAfterClear) {
				MenuBrowser.Items.RemoveAt(menusAfterClear);
			}
		}

		public string HeaderFromPath(string path) {
			string header = (path.Length == 0) ? "My Computer" : ((path[path.Length - 1] == '\\') ? path.Remove(path.Length - 1) : path);
			return header.Substring(header.LastIndexOf('\\') + 1);
		}

		/// <summary>
		/// Checks if a directory's file structure is accessible
		/// </summary>
		/// <param name="path">The path to the directory to check</param>
		/// <returns>Returns true if it's accessible, and false otherwise</returns>
		public bool IsDirectoryAccessible(string path) {
			try {
				DirectoryInfo di = new DirectoryInfo(path);
				if ((di.Attributes & FileAttributes.Directory) != FileAttributes.Directory)
					throw new Exception();
				if ((di.Attributes & FileAttributes.Hidden) == FileAttributes.Hidden && Directory.GetDirectoryRoot(path) != path)
					throw new Exception();
				Directory.GetFiles(path);
				return true;
			} catch { return false; }
		}

		/// <summary>
		/// Checks if a directory has accessible subdirectories
		/// </summary>
		/// <param name="path">The path to the directory to check</param>
		/// <returns>Returns true if the directory has at least one accessible subdirectory, and false otherwise</returns>
		public bool DirectoryHasItems(string path) {
			if (!IsDirectoryAccessible(path))
				return false;
			foreach (string s in Directory.GetDirectories(path)) {
				//if (mCubedWindow.IsDirectoryAccessible(s))
					return true;
			}
			return false;
		}
	}
}