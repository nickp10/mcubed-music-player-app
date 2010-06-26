using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.ComponentModel;

namespace mCubed.Core {
	public class ColumnDetail : INotifyPropertyChanged {
		#region INotifyPropertyChanged Members

		public event PropertyChangedEventHandler PropertyChanged;

		#endregion

		#region Data Store

		private string _display;
		private string _key;
		private ColumnType _type = ColumnType.Property;
		private double _width = double.NaN;

		#endregion

		#region Bindable Properties

		/// <summary>
		/// Get/set the value that should be displayed when the column itself is being displayed [Bindable]
		/// </summary>
		public string Display {
			get { return _display; }
			set { this.SetAndNotify(ref _display, value, "Display"); }
		}

		/// <summary>
		/// Get the key that will be used to retrieve the proper column type information in order to 
		/// generate the value for each individual item for this column. For instance, "Title" with Type
		/// equal to Property means that the "Title" property of each instance will be displayed as the
		/// value for this column. "Title" with Type equal to Formula means that the formula with a name
		/// of "Title" will be used to generate the value displayed for each instance under this column. [Bindable]
		/// </summary>
		public string Key {
			get { return _key; }
			private set { this.SetAndNotify(ref _key, value, "Key"); }
		}

		/// <summary>
		/// Get the type of column that the detail represents [Bindable]
		/// </summary>
		public ColumnType Type {
			get { return _type; }
			private set { this.SetAndNotify(ref _type, value, "Type"); }
		}

		/// <summary>
		/// Get/set the width that this column should be sized to, note that double.NaN means an automatic width [Bindable]
		/// </summary>
		public double Width {
			get { return _width; }
			set { this.SetAndNotify(ref _width, value, "Width"); }
		}

		#endregion

		#region Constructor

		/// <summary>
		/// Create a new column detail with the column type, key, and display for the column
		/// For more information, refer to the property documentation of each of the values
		/// </summary>
		/// <param name="type">The type for the column</param>
		/// <param name="key">The key for the column</param>
		/// <param name="display">The display for the column</param>
		public ColumnDetail(ColumnType type, string key, string display) {
			Type = type;
			Key = key;
			Display = display;
		}

		#endregion
	}
}