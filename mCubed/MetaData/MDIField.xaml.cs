using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Input;
using mCubed.Core;

namespace mCubed.MetaData {
	public partial class MDIField : UserControl, IExternalNotifyPropertyChanged, IExternalNotifyPropertyChanging {
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

		#region Data Store

		private string _displayName;
		private bool _isMarked;
		private bool _isMultiValues;
		private bool _isReadOnly;
		private bool _isValueChanged;
		private IEnumerable<MDIValueContainer> _oldValue = Enumerable.Empty<MDIValueContainer>();
		private IEnumerable<MDIValue> _metaDataValues = Enumerable.Empty<MDIValue>();
		private IEnumerable<MDIValueContainer> _newValue = Enumerable.Empty<MDIValueContainer>();
		private string _propertyName;
		private IEnumerable<MDIValueContainer> _suggestions = Enumerable.Empty<MDIValueContainer>();

		#endregion

		#region Bindable Properties

		/// <summary>
		/// Get a collection of alternative values for auto-completion [Bindable]
		/// </summary>
		public IEnumerable<MDIValueContainer> Alternatives {
			get { return OldValue.Union(Suggestions).DistinctEquals().Where(c => !String.IsNullOrEmpty(c.Value)).Select(c => new MDIValueContainer { IsSuggestion = c.IsSuggestion, Value = c.Value }).ToArray(); }
		}

		/// <summary>
		/// Get/set the display name for this meta-data field [Bindable]
		/// </summary>
		public string DisplayName {
			get { return String.IsNullOrEmpty(_displayName) ? PropertyName : _displayName; }
			set { this.SetAndNotify(ref _displayName, value, "DisplayName"); }
		}

		/// <summary>
		/// Get/set whether or not this meta-data field is manually marked to be saved [Bindable]
		/// </summary>
		public bool IsMarked {
			get { return _isMarked; }
			set { this.SetAndNotify(ref _isMarked, value, "IsMarked", "IsNewValue"); }
		}

		/// <summary>
		/// Get whether or not this field is managing multiple values or a single value [Bindable]
		/// </summary>
		public bool IsMultiValues {
			get { return _isMultiValues; }
			private set { this.SetAndNotify(ref _isMultiValues, value, "IsMultiValues"); }
		}

		/// <summary>
		/// Get whether or not this field should be saved [Bindable]
		/// </summary>
		public bool IsNewValue {
			get { return IsValueChanged || IsMarked; }
		}

		/// <summary>
		/// Get/set whether or not this field is read only [Bindable]
		/// </summary>
		public bool IsReadOnly {
			get { return _isReadOnly; }
			set { this.SetAndNotify(ref _isReadOnly, value, "IsReadOnly"); }
		}

		/// <summary>
		/// Get whether or not the value for this field has changed [Bindable]
		/// </summary>
		public bool IsValueChanged {
			get { return _isValueChanged; }
			private set { this.SetAndNotify(ref _isValueChanged, value, "IsValueChanged", "IsNewValue"); }
		}

		/// <summary>
		/// Get the original value for this field [Bindable]
		/// </summary>
		public IEnumerable<MDIValueContainer> OldValue {
			get { return _oldValue; }
			private set { this.SetAndNotify(ref _oldValue, value, "OldValue", "Alternatives"); }
		}

		/// <summary>
		/// Get the collection of meta-data values used to make up this field [Bindable]
		/// </summary>
		public IEnumerable<MDIValue> MetaDataValues {
			get { return _metaDataValues; }
			private set { this.SetAndNotify(ref _metaDataValues, value, "MetaDataValues"); }
		}

		/// <summary>
		/// Get the updated value for this field [Bindable]
		/// </summary>
		public IEnumerable<MDIValueContainer> NewValue {
			get { return _newValue; }
			private set { this.SetAndNotify(ref _newValue, value, null, OnNewValueChanged, "NewValue"); }
		}

		/// <summary>
		/// Get/set the property name that this field is responsible for managing [Bindable]
		/// </summary>
		public string PropertyName {
			get { return _propertyName; }
			set { this.SetAndNotify(ref _propertyName, value, null, OnPropertyNameChanged, "PropertyName", "DisplayName"); }
		}

		/// <summary>
		/// Get a collection of suggested values for this field
		/// </summary>
		public IEnumerable<MDIValueContainer> Suggestions {
			get { return _suggestions; }
			private set { this.SetAndNotify(ref _suggestions, value, "Suggestions", "Alternatives"); }
		}

		#endregion

		#region Properties

		/// <summary>
		/// Get/set the current selection within this field control
		/// </summary>
		public MDIValue.MDIValueSelection? CurrentSelection {
			get {
				if (IsKeyboardFocusWithin) {
					var ele = Keyboard.FocusedElement as FrameworkElement;
					var val = ele == null ? null : ele.DataContext as MDIValue;
					if (val != null)
						return val.PopulateValueSelection();
				}
				return null;
			}
			set {
				if (value != null) {
					var val = MetaDataValues.FirstOrDefault(v => v.Value.Value == value.Value.Value);
					if (val != null) {
						val.SelectFromValueSelection(value.Value);
					} else {
						Select(0);
					}
				}
			}
		}

		/// <summary>
		/// Get the element type for the specified property type
		/// </summary>
		public Type ElementType {
			get { return Utilities.IsTypeIEnumerable(PropertyType) ? Utilities.EnumerableType(PropertyType) : PropertyType; }
		}

		/// <summary>
		/// Get the property type that this field will be generating a value for
		/// </summary>
		public Type PropertyType { get; private set; }

		#endregion

		#region Events

		public event Action<MDIField, bool> SpecializedTabOut;
		public event Action<MDIField, int, int> SpecializedNavigation;

		#endregion

		#region Constructor

		public MDIField() {
			// Hook up event handlers
			AddHandler(UserControl.KeyDownEvent, new KeyEventHandler(OnMDIKeyDown), true);

			// Setup suggestions
			Utilities.MainSettings.PerformWhenLoaded(OnSetupSuggestions);

			// Initialize
			InitializeComponent();
		}

		#endregion

		#region Event Handlers

		/// <summary>
		/// Event that handles the keyboard input 
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnMDIKeyDown(object sender, KeyEventArgs e) {
			// Setup a temporary handled check
			bool handled = true;
			bool shift = (Keyboard.Modifiers & ModifierKeys.Shift) == ModifierKeys.Shift;
			bool ctrl = (Keyboard.Modifiers & ModifierKeys.Control) == ModifierKeys.Control;
			bool alt = (Keyboard.Modifiers & ModifierKeys.Alt) == ModifierKeys.Alt;

			// Sort through the keyboard shortcuts
			if (ctrl) {
				if (e.Key == Key.M) {
					IsMarked = !IsMarked;
				} else if (e.Key == Key.U) {
					UndoChange();
					OnSpecializedTabOut(!shift);
				} else {
					handled = false;
				}
			} else if (alt) {
				if (e.Key == Key.Left || e.SystemKey == Key.Left) {
					OnSpecializedNavigation(-1, 0);
				} else if (e.Key == Key.Right || e.SystemKey == Key.Right) {
					OnSpecializedNavigation(1, 0);
				} else if (e.Key == Key.Up || e.SystemKey == Key.Up) {
					OnSpecializedNavigation(0, -1);
				} else if (e.Key == Key.Down || e.SystemKey == Key.Down) {
					OnSpecializedNavigation(0, 1);
				} else {
					handled = false;
				}
			} else if (e.Key == Key.Enter) {
				SpecializedTab(!shift);
			} else {
				handled = false;
			}

			// Was the keypress handled
			if (handled)
				e.Handled = true;
		}

		/// <summary>
		/// Event handler that handles when the manual marking of a field should be toggled or the changes to the value of the field should be un-done
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnMarkOrUndoToggled(object sender, MouseButtonEventArgs e) {
			if (IsValueChanged) {
				UndoChange();
			} else {
				IsMarked = !IsMarked;
				if (IsMarked && !IsMultiValues && !IsReadOnly && NewValue != null && NewValue.Count() > 1) {
					NewValue = new[] { NewValue.First() };
					Select(0);
				}
			}
		}

		/// <summary>
		/// Event that handles the new value changed event
		/// </summary>
		private void OnNewValueChanged() {
			var items = new List<MDIValue>();
			foreach (var item in NewValue) {
				var value = new MDIValue() { Name = "Value" };
				value.SetBinding(MDIValue.ValueProperty, new Binding { Source = item });
				value.SetBinding(MDIValue.AlternativesProperty, new Binding { Source = this, Path = new PropertyPath("Alternatives") });
				value.SetBinding(MDIValue.IsReadOnlyProperty, new Binding { Source = this, Path = new PropertyPath("IsReadOnly") });
				value.StatusChanged += new Action<MDIValue>(OnStatusChanged);
				value.ValueChanged += new Action<MDIValue>(OnValueChanged);
				value.ValueDeleted += new Action<MDIValue>(OnValueDeleted);
				items.Add(value);
			}
			MetaDataValues = items.ToArray();
			OnValueChanged();
		}

		/// <summary>
		/// Handle the property name changed event
		/// </summary>
		private void OnPropertyNameChanged() {
			// Determine if the property allows multiple values
			PropertyType = typeof(MetaDataInfo).GetProperty(PropertyName).PropertyType;
			IsMultiValues = Utilities.IsTypeIEnumerable(PropertyType);

			// Load the proper suggestions
			Utilities.MainSettings.PerformWhenLoaded(ReloadSuggestions);
		}

		/// <summary>
		/// Event that handles when the suggestions should initially be setup
		/// </summary>
		private void OnSetupSuggestions() {
			Utilities.MainSettings.LibraryMediaCollectionChanged += l => ReloadSuggestions();
			Utilities.MainSettings.MediaFilePropertyChanged += (m, p) => { if (p == PropertyName) ReloadSuggestions(); };
		}

		/// <summary>
		/// Event that handles when the MDI field should navigate to the field at the given relative offset
		/// </summary>
		/// <param name="xOffset">The number of fields in the x-direction to move</param>
		/// <param name="yOffset">The number of fields in the y-direction to move</param>
		private void OnSpecializedNavigation(int xOffset, int yOffset) {
			var tempHandler = SpecializedNavigation;
			if (tempHandler != null) {
				tempHandler(this, xOffset, yOffset);
			}
		}

		/// <summary>
		/// Event that handles when the MDI field should be tabbed out using the special tab command
		/// </summary>
		/// <param name="direction">True if the next element should be forward, or false for backward</param>
		private void OnSpecializedTabOut(bool forward) {
			var tempHandler = SpecializedTabOut;
			if (tempHandler != null) {
				tempHandler(this, forward);
			}
		}

		/// <summary>
		/// Handle the status changed event
		/// </summary>
		/// <param name="sender">The sender object that the status has changed for</param>
		private void OnStatusChanged(MDIValue sender) {
			// When multiple values are selected for a single value field, then use the selected value
			if (!IsMultiValues && !IsReadOnly && sender.Status == MetaDataValueStatus.Edit && NewValue != null && NewValue.Count() > 1 && NewValue.Contains(sender.Value)) {
				NewValue = new[] { sender.Value };
				Select(0);
			}

			// Ensure the value is parseable
			if (sender.Status != MetaDataValueStatus.Edit) {
				var value = sender.Value.Value.Parse(ElementType);
				sender.Value.Value = value == null ? "" : value.ToString();
			}
		}

		/// <summary>
		/// Event handlers that handles when a value should be added to the existing list of values for the field
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnValueAdded(object sender, MouseButtonEventArgs e) {
			var value = NewValue.FirstOrDefault(v => String.IsNullOrEmpty(v.Value));
			if (value == null) {
				value = new MDIValueContainer();
				NewValue = NewValue.Concat(new[] { value }).ToArray();
			}
			Select(value);
		}

		/// <summary>
		/// Event that handles the value changed event
		/// </summary>
		private void OnValueChanged() {
			OnValueChanged(null);
		}

		/// <summary>
		/// Event that handles the value changed event
		/// </summary>
		/// <param name="sender">The sender object that has been changed</param>
		private void OnValueChanged(MDIValue sender) {
			if (OldValue != null && NewValue != null)
				IsValueChanged = !OldValue.Select(vc => vc.Value).DistinctUnordered().
					SequenceEqual(NewValue.Select(vc => vc.Value).DistinctUnordered());
		}

		/// <summary>
		/// Event that handles the value deleted event
		/// </summary>
		/// <param name="sender">The sender object that should be deleted or cleared from the value</param>
		private void OnValueDeleted(MDIValue sender) {
			OnValueDeleted(sender, true, true);
		}

		/// <summary>
		/// Event that handles the value deleted event
		/// </summary>
		/// <param name="sender">The sender object that should be deleted or cleared from the value</param>
		/// <param name="forward">True if the forward object should be given focus, or false if the backward object should be given focus</param>
		/// <param name="attemptSelect">True if the there should be an attempt to select a value, or false otherwise</param>
		/// <returns>True if the given forward direction was successful, or false otherwise</returns>
		private bool OnValueDeleted(MDIValue sender, bool forward, bool attemptSelect) {
			bool retValue = false;
			if (NewValue != null && NewValue.Contains(sender.Value)) {
				if (!IsMultiValues) {
					sender.Value.Value = "";
					Select(sender);
				} else {
					int index = NewValue.ToList().IndexOf(sender.Value);
					NewValue = NewValue.Where(v => v != sender.Value).ToArray();
					retValue = Select(forward ? index : index - 1);
					if (attemptSelect && !retValue)
						Select(forward ? index - 1 : index);
				}
			}
			return retValue;
		}

		#endregion

		#region Generation Members

		/// <summary>
		/// Select all the text for a value control given the index
		/// </summary>
		/// <param name="index">The index of the value control to select</param>
		/// <returns>True if a value control existed, or false otherwise</returns>
		private bool Select(int index) {
			return Select(MetaDataValues.ElementAtOrDefault(index));
		}

		/// <summary>
		/// Select all the text for a value control given the container
		/// </summary>
		/// <param name="container">The container of the value control to select</param>
		/// <returns>True if a value control existed, or false otherwise</returns>
		private bool Select(MDIValueContainer container) {
			return Select(MetaDataValues.FirstOrDefault(v => v.Value == container));
		}

		/// <summary>
		/// Select all the text for a value control given the value control
		/// </summary>
		/// <param name="value">The value conrol to select all the text of</param>
		/// <returns>True if the value control existed, or false otherwise</returns>
		private bool Select(MDIValue value) {
			if (value != null) {
				value.SelectAll();
			}
			return value != null;
		}

		#endregion

		#region Members

		/// <summary>
		/// Get the value that is generated by this meta-data field
		/// </summary>
		/// <returns>The value that is generated by this meta-data field</returns>
		public object GetValue() {
			if (IsMultiValues) {
				return Utilities.Parse(NewValue.Select(vc => vc.Value).DistinctUnordered(), ElementType);
			} else {
				var value = NewValue.SingleOrDefault();
				if (value == null) {
					return null;
				} else {
					return value.Value.Parse(ElementType);
				}
			}
		}

		/// <summary>
		/// Refresh the value changed status to ensure it's accuracy
		/// </summary>
		public void Refresh() {
			OnValueChanged();
		}

		/// <summary>
		/// Reload the list of suggestions to be used
		/// </summary>
		private void ReloadSuggestions() {
			Suggestions = Utilities.MainSettings.Libraries.
				SelectMany(l => l.MediaFiles.SelectMany(m => m.MetaData.GetValue(PropertyName))).
				DistinctOrdered().
				Select(s => new MDIValueContainer { Value = s, IsSuggestion = true }).
				ToArray();
		}

		/// <summary>
		/// Resets the field so that the new value becomes the old value
		/// </summary>
		public void Reset() {
			SetValue(NewValue.Select(vc => vc.Value), true);
		}

		/// <summary>
		/// Set the value that this field will display and be compared to for modifications
		/// </summary>
		/// <param name="value">The value to display and compare to</param>
		/// <param name="reselect">True if the previous selection should be reselected</param>
		public void SetValue(IEnumerable<string> value, bool reselect) {
			if (value != null) {
				// Derive the new list of values
				value = value.DistinctUnordered();
				value = IsMultiValues || value.Any() ? value : new[] { "" };

				// Check if the keyboard input is in this field
				var selection = CurrentSelection;

				// Set the list of values
				OldValue = value.Select(s => new MDIValueContainer { Value = s }).ToArray();
				NewValue = value.Select(s => new MDIValueContainer { Value = s }).ToArray();

				// Restore the keyboard input appropriately
				if (reselect) {
					CurrentSelection = selection;
				}

				// Undo the manually marking, if any
				IsMarked = false;
			}
		}

		/// <summary>
		/// Perform the specialized tab command on the field
		/// </summary>
		/// <param name="forward">True if the next element should be focused, or false for the previous element</param>
		public void SpecializedTab(bool forward) {
			var mdiValue = MetaDataValues.FirstOrDefault(v => v.Status == MetaDataValueStatus.Edit);
			var mdiVC = mdiValue == null ? null : mdiValue.Value;
			if (mdiVC != null) {
				if (IsMultiValues && String.IsNullOrEmpty(mdiVC.Value)) {
					if (!OnValueDeleted(mdiValue, forward, false))
						OnSpecializedTabOut(forward);
				} else if (IsMultiValues && forward && NewValue.LastOrDefault() == mdiVC) {
					OnValueAdded(null, null);
				} else {
					mdiValue = (forward) ? MetaDataValues.ElementAfter(mdiValue, false) : MetaDataValues.ElementBefore(mdiValue, false);
					if (!Select(mdiValue))
						OnSpecializedTabOut(forward);
				}
			}
		}

		/// <summary>
		/// Perform the specialized tab command into this field
		/// </summary>
		/// <param name="forward">True if this is the next element being focused, or false if its the previous element</param>
		public void SpecializedTabInto(bool forward) {
			if (IsMultiValues && NewValue.Count() == 0)
				OnValueAdded(null, null);
			else
				Select(forward ? 0 : MetaDataValues.Count() - 1);
		}

		/// <summary>
		/// Undo any and all changes that have occurred
		/// </summary>
		public void UndoChange() {
			SetValue(OldValue.Select(v => v.Value), false);
		}

		#endregion
	}
}