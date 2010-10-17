using System.Windows;

namespace mCubed {
	public partial class mCubedError : Window {
		#region Dependency Property: Message

		public static readonly DependencyProperty MessageProperty =
			DependencyProperty.Register("Message", typeof(string), typeof(mCubedError), new UIPropertyMetadata(null));

		/// <summary>
		/// Get/set the message that will be displayed [Bindable]
		/// </summary>
		public string Message {
			get { return (string)GetValue(MessageProperty); }
			set { SetValue(MessageProperty, value); }
		}

		#endregion

		#region Constructor

		/// <summary>
		/// Construct an error window
		/// </summary>
		public mCubedError() {
			InitializeComponent();
		}

		#endregion

		#region Event Handlers

		/// <summary>
		/// Event that handles when the close button is clicked
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnCloseClicked(object sender, RoutedEventArgs e) {
			Close();
		}

		#endregion
	}
}