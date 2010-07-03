using System;
using System.ComponentModel;
using System.Linq;

namespace mCubed.Core {
	public class ColumnDetail : INotifyPropertyChanged, IDisposable {
		#region INotifyPropertyChanged Members

		public event PropertyChangedEventHandler PropertyChanged;

		#endregion

		#region Data Store

		private string _display;
		private readonly MetaDataFormula _formula;
		private string _key;
		private readonly MetaDataAttribute _property;
		private ColumnType _type = ColumnType.Property;

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
		/// Get/set the ID that was used in the XML settings for this column detail [Bindable]
		/// </summary>
		public int XMLID { get; set; }

		#endregion

		#region Constructor

		/// <summary>
		/// Create a new column detail with the formula that the column is for
		/// </summary>
		/// <param name="formula">The formula for the column to replicate</param>
		public ColumnDetail(MetaDataFormula formula)
			: this(ColumnType.Formula, formula.Name, formula.Name) {
			_formula = formula;
			RegisterFormula();
		}

		/// <summary>
		/// Create a new column detail with the property that the column is for
		/// </summary>
		/// <param name="property">The property for the column to replicate</param>
		public ColumnDetail(MetaDataAttribute property)
			: this(ColumnType.Property, property.Property.Name, property.Display) {
			_property = property;
		}

		/// <summary>
		/// Create a new column detail with the column type and key for the column
		/// </summary>
		/// <param name="type">The type for the column</param>
		/// <param name="key">The key for the column</param>
		public ColumnDetail(ColumnType type, string key)
			: this(type, key, null) {
			if (type == ColumnType.Formula) {
				_formula = Utilities.MainSettings.Formulas.FirstOrDefault(f => f.Name == key);
				Display = _formula.Name;
				RegisterFormula();
			} else if (type == ColumnType.Property) {
				_property = MetaDataFormula.MetaDataProperties.FirstOrDefault(p => p.Property.Name == key);
				Display = _property.Display;
			}
		}

		/// <summary>
		/// Create a new column detail with the column type, key, and display for the column
		/// For more information, refer to the property documentation of each of the values
		/// </summary>
		/// <param name="type">The type for the column</param>
		/// <param name="key">The key for the column</param>
		/// <param name="display">The display for the column</param>
		private ColumnDetail(ColumnType type, string key, string display) {
			Type = type;
			Key = key;
			Display = display;
		}

		#endregion

		#region Event Handlers

		/// <summary>
		/// Event that handles when a property on the formula has changed
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnFormulaPropertyChanged(object sender, PropertyChangedEventArgs e) {
			if (e.PropertyName == "Name") {
				Key = _formula.Name;
				Display = _formula.Name;
			}
		}

		#endregion

		#region Members

		/// <summary>
		/// Register to the formula display name changed
		/// </summary>
		private void RegisterFormula() {
			if (_formula != null)
				_formula.PropertyChanged += new PropertyChangedEventHandler(OnFormulaPropertyChanged);
		}

		/// <summary>
		/// Get the value for the column details and the given media file
		/// </summary>
		/// <param name="file">The file that the column details should be applied on to generate a value</param>
		/// <returns>The value from the file as described by the column details</returns>
		public IComparable ProvideValue(MediaFile file) {
			// Retrieve the value
			object value = null;
			if (Type == ColumnType.Formula) {
				value = MDFFile.GetValue(_formula, file);
			} else if (Type == ColumnType.Property) {
				value = _property.Property.GetValue(file.MetaData, null);
			}

			// Return the value
			if (value == null)
				return null;
			if (value is IComparable)
				return (IComparable)value;
			return value.ToString();
		}

		#endregion

		#region IDisposable Members

		/// <summary>
		/// Dispose of the column detail appropriately
		/// </summary>
		public void Dispose() {
			if (_formula != null)
				_formula.PropertyChanged -= new PropertyChangedEventHandler(OnFormulaPropertyChanged);
			PropertyChanged = null;
		}

		#endregion
	}

	public class ColumnVector : INotifyPropertyChanged, IDisposable {
		#region INotifyPropertyChanged Members

		public event PropertyChangedEventHandler PropertyChanged;

		#endregion

		#region Data Store

		private readonly ColumnDetail _columnDetail;
		private ColumnDirection _direction = ColumnDirection.Ascending;
		private double _width = double.NaN;

		#endregion

		#region Bindable Properties

		/// <summary>
		/// Get the column detail that is being used as a backing field [Bindable]
		/// </summary>
		public ColumnDetail ColumnDetail { get { return _columnDetail; } }

		/// <summary>
		/// Get/set the direction of how the column detail will be used [Bindable]
		/// </summary>
		public ColumnDirection Direction {
			get { return _direction; }
			set { this.SetAndNotify(ref _direction, value, "Direction"); }
		}

		/// <summary>
		/// Get/set the width that will be displayed for this column [Bindable]
		/// </summary>
		public double Width {
			get { return _width; }
			set { this.SetAndNotify(ref _width, value, "Width"); }
		}

		#endregion

		#region Constructor

		/// <summary>
		/// Create a column vector that will specify the width and direction for a given column detail
		/// </summary>
		/// <param name="columnDetail">The column detail to specify additional information for</param>
		public ColumnVector(ColumnDetail columnDetail) {
			_columnDetail = columnDetail;
		}

		#endregion

		#region Members

		/// <summary>
		/// Get the value for the column details and the given media file
		/// </summary>
		/// <param name="file">The file that the column details should be applied on to generate a value</param>
		/// <returns>The value from the file as described by the column details</returns>
		public IComparable ProvideValue(MediaFile file) {
			return ColumnDetail.ProvideValue(file);
		}

		#endregion

		#region IDisposable Members

		/// <summary>
		/// Dispose of the column detail object appropriately
		/// </summary>
		public void Dispose() {
			PropertyChanged = null;
		}

		#endregion
	}
}