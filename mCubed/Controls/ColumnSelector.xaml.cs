using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Windows;
using System.Windows.Controls.Primitives;
using System.Windows.Input;
using mCubed.Core;

namespace mCubed.Controls {
	public partial class ColumnSelector : Popup {
		#region Dependency Property: ColumnSettings

		/// <summary>
		/// Event that handles when the column settings has changed
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private static void OnColumnSettingsChanged(DependencyObject sender, DependencyPropertyChangedEventArgs e) {
			var selector = sender as ColumnSelector;
			if (selector != null)
				selector.OnColumnSettingsChanged(e);
		}

		public static readonly DependencyProperty ColumnSettingsProperty =
		    DependencyProperty.Register("ColumnSettings", typeof(ColumnSettings), typeof(ColumnSelector), new UIPropertyMetadata(null, OnColumnSettingsChanged));

		/// <summary>
		/// Get/set the column settings used for this column selector [Bindable]
		/// </summary>
		public ColumnSettings ColumnSettings {
			get { return (ColumnSettings)GetValue(ColumnSettingsProperty); }
			set { SetValue(ColumnSettingsProperty, value); }
		}

		#endregion

		#region Dependency Property: SelectedColumns

		/// <summary>
		/// Coerces (recalculates) the selected columns value appropriately
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="baseValue">The previous value</param>
		/// <returns>The new value</returns>
		private static object CoerceSelectedColumns(DependencyObject sender, object baseValue) {
			var selector = sender as ColumnSelector;
			return selector == null ? baseValue : selector.OnSelectedColumnsChanging();
		}

		private static readonly DependencyPropertyKey SelectedColumnsProperty =
			DependencyProperty.RegisterReadOnly("SelectedColumns", typeof(IEnumerable<ColumnVector>), typeof(ColumnSelector), new UIPropertyMetadata(null, null, new CoerceValueCallback(CoerceSelectedColumns)));
		
		/// <summary>
		/// Get the collection of the selected columns [Bindable]
		/// </summary>
		public IEnumerable<ColumnVector> SelectedColumns {
			get { return (IEnumerable<ColumnVector>)GetValue(SelectedColumnsProperty.DependencyProperty); }
			private set { SetValue(SelectedColumnsProperty, value); }
		}

		#endregion

		#region Dependency Property: SelectedColumnGroup

		/// <summary>
		/// Event that handles when the selected column group changed
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private static void OnSelectedColumnGroupChanged(DependencyObject sender, DependencyPropertyChangedEventArgs e) {
			sender.InvalidateProperty(ColumnSelector.SelectedColumnsProperty.DependencyProperty);
		}

		public static readonly DependencyProperty SelectedColumnGroupProperty =
			DependencyProperty.Register("SelectedColumnGroup", typeof(string), typeof(ColumnSelector), new UIPropertyMetadata(null, OnSelectedColumnGroupChanged));

		/// <summary>
		/// Get/set the column group that is used for the selected columns [Bindable]
		/// </summary>
		public string SelectedColumnGroup {
			get { return (string)GetValue(SelectedColumnGroupProperty); }
			set { SetValue(SelectedColumnGroupProperty, value); }
		}

		#endregion

		#region Dependency Property: ShowSelectedColumns

		public static readonly DependencyProperty ShowSelectedColumnsProperty =
			DependencyProperty.Register("ShowSelectedColumns", typeof(bool), typeof(ColumnSelector), new UIPropertyMetadata(false));

		/// <summary>
		/// Get/set whether or not the previously selected columns should be shown [Bindable]
		/// </summary>
		public bool ShowSelectedColumns {
			get { return (bool)GetValue(ShowSelectedColumnsProperty); }
			set { SetValue(ShowSelectedColumnsProperty, value); }
		}		

		#endregion

		#region Events

		public event Action<ColumnDetail> ColumnDeselected;
		public event Action<ColumnDetail> ColumnSelected;

		#endregion

		#region Constructor

		/// <summary>
		/// Construct a new column selector user control that will be used to select columns
		/// </summary>
		public ColumnSelector() {
			InitializeComponent();
		}

		#endregion

		#region Event Handlers

		/// <summary>
		/// Event that handles when a particular column detail has been clicked
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnColumnMouseUp(object sender, MouseButtonEventArgs e) {
			var element = sender as FrameworkElement;
			var context = element == null ? null : element.DataContext as ColumnDetail;
			if (context != null) {
				if (SelectedColumns.Any(vector => vector.ColumnDetail == context))
					OnColumnDeselected(context);
				else
					OnColumnSelected(context);
			}
		}

		/// <summary>
		/// Event that handles when a particular column detail has been selected
		/// </summary>
		/// <param name="column">The column detail that has been selected</param>
		private void OnColumnSelected(ColumnDetail column) {
			if (ColumnSelected != null)
				ColumnSelected(column);
		}

		/// <summary>
		/// Event that handles when a particular column detail has been deselected
		/// </summary>
		/// <param name="column">The column detail that has been deselected</param>
		private void OnColumnDeselected(ColumnDetail column) {
			if (ColumnDeselected != null)
				ColumnDeselected(column);
		}
		
		/// <summary>
		/// Event that handles when the column settings has changed
		/// </summary>
		/// <param name="e">The event arguments</param>
		private void OnColumnSettingsChanged(DependencyPropertyChangedEventArgs e) {
			// Unregister to the property changed
			var oldValue = e.OldValue as INotifyPropertyChanged;
			if (oldValue != null)
				oldValue.PropertyChanged -= new PropertyChangedEventHandler(OnColumnSettingsPropertyChanged);

			// Register to the property changed
			var newValue = e.NewValue as INotifyPropertyChanged;
			if (newValue != null)
				newValue.PropertyChanged += new PropertyChangedEventHandler(OnColumnSettingsPropertyChanged);

			// And update the selected columns
			InvalidateProperty(ColumnSelector.SelectedColumnsProperty.DependencyProperty);
		}

		/// <summary>
		/// Event that handles when a property within the column settings has changed
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnColumnSettingsPropertyChanged(object sender, PropertyChangedEventArgs e) {
			if (e.PropertyName == SelectedColumnGroup)
				InvalidateProperty(ColumnSelector.SelectedColumnsProperty.DependencyProperty);
		}

		/// <summary>
		/// Event that handles when the selected columns are in the process of changing
		/// </summary>
		/// <returns>The collection of selected columns</returns>
		private object OnSelectedColumnsChanging() {
			// Ensure we have settings
			if (ColumnSettings == null)
				return null;

			// Update the property, or re-notify appropriately
			return ColumnSettings[SelectedColumnGroup].WrapEnumerable();
		}

		#endregion
	}
}