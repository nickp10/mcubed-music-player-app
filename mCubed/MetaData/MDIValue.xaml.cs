using System;
using System.Collections.Generic;
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

		private bool _customSelect = false;
		private int _selectionLength;
		private int _selectionStart;

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

		#region Events

		public event Action<MDIValue> StatusChanged;
		public event Action<MDIValue> ValueChanged;
		public event Action<MDIValue> ValueDeleted;

		#endregion

		#region Constructor

		public MDIValue() {
			// Hook up event handlers
			GotKeyboardFocus += new KeyboardFocusChangedEventHandler(OnGotKeyboardFocus);
			Loaded += new RoutedEventHandler(OnLoaded);
			AddHandler(UserControl.KeyDownEvent, new KeyEventHandler(OnMDIKeyDown), true);

			// Initialize
			InitializeComponent();
		}

		#endregion

		#region Event Handlers

		/// <summary>
		/// Event that handles when the value control received keyboard focus
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnGotKeyboardFocus(object sender, KeyboardFocusChangedEventArgs e) {
			if (Mouse.LeftButton == MouseButtonState.Released && !_customSelect)
				ValueTextBox.SelectAll();
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
			} else if (e.Key == Key.Escape) {
				IsAutoCompleteOpen = false;
			} else {
				handled = false;
			}

			// Was the keypress handled
			if (handled)
				e.Handled = true;
		}

		/// <summary>
		/// Event that handles when the status changed
		/// </summary>
		private void OnStatusChanged(MetaDataValueStatus prevStatus) {
			IsAutoCompleteOpen = false;
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

		/// <summary>
		/// Event that handles when an item in the auto-complete box is selected
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnValueSelected(object sender, MouseButtonEventArgs e) {
			var element = sender as FrameworkElement;
			var item = element == null ? null : element.DataContext as MDIValueContainer;
			if (item != null) {
				Value.Value = item.Value;
				IsAutoCompleteOpen = false;
				OnStatusChanged(MetaDataValueStatus.Edit);
				SelectAll();
			}
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
				ValueTextBox.Focus();
			} else {
				_customSelect = true;
				_selectionStart = start;
				_selectionLength = length;
			}
		}

		/// <summary>
		/// Display the auto-complete box there are any suggestions to display
		/// </summary>
		public void ShowAutoComplete() {
			IsAutoCompleteOpen = !IsReadOnly && Alternatives != null && Alternatives.Count() > 0;
		}

		#endregion
	}
}