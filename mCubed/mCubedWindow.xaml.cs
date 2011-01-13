using System;
using System.ComponentModel;
using System.Windows;
using System.Windows.Input;
using mCubed.Core;

namespace mCubed {
	public partial class mCubedWindow : Window {
		#region Data Store

		private Rect _fullPos = new Rect(double.NaN, double.NaN, 1000, 800);
		private WindowState _fullState = WindowState.Maximized;

		#endregion

		#region Constructor

		/// <summary>
		/// Create a new mCubed window
		/// </summary>
		public mCubedWindow() {
			// Initialize
			InitializeComponent();

			// Hook up event handlers
			Logger.RegisterListener(OnLogSent, LogLevel.Error);
			Utilities.MainSettings.ShowMiniChanged += new Action(OnShowMiniChanged);
			Closing += new CancelEventHandler(OnClosing);
			PreviewKeyDown += new KeyEventHandler(OnKeyDown);
			GlobalKeyboardHook.OnKeyDown += new Action<object, Key>(OnGlobalKeyDown);

			// Finalizations
			Utilities.MainSettings.InvokeEvents();
		}

		#endregion

		#region Event Handlers

		/// <summary>
		/// Event that handles the global keyboard input
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="key">The event arguments</param>
		private void OnGlobalKeyDown(object sender, Key key) {
			MediaAction? action = null;
			if (key == Key.MediaNextTrack) {
				action = MediaAction.Next;
			} else if (key == Key.MediaPreviousTrack) {
				action = MediaAction.Prev;
			} else if (key == Key.MediaStop) {
				action = MediaAction.Stop;
			} else if (key == Key.MediaPlayPause) {
				action = MediaAction.PlayPause;
			}
			if (action.HasValue)
				Utilities.MainSettings.PerformAction(action.Value);
		}

		/// <summary>
		/// Event that handles the keyboard input
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnKeyDown(object sender, KeyEventArgs e) {
			MediaAction? action = null;
			bool controlDown = (Keyboard.Modifiers & ModifierKeys.Control) == ModifierKeys.Control;
			bool shiftDown = (Keyboard.Modifiers & ModifierKeys.Shift) == ModifierKeys.Shift;
			if (controlDown && e.Key == Key.I) {
				action = MediaAction.ToggleMDI;
			} else if (controlDown && e.Key == Key.R) {
				action = MediaAction.Restart;
			} else if (controlDown && e.Key == Key.T) {
				Utilities.MainSettings.LibraryCurrent.ToggleRepeat(!shiftDown);
			} else if (controlDown && e.Key == Key.O) {
				Utilities.MainSettings.LibraryCurrent.ToggleMediaOrders(!shiftDown);
			} else if (controlDown && e.Key == Key.H) {
				action = MediaAction.ToggleShuffle;
			}
			if (action.HasValue)
				Utilities.MainSettings.PerformAction(action.Value);
		}

		/// <summary>
		/// Event that handles when the window is closing
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnClosing(object sender, CancelEventArgs e) {
			if (Utilities.MainProcessManager.IsProcessActive && 
				!mCubedError.ShowConfirm("There is currently one or more processes running. Closing the application now may corrupt data. Continue?")) {
				e.Cancel = true;
			} else {
				if (Utilities.MainSettings.ShowMini) {
					Utilities.MainSettings.MiniLocation = new Point(Left / SystemParameters.VirtualScreenWidth, Top / SystemParameters.VirtualScreenHeight);
				} else {
					Utilities.MainSettings.MiniLocation = null;
				}
				Utilities.MainSettings.Save();
				GlobalKeyboardHook.Dispose();
			}
		}

		/// <summary>
		/// Event that handles when a log has been sent
		/// </summary>
		/// <param name="log">The log that has been sent</param>
		private void OnLogSent(Log log) {
			if (Dispatcher.CheckAccess()) {
				mCubedError.ShowDisplay(log.ToStringMessageOnly());
			} else {
				Dispatcher.Invoke(new Action<Log>(OnLogSent), log);
			}
		}

		/// <summary>
		/// Update the window state and positioning to accommodate for the change in showing the mini/full player
		/// </summary>
		private void OnShowMiniChanged() {
			// Show the mini player fixing the window state and positioning
			if (Utilities.MainSettings.ShowMini) {
				_fullState = WindowState;
				_fullPos = RestoreBounds;
				if (_fullPos == Rect.Empty)
					_fullPos = new Rect(double.NaN, double.NaN, ActualWidth == 0 ? Width : ActualWidth, ActualHeight == 0 ? Height : ActualHeight);
				Width = Height = Double.NaN;
				WindowState = WindowState.Normal;
				SizeToContent = SizeToContent.WidthAndHeight;
				ResizeMode = ResizeMode.NoResize;
				if (Utilities.MainSettings.MiniLocation.HasValue) {
					Top = Utilities.MainSettings.MiniLocation.Value.Y * SystemParameters.VirtualScreenHeight;
					Left = Utilities.MainSettings.MiniLocation.Value.X * SystemParameters.VirtualScreenWidth;
					Utilities.MainSettings.MiniLocation = null;
				}
			}

			// Show the full player fixing the window state and positioning
			else {
				ResizeMode = ResizeMode.CanResize;
				SizeToContent = SizeToContent.Manual;
				WindowState = _fullState;
				Width = _fullPos.Width;
				Height = _fullPos.Height;
				if (_fullState == WindowState.Normal) {
					Top = _fullPos.Top;
					Left = _fullPos.Left;
				}
			}
		}

		/// <summary>
		/// Event that handles when the application should exit
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnClose(object sender, RoutedEventArgs e) {
			Close();
		}

		/// <summary>
		/// Event that handles when the library should be reloaded
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnLibraryReloaded(object sender, MouseButtonEventArgs e) {
			var element = sender as FrameworkElement;
			var library = element == null ? null : element.DataContext as Library;
			if (library != null) {
				library.Reload();
			}
		}

		/// <summary>
		/// Event that handles when the secondary window should be shown
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnShowSecondary(object sender, RoutedEventArgs e) {
			new mCubedSecondary { Owner = this }.Show();
		}

		#endregion
	}
}