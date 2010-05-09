using System;
using System.Collections.Generic;
using System.Linq;
using System.Windows.Data;
using mCubed.Core;

namespace mCubed.MetaData
{
	public class MDIStatusConverter : IValueConverter
	{
		#region IValueConverter Members

		public object Convert(object value, Type targetType, object parameter, System.Globalization.CultureInfo culture)
		{
			return (value is MetaDataStatus && ((MetaDataStatus)value) == MetaDataStatus.Edit) ? true : false;
		}

		public object ConvertBack(object value, Type targetType, object parameter, System.Globalization.CultureInfo culture)
		{
			return (value is bool && (bool)value) ? MetaDataStatus.Edit : MetaDataStatus.Loaded;
		}

		#endregion
	}

	public class MDIValueStatusConverter : IMultiValueConverter
	{
		#region IMultiValueConverter Members

		public object Convert(object[] values, Type targetType, object parameter, System.Globalization.CultureInfo culture)
		{
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

		public object[] ConvertBack(object value, Type[] targetTypes, object parameter, System.Globalization.CultureInfo culture)
		{
			return null;
		}

		#endregion
	}

	public class MDPFullSizeConverter : IMultiValueConverter
	{
		#region IMultiValueConverter Members

		public object Convert(object[] values, Type targetType, object parameter, System.Globalization.CultureInfo culture)
		{
			// 0 == PicThumb.IsMouseOver, 1 == PicPopup.IsMouseOver
			bool[] bools = values.OfType<bool>().ToArray();
			return bools.Length == 2 && (bools[0] || bools[1]);
		}

		public object[] ConvertBack(object value, Type[] targetTypes, object parameter, System.Globalization.CultureInfo culture)
		{
			return null;
		}

		#endregion
	}

	public class MDPListConverter : IMultiValueConverter
	{
		#region IMultiValueConverter Members

		public object Convert(object[] values, Type targetType, object parameter, System.Globalization.CultureInfo culture)
		{
			if (values.Length == 2 && values[0] is IEnumerable<MetaDataPic> && values[1] is bool)
				return (bool)values[1] ? ((IEnumerable<MetaDataPic>)values[0]).DistinctEquals() : values[0];
			return null;
		}

		public object[] ConvertBack(object value, Type[] targetTypes, object parameter, System.Globalization.CultureInfo culture)
		{
			return null;
		}

		#endregion
	}
}