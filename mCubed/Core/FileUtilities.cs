using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;

namespace mCubed.Core {
	public static class FileUtilities {
		#region Data Store

		private static readonly char[] predefinedInvalidChars = "\\/:*?\"<>|".ToCharArray();

		#endregion

		#region Static Directory Members

		/// <summary>
		/// Determines if the given directory is valid and may be used for the operations
		/// </summary>
		/// <param name="directory">The directory to check if it's valid</param>
		/// <returns>True if the directory is valid, or false otherwise</returns>
		private static bool IsValidDir(this string directory) {
			return !string.IsNullOrEmpty(directory) && Directory.Exists(directory);
		}

		/// <summary>
		/// Determines if the two given directories are equivalent
		/// </summary>
		/// <param name="left">The first directory to compare</param>
		/// <param name="right">The second directory to compare</param>
		/// <returns>True if the directories are the same, or false otherwise</returns>
		public static bool DirEquals(string left, string right) {
			return String.Equals(
				Path.GetFullPath(left).TrimEnd(Path.DirectorySeparatorChar),
				Path.GetFullPath(right).TrimEnd(Path.DirectorySeparatorChar),
				StringComparison.InvariantCultureIgnoreCase);
		}

		/// <summary>
		/// Determines if the two given directories are equivalent
		/// </summary>
		/// <param name="left">The first directory to compare</param>
		/// <param name="right">The second directory to compare</param>
		/// <returns>True if the directories are the same, or false otherwise</returns>
		public static bool DirEquals(this DirectoryInfo left, DirectoryInfo right) {
			return String.Equals(
				left.FullName.TrimEnd(Path.DirectorySeparatorChar),
				right.FullName.TrimEnd(Path.DirectorySeparatorChar),
				StringComparison.InvariantCultureIgnoreCase);
		}

		/// <summary>
		/// Generates if the subdirectory isn't found or retrieves if the subdirectory is found for the given directory
		/// </summary>
		/// <param name="directory">The directory to generate or retrieve the subdirectory for</param>
		/// <param name="subDirectoryName">The name of the subdirectory to generate or retrieve</param>
		/// <returns>The subdirectory of the given directory with the given name</returns>
		private static DirectoryInfo GenerateDir(this DirectoryInfo directory, string subDirectoryName) {
			var subDirectory = directory.GetDirectories().FirstOrDefault(d => d.Name == subDirectoryName);
			if (subDirectory == null) {
				subDirectory = directory.CreateSubdirectory(subDirectoryName);
			}
			return subDirectory;
		}

		#endregion

		#region Static File Members

		/// <summary>
		/// Determines if the given media file is valid and may be used for the operations
		/// </summary>
		/// <param name="file">The media file to check if it's valid</param>
		/// <returns>True if the media file is valid, or false otherwise</returns>
		private static bool IsValidFile(this MediaFile file) {
			return file != null && file.Parent != null && file.MetaData != null;
		}

		/// <summary>
		/// Determines if the two given files are equivalent
		/// </summary>
		/// <param name="left">The first file to compare</param>
		/// <param name="right">The second file to compare</param>
		/// <returns>True if the files are the same, or false otherwise</returns>
		public static bool FileEquals(string left, string right) {
			return String.Equals(left, right, StringComparison.InvariantCultureIgnoreCase);
		}

		/// <summary>
		/// Determines if the two given files are equivalent
		/// </summary>
		/// <param name="left">The first file to compare</param>
		/// <param name="right">The second file to compare</param>
		/// <returns>True if the files are the same, or false otherwise</returns>
		public static bool FileEquals(this FileInfo left, FileInfo right) {
			return FileEquals(left.FullName, right.FullName);
		}

		/// <summary>
		/// Retrieves the media file's source directory (the directory that was responsible for adding it to the library)
		/// </summary>
		/// <param name="file">The media file to get the source directory for</param>
		/// <returns>The source directory responsible for adding the given media file to the library</returns>
		private static string GetFileSourceDirectory(this MediaFile file) {
			Library library = file.Parent;
			return library.Directories.
				OrderByDescending(s => s.Length).
				FirstOrDefault(s => file.MetaData.FilePath.StartsWith(s));
		}

		#endregion

		#region Static Coercion Members

		/// <summary>
		/// Coerces the given value to ensure invalid characters are replaced appropriately
		/// </summary>
		/// <param name="value">The value to be coerced</param>
		/// <param name="invalidChars">The array of invalid characters that need to be replaced or removed</param>
		/// <returns>The coerced value that does not contain invalid characters</returns>
		private static string CoerceValue(string value, char[] invalidChars) {
			value = value.Replace(':', '-');
			foreach (var c in invalidChars.Union(predefinedInvalidChars)) {
				value = value.Replace(c, '_');
			}
			return value;
		}

		/// <summary>
		/// Coerces the given filename to ensure invalid characters are replaced appropriately
		/// </summary>
		/// <param name="filename">The filename to be coerced</param>
		/// <returns>The coerced filename that does not contain invalid characters</returns>
		private static string CoerceFilenameValue(string filename) {
			return CoerceValue(filename, Path.GetInvalidFileNameChars());
		}

		/// <summary>
		/// Coerces the given directory name to ensure invalid characters are replaced appropriately
		/// </summary>
		/// <param name="directory">The directory name to be coerced</param>
		/// <returns>The coerced directory name that does not contain invalid characters</returns>
		private static string CoerceDirectoryValue(string directory) {
			return CoerceValue(directory, Path.GetInvalidPathChars());
		}

		#endregion

		#region Static Operation Members

		/// <summary>
		/// Renames the given media file to its proper location
		/// </summary>
		/// <param name="file">The media file to be renamed</param>
		/// <returns>The new full path to the media file, or null if the rename failed</returns>
		public static string Rename(MediaFile file) {
			return Move(file, file.GetFileSourceDirectory());
		}

		/// <summary>
		/// Moves the given media file to its proper location within the given destination directory
		/// </summary>
		/// <param name="file">The media file that will be moved to the destination directory</param>
		/// <param name="destDirectory">The directory that the media file will be moved to</param>
		/// <returns>The new full path to the media file, or null if the move failed</returns>
		public static string Move(MediaFile file, string destDirectory) {
			return Move(file, file.Parent, destDirectory);
		}

		/// <summary>
		/// Moves the given media file to its proper location within the given destination directory
		/// </summary>
		/// <param name="file">The media file that will be moved to the destination directory</param>
		/// <param name="destLibrary">The library that will be the destination for the media file</param>
		/// <param name="destDirectory">The directory that the media file will be moved to</param>
		/// <returns>The new full path to the media file, or null if the move failed</returns>
		public static string Move(MediaFile file, Library destLibrary, string destDirectory) {
			// Validate the parameters
			if (!file.IsValidFile() || destLibrary == null || !destDirectory.IsValidDir()) {
				return null;
			}

			// Copy the file over, making sure it succeeded
			string oldLocation = file.MetaData.FilePath;
			string newLocation = InternalCopy(file, destDirectory, destLibrary.FilenameFormula);
			if (newLocation == null) {
				return null;
			}

			// If the library didn't change, reuse the given media file
			if (file.Parent == destLibrary) {
				// Unlock the media file
				var state = file.UnlockFile();

				// Delete the old file, if it changed
				if (!FileEquals(oldLocation, newLocation)) {
					InternalDelete(file, false);
				}

				// Update the path
				file.MetaData.FilePath = newLocation;
				if (state != null) {
					state.Path = newLocation;
				}

				// Restore the media file
				file.RestoreState(state);
			}

			// Otherwise, start fresh with a new media file
			else {
				// Alter the libraries accordingly
				file.Parent.RemoveMedia(new[] { file });
				destLibrary.AddMedia(new[] { destLibrary.GenerateMedia(newLocation) });

				// Delete the old file, if it changed
				if (!FileEquals(oldLocation, newLocation)) {
					InternalDelete(file, false);
				}
			}
			return newLocation;
		}

		/// <summary>
		/// Copies the given media file to its proper location within the given destination directory
		/// </summary>
		/// <param name="file">The media file that will be copied to the destination directory</param>
		/// <param name="destDirectory">The directory that the media file will be copied to</param>
		/// <returns>The new full path to the media file, or null if the copy failed</returns>
		public static string Copy(MediaFile file, string destDirectory) {
			return Copy(file, file.Parent, destDirectory);
		}

		/// <summary>
		/// Copies the given media file to its proper location within the given destination directory
		/// </summary>
		/// <param name="file">The media file that will be copied to the destination directory</param>
		/// <param name="destLibrary">The library that will be the destination for the media file</param>
		/// <param name="destDirectory">The directory that the media file will be copied to</param>
		/// <returns>The new full path to the media file, or null if the copy failed</returns>
		public static string Copy(MediaFile file, Library destLibrary, string destDirectory) {
			// Validate the parameters
			if (!file.IsValidFile() || destLibrary == null || !destDirectory.IsValidDir()) {
				return null;
			}

			// Copy the file over, making sure it succeeded
			string oldLocation = file.MetaData.FilePath;
			string newLocation = InternalCopy(file, destDirectory, destLibrary.FilenameFormula);
			if (newLocation == null) {
				return null;
			}

			// Alter the libraries accordingly
			if (!FileEquals(oldLocation, newLocation) || file.Parent != destLibrary) {
				destLibrary.AddMedia(new[] { destLibrary.GenerateMedia(newLocation) });
			}
			return newLocation;
		}

		/// <summary>
		/// Deletes the given media file permanently
		/// </summary>
		/// <param name="file">The media file to be deleted</param>
		/// <returns>True if the media file was successfully deleted, or false otherwise</returns>
		public static bool Delete(MediaFile file) {
			return Delete(file, false);
		}

		/// <summary>
		/// Deletes the given media file, optionally sending it to the recycle bin
		/// </summary>
		/// <param name="file">The media file to be deleted</param>
		/// <param name="sendToRecycleBin">True to send it to the recycle bin, or false to delete it permanently</param>
		/// <returns>True if the media file was successfully deleted, or false otherwise</returns>
		public static bool Delete(MediaFile file, bool sendToRecycleBin) {
			return file.IsValidFile() && InternalDelete(file, sendToRecycleBin);
		}

		#endregion

		#region Static Internal Operation Members

		/// <summary>
		/// Copies the given media file to its proper location within the given destination directory for internal uses
		/// </summary>
		/// <param name="file">The media file that will be copied</param>
		/// <param name="destDirectory">The directory that the media file will be copied to</param>
		/// <returns>The new full path to the media file, or null if the copy failed</returns>
		private static string InternalCopy(MediaFile file, string destDirectory) {
			return InternalCopy(file, destDirectory, file.Parent.FilenameFormula);
		}

		/// <summary>
		/// Copies the given media file to its proper location within the given destination directory for internal uses
		/// </summary>
		/// <param name="file">The media file that will be copied</param>
		/// <param name="destDirectory">The directory that the media file will be copied to</param>
		/// <param name="destFormula">The formula to generate the destination filename for the file</param>
		/// <returns>The new full path to the media file, or null if the copy failed</returns>
		private static string InternalCopy(MediaFile file, string destDirectory, string destFormula) {
			// Ensure the file exists
			var filePath = new FileInfo(file.MetaData.FilePath);
			if (!filePath.Exists) {
				return null;
			}

			// Ensure a proper formula
			if (string.IsNullOrEmpty(destFormula)) {
				return null;
			}

			// Finish setup
			var formulaParts = destFormula.Split(Path.DirectorySeparatorChar);
			var curDirectory = new DirectoryInfo(destDirectory);
			var i = 0;

			// Generate the directory name and structure if needed
			for (; i < formulaParts.Length - 1; i++) {
				var dirValue = MDFFile.GetValue(formulaParts[i], file);
				dirValue = CoerceDirectoryValue(dirValue);
				curDirectory = curDirectory.GenerateDir(dirValue);
			}

			// Generate the filename
			var fileValue = MDFFile.GetValue(formulaParts[i], file);
			fileValue = CoerceFilenameValue(fileValue);
			fileValue += filePath.Extension;

			// Copy the file, if it moved
			var newPath = Path.Combine(curDirectory.FullName, fileValue);
			if (!FileEquals(filePath.FullName, newPath)) {
				filePath.CopyTo(newPath, true);
			}
			return newPath;
		}

		/// <summary>
		/// Deletes the given media file and recurses upward deleting all empty directories until the root directory is reached
		/// </summary>
		/// <param name="file">The media file to be deleted</param>
		/// <param name="sendToRecycleBin">True to send it to the recycle bin, or false to delete it permanently</param>
		/// <returns>True if the media file was successfully deleted, or false otherwise</returns>
		private static bool InternalDelete(MediaFile file, bool sendToRecycleBin) {
			// Ensure the file exists
			var filePath = new FileInfo(file.MetaData.FilePath);
			if (!filePath.Exists) {
				return false;
			}

			// Finish setup
			var srcDirectory = new DirectoryInfo(file.GetFileSourceDirectory());
			var curDirectory = filePath.Directory;
			DirectoryInfo deleteDir = null;

			// Recurse up the directory structure deleting all EMPTY directories up until the source directory
			if (!curDirectory.DirEquals(srcDirectory) && curDirectory.GetDirectories().Length == 0 && curDirectory.GetFiles().All(filePath.FileEquals)) {
				deleteDir = curDirectory;
				curDirectory = curDirectory.Parent;
				while (!curDirectory.DirEquals(srcDirectory) && curDirectory.GetDirectories().All(deleteDir.DirEquals) && curDirectory.GetFiles().Length == 0) {
					deleteDir = curDirectory;
					curDirectory = curDirectory.Parent;
				}
			}

			// Use Visual Basic to send to recycle bin or to permanently delete file
			var recycleOption = sendToRecycleBin ? Microsoft.VisualBasic.FileIO.RecycleOption.SendToRecycleBin : Microsoft.VisualBasic.FileIO.RecycleOption.DeletePermanently;
			Microsoft.VisualBasic.FileIO.FileSystem.DeleteFile(filePath.FullName, Microsoft.VisualBasic.FileIO.UIOption.OnlyErrorDialogs, recycleOption);
			
			// Delete the directory
			if (deleteDir != null) {
				deleteDir.Delete(true);
			}
			return true;
		}

		#endregion
	}
}