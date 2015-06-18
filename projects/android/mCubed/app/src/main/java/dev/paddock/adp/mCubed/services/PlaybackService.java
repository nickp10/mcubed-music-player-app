package dev.paddock.adp.mCubed.services;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.activities.LibraryActivity;
import dev.paddock.adp.mCubed.activities.OverlayActivity;
import dev.paddock.adp.mCubed.listeners.HeadsetListener;
import dev.paddock.adp.mCubed.listeners.IListener;
import dev.paddock.adp.mCubed.listeners.MediaAssociateListener;
import dev.paddock.adp.mCubed.listeners.MountListener;
import dev.paddock.adp.mCubed.listeners.PhoneStateListener;
import dev.paddock.adp.mCubed.model.AudioFocusState;
import dev.paddock.adp.mCubed.model.MediaFile;
import dev.paddock.adp.mCubed.model.MediaStatus;
import dev.paddock.adp.mCubed.model.OutputMode;
import dev.paddock.adp.mCubed.preferences.NotificationVisibility;
import dev.paddock.adp.mCubed.preferences.PlayModeEnum;
import dev.paddock.adp.mCubed.receivers.ClientReceiver;
import dev.paddock.adp.mCubed.receivers.HeadsetReceiver;
import dev.paddock.adp.mCubed.receivers.IReceiver;
import dev.paddock.adp.mCubed.receivers.NotificationReceiver;
import dev.paddock.adp.mCubed.receivers.PhoneStateReceiver;
import dev.paddock.adp.mCubed.receivers.RemoteControlReceiver;
import dev.paddock.adp.mCubed.scrobble.ScrobbleListener;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.Log;
import dev.paddock.adp.mCubed.utilities.PreferenceManager;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class PlaybackService extends Service {
	private Runnable initCallback;
	private IServerCallback serverCallback;
	private BroadcastReceiver clientReceiver;
	private OnSharedPreferenceChangeListener preferenceListener;
	private final Queue<Intent> queuedIntents = new LinkedList<Intent>();
	private static final List<BroadcastReceiver> receivers = new ArrayList<BroadcastReceiver>();
	private static final List<IListener> listeners = new ArrayList<IListener>();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Utilities.pushContext(this);
		try {
			super.onStart(intent, startId);
			if (App.isMounted()) {
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

			// Register the various receivers
			receivers.clear();
			receivers.add(new HeadsetReceiver());
			receivers.add(new PhoneStateReceiver());
			receivers.add(getClientReceiver());
			for (BroadcastReceiver receiver : receivers) {
				if (receiver instanceof IReceiver) {
					IReceiver rec = (IReceiver) receiver;
					IntentFilter filter = rec.getIntentFilter();
					if (filter != null) {
						registerReceiver(receiver, filter);
					}
				}
			}

			// Create the various listeners
			listeners.clear();
			listeners.add(new MountListener());
			listeners.add(new MediaAssociateListener());
			listeners.add(new HeadsetListener());
			listeners.add(new PhoneStateListener());
			listeners.add(new ScrobbleListener());

			// Register the various listeners
			for (IListener listener : listeners) {
				listener.register();
			}

			// Register the property changed listener
			PreferenceManager.registerPreferenceChangeListener(getPreferenceListener());

			// Setup the notification
			cancelNotification();
			updateNotification();

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

			// Unregister the various listeners
			for (IListener listener : listeners) {
				listener.unregister();
			}

			// Unregister the various receiver
			for (BroadcastReceiver receiver : receivers) {
				if (receiver instanceof IReceiver) {
					IReceiver rec = (IReceiver) receiver;
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

	private void cancelNotification() {
		stopForeground(true);
	}

	private void updateNotification() {
		// Grab some data
		NotificationVisibility visibility = PreferenceManager.getSettingEnum(NotificationVisibility.class, R.string.pref_notification_visibility);
		MediaFile media = App.getPlayingMedia();
		boolean isPlaying = App.getPlayer().isPlaying();

		// Make sure we have media
		if (media == null) {
			cancelNotification();
			return;
		}

		// Make sure the notification visibility isn't never
		if (visibility == NotificationVisibility.Never) {
			cancelNotification();
			return;
		}

		// If we only show the notification when playing, make sure we're playing
		if (visibility == NotificationVisibility.OnlyWhilePlaying && !isPlaying) {
			cancelNotification();
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
		String ticker = String.format("%s - %s", content, title);
		int icon = isPlaying ? R.drawable.notify_play : R.drawable.notify_pause;

		// Create and show the notification
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this).
			setContent(createNotificationView(media)).
			setContentIntent(pendingIntent).
			setContentText(content).
			setContentTitle(title).
			setOngoing(true).
			setSmallIcon(icon).
			setTicker(ticker).
			setWhen(System.currentTimeMillis());
		startForeground(Schema.NOTIF_PLAYING_MEDIA, builder.build());
	}

	private RemoteViews createNotificationView(MediaFile media) {
		String info = String.format("\"%s\" by %s", media.getTitle(), media.getArtist());
		RemoteViews view = new RemoteViews(getPackageName(), R.layout.notification);
		Uri art = media.getAlbumArt();
		if (art == null) {
			view.setImageViewResource(R.id.notif_cover_image, R.drawable.img_cover_missing);
		} else {
			view.setImageViewUri(R.id.notif_cover_image, art);
		}
		view.setTextViewText(R.id.notif_playing_info, info);
		view.setImageViewResource(R.id.notif_play_button, App.isInitialized() && App.getPlayer().isPlaying() ? R.drawable.ic_media_pause : R.drawable.ic_media_play);
		view.setOnClickPendingIntent(R.id.notif_next_button, generateNotificationClickIntent(Schema.NOTIF_NEXT_CLICK));
		view.setOnClickPendingIntent(R.id.notif_prev_button, generateNotificationClickIntent(Schema.NOTIF_PREV_CLICK));
		view.setOnClickPendingIntent(R.id.notif_play_button, generateNotificationClickIntent(Schema.NOTIF_PLAY_CLICK));
		return view;
	}

	private PendingIntent generateNotificationClickIntent(String action) {
		Intent intent = new Intent(this, NotificationReceiver.class);
		intent.setAction(action);
		return PendingIntent.getBroadcast(this, 0, intent, 0);
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
				public void startService(int intentID) {
				}

				@Override
				public void stopService(int intentID) {
					PlaybackService.this.cancelNotification();
					PlaybackService.this.stopSelf();
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
						PlaybackService.this.updateNotification();
					} else if (preferenceName.equals(Utilities.getResourceString(R.string.pref_open_overlay_player))) {
						PlaybackService.this.updateNotification();
					} else if (preferenceName.equals(Utilities.getResourceString(R.string.pref_play_mode))) {
						PlaybackService.this.updatePlayMode();
					} else if (preferenceName.equals(Utilities.getResourceString(R.string.pref_clear_queue_with_play_mode))) {
						PlaybackService.this.updatePlayMode();
					} else if (preferenceName.equals(Utilities.getResourceString(R.string.pref_volume_speaker))) {
						HeadsetListener.updateVolume(OutputMode.Speaker, false);
					} else if (preferenceName.equals(Utilities.getResourceString(R.string.pref_volume_headphones))) {
						HeadsetListener.updateVolume(OutputMode.Headphones, false);
					} else if (preferenceName.equals(Utilities.getResourceString(R.string.pref_volume_bluetooth))) {
						HeadsetListener.updateVolume(OutputMode.Bluetooth, false);
					} else if (preferenceName.equals(Utilities.getResourceString(R.string.pref_record_logs))) {
						Log.setFileLoggingEnabled(PreferenceManager.getSettingBoolean(R.string.pref_record_logs));
					}
				}

				@Override
				public void propertyAudioFocusStateChanged(AudioFocusState audioFocusState) {
					if (audioFocusState == AudioFocusState.AudioFocusDuck) {
						App.getPlayer().adjustVolumeDuck();
					} else {
						App.getPlayer().adjustVolumeFull();
					}
				}

				@Override
				public void propertyPlaybackIDChanged(long playbackID) {
					PlaybackService.this.updateNotification();
					RemoteControlReceiver.updateCurrentMetadata();
				}

				@Override
				public void propertyPlaybackStatusChanged(MediaStatus playbackStatus) {
					PlaybackService.this.updateNotification();
					RemoteControlReceiver.updatePlaybackState();
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