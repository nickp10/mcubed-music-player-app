package dev.paddock.adp.mCubed.services;

import java.io.Serializable;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.model.MediaStatus;
import dev.paddock.adp.mCubed.utilities.Log;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class PlaybackClient {
	/**
	 * Prevents an instance of a PlaybackClient
	 */
	private PlaybackClient() { }
	
	private static Intent createIntent(int method) {
		return createIntent(method, Schema.I_MCUBED);
	}
	
	private static Intent createIntent(int method, String intentAction) {
		Context context = Utilities.getContext();
		if (context != null) {
			Intent intent = new Intent(context, PlaybackService.class);
			intent.setAction(intentAction);
			intent.putExtra(Schema.I_PARAM_INTENT_ID, Schema.getIntentID());
			intent.putExtra(Schema.I_METHOD, method);
			return intent;
		}
		return null;
	}
	
	private static void sendIntent(Intent intent) {
		if (intent != null) {
			Context context = Utilities.getContext();
			if (context == null) {
				Log.e("Playback client intent has no context.", true);
			} else {
				context.startService(intent);
			}
		}
	}
	
	public static void startService() {
		Intent intent = createIntent(Schema.MC_START_SERVICE);
		sendIntent(intent);
	}
	
	public static void stopService() {
		Intent intent = createIntent(Schema.MC_STOP_SERVICE);
		sendIntent(intent);
	}
	
	public static void setPlaybackSeek(int playbackSeek) {
		Intent intent = createIntent(Schema.MC_SET_PLAYBACK_SEEK);
		intent.putExtra(Schema.I_PARAM_PB_SEEK, playbackSeek);
		sendIntent(intent);
	}
	
	public static void setPlaybackStatus(MediaStatus playbackStatus) {
		Intent intent = createIntent(Schema.MC_SET_PLAYBACK_STATUS);
		intent.putExtra(Schema.I_PARAM_PB_STATUS, playbackStatus);
		sendIntent(intent);
	}
	
	public static void movePlaybackNext() {
		Intent intent = createIntent(Schema.MC_MOVE_PLAYBACK_NEXT);
		sendIntent(intent);
	}
	
	public static void movePlaybackPrev() {
		Intent intent = createIntent(Schema.MC_MOVE_PLAYBACK_PREV);
		sendIntent(intent);
	}
	
	public static void play() {
		setPlaybackStatus(MediaStatus.Play);
	}
	
	public static void pause() {
		setPlaybackStatus(MediaStatus.Pause);
	}
	
	public static void stop() {
		setPlaybackStatus(MediaStatus.Stop);
	}
	
	public static boolean handleIntent(Intent intent, IClientCallback callback) {
		return handleIntent(intent, callback, true);
	}
	
	/**
	 * Handles the given intent by calling the appropriate method on the callback based on the command specified by the intent.
	 * @param intent The intent that contains the command and the parameter information for the method to call.
	 * @param callback The callback that the method will be called upon.
	 * @param isAsynchronous True specifies that this method returns immediately and the callback process is placed on a separate thread, or
	 * 						 false specifies that the method blocks until the callback process is complete since it remains on the same thread.
	 * @return
	 */
	public static boolean handleIntent(final Intent intent, final IClientCallback callback, boolean isAsynchronous) {
		// Make sure it came from the mCubed service
		if (Schema.ismCubedIntent(intent) && callback != null) {
			// All mCubed intents come with extras
			final Bundle extras = intent.getExtras();
			if (extras != null) {
				// All mCubed intents have a method call and a response ID, 0 indicates the service initiated the intent
				final int method = extras.getInt(Schema.I_METHOD);
				final int intentID = extras.getInt(Schema.I_PARAM_INTENT_ID);
				
				// Create the runnable
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						switch (method) {
							
						// A property has changed
						case Schema.MS_PROPERTY_CHANGED:
							String propertyName = extras.getString(Schema.I_PARAM_PROP_NAME);
							Serializable propertyValue = extras.getSerializable(Schema.I_PARAM_PROP_VALUE);
							Log.i(String.format(Locale.US, "Handle property changed [Property=%s, Value=%s, IntentID=%d]", propertyName, propertyValue, intentID));
							callback.propertyChanged(intentID, propertyName, propertyValue);
							break;
							
						// A preference has changed
						case Schema.MS_PREFERENCE_CHANGED:
							String preferenceName = extras.getString(Schema.I_PARAM_PREF_NAME);
							Log.i(String.format(Locale.US, "Handle preference changed [Preference=%s, IntentID=%d]", preferenceName, intentID));
							callback.preferenceChanged(intentID, preferenceName);
							break;
							
						// Progress for a task(s) changed
						case Schema.MS_PROGRESS_CHANGED:
							String progressID = extras.getString(Schema.I_PARAM_PROGRESS_ID);
							String progressTitle = extras.getString(Schema.I_PARAM_PROGRESS_TITLE);
							byte progressValue = extras.getByte(Schema.I_PARAM_PROGRESS_VALUE);
							boolean progressBlocking = extras.getBoolean(Schema.I_PARAM_PROGRESS_BLOCKING);
							Log.i(String.format(Locale.US, "Handle progress changed [Title=%s, Value=%d, IntentID=%d]", progressTitle, progressValue, intentID));
							callback.progressChanged(intentID, progressID, progressTitle, progressValue, progressBlocking);
							break;
							
						}
					}
				};
				
				// Call the appropriate method asynchronously or synchronously
				if (isAsynchronous) {
					new Thread(runnable).start();
				} else {
					runnable.run();
				}
				return true;
			}
		}
		return false;
	}
}