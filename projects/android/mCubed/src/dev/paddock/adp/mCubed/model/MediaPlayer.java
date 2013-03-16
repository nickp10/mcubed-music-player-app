package dev.paddock.adp.mCubed.model;

import java.util.Locale;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
	
	// Volume members
	private static final float DUCK_VOLUME = 0.1f;
	private static final float FULL_VOLUME = 1.0f;
	
	// Media player members
	private static final MediaPlayer instance = new MediaPlayer();
	private final ReadWriteLock lock = new ReentrantReadWriteLock(true);
	private final Lock read = lock.readLock();
	private final Lock write = lock.writeLock();
	private android.media.MediaPlayer player;
	private int currentState = STATE_DEFAULT, seekLockCount, statusLockCount;
	private MediaPlayerState internalState;
	private MediaFile mediaFile;
	private MediaStatus status = MediaStatus.Pause;
	
	// Seek members
	private final Runnable seekTask = new Runnable() {
		@Override
		public void run() {
			Utilities.pushContext(App.getAppContext());
			try {
				int seek = 0;
				read.lock();
				try {
					if (player != null) {
						seek = player.getCurrentPosition();
					}
				} finally {
					read.unlock();
				}
				if (seek != 0) {
					setSeek(seek, true);
				}
			} finally {
				Utilities.popContext();
			}
		}
	};
	private final TimerTask seekTimer = new TimerTask(seekTask, 500L);
	private int seek;
	
	public static MediaPlayer getInstance() {
		return instance;
	}
	
	/**
	 * Prevents external instances of a MediaPlayer
	 */
	private MediaPlayer() { }
	
	public void open() {
		write.lock();
		try {
			if (player == null) {
				player = new android.media.MediaPlayer();
				player.setLooping(false);
				player.setOnCompletionListener(this);
				player.setOnErrorListener(this);
				player.setAudioStreamType(AudioManager.STREAM_MUSIC);
				if (App.getAudioFocusState() == AudioFocusState.AudioFocusDuck) {
					player.setVolume(DUCK_VOLUME, DUCK_VOLUME);
				} else {
					player.setVolume(FULL_VOLUME, FULL_VOLUME);
				}
				if (!syncMediaFile(getMediaFile(), false)) {
					setMediaFile(null, false);
				}
			}
		} finally {
			write.unlock();
		}
	}
	
	public void close() {
		close(true);
	}
	
	public void close(boolean releaseFile) {
		seekTimer.stop();
		write.lock();
		try {
			if (player != null) {
				player.release();
			}
			player = null;
			currentState = STATE_DEFAULT;
		} finally {
			write.unlock();
		}
		if (releaseFile) {
			setMediaFile(null, false);
		}
		setStatus(MediaStatus.Pause, false);
		seek = 0;
	}
	
	private void setDataSourceInternal(String dataSource) {
		write.lock();
		try {
			player.reset();
			try {
				player.setDataSource(dataSource);
				currentState = STATE_STOPPED;
				prepareInternal();
				setSeek(0, true);
			} catch (Exception e) {
				Log.e(e);
			}
		} finally {
			write.unlock();
		}
	}
	
	private void prepareInternal() {
		int state = getCurrentState();
		if (state != STATE_PREPARED) {
			if (state != STATE_STOPPED) {
				stopInternal();
			}
			write.lock();
			try {
				player.prepare();
				currentState = STATE_PREPARED;
			} catch (Exception e) {
				Log.e(e);
			} finally {
				write.unlock();
			}
		}
	}
	
	public void play() {
		setStatus(MediaStatus.Play);
	}
	
	private void playInternal() {
		int state = getCurrentState();
		if (state == STATE_STOPPED) {
			prepareInternal();
		}
		write.lock();
		try {
			player.start();
			currentState = STATE_STARTED;
		} finally {
			write.unlock();
		}
		seekTimer.start(true);
	}
	
	public void pause() {
		setStatus(MediaStatus.Pause);
	}
	
	private void pauseInternal() {
		int state = getCurrentState();
		if (state != STATE_STARTED || state != STATE_PAUSED) {
			playInternal();
		}
		write.lock();
		try {
			player.pause();
			currentState = STATE_PAUSED;
		} finally {
			write.unlock();
		}
		seekTimer.stop(true);
	}
	
	public void stop() {
		setStatus(MediaStatus.Stop);
	}
	
	private void stopInternal() {
		write.lock();
		try {
			player.stop();
			currentState = STATE_STOPPED;
		} finally {
			write.unlock();
		}
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
	
	public int getCurrentState() {
		int state = STATE_DEFAULT;
		read.lock();
		try {
			state = currentState;
		} finally {
			read.unlock();
		}
		return state;
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
			PropertyManager.notifyPropertyChanging(args);
			this.status = status;
			if (doSync) {
				syncStatus();
			}
			PlaybackServer.propertyChanged(0, Schema.PROP_PB_STATUS, this.status);
			PropertyManager.notifyPropertyChanged(args);
		}
	}
	
	/**
	 * Returns the current duration of the loaded song in milliseconds.
	 * @return The current duration of the loaded song in milliseconds.
	 */
	public int getDuration() {
		read.lock();
		try {
			int state = getCurrentState();
			if (player == null || mediaFile == null || state == STATE_DEFAULT || state == STATE_STOPPED) {
				return 0;
			}
			return player.getDuration();
		} finally {
			read.unlock();
		}
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
				PropertyManager.notifyPropertyChanging(args);
				
				// Change the property
				seek = ms;
				
				// Send property changed
				PropertyManager.notifyPropertyChanged(args);
				PlaybackServer.propertyChanged(0, Schema.PROP_PB_SEEK, seek, Schema.I_MCUBED_SEEK);
			}
		} else if (!isSetSeekLocked()) {
			open();
			if (getCurrentState() == STATE_STOPPED) {
				prepareInternal();
			}
			write.lock();
			try {
				player.seekTo(ms);
			} finally {
				write.unlock();
			}
			int seek = 0;
			read.lock();
			try {
				seek = player.getCurrentPosition();
			} finally {
				read.unlock();
			}
			setSeek(seek, true);
		}
	}
	
	private void adjustVolume(float volume) {
		write.lock();
		try {
			if (player != null) {
				player.setVolume(volume, volume);
			}
		} finally {
			write.unlock();
		}
	}
	
	public void adjustVolumeDuck() {
		adjustVolume(DUCK_VOLUME);
	}
	
	public void adjustVolumeFull() {
		adjustVolume(FULL_VOLUME);
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
			PropertyManager.notifyPropertyChanging(args);
			this.mediaFile = mediaFile;
			long id = 0L;
			
			// Sync the media player to the new file
			if (syncMediaFile(mediaFile, doClose)) {
				this.mediaFile.setPlaying(true);
				id = this.mediaFile.getID();
				syncStatus();
			}
			
			// Send property changed
			PropertyManager.notifyPropertyChanged(args);
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
					if (getCurrentState() == STATE_COMPLETED) {
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
				String error = String.format(Locale.US, "MediaPlayer error [What=%d, Extra=%d]", what, extra);
				if (what == android.media.MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
					Log.i(error);
					internalState = getMediaPlayerState(true, true, false);
					close(false);
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