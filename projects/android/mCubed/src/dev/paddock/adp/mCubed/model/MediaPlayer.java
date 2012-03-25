package dev.paddock.adp.mCubed.model;

import android.media.AudioManager;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;

import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.preferences.RepeatStatus;
import dev.paddock.adp.mCubed.services.PlaybackServer;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.Log;
import dev.paddock.adp.mCubed.utilities.PreferenceManager;
import dev.paddock.adp.mCubed.utilities.PropertyManager;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class MediaPlayer implements OnCompletionListener, OnErrorListener {
	// State members
	private static final int STATE_DEFAULT = 0;
	private static final int STATE_PREPARED = 1;
	private static final int STATE_STARTED = 2;
	private static final int STATE_PAUSED = 3;
	private static final int STATE_STOPPED = 4;
	private static final int STATE_COMPLETED = 5;
	
	// Media player members
	private static final MediaPlayer instance = new MediaPlayer();
	private android.media.MediaPlayer player;
	private int currentState = STATE_DEFAULT, seekLockCount, statusLockCount;
	private MediaPlayerState internalState;
	private MediaFile mediaFile;
	private MediaStatus status = MediaStatus.Stop;
	
	// Seek members
	private final Runnable seekTask = new Runnable() {
		@Override
		public void run() {
			Utilities.pushContext(App.getAppContext());
			try {
				if (player != null) {
					int seek = player.getCurrentPosition();
					if (seek != 0) {
						setSeek(seek, true);
					}
				}
			} finally {
				Utilities.popContext();
			}
		}
	};
	private final TimerTask seekTimer = new TimerTask(seekTask, 500L);
	private int seek, seekListenerCount;
	
	public static MediaPlayer getInstance() {
		return instance;
	}
	
	/**
	 * Prevents external instances of a MediaPlayer
	 */
	private MediaPlayer() { }
	
	public void open() {
		if (player == null) {
			player = new android.media.MediaPlayer();
			player.setLooping(false);
			player.setOnCompletionListener(this);
			player.setOnErrorListener(this);
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			if (!syncMediaFile(getMediaFile(), false)) {
				setMediaFile(null, false);
			}
		}
	}
	
	public void close() {
		close(true);
	}
	
	public void close(boolean releaseFile) {
		seekTimer.stop();
		if (player != null) {
			player.release();
		}
		player = null;
		if (releaseFile) {
			setMediaFile(null, false);
		}
		setStatus(MediaStatus.Stop, false);
		currentState = STATE_DEFAULT;
		seek = 0;
	}
	
	private void setDataSourceInternal(String dataSource) {
		player.reset();
		try {
			player.setDataSource(dataSource);
			currentState = STATE_STOPPED;
			prepareInternal();
			setSeek(0, true);
		} catch (Exception e) {
			Log.e(e);
		}
	}
	
	private void prepareInternal() {
		if (currentState != STATE_PREPARED) {
			if (currentState != STATE_STOPPED) {
				stopInternal();
			}
			try {
				player.prepare();
				currentState = STATE_PREPARED;
			} catch (Exception e) {
				Log.e(e);
			}
		}
	}
	
	public void play() {
		setStatus(MediaStatus.Play);
	}
	
	private void playInternal() {
		if (currentState == STATE_STOPPED) {
			prepareInternal();
		}
		player.start();
		currentState = STATE_STARTED;
		seekTimer.start(true);
	}
	
	public void pause() {
		setStatus(MediaStatus.Pause);
	}
	
	private void pauseInternal() {
		if (currentState != STATE_STARTED || currentState != STATE_PAUSED) {
			playInternal();
		}
		player.pause();
		currentState = STATE_PAUSED;
		seekTimer.stop(true);
	}
	
	public void stop() {
		setStatus(MediaStatus.Stop);
	}
	
	private void stopInternal() {
		player.stop();
		currentState = STATE_STOPPED;
		seekTimer.stop(true);
	}
	
	private void syncStatus() {
		open();
		if (status == MediaStatus.Play) {
			playInternal();
		} else if (status == MediaStatus.Pause) {
			pauseInternal();
		} else if (status == MediaStatus.Stop) {
			stopInternal();
		}
	}
	
	private boolean syncMediaFile(MediaFile mediaFile, boolean doClose) {
		// Close/open the media player for the new file
		if (mediaFile == null) {
			if (doClose) {
				close();
			}
			return false;
		} else {
			open();
			
			// Grab and set the new file location, or close the media player
			Uri fileLocation = mediaFile.getFileLocation();
			if (fileLocation == null) {
				if (doClose) {
					close();
				}
				return false;
			} else {
				// Update the data source
				setDataSourceInternal(fileLocation.toString());
			}
		}
		return true;
	}
	
	public boolean isPlaying() {
		return getStatus() == MediaStatus.Play;
	}
	
	public MediaStatus getStatus() {
		return status;
	}
	
	public boolean isSetStatusLocked() {
		return statusLockCount > 0;
	}
	
	public void setStatus(MediaStatus status) {
		setStatus(status, true);
	}
	
	private void setStatus(MediaStatus status, boolean doSync) {
		if (!isSetStatusLocked() && status != null && this.status != status) {
			NotificationArgs args = new NotificationArgs(this, "Status", this.status, status);
			PropertyManager.notifyPropertyChanging(this, "Status", args);
			this.status = status;
			if (doSync) {
				syncStatus();
			}
			PlaybackServer.propertyChanged(0, Schema.PROP_PB_STATUS, this.status);
			PropertyManager.notifyPropertyChanged(this, "Status", args);
		}
	}
	
	public void registerSeekListener() {
		seekListenerCount++;
	}
	
	public void unregisterSeekListener() {
		seekListenerCount--;
		if (seekListenerCount < 0) {
			seekListenerCount = 0;
		}
	}
	
	public void unregisterAllSeekListeners() {
		seekListenerCount = 0;
	}
	
	/**
	 * Returns the current duration of the loaded song in milliseconds.
	 * @return The current duration of the loaded song in milliseconds.
	 */
	public int getDuration() {
		if (player == null || mediaFile == null || currentState == STATE_DEFAULT || currentState == STATE_STOPPED) {
			return 0;
		}
		return player.getDuration();
	}
	
	/**
	 * Returns the current position in the song in milliseconds.
	 * @return The current position in the song in milliseconds.
	 */
	public int getSeek() {
		return seek;
	}
	
	public boolean isSetSeekLocked() {
		return seekLockCount > 0;
	}
	
	public void setSeek(int ms) {
		setSeek(ms, false);
	}
	
	private void setSeek(int ms, boolean fromAndroidPlayer) {
		if (fromAndroidPlayer) {
			if (seek != ms) {
				// Send property changing
				NotificationArgs args = new NotificationArgs(this, "Seek", this.seek, ms);
				PropertyManager.notifyPropertyChanging(this, "Seek", args);
				
				// Change the property
				seek = ms;
				
				// Send property changed
				PropertyManager.notifyPropertyChanged(this, "Seek", args);
				if (seekListenerCount > 0) {
					PlaybackServer.propertyChanged(0, Schema.PROP_PB_SEEK, seek);
				}
			}
		} else if (!isSetSeekLocked()) {
			open();
			if (currentState == STATE_STOPPED) {
				prepareInternal();
			}
			player.seekTo(ms);
			setSeek(player.getCurrentPosition(), true);
		}
	}
	
	public MediaFile getMediaFile() {
		return mediaFile;
	}
	
	public void setMediaFile(MediaFile mediaFile) {
		setMediaFile(mediaFile, true);
	}
	
	private void setMediaFile(MediaFile mediaFile, boolean doClose) {
		// Ensure the file changed and the location can be loaded
		if (this.mediaFile != mediaFile) {
			// Set the current media file to no longer be flagged as playing
			if (this.mediaFile != null) {
				this.mediaFile.setPlaying(false);
			}
			
			// Send property changing
			NotificationArgs args = new NotificationArgs(this, "MediaFile", this.mediaFile, mediaFile);
			PropertyManager.notifyPropertyChanging(this, "MediaFile", args);
			this.mediaFile = mediaFile;
			long id = 0L;
			
			// Sync the media player to the new file
			if (syncMediaFile(mediaFile, doClose)) {
				this.mediaFile.setPlaying(true);
				id = this.mediaFile.getID();
				syncStatus();
			}
			
			// Send property changed
			PropertyManager.notifyPropertyChanged(this, "MediaFile", args);
			PlaybackServer.propertyChanged(0, Schema.PROP_PB_ID, id);
		} else if (mediaFile != null) {
			setSeek(0);
		}
	}
	
	public MediaPlayerState getMediaPlayerStateWithLocks(boolean acquireSeek, boolean acquireStatus, boolean doPause) {
		return getMediaPlayerState(acquireSeek, acquireSeek, acquireStatus, acquireStatus, doPause);
	}
	
	public MediaPlayerState getMediaPlayerState(boolean acquireSeek, boolean acquireStatus, boolean doPause) {
		return getMediaPlayerState(acquireSeek, false, acquireStatus, false, doPause);
	}
	
	public MediaPlayerState getMediaPlayerState(boolean acquireSeek, boolean lockSeek, boolean acquireStatus, boolean lockStatus, boolean doPause) {
		int seek = 0;
		MediaStatus status = null;
		if (acquireSeek) {
			seek = internalState == null ? getSeek() : internalState.getSeek();
		}
		if (acquireStatus) {
			status = internalState == null ? getStatus() : internalState.getStatus();
		}
		if (doPause && this.status == MediaStatus.Play) {
			pause();
		}
		if (lockSeek) {
			seekLockCount++;
		}
		if (lockStatus) {
			statusLockCount++;
		}
		return new MediaPlayerState(seek, acquireSeek, lockSeek, status, acquireStatus, lockStatus);
	}
	
	public void setMediaPlayerState(MediaPlayerState state) {
		if (state != null) {
			if (state.isSeekLockAcquired() && isSetSeekLocked()) {
				seekLockCount--;
			}
			if (state.isStatusLockAcquired() && isSetStatusLocked()) {
				statusLockCount--;
			}
			if (state.isSeekValueAcquired()) {
				setSeek(state.getSeek());
			}
			if (state.isStatusValueAcquired()) {
				MediaStatus status = state.getStatus();
				if (status != MediaStatus.Play || PreferenceManager.getSettingBoolean(R.string.pref_resume_automatically)) {
					setStatus(status);
				}
			}
			internalState = null;
		}
	}

	@Override
	public void onCompletion(android.media.MediaPlayer player) {
		Utilities.pushContext(App.getAppContext());
		try {
			if (this.player == player) {
				currentState = STATE_COMPLETED;
				RepeatStatus repeat = PreferenceManager.getSettingEnum(RepeatStatus.class, R.string.pref_repeat_status);
				if (repeat == RepeatStatus.RepeatSong) {
					syncStatus();
				} else {
					App.getNowPlaying().next();
					if (currentState == STATE_COMPLETED) {
						syncStatus();
					}
					if (PreferenceManager.getSettingBoolean(R.string.pref_light_up_screen)) {
						Utilities.turnScreenOn(5000);
					}
				}
			}
		} finally {
			Utilities.popContext();
		}
	}

	@Override
	public boolean onError(android.media.MediaPlayer player, int what, int extra) {
		Utilities.pushContext(App.getAppContext());
		try {
			if (this.player == player) {
				String error = String.format("MediaPlayer error [What=%d, Extra=%d]", what, extra);
				if (what == android.media.MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
					Log.i(error);
					internalState = getMediaPlayerState(true, true, false);
					close(false);
					Log.i(error);
				} else {
					Log.e(error);
				}
			}
			return true;
		} finally {
			Utilities.popContext();
		}
	}
}