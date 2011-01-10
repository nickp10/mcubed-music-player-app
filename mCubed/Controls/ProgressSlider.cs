using System;
using System.ComponentModel;
using System.Linq;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Controls.Primitives;
using System.Windows.Data;
using System.Windows.Input;
using System.Windows.Media;
using mCubed.Core;

namespace mCubed.Controls {
	public class ProgressSlider : Slider, IExternalNotifyPropertyChanged, IExternalNotifyPropertyChanging {
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

		#region Dependency Property: IsSeeking (Read-Only)

		/// <summary>
		/// Event that handles when the seeking status changed 
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private static void OnSeekingChanged(DependencyObject sender, DependencyPropertyChangedEventArgs e) {
			var slider = sender as ProgressSlider;
			if (slider != null)
				slider.OnSeekingChanged();
		}

		private static readonly DependencyPropertyKey IsSeekingProperty =
			DependencyProperty.RegisterReadOnly("IsSeeking", typeof(bool), typeof(ProgressSlider), new PropertyMetadata(false, new PropertyChangedCallback(OnSeekingChanged)));
		public bool IsSeeking {
			get { return (bool)GetValue(IsSeekingProperty.DependencyProperty); }
			private set { SetValue(IsSeekingProperty, value); }
		}

		#endregion

		#region Dependency Property: ProgressMaximum

		public static readonly DependencyProperty ProgressMaximumProperty =
			DependencyProperty.Register("ProgressMaximum", typeof(double), typeof(ProgressSlider), new UIPropertyMetadata(0.0));
		public double ProgressMaximum {
			get { return (double)GetValue(ProgressMaximumProperty); }
			set { SetValue(ProgressMaximumProperty, value); }
		}

		#endregion

		#region Dependency Property: ProgressValue

		public static readonly DependencyProperty ProgressValueProperty =
			DependencyProperty.Register("ProgressValue", typeof(double), typeof(ProgressSlider), new UIPropertyMetadata(0.0));
		public double ProgressValue {
			get { return (double)GetValue(ProgressValueProperty); }
			set { SetValue(ProgressValueProperty, value); }
		}

		#endregion

		#region Dependency Property: SeekValue (Read-Only)

		private static readonly DependencyPropertyKey SeekValueProperty =
			DependencyProperty.RegisterReadOnly("SeekValue", typeof(double), typeof(ProgressSlider), new PropertyMetadata(0.0));
		public double SeekValue {
			get { return (double)GetValue(SeekValueProperty.DependencyProperty); }
			private set { SetValue(SeekValueProperty, value); }
		}

		#endregion

		#region Data Store

		private ToolTip _sliderToolTip;

		#endregion

		#region Events

		public event Action<double> Seek;

		#endregion

		#region Constructor

		public ProgressSlider() {
			// Set up the resources
			Resources.Source = new Uri("pack://application:,,,/Resources/Resources.xaml");

			// Set the background to a progress bar
			ProgressBar sliderProgress = new ProgressBar { Minimum = 0, Maximum = 1, DataContext = this };
			sliderProgress.SetBinding(ProgressBar.WidthProperty, new Binding { Path = new PropertyPath(Slider.ActualWidthProperty) });
			sliderProgress.SetBinding(ProgressBar.HeightProperty, new Binding { Path = new PropertyPath(Slider.ActualHeightProperty) });
			sliderProgress.SetBinding(ProgressBar.ValueProperty, new Binding { Path = new PropertyPath(Slider.ValueProperty) });
			Background = new VisualBrush { Visual = sliderProgress };

			// Set up the tooltip
			var binding = new MultiBinding { Converter = new ProgressSliderToolTipConverter() };
			binding.Bindings.Add(new Binding { Source = this, Path = new PropertyPath("SeekValue") });
			binding.Bindings.Add(new Binding { Source = this, Path = new PropertyPath("ProgressMaximum") });
			_sliderToolTip = new ToolTip { PlacementTarget = this, Placement = PlacementMode.Relative };
			_sliderToolTip.SetBinding(System.Windows.Controls.ToolTip.ContentProperty, binding);

			// Set up properties
			Minimum = 0;
			Maximum = 1;

			// Set up event handlers
			AddHandler(Slider.PreviewMouseLeftButtonDownEvent, new MouseButtonEventHandler(OnMouseClickCaptured), true);
			AddHandler(Slider.PreviewMouseLeftButtonUpEvent, new MouseButtonEventHandler(OnMouseClickReleased), true);
			AddHandler(Slider.PreviewMouseMoveEvent, new MouseEventHandler(OnMouseMoved), true);
			AddHandler(Slider.PreviewMouseRightButtonDownEvent, new MouseButtonEventHandler(OnMouseClickReleased), true);
			AddHandler(Slider.PreviewKeyDownEvent, new KeyEventHandler(OnKeyDown));
			OnSeekingChanged();
		}

		#endregion

		#region Event Handlers

		/// <summary>
		/// Event that handles when a key is pressed
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnKeyDown(object sender, KeyEventArgs e) {
			if ((Keyboard.Modifiers & ModifierKeys.Control) != ModifierKeys.Control) {
				int value = e.Key == Key.Left ? -1 : (e.Key == Key.Right ? 1 : 0);
				if (value != 0) {
					SeekTo(ProgressValue + (value * .05), true);
					e.Handled = true;
				}
			}
		}

		/// <summary>
		/// Event that handles when the slider has captured the mouse click
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnMouseClickCaptured(object sender, MouseButtonEventArgs e) {
			// Check if there is current media playing
			if (ProgressMaximum > 0) {
				// Start seeking
				IsSeeking = true;

				// Update properties
				_sliderToolTip.VerticalOffset = 0 - _sliderToolTip.ActualHeight;
				CaptureMouse();
			} else {
				e.Handled = true;
			}
		}

		/// <summary>
		/// Event that handles when the slider has released the mouse click
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnMouseClickReleased(object sender, MouseButtonEventArgs e) {
			// Check if we're seeking
			if (ProgressMaximum > 0 && IsSeeking) {
				// End seeking
				IsSeeking = false;

				// Seek, if necessary
				if (e.ChangedButton == MouseButton.Left)
					OnSeek();

				// Update properties
				ReleaseMouseCapture();
			}
		}

		/// <summary>
		/// Event that handles when the slider thumb has moved
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnMouseMoved(object sender, MouseEventArgs e) {
			// Check if we're seeking
			if (ProgressMaximum > 0 && IsSeeking) {
				// Update the seek value
				SeekTo(Mouse.GetPosition(this).X / ActualWidth, false);

				// Update properties
				_sliderToolTip.HorizontalOffset = (Value * ActualWidth) - (_sliderToolTip.ActualWidth * .5);
			}
		}

		/// <summary>
		/// Event that handles when the slider should seek to the new value
		/// </summary>
		private void OnSeek() {
			if (Seek != null)
				Seek(SeekValue);
		}

		/// <summary>
		/// Event that handles when the seeking status changed
		/// </summary>
		private void OnSeekingChanged() {
			// Update the value binding
			var binding = new Binding { Source = this, Mode = BindingMode.OneWay };
			if (IsSeeking) {
				binding.Path = new PropertyPath("SeekValue");
			} else {
				binding.Path = new PropertyPath("ProgressValue");
			}
			SetBinding(ProgressSlider.ValueProperty, binding);

			// Update the tooltip
			_sliderToolTip.IsOpen = IsSeeking;
		}

		#endregion

		#region Members

		/// <summary>
		/// Seek to the specified value
		/// </summary>
		/// <param name="value">The value to seek to</param>
		/// <param name="invokeSeek">True if the seek event should be invoked, or false otherwise</param>
		public void SeekTo(double value, bool invokeSeek) {
			// Update the seek value
			value = value > Maximum ? Maximum : value;
			value = value < Minimum ? Minimum : value;
			SeekValue = value;

			// Seek, if requested
			if (invokeSeek)
				OnSeek();
		}

		#endregion
	}
}