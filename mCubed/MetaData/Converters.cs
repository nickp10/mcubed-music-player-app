using System;
using System.Collections.Generic;
using System.Linq;
using System.Windows.Data;
using mCubed.Core;

namespace mCubed.MetaData {
	/// <summary>
	/// Used to set the checkbox state for the appropriate meta-data information status
	/// to determine if the MDI is only loaded or is being edited also [Binding, Two-Way]
	/// </summary>
	public class MDIStatusConverter : IValueConverter {
		#region IValueConverter Members

		public object Convert(object value, Type targetType, object parameter, System.Globalization.CultureInfo culture) {
			return (value is MetaDataStatus && ((MetaDataStatus)value) == MetaDataStatus.Edit) ? true : false;
		}

		public object ConvertBack(object value, Type targetType, object parameter, System.Globalization.CultureInfo culture) {
			return (value is bool && (bool)value) ? MetaDataStatus.Edit : MetaDataStatus.Loaded;
		}

		#endregion
	}

	/// <summary>
	/// Used to set the state of the meta-data information value control to determine if
	/// the control is being read, modified, or in between [Multi-Binding, One-Way]
	/// </summary>
	public class MDIValueStatusConverter : IMultiValueConverter {
		#region IMultiValueConverter Members

		public object Convert(object[] values, Type targetType, object parameter, System.Globalization.CultureInfo culture) {
			// Setup the conversion
			MetaDataValueStatus status = MetaDataValueStatus.Read;
			bool[] bools = values.OfType<bool>().ToArray();
			if (bools.Length == 3) {
				// 0 == IsMouseOver
				status = bools[0] ? MetaDataValueStatus.ReadEdit : status;
				// 1 == IsKeyboardFocusWithin, 2 == IsOpen (Context Menu)
				status = bools[1] || bools[2] ? MetaDataValueStatus.Edit : status;
			}
			return status;
		}

		public object[] ConvertBack(object value, Type[] targetTypes, object parameter, System.Globalization.CultureInfo culture) {
			return null;
		}

		#endregion
	}

	/// <summary>
	/// Used to display the appropriate collection of meta-data pictures in the MDP manager which accounts
	/// for the list of pictures and whether or not to include duplicates [Multi-Binding, One-Way]
	/// </summary>
	public class MDPListConverter : IMultiValueConverter {
		#region IMultiValueConverter Members

		public object Convert(object[] values, Type targetType, object parameter, System.Globalization.CultureInfo culture) {
			if (values.Length == 2 && values[0] is IEnumerable<MetaDataPic> && values[1] is bool)
				return (bool)values[1] ? ((IEnumerable<MetaDataPic>)values[0]).DistinctEquals() : values[0];
			return null;
		}

		public object[] ConvertBack(object value, Type[] targetTypes, object parameter, System.Globalization.CultureInfo culture) {
			return null;
		}

		#endregion
	}
}