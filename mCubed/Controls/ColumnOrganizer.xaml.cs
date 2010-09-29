using System.Windows;
using System.Windows.Controls;
using System.Windows.Controls.Primitives;
using System.Windows.Data;
using System.Windows.Input;
using mCubed.Core;

namespace mCubed.Controls {
	public partial class ColumnOrganizer : UserControl {
		#region Dependency Property: ColumnSettings

		public static readonly DependencyProperty ColumnSettingsProperty =
			DependencyProperty.Register("ColumnSettings", typeof(ColumnSettings), typeof(ColumnOrganizer), new UIPropertyMetadata(null));

		/// <summary>
		/// Get/set the column settings that will be used by the organizer [Bindable]
		/// </summary>
		public ColumnSettings ColumnSettings {
			get { return (ColumnSettings)GetValue(ColumnSettingsProperty); }
			set { SetValue(ColumnSettingsProperty, value); }
		}

		#endregion

		#region Dependency Property: Header

		public static readonly DependencyProperty HeaderProperty =
			DependencyProperty.Register("Header", typeof(string), typeof(ColumnOrganizer), new UIPropertyMetadata(null));

		/// <summary>
		/// Get/set the header that will be used for the column organizer [Bindable]
		/// </summary>
		public string Header {
			get { return (string)GetValue(HeaderProperty); }
			set { SetValue(HeaderProperty, value); }
		}

		#endregion

		#region Dependency Property: SelectedColumnGroup

		public static readonly DependencyProperty SelectedColumnGroupProperty =
			DependencyProperty.Register("SelectedColumnGroup", typeof(string), typeof(ColumnOrganizer), new UIPropertyMetadata(null));

		/// <summary>
		/// Get/set the column group that is used for the selected columns [Bindable]
		/// </summary>
		public string SelectedColumnGroup {
			get { return (string)GetValue(SelectedColumnGroupProperty); }
			set { SetValue(SelectedColumnGroupProperty, value); }
		}

		#endregion

		#region Constructor

		/// <summary>
		/// Create a new column organizer control
		/// </summary>
		public ColumnOrganizer() {
			// Setup the loaded event
			Loaded += delegate
			{
				MultiBinding binding = new MultiBinding { Converter = new PopupOpenConverter(), Mode = BindingMode.OneWay };
				binding.Bindings.Add(new Binding { Source = OrganizerAddButton, Path = new PropertyPath("IsMouseOver") });
				binding.Bindings.Add(new Binding { Source = ColumnSelector, Path = new PropertyPath("IsMouseOver") });
				ColumnSelector.SetBinding(Popup.IsOpenProperty, binding);
			};

			// Initialize
			InitializeComponent();
		}

		#endregion

		#region Event Handlers

		/// <summary>
		/// Event that handles when the organizer should be shown
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnOrganizerShown(object sender, RoutedEventArgs e) {
			OrganizerPopup.IsOpen = true;
		}

		/// <summary>
		/// Event that handles when the column selector should be shown
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnSelectorShown(object sender, MouseButtonEventArgs e) {
			ColumnSelector.IsOpen = true;
		}

		/// <summary>
		/// Event that handles when a column has been selected
		/// </summary>
		/// <param name="detail">The column detail that has been selected</param>
		private void OnColumnSelected(ColumnDetail detail) {
			ColumnSettings[SelectedColumnGroup].Add(new ColumnVector(detail));
		}

		/// <summary>
		/// Event that handles when a column should be deleted
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnColumnDeleted(object sender, MouseButtonEventArgs e) {
			var element = sender as FrameworkElement;
			var vector = element == null ? null : element.DataContext as ColumnVector;
			if (vector != null)
				ColumnSettings[SelectedColumnGroup].Remove(vector);
		}

		/// <summary>
		/// Event that handles when a column's direction should be reversed
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnColumnDirectionSwithced(object sender, MouseButtonEventArgs e) {
			var element = sender as FrameworkElement;
			var vector = element == null ? null : element.DataContext as ColumnVector;
			if (vector != null)
				vector.Direction = vector.Direction == ColumnDirection.Ascending ? ColumnDirection.Descending : ColumnDirection.Ascending;
		}

		#endregion
	}
}