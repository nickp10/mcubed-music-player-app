using System.Windows;
using mCubed.Core;

namespace mCubed {
	public partial class mCubedError : Window {
		#region Static Members

		/// <summary>
		/// Confirms with the user before continuing with the next action
		/// </summary>
		/// <param name="message">The message to prompt the user for confirmation of</param>
		/// <returns>True if the user confirmed the message, or false otherwise</returns>
		public static bool ShowConfirm(string message) {
			return ShowConfirm(message, "Continue", "Cancel");
		}

		/// <summary>
		/// Confirms with the user before continuing with the next action
		/// </summary>
		/// <param name="message">The message to prompt the user for confirmation of</param>
		/// <param name="continueText">The text that should be displayed on the continue/accept button</param>
		/// <param name="cancelText">The text that should be displayed on the cancel/decline button</param>
		/// <returns>True if the user confirmed the message, or false otherwise</returns>
		public static bool ShowConfirm(string message, string continueText, string cancelText) {
			var error = new mCubedError
			{
				Owner = Application.Current.MainWindow,
				Message = message
			};
			error.ContinueButton.Content = continueText;
			error.ContinueButton.IsDefault = true;
			error.CancelButton.Content = cancelText;
			error.CancelButton.IsCancel = true;
			var result = error.ShowDialog();
			return result.HasValue && result.Value;
		}

		/// <summary>
		/// Displays the given message to the user, forcing the user to acknowledge it before proceeding
		/// </summary>
		/// <param name="message">The message to display to the user</param>
		public static void ShowDisplay(string message) {
			ShowDisplay(message, "OK");
		}

		/// <summary>
		/// Displays the given message to the user, forcing the user to acknowledge it before proceeding
		/// </summary>
		/// <param name="message">The message to display to the user</param>
		/// <param name="buttonText">The text that should be display on the cancel/acknowledgment button</param>
		public static void ShowDisplay(string message, string buttonText) {
			var error = new mCubedError
			{
				Owner = Application.Current.MainWindow,
				Message = message
			};
			error.ContinueButton.Visibility = Visibility.Collapsed;
			error.CancelButton.Content = buttonText;
			error.CancelButton.IsDefault = true;
			error.CancelButton.IsCancel = true;
			error.ShowDialog();
		}

		#endregion

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
		private mCubedError() {
			InitializeComponent();
		}

		#endregion

		#region Event Handlers

		/// <summary>
		/// Event that handles when the continue button is clicked
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnContinueClicked(object sender, RoutedEventArgs e) {
			DialogResult = true;
			Close();
		}

		/// <summary>
		/// Event that handles when the cancel button is clicked
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnCancelClicked(object sender, RoutedEventArgs e) {
			DialogResult = false;
			Close();
		}

		#endregion
	}
}