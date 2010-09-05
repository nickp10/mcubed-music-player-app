using System;
using System.Linq;
using System.Windows;
using System.Windows.Controls;
using mCubed.Core;

namespace mCubed.Controls {
	public partial class PlayerControls : UserControl {
		#region Constructor

		public PlayerControls() {
			// Initialize
			InitializeComponent();
		}

		#endregion

		#region Event Handlers

		/// <summary>
		/// The action to be taken on the media, in this case seeking
		/// </summary>
		/// <param name="value">The value to seek the media too</param>
		private void OnSeek(double value) {
			Utilities.MainSettings.LibraryCurrent.MediaObject.Seek(value);
		}

		/// <summary>
		/// The action to be taken on the media
		/// </summary>
		/// <param name="sender">The object sending the request</param>
		/// <param name="e">The arguments for the request</param>
		private void MediaButton_Clicked(object sender, RoutedEventArgs e) {
			Button button = sender as Button;
			string content = (button == null) ? null : button.Tag as string;
			if (Enum.GetNames(typeof(MediaAction)).Contains(content)) {
				MediaAction action = (MediaAction)Enum.Parse(typeof(MediaAction), content, true);
				Utilities.MainSettings.PerformAction(action);
			}
		}

		#endregion
	}
}