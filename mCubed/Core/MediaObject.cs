using System;
using System.ComponentModel;
using System.Timers;
using System.Windows.Media;

namespace mCubed.Core {
	public class MediaObject : IExternalNotifyPropertyChanged, IExternalNotifyPropertyChanging, IDisposable {
		#region MediaObjectState

		public class MediaObjectState {
			public string Path { get; set; }
			public double Progress { get; set; }
			public MediaState State { get; set; }
		}

		#endregion

		#region Data Store

		private double _balance;
		private TimeSpan _length;
		private string _path;
		private double _playbackSpeed = 1;
		private MediaPlayer _player = new MediaPlayer { Volume = 1 };
		private TimeSpan _position;
		private MediaObjectState _savedState;
		private double _seekValue;
		private MediaState _state = MediaState.Stop;
		private Timer _timer = new Timer(500);
		private double _volume = 1;

		#endregion

		#region Bindable Properties

		/// <summary>
		/// Get/set the balance of the left and right speakers between -1 and 1 [Bindable]
		/// </summary>
		[MetaData("The balance of the media playback for the library.")]
		public double Balance {
			get { return _balance; }
			set { this.SetAndNotify(ref _balance, value, null, OnBalanceChanged, "Balance"); }
		}

		/// <summary>
		/// Get the length/duration of the current loaded media [Bindable]
		/// </summary>
		public TimeSpan Length {
			get { return _length; }
			private set { this.SetAndNotify(ref _length, value, "Length", "LengthString", "Progress"); }
		}

		/// <summary>
		/// Get the length/duration of the current loaded media as a human-readable string [Bindable]
		/// </summary>
		[MetaData("The length of the current media using the format that is used throughout the application.", Formula = "Length")]
		public string LengthString { get { return Length.Format(); } }

		/// <summary>
		/// Get/set the filepath of the media to load [Bindable]
		/// </summary>
		public string Path {
			get { return _path; }
			set { this.SetAndNotify(ref _path, value, null, OnPathChanged, "Path"); }
		}

		/// <summary>
		/// Get/set the playback speed ratio of the media between 0 and infinity where 1 is the normal speed [Bindable]
		/// </summary>
		[MetaData("The playback speed of the media for the library.")]
		public double PlaybackSpeed {
			get { return _playbackSpeed; }
			set { this.SetAndNotify(ref _playbackSpeed, value, null, OnPlaybackSpeedChanged, "PlaybackSpeed"); }
		}

		/// <summary>
		/// Get the current position of the media [Bindable]
		/// </summary>
		public TimeSpan Position {
			get { return _position; }
			private set { this.SetAndNotify(ref _position, value, "Position", "PositionString", "Progress"); }
		}

		/// <summary>
		/// Get the current position of the media as a human-readable string [Bindable]
		/// </summary>
		[MetaData("The current playback position of the current media using the format that is used throughout the application.", Formula = "Position")]
		public string PositionString { get { return Position.Format(); } }

		/// <summary>
		/// Get the current progress of the media as a percentage between 0 and 1 [Bindable]
		/// </summary>
		public double Progress { get { return Length.TotalMilliseconds == 0 ? 0 : Position.TotalMilliseconds / Length.TotalMilliseconds; ; } }

		/// <summary>
		/// Get/set the current state of the media file [Bindable]
		/// </summary>
		[MetaData("The playback state of the current media (Play/Pause/Stop).")]
		public MediaState State {
			get { return _state; }
			set { OnStateChanged(value); }
		}

		/// <summary>
		/// Get/set the volume of the media object between 0 and 1 [Bindable]
		/// </summary>
		[MetaData("The volume level of the playback for the library.")]
		public double Volume {
			get { return _volume; }
			set { this.SetAndNotify(ref _volume, value, null, OnVolumeChanged, "Volume"); }
		}

		#endregion

		#region Events

		public event Action MediaEnded;
		public event Action<string> MediaFailed;

		#endregion

		#region Constructor

		public MediaObject() {
			// Set up event handlers
			_timer.Elapsed += new ElapsedEventHandler(OnTimerElapsed);
			_player.MediaEnded += new EventHandler(OnMediaEnded);
			_player.MediaFailed += new EventHandler<ExceptionEventArgs>(OnMediaFailed);
			_player.MediaOpened += new EventHandler(OnMediaOpened);
		}

		#endregion

		#region Event Handlers

		/// <summary>
		/// Event that handles when the media has ended, notifying all those who want to know
		/// </summary>
		private void OnMediaEnded() {
			if (MediaEnded != null)
				MediaEnded();
		}

		/// <summary>
		/// Event that handles when the media has ended, notifying all those who want to know
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnMediaEnded(object sender, EventArgs e) {
			OnMediaEnded();
		}

		/// <summary>
		/// Event that handles when the media playback has failed
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnMediaFailed(object sender, ExceptionEventArgs e) {
			if (MediaFailed != null)
				MediaFailed(e.ErrorException.Message);
		}

		/// <summary>
		/// Event that handles when the media has opened
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnMediaOpened(object sender, EventArgs e) {
			var timespan =  _player.NaturalDuration;
			if (timespan.HasTimeSpan)
				Length = timespan.TimeSpan;
			if (_seekValue != 0) {
				Seek(_seekValue);
				_seekValue = 0;
			}
		}

		/// <summary>
		/// Event that handles when the media file path has changed
		/// </summary>
		private void OnPathChanged() {
			// Load the media through the UI thread
			PerformAction(delegate(MediaPlayer player)
			{
				if (string.IsNullOrEmpty(Path)) {
					player.Close();
					Length = TimeSpan.FromMilliseconds(0);
				} else {
					player.Open(new Uri(Path));
				}
			});

			// Invoke the event handlers
			OnBalanceChanged();
			OnPlaybackSpeedChanged();
			OnVolumeChanged();
			State = (string.IsNullOrEmpty(Path)) ? MediaState.Stop : State;
			UpdatePosition();
		}

		/// <summary>
		/// Event that handles when state of the media object has changed
		/// </summary>
		/// <param name="newState">The media state the file should be updated to</param>
		private void OnStateChanged(MediaState newState) {
			// Check if there's media first
			if (string.IsNullOrEmpty(Path) && newState != MediaState.Stop)
				return;

			// Update the state
			_state = newState;

			// Retrieve the new state details
			Action<MediaPlayer> action = null;
			bool startTimer = false;
			switch (State) {
				case MediaState.Pause:
					action = player => player.Pause();
					break;
				case MediaState.Stop:
					action = player => player.Stop();
					break;
				default:
					action = player => player.Play();
					startTimer = true;
					break;
			}

			// Update to the new state
			if (action != null)
				PerformAction(action);

			// Update the timer
			_timer.Enabled = startTimer;
			if (!startTimer)
				UpdatePosition();

			// Notify
			this.OnPropertyChanged("State");
		}

		/// <summary>
		/// Event that handles when the timer has elapsed
		/// </summary>
		/// <param name="sender">The sender object</param>
		/// <param name="e">The event arguments</param>
		private void OnTimerElapsed(object sender, ElapsedEventArgs e) {
			UpdatePosition();
		}

		/// <summary>
		/// Event that handles when the balance changed
		/// </summary>
		private void OnBalanceChanged() {
			PerformAction(player => player.Balance = Balance);
		}

		/// <summary>
		/// Event that handles when the playback speed changed
		/// </summary>
		private void OnPlaybackSpeedChanged() {
			PerformAction(player => player.SpeedRatio = PlaybackSpeed);
		}

		/// <summary>
		/// Event that handles when the volume changed
		/// </summary>
		private void OnVolumeChanged() {
			PerformAction(player => player.Volume = Volume);
		}

		#endregion

		#region Update/Seek Members

		/// <summary>
		/// Seek to a position in the media file
		/// </summary>
		/// <param name="value">The position in the file to seek to</param>
		public void Seek(double value) {
			// Check if there is a source
			if (value >= 1) {
				OnMediaEnded();
			} else if (value < 0) {
				Seek(0);
			} else if (!string.IsNullOrEmpty(Path)) {
				PerformAction(player => player.Position = TimeSpan.FromMilliseconds(value * Length.TotalMilliseconds));
				UpdatePosition();
			}
		}

		/// <summary>
		/// Update the media position information
		/// </summary>
		private void UpdatePosition() {
			// Check if there is a source, and update the position accordingly
			if (!string.IsNullOrEmpty(Path))
				PerformAction(player => Position = player.Position);
			else
				Position = TimeSpan.FromMilliseconds(0);
		}

		#endregion

		#region State Preservation Members

		/// <summary>
		/// Restore the state of the media file that was being played
		/// </summary>
		public void RestoreState() {
			// Restore the saved state
			RestoreState(_savedState);

			// Clear the saved state
			_savedState = null;
		}

		/// <summary>
		/// Restore the state of the media file that was previously unloaded
		/// </summary>
		/// <param name="state">The state of the file that was previously unloaded</param>
		public void RestoreState(MediaObjectState state) {
			// Check if the state has a value
			if (state != null) {
				RestoreState(state.Path, state.State, state.Progress);
			}
		}

		/// <summary>
		/// Restore the state of the media file that is specified
		/// </summary>
		/// <param name="path">The path to the media file to restore</param>
		/// <param name="state">The state of which to restore it to</param>
		/// <param name="progress">The progress of which to seek in the file</param>
		public void RestoreState(string path, MediaState state, double progress) {
			// Seek appropriately
			bool postSeek = true;
			if (string.IsNullOrEmpty(Path)) {
				_seekValue = progress;
				postSeek = false;
			}

			// Restore the path and state
			Path = path;
			State = state;

			// Post seek appropriately
			if (postSeek) {
				Seek(progress);
			}
		}

		/// <summary>
		/// Save the state of the media file that is currently being played
		/// </summary>
		public void SaveState() {
			_savedState = UnlockFile();
		}

		/// <summary>
		/// Unlocks the file that is currently playing so it can be reloaded
		/// </summary>
		/// <returns>The state of the file that is currently playing so it can be reloaded</returns>
		public MediaObjectState UnlockFile() {
			return UnlockFile(Path);
		}

		/// <summary>
		/// Unlock the file returning the current state of the file so it can be reloaded
		/// </summary>
		/// <param name="path">The file path that will be unlocked</param>
		/// <returns>The state of the file so it can be reloaded or null if the file is not locked</returns>
		public MediaObjectState UnlockFile(string path) {
			// Check if the file is locked
			if (path != Path)
				return null;

			// Save the information
			MediaObjectState state = new MediaObjectState { Path = Path, Progress = Progress, State = State };

			// Unlock the file, and wait for the unlock
			Path = null;
			System.Threading.Thread.Sleep(50);

			// Return the information
			return state;
		}

		#endregion

		#region UI Thread Members

		/// <summary>
		/// Perform an action on the media player through the UI thread
		/// </summary>
		/// <param name="action">The action that needs to be performed</param>
		private void PerformAction(Action<MediaPlayer> action) {
			if (_player.Dispatcher.CheckAccess()) {
				action(_player);
			} else {
				_player.Dispatcher.Invoke(action, _player);
			}
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
		/// Dispose of the media object appropriately
		/// </summary>
		public void Dispose() {
			// Unsubscribe others from its events
			PropertyChanged = null;
			PropertyChanging = null;
			MediaEnded = null;
			MediaFailed = null;

			// Unsubscribe from delegates
			_timer.Elapsed -= new ElapsedEventHandler(OnTimerElapsed);
			_player.MediaEnded -= new EventHandler(OnMediaEnded);
			_player.MediaFailed -= new EventHandler<ExceptionEventArgs>(OnMediaFailed);
			_player.MediaOpened -= new EventHandler(OnMediaOpened);

			// Dispose all disposable references it created
			_timer.Dispose();
		}

		#endregion
	}
}