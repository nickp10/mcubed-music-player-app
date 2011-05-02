package dev.paddock.adp.mCubed.services;

import java.io.Serializable;

import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.model.MediaStatus;
import dev.paddock.adp.mCubed.utilities.Log;
import dev.paddock.adp.mCubed.utilities.Utilities;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class PlaybackServer {
	/**
	 * Prevents an instance of a PlaybackServer
	 */
	private PlaybackServer() { }
	
	private static Intent createIntent(int method, int intentID) {
		return createIntent(method, intentID, Schema.I_MCUBED);
	}
	
	private static Intent createIntent(int method, int intentID, String intentAction) {
		intentID = intentID == 0 ? Schema.getIntentID() : intentID;
		Intent intent = new Intent();
		intent.setAction(intentAction);
		intent.putExtra(Schema.I_PARAM_INTENT_ID, intentID);
		intent.putExtra(Schema.I_METHOD, method);
		return intent;
	}
	
	private static void sendIntent(Intent intent) {
		if (intent != null) {
			Context context = Utilities.getContext();
			if (context == null) {
				Log.e("Playback server intent has no context.", true);
			} else {
				context.sendBroadcast(intent);
			}
		}
	}
	
	public static void progressChanged(int intentID, String progressID, String progressTitle, byte progressValue, boolean progressBlocking) {
		Intent intent = createIntent(Schema.MS_PROGRESS_CHANGED, intentID, Schema.I_MCUBED_PROGRESS);
		intent.putExtra(Schema.I_PARAM_PROGRESS_ID, progressID);
		intent.putExtra(Schema.I_PARAM_PROGRESS_TITLE, progressTitle);
		intent.putExtra(Schema.I_PARAM_PROGRESS_VALUE, progressValue);
		intent.putExtra(Schema.I_PARAM_PROGRESS_BLOCKING, progressBlocking);
		sendIntent(intent);
	}
	
	public static void propertyChanged(int intentID, String propertyName, Serializable propertyValue) {
		Intent intent = createIntent(Schema.MS_PROPERTY_CHANGED, intentID);
		intent.putExtra(Schema.I_PARAM_PROP_NAME, propertyName);
		intent.putExtra(Schema.I_PARAM_PROP_VALUE, propertyValue);
		sendIntent(intent);
	}
	
	public static void preferenceChanged(int intentID, String preferenceName) {
		Intent intent = createIntent(Schema.MS_PREFERENCE_CHANGED, intentID);
		intent.putExtra(Schema.I_PARAM_PREF_NAME, preferenceName);
		sendIntent(intent);
	}
	
	public static boolean handleIntent(Intent intent, IServerCallback callback) {
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
	public static boolean handleIntent(final Intent intent, final IServerCallback callback, boolean isAsynchronous) {
		if (Schema.ismCubedIntent(intent) && callback != null) {
			// All mCubed intents come with extras
			final Bundle extras = intent.getExtras();
			if (extras != null) {
				// All mCubed intents have a method call and a request ID
				final int method = extras.getInt(Schema.I_METHOD);
				final int intentID = extras.getInt(Schema.I_PARAM_INTENT_ID);
				Log.i(String.format("Handle server intent [Method=%d, IntentID=%d]", method, intentID));
				
				// Create the runnable
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						switch (method) {
						
						// Start the service
						case Schema.MC_START_SERVICE:
							callback.startService(intentID);
							break;
							
						// Stop the service
						case Schema.MC_STOP_SERVICE:
							callback.stopService(intentID);
							break;
							
						// The seek listener value was set
						case Schema.MC_SET_SEEK_LISTENER:
							boolean sslIsSeekListener = extras.getBoolean(Schema.I_PARAM_SEEK_LISTENER);
							callback.setSeekListener(intentID, sslIsSeekListener);
							break;
							
						// The playback seek was set
						case Schema.MC_SET_PLAYBACK_SEEK:
							int spsPlaybackSeek = extras.getInt(Schema.I_PARAM_PB_SEEK);
							callback.setPlaybackSeek(intentID, spsPlaybackSeek);
							break;
						
						// The playback status was set
						case Schema.MC_SET_PLAYBACK_STATUS:
							Serializable spsPlaybackStatus = extras.getSerializable(Schema.I_PARAM_PB_STATUS);
							callback.setPlaybackStatus(intentID, (MediaStatus)spsPlaybackStatus);
							break;
							
						// The playback was moved to the next media
						case Schema.MC_MOVE_PLAYBACK_NEXT:
							callback.movePlaybackNext(intentID);
							break;
							
						// The playback was moved to the previous media
						case Schema.MC_MOVE_PLAYBACK_PREV:
							callback.movePlaybackPrev(intentID);
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