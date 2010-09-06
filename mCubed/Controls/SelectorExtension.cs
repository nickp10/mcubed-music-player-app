using System.Windows;

namespace mCubed.Controls {
	public static class SelectorExtension {
		#region Attached Dependency Property: IsSelectable

		public static readonly DependencyProperty IsSelectableProperty =
			DependencyProperty.RegisterAttached("IsSelectable", typeof(bool), typeof(SelectorExtension), new PropertyMetadata(true));

		/// <summary>
		/// Get whether or not the item is selectable inside of a Selector control or not
		/// </summary>
		/// <param name="item">The item to get its selectable state for</param>
		/// <returns>True if the item can be selected, or false if it cannot be selected</returns>
		public static bool GetIsSelectable(DependencyObject item) {
			return (bool)item.GetValue(IsSelectableProperty);
		}

		/// <summary>
		/// Set whether or not the item is selectable inside of a Selector control or not
		/// </summary>
		/// <param name="item">The item to set its selectable state for</param>
		/// <param name="isSelectable">Ture to allow the item to be selected, or false if it cannot be selected</param>
		public static void SetIsSelectable(DependencyObject item, bool isSelectable) {
			item.SetValue(IsSelectableProperty, isSelectable);
		}

		#endregion

		#region Attached Dependency Property: IsGridViewRow

		public static readonly DependencyProperty IsGridViewRowProperty =
			DependencyProperty.RegisterAttached("IsGridViewRow", typeof(bool), typeof(SelectorExtension), new PropertyMetadata(true));

		/// <summary>
		/// Get whether or not the item is a row in a GridView or not
		/// </summary>
		/// <param name="item">The item to get its GridView row state for</param>
		/// <returns>True to display the item as a row in a GridView or false to display the item using its template</returns>
		public static bool GetIsGridViewRow(DependencyObject item) {
			return (bool)item.GetValue(IsGridViewRowProperty);
		}

		/// <summary>
		/// Set whether or not the item is a row in a GridView or not
		/// </summary>
		/// <param name="item">The item to set its GridView row state for</param>
		/// <param name="isGridViewRow">True to display the item as a row in a GridView or false to display the item using its template</param>
		public static void SetIsGridViewRow(DependencyObject item, bool isGridViewRow) {
			item.SetValue(IsGridViewRowProperty, isGridViewRow);
		}

		#endregion
	}
}