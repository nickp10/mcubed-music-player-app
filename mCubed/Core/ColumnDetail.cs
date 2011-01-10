using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;

namespace mCubed.Core {
	public class ColumnDetail : IExternalNotifyPropertyChanged, IExternalNotifyPropertyChanging, IDisposable {
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
		/// Get the meta-data formula that is being used to generate the values, only if the type is a formula type [Bindable]
		/// </summary>
		public MetaDataFormula Formula { get { return _formula; } }

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
		/// Get the meta-data attribute that is being used to generate the values, only if the type is a property type [Bindable]
		/// </summary>
		public MetaDataAttribute Property { get { return _property; } }

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

		#region IExternalNotifyPropertyChanged Members

		public PropertyChangedEventHandler PropertyChangedHandler {
			get { return PropertyChanged; }
		}

		public event PropertyChangedEventHandler PropertyChanged;

		#endregion

		#region IExternalNotifyPropertyChanging Members

		public PropertyChangingEventHandler PropertyChangingHandler {
			get { return PropertyChanging; }
		}

		public event PropertyChangingEventHandler PropertyChanging;

		#endregion

		#region IDisposable Members

		/// <summary>
		/// Dispose of the column detail appropriately
		/// </summary>
		public void Dispose() {
			// Unsubscribe from delegates
			if (_formula != null)
				_formula.PropertyChanged -= new PropertyChangedEventHandler(OnFormulaPropertyChanged);

			// Unsubscribe others from its events
			PropertyChanged = null;
			PropertyChanging = null;
		}

		#endregion
	}

	public class ColumnVector : IKeyProvider<MediaFile>, IComparer<MediaFile>, IResettable<MediaFile>, IExternalNotifyPropertyChanged, IExternalNotifyPropertyChanging, IDisposable {
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
			set { this.SetAndNotify(ref _direction, value, null, OnReset, "Direction"); }
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
			// Store the column detail
			_columnDetail = columnDetail;

			// Register to the formula changed event so it can reset itself
			if (_columnDetail.Type == ColumnType.Formula && _columnDetail.Formula != null)
				_columnDetail.Formula.FormulaChanged += new Action(OnReset);
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

		/// <summary>
		/// Event that notifies when the column vector should be reset, meaning its provide value formula has changed
		/// </summary>
		private void OnReset() {
			if (Reset != null)
				Reset(this);
		}

		#endregion

		#region IKeyProvider<MediaFile> Members

		/// <summary>
		/// Get the value for the column details and the given media file
		/// </summary>
		/// <param name="item">The file that the column details should be applied on to generate a value</param>
		/// <returns>The value from the file as described by the column details</returns>
		public string GetKey(MediaFile item) {
			IComparable comparable = ProvideValue(item);
			if (comparable == null)
				return null;
			return comparable.ToString();
		}

		#endregion

		#region IComparer<MediaFile> Members

		/// <summary>
		/// Compare the two given media files based off the column details for this vector
		/// </summary>
		/// <param name="x">The first media file to compare</param>
		/// <param name="y">The second media file to compare</param>
		/// <returns>A negative integer representing x should come before y, a positive integer representing y should come before x, or 0 if they are equivalent</returns>
		public int Compare(MediaFile x, MediaFile y) {
			// Get the values to compare by
			IComparable xComp = x == null ? null : ProvideValue(x);
			IComparable yComp = y == null ? null : ProvideValue(y);
			int compare = 0;

			// Now compare
			if (xComp == null) {
				compare =  yComp == null ? 0 : -1;
			} else if (yComp == null) {
				compare = 1;
			} else {
				compare = xComp.CompareTo(yComp);
			}
			return Direction == ColumnDirection.Ascending ? compare : 0 - compare;
		}

		#endregion

		#region IResettable<MediaFile> Members

		public event Action<IComparer<MediaFile>> Reset;

		#endregion

		#region IExternalNotifyPropertyChanged Members

		public PropertyChangedEventHandler PropertyChangedHandler {
			get { return PropertyChanged; }
		}

		public event PropertyChangedEventHandler PropertyChanged;

		#endregion

		#region IExternalNotifyPropertyChanging Members

		public PropertyChangingEventHandler PropertyChangingHandler {
			get { return PropertyChanging; }
		}

		public event PropertyChangingEventHandler PropertyChanging;

		#endregion

		#region IDisposable Members

		/// <summary>
		/// Dispose of the column detail object appropriately
		/// </summary>
		public void Dispose() {
			// Unsubscribe others from its events
			PropertyChanged = null;
			PropertyChanging = null;
			Reset = null;

			// Unsubscribe its delegates from other events
			if (_columnDetail.Type == ColumnType.Formula && _columnDetail.Formula != null)
				_columnDetail.Formula.FormulaChanged -= new Action(OnReset);
		}

		#endregion
	}
}