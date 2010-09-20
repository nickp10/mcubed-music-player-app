using System;
using System.Collections.Generic;
using System.Linq;
using System.Windows;
using System.Windows.Data;
using mCubed.Core;

namespace mCubed.Controls {
	/// <summary>
	/// Used to determine if the given object is an instance of a
	/// group item or not [Binding, One-Way]
	/// </summary>
	public class IsGroupItemConverter : IValueConverter {
		#region IValueConverter Members

		public object Convert(object value, Type targetType, object parameter, System.Globalization.CultureInfo culture) {
			return GroupListHelper.IsGroupList(value);
		}

		public object ConvertBack(object value, Type targetType, object parameter, System.Globalization.CultureInfo culture) {
			return null;
		}

		#endregion
	}

	/// <summary>
	/// Used to specify the indentation level margin for the group item
	/// based off the depth of the group item [Binding, One-Way]
	/// </summary>
	public class GroupItemIndentConverter : IValueConverter {
		#region IValueConverter Members

		public object Convert(object value, Type targetType, object parameter, System.Globalization.CultureInfo culture) {
			int depth = 0;
			if (value is int)
				depth = (int)value - 1;
			return new Thickness(depth * 25, 0, 0, 0);
		}

		public object ConvertBack(object value, Type targetType, object parameter, System.Globalization.CultureInfo culture) {
			return null;
		}

		#endregion
	}

	/// <summary>
	/// Used to specify the margin of a group item and also forces the group item to remain
	/// stationary when its containing ScrollViewer is scrolled horizontally [Binding, One-Way]
	/// </summary>
	public class GroupItemMarginConverter : IValueConverter {
		#region IValueConverter Members

		public object Convert(object value, Type targetType, object parameter, System.Globalization.CultureInfo culture) {
			return new Thickness((double)value + 5, 5, 5, 5);
		}

		public object ConvertBack(object value, Type targetType, object parameter, System.Globalization.CultureInfo culture) {
			return null;
		}

		#endregion
	}

	/// <summary>
	/// Used to specify the width of a group item, which should remain to be equivalent to the viewport
	/// width of its contain ScrollViewer while taking into account its margin [Binding, One-Way]
	/// </summary>
	public class GroupItemWidthConverter : IValueConverter {
		#region IValueConverter Members

		public object Convert(object value, Type targetType, object parameter, System.Globalization.CultureInfo culture) {
			return (double)value - 12;
		}

		public object ConvertBack(object value, Type targetType, object parameter, System.Globalization.CultureInfo culture) {
			return null;
		}

		#endregion
	}

	/// <summary>
	/// Used to specify the collection of columns to be able to select from. It should filter out the
	/// already selected columns if the selected columns should not be shown. [Multi-Binding, One-Way]
	/// </summary>
	public class ColumnListConverter : IMultiValueConverter {
		#region IMultiValueConverter Members

		public object Convert(object[] values, Type targetType, object parameter, System.Globalization.CultureInfo culture) {
			/*
			 * 0 == AllColumns (IEnumerable<ColumnDetail>)
			 * 1 == SelectedColumns (IEnumerable<ColumnVector>)
			 * 2 == ShowSelectedColumns (bool)
			 */

			// Make sure the parameters came back properly
			if (values.Length != 3)
				return null;
			var all = values[0] as IEnumerable<ColumnDetail>;
			var sel = values[1] as IEnumerable<ColumnVector>;
			var showSel = values[2] is bool && (bool)values[2];
			if (all == null || sel == null)
				return null;

			// Return all of them if we're including the selected
			if (showSel)
				return all.WrapEnumerable();

			// We have to filter otherwise
			return all.Where(detail => !sel.Any(vector => vector.ColumnDetail == detail)).ToArray();
		}

		public object[] ConvertBack(object value, Type[] targetTypes, object parameter, System.Globalization.CultureInfo culture) {
			return null;
		}

		#endregion
	}

	/// <summary>
	/// Used to determine if a given column is within the selected columns or not. It takes the collection
	/// of selected columns and the column to determine if it's within it. [Multi-Binding, One-Way]
	/// </summary>
	public class IsColumnSelectedConverter : IMultiValueConverter {
		#region IMultiValueConverter Members

		public object Convert(object[] values, Type targetType, object parameter, System.Globalization.CultureInfo culture) {
			/*
			 * 0 == SelectedColumns (IEnumerable<ColumnVector>)
			 * 1 == ColumnDetail (ColumnDetail)
			 */

			// Make sure the parameters came back properly
			if (values.Length != 2)
				return null;
			var sel = values[0] as IEnumerable<ColumnVector>;
			var det = values[1] as ColumnDetail;
			if (sel == null || det == null)
				return false;

			// Check if it's selected
			return sel.Any(vector => vector.ColumnDetail == det);
		}

		public object[] ConvertBack(object value, Type[] targetTypes, object parameter, System.Globalization.CultureInfo culture) {
			return null;
		}

		#endregion
	}
}