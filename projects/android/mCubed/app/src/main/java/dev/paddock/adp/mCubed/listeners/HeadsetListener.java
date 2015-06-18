package dev.paddock.adp.mCubed.listeners;

import android.content.Context;
import android.media.AudioManager;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.activities.BluetoothPreferences;
import dev.paddock.adp.mCubed.model.DelayedTask;
import dev.paddock.adp.mCubed.model.NotificationArgs;
import dev.paddock.adp.mCubed.model.OutputMode;
import dev.paddock.adp.mCubed.preferences.PlaybackAction;
import dev.paddock.adp.mCubed.receivers.HeadsetReceiver;
import dev.paddock.adp.mCubed.services.PlaybackServer;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.INotifyListener;
import dev.paddock.adp.mCubed.utilities.PreferenceManager;
import dev.paddock.adp.mCubed.utilities.PropertyManager;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class HeadsetListener implements IListener {
	private final INotifyListener isBluetoothConnectedListener = new INotifyListener() {
		@Override
		public void propertyChanging(Object instance, NotificationArgs args) { }

		@Override
		public void propertyChanged(Object instance, NotificationArgs args) {
			onIsBluetoothConnectedChanged(args);
		}
	};

	private final INotifyListener isHeadphonesConnectedListener = new INotifyListener() {
		@Override
		public void propertyChanging(Object instance, NotificationArgs args) { }

		@Override
		public void propertyChanged(Object instance, NotificationArgs args) {
			onIsHeadphonesConnectedChanged(args);
		}
	};

	private final INotifyListener outputModeListener = new INotifyListener() {
		@Override
		public void propertyChanging(Object instance, NotificationArgs args) { }

		@Override
		public void propertyChanged(Object instance, NotificationArgs args) {
			onOutputModeChanged(args);
		}
	};

	@Override
	public void register() {
		PropertyManager.register(HeadsetReceiver.class, "BluetoothConnected", isBluetoothConnectedListener);
		PropertyManager.register(HeadsetReceiver.class, "HeadphonesConnected", isHeadphonesConnectedListener);
		PropertyManager.register(HeadsetReceiver.class, "OutputMode", outputModeListener);
		updateVolume(null, false);
	}

	@Override
	public void unregister() {
		PropertyManager.unregister(isBluetoothConnectedListener);
		PropertyManager.unregister(isHeadphonesConnectedListener);
		PropertyManager.unregister(outputModeListener);
	}
	
	private void onIsBluetoothConnectedChanged(NotificationArgs args) {
		// Retrieve the action
		PlaybackAction action = null;
		if (HeadsetReceiver.isBluetoothConnected()) {
			action = BluetoothPreferences.getAction(R.string.pref_bluetooth_connected);
		} else {
			action = BluetoothPreferences.getAction(R.string.pref_bluetooth_disconnected);
		}
		
		// Perform the appropriate action
		if (App.isInitialized()) {
			if (action == PlaybackAction.Play) {
				App.getPlayer().play();
			} else if (action == PlaybackAction.Pause) {
				App.getPlayer().pause();
			}
		}
		
		// Re-route the event
		PlaybackServer.propertyChanged(0, Schema.PROP_BLUETOOTH, HeadsetReceiver.isBluetoothConnected());
	}
	
	private void onIsHeadphonesConnectedChanged(NotificationArgs args) {
		// Retrieve the action
		PlaybackAction action = null;
		if (HeadsetReceiver.isHeadphonesConnected()) {
			action = PreferenceManager.getSettingEnum(PlaybackAction.class, R.string.pref_headphones_connected);
		} else {
			action = PreferenceManager.getSettingEnum(PlaybackAction.class, R.string.pref_headphones_disconnected);
		}
		
		// Perform the appropriate action
		if (App.isInitialized()) {
			if (action == PlaybackAction.Play) {
				App.getPlayer().play();
			} else if (action == PlaybackAction.Pause) {
				App.getPlayer().pause();
			}
		}
		
		// Re-route the event
		PlaybackServer.propertyChanged(0, Schema.PROP_HEADPHONES, HeadsetReceiver.isHeadphonesConnected());
	}
	
	private void onOutputModeChanged(NotificationArgs args) {
		// Update the volume
		updateVolume(null, args.getNewValue().equals(OutputMode.Bluetooth) || args.getOldValue().equals(OutputMode.Bluetooth));
		
		// Re-route the event
		PlaybackServer.propertyChanged(0, Schema.PROP_OUTPUT_MODE, HeadsetReceiver.getOutputMode());
	}
	
	/**
	 * Updates the volume to be the volume defined in the user's settings. If the volume
	 * setting is negative, then the volume isn't changed. If the volume is greater than 
	 * 100, then the volume isn't changed either. If the current output mode isn't the 
	 * specified output mode, then the volume won't be updated either.
	 * 
	 * If the output mode changed, then this method should be called with null so that
	 * the volume level is adjusted for the new output mode.
	 * 
	 * If a volume preference changed, then this method should be called with the output
	 * mode for the volume preference that changed. For instance, if the speaker volume
	 * preference changed, then this should be called with the volume output mode. This
	 * output mode ensures the volume is only updated for the preference the user changed.
	 * For instance, if the user is listening to audio via the speaker, has a speaker
	 * volume preference of 20, and is currently listening to the audio at a volume of 30,
	 * then when the user changes the headphone speaker volume, the volume should not be
	 * re-adjusted to 30 since the user changed the headphone speaker volume.
	 *  
	 * @param outputMode The output mode that the audio must be playing to in order for
	 * @param isDelayed Whether or not to delay updating the volume.
	 * the volume to be updated. Specifying null will bypass this check.
	 */
	public static void updateVolume(OutputMode outputMode, boolean isDelayed) {
		// Ensure we are in the right mode
		OutputMode currentOutputMode = HeadsetReceiver.getOutputMode();
		if (outputMode != null && outputMode != currentOutputMode) {
			return;
		}
		
		// Get the volume preference for the current output mode
		int volume = -1;
		if (currentOutputMode == OutputMode.Speaker) {
			volume = PreferenceManager.getSettingInt(R.string.pref_volume_speaker);
		} else if (currentOutputMode == OutputMode.Headphones) {
			volume = PreferenceManager.getSettingInt(R.string.pref_volume_headphones);
		} else if (currentOutputMode == OutputMode.Bluetooth) {
			volume = PreferenceManager.getSettingInt(R.string.pref_volume_bluetooth);
		}
		
		// Update the volume accordingly
		if (volume >= 0 && volume <= 100) {
			final AudioManager manager = App.getSystemService(AudioManager.class, Utilities.getContext(), Context.AUDIO_SERVICE);
			if (manager != null) {
				double percent = volume / 100d;
				int max = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
				final int volumeValue = (int)(percent * max);
				
				// On some devices (ahem...HTC...ahem), they will manage their own output-mode-specific
				// volume levels. We need to allow these devices to update the volume level and then go
				// back in and update the volume again. Therefore, we wait for just enough time to allow
				// the device to set the volume level before attempting to update the volume ourself.
				if (isDelayed) {
					adjustVolumeDelayed(manager, volumeValue);
				} else {
					adjustVolume(manager, volumeValue);
				}
			}
		}
	}
	
	private static void adjustVolume(AudioManager manager, int volume) {
		manager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
	}
	
	private static void adjustVolumeDelayed(final AudioManager manager, final int volume) {
		new DelayedTask(Utilities.getContext(), new Runnable() {
			
			@Override
			public void run() {
				adjustVolume(manager, volume);
			}
		}, 500L);
	}
}