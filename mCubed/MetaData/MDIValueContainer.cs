using System;
using System.ComponentModel;
using mCubed.Core;

namespace mCubed.MetaData {
	public class MDIValueContainer : IComparable, IComparable<MDIValueContainer>, IEquatable<MDIValueContainer>, IExternalNotifyPropertyChanged, IExternalNotifyPropertyChanging {
		#region Data Store

		private bool _isSelected;
		private bool _isSuggestion;
		private string _value;

		#endregion

		#region Bindable Properties

		/// <summary>
		/// Get/set whether or not this value is selected or not [Bindable]
		/// </summary>
		public bool IsSelected {
			get { return _isSelected; }
			set { this.SetAndNotify(ref _isSelected, value, "IsSelected"); }
		}

		/// <summary>
		/// Get/set whether or not this value is a suggestion or not [Bindable]
		/// </summary>
		public bool IsSuggestion {
			get { return _isSuggestion; }
			set { this.SetAndNotify(ref _isSuggestion, value, "IsSuggestion"); }
		}

		/// <summary>
		/// Get/set the value that this value container is currently holding [Bindable]
		/// </summary>
		public string Value {
			get { return _value; }
			set { this.SetAndNotify(ref _value, value, "Value"); }
		}

		#endregion

		#region IComparable<MDIValueContainer> Members

		/// <summary>
		/// Compare this value container to another value container for sorting purposes
		/// </summary>
		/// <param name="other">The value container to compare to</param>
		/// <returns>An integer representing the order which value containers should be sorted</returns>
		public int CompareTo(MDIValueContainer other) {
			if (other == null)
				return -1;
			return Value.CompareTo(other.Value);
		}

		#endregion

		#region IComparable Members

		/// <summary>
		/// Compare this value container to another value container for sorting purposes
		/// </summary>
		/// <param name="other">The value container to compare to</param>
		/// <returns>An integer representing the order which value containers should be sorted</returns>
		public int CompareTo(object obj) {
			return CompareTo(obj as MDIValueContainer);
		}

		#endregion

		#region IEquatable<MDIValueContainer> Members

		/// <summary>
		/// Check if a given value container is equal to the current value container
		/// </summary>
		/// <param name="other">The value container to compare to</param>
		/// <returns>True if the value containers are equivalent or false otherwise</returns>
		public bool Equals(MDIValueContainer other) {
			return other != null && other.Value == Value;
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
	}
}