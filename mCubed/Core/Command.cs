using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.ComponentModel;

namespace mCubed.Core {
	public class Command : IExternalNotifyPropertyChanged, IExternalNotifyPropertyChanging {
		#region Data Store

		private string _displayName;
		private string _value;

		#endregion

		#region Bindable Properties

		/// <summary>
		/// Get/set a display name for this command [Bindable]
		/// </summary>
		public string DisplayName {
			get { return _displayName; }
			set { this.SetAndNotify(ref _displayName, value, "DisplayName"); }
		}

		/// <summary>
		/// Get/set the actual command-line string for this command [Bindable]
		/// </summary>
		public string Value {
			get { return _value; }
			set { this.SetAndNotify(ref _value, value, "Value"); }
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