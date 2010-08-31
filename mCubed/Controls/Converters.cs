using System;
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
}