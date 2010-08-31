﻿using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using mCubed.Core;

namespace mCubed.MetaData {
	public partial class MDIManager : UserControl, INotifyPropertyChanged {
		#region INotifyPropertyChanged Members

		public event PropertyChangedEventHandler PropertyChanged;

		#endregion

		#region Data Store

		private string _currentKey;
		private IEnumerable<MDIField> _metaDataFields = Enumerable.Empty<MDIField>();
		private IEnumerable<MetaDataInfo> _metaDataInfo = Enumerable.Empty<MetaDataInfo>();
		private Dictionary<string, IEnumerable<MetaDataInfo>> _metaDataInfoDictionary = new Dictionary<string, IEnumerable<MetaDataInfo>>();

		#endregion

		#region Bindable Properties

		/// <summary>
		/// Get/set the current key of meta-data information to use [Bindable]
		/// </summary>
		public string CurrentKey {
			get { return _currentKey; }
			set { this.SetAndNotify(ref _currentKey, value, null, OnCurrentKeyChanged, "CurrentKey"); }
		}

		/// <summary>
		/// Get whether or not multiple files are being edited [Bindable]
		/// </summary>
		public bool IsMultiFiles { get { return MetaDataInfo.Count(m => m.Status == MetaDataStatus.Edit) > 1; } }

		/// <summary>
		/// Get the current list of possible keys for the meta-data information tags [Bindable]
		/// </summary>
		public IEnumerable<string> Keys { get { return _metaDataInfoDictionary.Keys.ToArray(); } }

		/// <summary>
		/// Get the meta data fields that are contained within this manager [Bindable]
		/// </summary>
		public IEnumerable<MDIField> MetaDataFields {
			get { return _metaDataFields; }
			private set { this.SetAndNotify(ref _metaDataFields, (value ?? Enumerable.Empty<MDIField>()).ToArray(), "MetaDataFields"); }
		}

		/// <summary>
		/// Get the current collection of meta-data information [Bindable]
		/// </summary>
		public IEnumerable<MetaDataInfo> MetaDataInfo {
			get { return _metaDataInfo; }
			private set { OnMetaDataInfoChanged(MetaDataInfo, value); }
		}

		#endregion

		#region Indexer

		/// <summary>
		/// Get/set the collection of meta-data information for the given key
		/// </summary>
		/// <param name="index">The index for the meta-data information to get or set the collection for</param>
		/// <returns>The collection of items for the given index</returns>
		public IEnumerable<MetaDataInfo> this[string index] {
			get { return _metaDataInfoDictionary[index]; }
			set {
				if (index != null) {
					value = (value ?? Enumerable.Empty<MetaDataInfo>()).ToArray();
					if (_metaDataInfoDictionary.ContainsKey(index)) {
						_metaDataInfoDictionary[index] = value;
						if (CurrentKey == index)
							MetaDataInfo = value;
					} else if (value.Any()) {
						_metaDataInfoDictionary.Add(index, value);
						this.OnPropertyChanged("Keys");
						CurrentKey = index;
					}
				}
			}
		}

		#endregion

		#region Constructor

		public MDIManager() {
			// Hook up event handlers
			Loaded += new RoutedEventHandler(OnLoaded);
			AddHandler(UserControl.KeyDownEvent, new KeyEventHandler(OnMDIKeyDown), true);

			// Initialize
			InitializeComponent();
		}

		#endregion

		#region Event Handlers

		/// <summary>
		/// Event that handles when the meta-data manager loaded
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnLoaded(object sender, RoutedEventArgs e) {
			MetaDataFields = new[] { ManagerPanel1, ManagerPanel2 }.SelectMany(p => p.Children.OfType<MDIField>());
			foreach (var field in MetaDataFields) {
				field.SpecializedTabOut += new Action<MDIField, bool>(OnSpecializedTabOut);
			}
			OnMetaDataInfoChanged(null, null);
			Loaded -= new RoutedEventHandler(OnLoaded);
		}

		/// <summary>
		/// Event that handles the keyboard input
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnMDIKeyDown(object sender, KeyEventArgs e) {
			// Setup a temporary handled check
			bool handled = true;

			// Sort through the keyboard shortcuts
			if ((Keyboard.Modifiers & ModifierKeys.Control) == ModifierKeys.Control) {
				if (e.Key == Key.S) {
					OnSave(null, null);
				} else if (e.Key == Key.E) {
					OnCancel(null, null);
				} else if (e.Key == Key.N) {
					Select(1);
				} else if (e.Key == Key.P) {
					Select(-1);
				} else {
					handled = false;
				}
			} else {
				handled = false;
			}

			// Was the keypress handled
			if (handled)
				e.Handled = true;
		}

		/// <summary>
		/// Event that handles when the current meta-data information key changed
		/// </summary>
		private void OnCurrentKeyChanged() {
			if(_metaDataInfoDictionary.ContainsKey(CurrentKey))
				MetaDataInfo = _metaDataInfoDictionary[CurrentKey];
		}

		/// <summary>
		/// Event that handles when the visible elements and the field values should change
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnMetaDataInfoChanged(object sender, RoutedEventArgs e) {
			OnMetaDataInfoChanged(null, null);
		}

		/// <summary>
		/// Event that handles when the collection of meta-data information has changed
		/// </summary>
		/// <param name="oldInfo">The old meta-data info collection</param>
		/// <param name="newInfo">The new meta-data info collection</param>
		private void OnMetaDataInfoChanged(IEnumerable<MetaDataInfo> oldInfo, IEnumerable<MetaDataInfo> newInfo) {
			// Update the info collection
			oldInfo = oldInfo == null ? Enumerable.Empty<MetaDataInfo>() : oldInfo.ToArray();
			newInfo = newInfo == null ? Enumerable.Empty<MetaDataInfo>() : newInfo.ToArray();

			// Check if the sequences are equal
			if (!oldInfo.SequenceEqual(newInfo)) {
				// Reset the old and set up the new
				foreach (var item in oldInfo)
					item.Status = MetaDataStatus.None;
				foreach (var item in newInfo)
					item.Status = MetaDataStatus.Edit;

				// Notify that multi-files changed
				this.SetAndNotify(ref _metaDataInfo, newInfo, "MetaDataInfo", "IsMultiFiles");
			}

			// Refresh the manager panels
			ManagerHelp.Visibility = MetaDataInfo.Any(m => m.Status == MetaDataStatus.Edit) ? Visibility.Collapsed : Visibility.Visible;
			ManagerPanel.Visibility = MetaDataInfo.Any(m => m.Status == MetaDataStatus.Edit) ? Visibility.Visible : Visibility.Collapsed;

			// Refresh the field values
			ReloadPictures();
			foreach (MDIField field in MetaDataFields)
				field.SetValue(GetValue(field.PropertyName), true);
		}

		/// <summary>
		/// Event that handles when the meta-data information should be cancelled and restored to the original values
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnCancel(object sender, RoutedEventArgs e) {
			// Reload the pictures
			ReloadPictures();

			// Undo the changes
			foreach (MDIField field in MetaDataFields) {
				field.IsMarked = false;
				field.UndoChange();
			}
		}

		/// <summary>
		/// Event that handles when the meta-data information should be saved
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnSave(object sender, RoutedEventArgs e) {
			// Retrieve the information necessary to process the save
			var updateMDI = MetaDataInfo.Where(mdi => mdi.Status == MetaDataStatus.Edit).ToArray();
			var updateMDF = new Dictionary<string, IEnumerable<string>>();
			MetaDataPic[] updateMDP = null;
			if (MDPManager.IsNewValue)
				updateMDP = MDPManager.PicListControl.ItemsSource.OfType<MetaDataPic>().Select(p => new MetaDataPic(p)).ToArray();
			foreach (var item in MetaDataFields) {
				item.Refresh();
				if (item.IsNewValue) {
					var tempItem = item.NewValue.Select(vc => vc.Value).DistinctUnordered().ToArray();
					updateMDF.Add(item.PropertyName, tempItem);
					item.SetValue(tempItem, true);
					item.IsMarked = false;
				}
			}

			// Queue the save process in the progress manager
			if (updateMDF.Count > 0 || updateMDP != null) {
				Utilities.MainProcessManager.AddProcess(delegate(Process process)
				{
					foreach (var mdi in updateMDI) {
						foreach (var mdf in updateMDF)
							mdi.SetProperty(mdf.Key, mdf.Value);
						if (updateMDP != null)
							mdi.Pictures = updateMDP;
						mdi.Save();
						mdi.Parent.Parent.MediaFiles.Reset(mdi.Parent);
						process.CompletedCount++;
					}
					ReloadSuggestions();
					ReloadPictures();
				}, "Saving meta-data information", updateMDI.Length);
			}
		}

		/// <summary>
		/// Event that handles when the specialized tab out has occured and the next element needs to be tabbed in
		/// </summary>
		/// <param name="field">The field that was tabbed out of</param>
		/// <param name="forward">True if the next element needs tabbed in, or false if the previous element</param>
		private void OnSpecializedTabOut(MDIField field, bool forward) {
			MDIField select = null;
			if (forward)
				select = MetaDataFields.ElementAfter(field, true);
			else
				select = MetaDataFields.ElementBefore(field, true);
			select.SpecializedTabInto(forward);
		}

		#endregion

		#region Members

		/// <summary>
		/// Get the value of a given property from the selected meta-data information collection
		/// </summary>
		/// <param name="property">The property name to retrieve the value of</param>
		/// <returns>A collection of all the values for the given property name</returns>
		private IEnumerable<string> GetValue(string property) {
			// Check if there is info
			if (!MetaDataInfo.Any(m => m.Status == MetaDataStatus.Edit))
				return new[] { "" };

			// Aggregate all the values if multiple values are allowed
			return MetaDataInfo.Where(m => m.Status == MetaDataStatus.Edit).SelectMany(mdi => mdi.GetValue(property)).ToArray();
		}

		/// <summary>
		/// Reload the suggestions in all the meta-data fields
		/// </summary>
		private void ReloadSuggestions() {
			foreach (var mdf in MetaDataFields)
				mdf.ReloadSuggestions();
		}

		/// <summary>
		/// Reload the collection of meta-data pictures in the manager
		/// </summary>
		private void ReloadPictures() {
			MDPManager.SetValue(MetaDataInfo.Where(mdi => mdi.Status == MetaDataStatus.Edit).SelectMany(mdi => mdi.Pictures).ToArray());
		}

		/// <summary>
		/// Select the MDI tag that is the given number of indices from the current selected MDI tag (postive selects the first if non-existant, negative selects the last)
		/// </summary>
		/// <param name="fromCurrent">The number of indices from the current selected MDI tag</param>
		private void Select(int fromCurrent) {
			// Make sure we're moving first
			if (fromCurrent != 0) {
				// Get the current index
				int selIndex = -1;
				int curIndex = 0;
				int totalCount = MetaDataInfo.Count();
				foreach (var mdi in MetaDataInfo) {
					if (mdi.Status == MetaDataStatus.Edit) {
						if (selIndex == -1) {
							selIndex = curIndex;
						} else {
							selIndex = -1;
							break;
						}
					}
					curIndex++;
				}

				// Get the new index
				if (selIndex != -1) {
					selIndex += fromCurrent;
					while (selIndex >= totalCount)
						selIndex -= totalCount;
					while (selIndex < 0)
						selIndex += totalCount;
				} else if (fromCurrent > 0) {
					selIndex = 0;
				} else {
					selIndex = totalCount - 1;
				}

				// Select and deselect appropriately
				curIndex = 0;
				foreach (var mdi in MetaDataInfo) {
					if (curIndex == selIndex)
						mdi.Status = MetaDataStatus.Edit;
					else
						mdi.Status = MetaDataStatus.Loaded;
					curIndex++;
				}

				// Finally refresh
				OnMetaDataInfoChanged(null, null);
			}
		}

		#endregion
	}
}