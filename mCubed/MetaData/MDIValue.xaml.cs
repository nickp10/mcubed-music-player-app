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
	public partial class MDIValue : UserControl {
		#region MDIValueSelection

		public struct MDIValueSelection {
			public int SelectionLength { get; set; }
			public int SelectionStart { get; set; }
			public string Value { get; set; }
		}

		#endregion

		#region Data Store

		private CollectionViewSource _autoCompleteViewSource;
		private bool _customSelect;
		private int _selectionLength;
		private int _selectionStart;
		private double _scrollOffset;

		#endregion

		#region Dependency Property: Alternatives

		public static readonly DependencyProperty AlternativesProperty =
			DependencyProperty.Register("Alternatives", typeof(IEnumerable<MDIValueContainer>), typeof(MDIValue), new UIPropertyMetadata(null));
		public IEnumerable<MDIValueContainer> Alternatives {
			get { return (IEnumerable<MDIValueContainer>)GetValue(AlternativesProperty); }
			set { SetValue(AlternativesProperty, value); }
		}

		#endregion

		#region Dependency Property: IsAutoCompleteOpen

		public static readonly DependencyProperty IsAutoCompleteOpenProperty =
			DependencyProperty.Register("IsAutoCompleteOpen", typeof(bool), typeof(MDIValue), new UIPropertyMetadata(false));
		public bool IsAutoCompleteOpen {
			get { return (bool)GetValue(IsAutoCompleteOpenProperty); }
			set { SetValue(IsAutoCompleteOpenProperty, value); }
		}

		#endregion

		#region Dependency Property: IsReadOnly

		public static readonly DependencyProperty IsReadOnlyProperty =
			DependencyProperty.Register("IsReadOnly", typeof(bool), typeof(MDIValue), new UIPropertyMetadata(false));
		public bool IsReadOnly {
			get { return (bool)GetValue(IsReadOnlyProperty); }
			set { SetValue(IsReadOnlyProperty, value); }
		}

		#endregion

		#region Dependency Property: SelectedAutoCompleteItem

		/// <summary>
		/// Event that handles when the selected auto-complete item changed
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private static void OnSelectedAutoCompleteItemChanged(DependencyObject sender, DependencyPropertyChangedEventArgs e) {
			MDIValue value = sender as MDIValue;
			if (value != null)
				value.OnSelectedAutoCompleteItemChanged((MDIValueContainer)e.OldValue, (MDIValueContainer)e.NewValue);
		}

		public static readonly DependencyProperty SelectedAutoCompleteItemProperty =
		    DependencyProperty.Register("SelectedAutoCompleteItem", typeof(MDIValueContainer), typeof(MDIValue), new UIPropertyMetadata(null, new PropertyChangedCallback(OnSelectedAutoCompleteItemChanged)));
		public MDIValueContainer SelectedAutoCompleteItem {
			get { return (MDIValueContainer)GetValue(SelectedAutoCompleteItemProperty); }
			set { SetValue(SelectedAutoCompleteItemProperty, value); }
		}

		#endregion

		#region Dependency Property: Status

		/// <summary>
		/// Event that handles when the status changed
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private static void OnStatusChanged(DependencyObject sender, DependencyPropertyChangedEventArgs e) {
			MDIValue value = sender as MDIValue;
			if (value != null)
				value.OnStatusChanged((MetaDataValueStatus)e.OldValue);
		}

		public static readonly DependencyProperty StatusProperty =
			DependencyProperty.Register("Status", typeof(MetaDataValueStatus), typeof(MDIValue), new UIPropertyMetadata(MetaDataValueStatus.Read, new PropertyChangedCallback(OnStatusChanged)));
		public MetaDataValueStatus Status {
			get { return (MetaDataValueStatus)GetValue(StatusProperty); }
			set { if (!IsReadOnly) SetValue(StatusProperty, value); }
		}

		#endregion

		#region Dependency Property: Value

		public static readonly DependencyProperty ValueProperty =
			DependencyProperty.Register("Value", typeof(MDIValueContainer), typeof(MDIValue), new UIPropertyMetadata(null));
		public MDIValueContainer Value {
			get { return (MDIValueContainer)GetValue(ValueProperty); }
			set { SetValue(ValueProperty, value); }
		}

		#endregion

		#region Properties

		/// <summary>
		/// Get the auto-complete items collectoin view from the collection view source resource
		/// </summary>
		public ICollectionView AutoCompleteView {
			get {
				var viewSource = AutoCompleteViewSource;
				return viewSource == null ? null : viewSource.View;
			}
		}

		/// <summary>
		/// Get the auto-complete items collection view source resource
		/// </summary>
		public CollectionViewSource AutoCompleteViewSource {
			get {
				if (_autoCompleteViewSource == null) {
					_autoCompleteViewSource = ChildGrid.Resources["AutoCompleteViewSource"] as CollectionViewSource;
				}
				return _autoCompleteViewSource;
			}
		}

		#endregion

		#region Events

		public event Action<MDIValue> StatusChanged;
		public event Action<MDIValue> ValueChanged;
		public event Action<MDIValue> ValueDeleted;

		#endregion

		#region Constructor

		public MDIValue() {
			// Hook up event handlers
			GotKeyboardFocus += new KeyboardFocusChangedEventHandler(OnGotKeyboardFocus);
			LostKeyboardFocus += new KeyboardFocusChangedEventHandler(OnLostKeyboardFocus);
			Loaded += new RoutedEventHandler(OnLoaded);
			AddHandler(UserControl.KeyDownEvent, new KeyEventHandler(OnMDIKeyDown), true);

			// Initialize
			InitializeComponent();
		}

		#endregion

		#region Auto-Complete Event Handlers

		/// <summary>
		/// Event that handles when the auto-complete items should be filtered
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnFilterAutoCompleteItem(object sender, FilterEventArgs e) {
			var item = e.Item as MDIValueContainer;
			if (item != null) {
				e.Accepted = ValueTextBox.Text.Split(' ').All(p => item.Value.IndexOf(p, StringComparison.CurrentCultureIgnoreCase) >= 0);
			}
		}

		/// <summary>
		/// Event that handles when the mouse enters a given auto-complete item
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnAutoCompleteItemEntered(object sender, MouseEventArgs e) {
			var element = sender as FrameworkElement;
			var item = element == null ? null : element.DataContext as MDIValueContainer;
			if (item != null) {
				SelectedAutoCompleteItem = item;
			}
		}

		/// <summary>
		/// Event that handles when an item in the auto-complete box is selected
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnAutoCompleteItemSelected(object sender, MouseButtonEventArgs e) {
			var element = sender as FrameworkElement;
			var item = element == null ? null : element.DataContext as MDIValueContainer;
			OnAutoCompleteItemSelected(item, true);
		}

		/// <summary>
		/// Event that completes the selection of an auto-complete item
		/// </summary>
		/// <param name="autoCompleteItem">The auto-complete item that was selected</param>
		/// <param name="selectItem">True to select all the text in the textbox after selecting the item, or false to continue without selecting it</param>
		private void OnAutoCompleteItemSelected(MDIValueContainer autoCompleteItem, bool selectItem) {
			if (autoCompleteItem != null) {
				Value.Value = autoCompleteItem.Value;
				CloseAutoComplete();
				if (selectItem) {
					OnStatusChanged(MetaDataValueStatus.Edit);
					SelectAll();
				}
			}
		}

		/// <summary>
		/// Event that handles when the text has changed so the filtering of the auto-complete items needs to change as well
		/// </summary>
		private void OnAutoCompleteTextChanged() {
			RefreshAutoComplete();
		}

		/// <summary>
		/// Event that handles when the selected auto-completed item has changed
		/// </summary>
		/// <param name="oldValue">The value that was previously selected</param>
		/// <param name="newValue">The value that is newly selected</param>
		private void OnSelectedAutoCompleteItemChanged(MDIValueContainer oldValue, MDIValueContainer newValue) {
			if (oldValue != null) {
				oldValue.IsSelected = false;
			}
			if (newValue != null) {
				newValue.IsSelected = true;
				if (IsAutoCompleteOpen) {
					var element = AutoCompleteItems.ItemContainerGenerator.ContainerFromItem(newValue) as FrameworkElement;
					if (element != null) {
						element.BringIntoView();
					}
				}
			}
		}

		#endregion

		#region Event Handlers

		/// <summary>
		/// Event that handles when the value control received keyboard focus
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnGotKeyboardFocus(object sender, KeyboardFocusChangedEventArgs e) {
			if (Mouse.LeftButton == MouseButtonState.Released && !_customSelect) {
				if (e.OldFocus != null) {
					ValueTextBox.SelectAll();
				} else if (ValueTextBox.Text.Length > 0) {
					ValueTextBox.ScrollToHorizontalOffset(_scrollOffset);
				}
			}
		}

		/// <summary>
		/// Event that handles when the value control has lost keyboard focus
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnLostKeyboardFocus(object sender, KeyboardFocusChangedEventArgs e) {
			_scrollOffset = ValueTextBox.HorizontalOffset;
		}

		/// <summary>
		/// Event that handles when the value control has loaded
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnLoaded(object sender, RoutedEventArgs e) {
			// Setup the value status binding
			var binding = new MultiBinding { Converter = new MDIValueStatusConverter() };
			binding.Bindings.Add(new Binding { Source = this, Path = new PropertyPath("IsMouseOver") });
			binding.Bindings.Add(new Binding { Source = this, Path = new PropertyPath("IsKeyboardFocusWithin") });
			binding.Bindings.Add(new Binding { Source = ValueTextBox.ContextMenu, Path = new PropertyPath("IsOpen") });
			SetBinding(MDIValue.StatusProperty, binding);

			// Send the value updated event when the text changes
			ValueTextBox.TextChanged += new TextChangedEventHandler(OnValueTextBoxChanged);

			// Select text
			if (_customSelect) {
				Select(_selectionStart, _selectionLength);
				_customSelect = false;
			}
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
			if ((Keyboard.Modifiers & ModifierKeys.Control) == ModifierKeys.Control && !IsReadOnly) {
				if (e.Key == Key.Space) {
					ShowAutoComplete();
				} else if (e.Key == Key.D) {
					OnValueDeleted(null, null);
				} else {
					handled = false;
				}
			} else if (IsAutoCompleteOpen) {
				if (e.Key == Key.Escape) {
					CloseAutoComplete();
				} else if (e.Key == Key.Up) {
					SelectedAutoCompleteItem = AutoCompleteView.OfType<MDIValueContainer>().ElementBefore(SelectedAutoCompleteItem, true);
				} else if (e.Key == Key.Down) {
					SelectedAutoCompleteItem = AutoCompleteView.OfType<MDIValueContainer>().ElementAfter(SelectedAutoCompleteItem, true);
				} else if (e.Key == Key.Enter || e.Key == Key.Tab) {
					OnAutoCompleteItemSelected(SelectedAutoCompleteItem, false);
					handled = e.Key == Key.Enter;
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
		/// Event that handles when the value within the text box has changed
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnValueTextBoxChanged(object sender, TextChangedEventArgs e) {
			OnValueChanged();
		}

		/// <summary>
		/// Event that handles when the status changed
		/// </summary>
		private void OnStatusChanged(MetaDataValueStatus prevStatus) {
			CloseAutoComplete();
			if (StatusChanged != null)
				StatusChanged(this);
			if (prevStatus == MetaDataValueStatus.Edit)
				OnValueChanged();
		}

		/// <summary>
		/// Event that handles when the value changed
		/// </summary>
		private void OnValueChanged() {
			if (ValueChanged != null)
				ValueChanged(this);
			OnAutoCompleteTextChanged();
		}

		/// <summary>
		/// Event that handles when the value is deleted or cleared
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnValueDeleted(object sender, MouseButtonEventArgs e) {
			if (ValueDeleted != null)
				ValueDeleted(this);
		}

		#endregion

		#region Members

		/// <summary>
		/// Popuplate a value selection for this value control
		/// </summary>
		/// <returns>A current value selection for this value control</returns>
		public MDIValueSelection PopulateValueSelection() {
			return new MDIValueSelection
			{
				SelectionLength = ValueTextBox.SelectionLength,
				SelectionStart = ValueTextBox.SelectionStart,
				Value = Value.Value
			};
		}

		/// <summary>
		/// Select the text from a value selection
		/// </summary>
		/// <param name="selection">The value selection to select text from</param>
		public void SelectFromValueSelection(MDIValueSelection selection) {
			Select(selection.SelectionStart, selection.SelectionLength);
		}

		/// <summary>
		/// Select all the text in the textbox and give it keyboard focus
		/// </summary>
		public void SelectAll() {
			Select(0, -1);
		}

		/// <summary>
		/// Select a specified length of text starting at a given index, and then give it keyboard focus
		/// </summary>
		/// <param name="start">The index at which to start the selection</param>
		/// <param name="length">The length the selection will be, or -1 to select all</param>
		public void Select(int start, int length) {
			if (IsLoaded) {
				if (length == -1)
					ValueTextBox.SelectAll();
				else
					ValueTextBox.Select(start, length);
				ValueTextBox.ForceFocus();
			} else {
				_customSelect = true;
				_selectionStart = start;
				_selectionLength = length;
			}
		}

		/// <summary>
		/// Closes the auto-complete box and de-selects the current item
		/// </summary>
		public void CloseAutoComplete() {
			if (IsAutoCompleteOpen) {
				var currentItem = SelectedAutoCompleteItem;
				if (currentItem != null) {
					currentItem.IsSelected = false;
				}
				IsAutoCompleteOpen = false;
			}
		}

		/// <summary>
		/// Display the auto-complete box if there are any suggestions to display
		/// </summary>
		public void ShowAutoComplete() {
			if (!IsAutoCompleteOpen && !IsReadOnly) {
				IsAutoCompleteOpen = true;
				RefreshAutoComplete();
			}
		}

		/// <summary>
		/// Refreshes the auto-complete view to contain the correct items with the selected item being brought into view
		/// </summary>
		private void RefreshAutoComplete() {
			AutoCompleteView.Refresh();
			var currentItem = SelectedAutoCompleteItem;
			if (currentItem == null || AutoCompleteView.OfType<MDIValueContainer>().All(c => c != currentItem))
				currentItem = AutoCompleteView.OfType<MDIValueContainer>().FirstOrDefault();
			SelectedAutoCompleteItem = currentItem;
			if (currentItem != null) {
				currentItem.IsSelected = true;
				var element = AutoCompleteItems.ItemContainerGenerator.ContainerFromItem(currentItem) as FrameworkElement;
				if (element != null) {
					element.BringIntoView();
				}
			}
		}

		#endregion
	}
}