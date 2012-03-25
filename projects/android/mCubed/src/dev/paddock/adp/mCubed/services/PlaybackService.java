package dev.paddock.adp.mCubed.services;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.activities.LibraryActivity;
import dev.paddock.adp.mCubed.activities.OverlayActivity;
import dev.paddock.adp.mCubed.model.MediaFile;
import dev.paddock.adp.mCubed.model.MediaStatus;
import dev.paddock.adp.mCubed.model.OutputMode;
import dev.paddock.adp.mCubed.preferences.NotificationVisibility;
import dev.paddock.adp.mCubed.preferences.PlayModeEnum;
import dev.paddock.adp.mCubed.receivers.ClientReceiver;
import dev.paddock.adp.mCubed.receivers.IReceiver;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.Log;
import dev.paddock.adp.mCubed.utilities.PreferenceManager;
import dev.paddock.adp.mCubed.utilities.Utilities;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.IBinder;

public class PlaybackService extends Service {
	private Runnable initCallback;
	private IServerCallback serverCallback;
	private BroadcastReceiver clientReceiver;
	private OnSharedPreferenceChangeListener preferenceListener;
	private final Queue<Intent> queuedIntents = new LinkedList<Intent>();
	private static final List<BroadcastReceiver> receivers = new ArrayList<BroadcastReceiver>();
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		Utilities.pushContext(this);
		try {
			super.onStart(intent, startId);
			if (App.getMount().isMounted()) {
				if (App.isInitialized()) {
					PlaybackServer.handleIntent(intent, getServerCallback(), false);
				} else {
					queuedIntents.offer(intent);
				}
			}
		} finally {
			Utilities.popContext();
		}
	}
	
	@Override
	public void onCreate() {
		Utilities.pushContext(this);
		try {
			// Start the service
			Log.i("PlaybackService started");
			App.setIsServiceRunning(true);
			startForeground(Schema.NOTIF_PLAYING_MEDIA, new Notification());
			
			// Register the various receivers
			receivers.clear();
			receivers.add(App.getHeadset());
			receivers.add(App.getMount());
			receivers.add(App.getPhoneState());
			receivers.add(getClientReceiver());
			for (BroadcastReceiver receiver : receivers) {
				if (receiver instanceof IReceiver) {
					IReceiver rec = (IReceiver)receiver;
					IntentFilter filter = rec.getIntentFilter();
					if (filter != null) {
						registerReceiver(receiver, filter);
					}
				}
			}
			
			// Register the property changed listener
			PreferenceManager.registerPreferenceChangeListener(getPreferenceListener());
			
			// Setup the notification
			cancelNotification(null);
			updateNotification(true);
			
			// Setup other preferences
			updatePlayMode();
			Log.setFileLoggingEnabled(PreferenceManager.getSettingBoolean(R.string.pref_record_logs));
			
			// Initialize
			App.addInitCallback(getInitCallback());
			App.initialize();
		} finally {
			Utilities.popContext();
		}
	}
	
	@Override
	public void onDestroy() {
		Utilities.pushContext(this);
		try {
			// De-initialize
			App.deinitialize();
			App.removeInitCallback(getInitCallback());
			
			// Unregister the property changed listener
			if (preferenceListener != null) {
				PreferenceManager.unregisterPreferenceChangeListener(preferenceListener);
			}
			
			// Unregister the various receiver
			for (BroadcastReceiver receiver : receivers) {
				if (receiver instanceof IReceiver) {
					IReceiver rec = (IReceiver)receiver;
					IntentFilter filter = rec.getIntentFilter();
					if (filter != null) {
						unregisterReceiver(receiver);
					}
				}
			}
			
			// Stop the service
			App.setIsServiceRunning(false);
			Log.i("PlaybackService stopped");
		} finally {
			Utilities.popContext();
		}
	}
	
	private void cancelNotification(NotificationManager manager) {
		if (manager == null) {
			manager = App.getSystemService(NotificationManager.class, NOTIFICATION_SERVICE);
		}
		manager.cancel(Schema.TAG, Schema.NOTIF_PLAYING_MEDIA);
	}
	
	private void updateNotification(boolean doTicker) {
		// Grab some data
		NotificationManager manager = App.getSystemService(NotificationManager.class, NOTIFICATION_SERVICE);
		NotificationVisibility visibility = PreferenceManager.getSettingEnum(NotificationVisibility.class, R.string.pref_notification_visibility);
		MediaFile media = App.getPlayingMedia();
		boolean isPlaying = App.getPlayer().isPlaying();
		
		// Cancel the existing one to signal the ticker
		if (doTicker) {
			cancelNotification(manager);
		}
		
		// Make sure we have media
		if (media == null) {
			cancelNotification(manager);
			return;
		}
		
		// Make sure the notification visibility isn't never
		if (visibility == NotificationVisibility.Never) {
			cancelNotification(manager);
			return;
		}
		
		// If we only show the notification when playing, make sure we're playing
		if (visibility == NotificationVisibility.OnlyWhilePlaying && !isPlaying) {
			cancelNotification(manager);
			return;
		}
		
		// Determine which class to open when clicked
		Class<?> activityClass = LibraryActivity.class;
		if (PreferenceManager.getSettingBoolean(R.string.pref_open_overlay_player)) {
			activityClass = OverlayActivity.class;
		}
		
		// Create the intent
		Intent intent = new Intent(this, activityClass);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
		
		// Setup the notification details
		String title = media.getTitle();
		String content = media.getArtist();
		String ticker = content + " - " + title;
		int icon = isPlaying ? R.drawable.notify_play : R.drawable.notify_pause;
		
		// Create and show the notification
		Notification notification = new Notification(icon, ticker, System.currentTimeMillis());
		notification.setLatestEventInfo(this, title, content, pendingIntent);
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		manager.notify(Schema.TAG, Schema.NOTIF_PLAYING_MEDIA, notification);
	}
	
	private void updatePlayMode() {
		PlayModeEnum playMode = PreferenceManager.getSettingEnum(PlayModeEnum.class, R.string.pref_play_mode);
		boolean clearQueue = PreferenceManager.getSettingBoolean(R.string.pref_clear_queue_with_play_mode);
		App.getNowPlaying().resetPlayMode(playMode, clearQueue);
	}
	
	private IServerCallback getServerCallback() {
		if (serverCallback == null) {
			serverCallback = new IServerCallback() {
				@Override
				public void startService(int intentID) { }
				
				@Override
				public void stopService(int intentID) {
					PlaybackService.this.cancelNotification(null);
					PlaybackService.this.stopSelf();
				}
				
				@Override
				public void setSeekListener(int intentID, boolean isSeekListener) {
					if (isSeekListener) {
						App.getPlayer().registerSeekListener();
					} else {
						App.getPlayer().unregisterSeekListener();
					}
				}
				
				@Override
				public void setPlaybackStatus(int intentID, MediaStatus playbackStatus) {
					App.getPlayer().setStatus(playbackStatus);
				}
				
				@Override
				public void setPlaybackSeek(int intentID, int playbackSeek) {
					App.getPlayer().setSeek(playbackSeek);
				}

				@Override
				public void movePlaybackNext(int intentID) {
					App.movePlaybackNext();
				}

				@Override
				public void movePlaybackPrev(int intentID) {
					App.movePlaybackPrev();
				}
			};
		}
		return serverCallback;
	}
	
	private BroadcastReceiver getClientReceiver() {
		if (clientReceiver == null) {
			clientReceiver = new ClientReceiver(new ClientCallback() {
				@Override
				public void preferenceChanged(int intentID, String preferenceName) {
					if (preferenceName.equals(Utilities.getResourceString(R.string.pref_notification_visibility))) {
						PlaybackService.this.updateNotification(false);
					} else if (preferenceName.equals(Utilities.getResourceString(R.string.pref_open_overlay_player))) {
						PlaybackService.this.updateNotification(false);
					} else if (preferenceName.equals(Utilities.getResourceString(R.string.pref_play_mode))) {
						PlaybackService.this.updatePlayMode();
					} else if (preferenceName.equals(Utilities.getResourceString(R.string.pref_clear_queue_with_play_mode))) {
						PlaybackService.this.updatePlayMode();
					} else if (preferenceName.equals(Utilities.getResourceString(R.string.pref_volume_speaker))) {
						App.getOutput().updateVolume(OutputMode.Speaker);
					} else if (preferenceName.equals(Utilities.getResourceString(R.string.pref_volume_headphones))) {
						App.getOutput().updateVolume(OutputMode.Headphones);
					} else if (preferenceName.equals(Utilities.getResourceString(R.string.pref_volume_bluetooth))) {
						App.getOutput().updateVolume(OutputMode.Bluetooth);
					} else if (preferenceName.equals(Utilities.getResourceString(R.string.pref_record_logs))) {
						Log.setFileLoggingEnabled(PreferenceManager.getSettingBoolean(R.string.pref_record_logs));
					}
				}
				
				@Override
				public void propertyPlaybackIDChanged(long playbackID) {
					PlaybackService.this.updateNotification(true);
				}
				
				@Override
				public void propertyPlaybackStatusChanged(MediaStatus playbackStatus) {
					PlaybackService.this.updateNotification(false);
				}
				
				@Override
				public void propertyBlueoothChanged(boolean isBluetoothConnected) {
					if (isBluetoothConnected) {
						App.getOutput().updateOutputMode(OutputMode.Bluetooth);
					} else {
						App.getOutput().updateOutputMode();
					}
				}
				
				@Override
				public void propertyHeadphoneChanged(boolean isHeadphoneConnected) {
					if (isHeadphoneConnected) {
						App.getOutput().updateOutputMode(OutputMode.Headphones);
					} else {
						App.getOutput().updateOutputMode();
					}
				}
			}, false);
		}
		return clientReceiver;
	}
	
	private Runnable getInitCallback() {
		if (initCallback == null) {
			initCallback = new Runnable() {
				@Override
				public void run() {
					// Execute the queued intents
					while (!queuedIntents.isEmpty()) {
						Intent intent = queuedIntents.poll();
						PlaybackServer.handleIntent(intent, getServerCallback(), false);
					}
				}
			};
		}
		return initCallback;
	}
	
	private OnSharedPreferenceChangeListener getPreferenceListener() {
		if (preferenceListener == null) {
			preferenceListener = new OnSharedPreferenceChangeListener() {
				@Override
				public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
					Utilities.pushContext(PlaybackService.this);
					try {
						PlaybackServer.preferenceChanged(0, key);
					} finally {
						Utilities.popContext();
					}
				}
			};
		}
		return preferenceListener;
	}
}