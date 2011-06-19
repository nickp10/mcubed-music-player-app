package dev.paddock.adp.mCubed.receivers;

import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.model.NotificationArgs;
import dev.paddock.adp.mCubed.model.OutputMode;
import dev.paddock.adp.mCubed.services.PlaybackServer;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.PreferenceManager;
import dev.paddock.adp.mCubed.utilities.PropertyManager;
import dev.paddock.adp.mCubed.utilities.Utilities;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

public class OutputReceiver extends BroadcastReceiver implements IReceiver {
	private static final OutputReceiver instance = new OutputReceiver();
	private AudioManager manager;
	private OutputMode outputMode = OutputMode.Speaker;
	
	public static OutputReceiver getInstance() {
		return instance;
	}
	
	private OutputReceiver() {
		updateOutputMode();
	}
	
	private AudioManager getManager() {
		if (manager == null) {
			Context context = Utilities.getContext();
			if (context != null) {
				Object service = context.getSystemService(Context.AUDIO_SERVICE);
				if (service != null && service instanceof AudioManager) {
					manager = (AudioManager)service;
				}
			}
		}
		return manager;
	}
	
	public void updateOutputMode() {
		// Get the new mode
		OutputMode outputMode = this.outputMode;
		if (isRoutingToHeadphones()) {
			outputMode = OutputMode.Headphones;
		} else if (isRoutingToBluetooth()) {
			outputMode = OutputMode.Bluetooth;
		} else if (isRoutingToSpeaker()) {
			outputMode = OutputMode.Speaker;
		}
		
		// Update to the new mode
		updateOutputMode(outputMode);
	}
	
	public void updateOutputMode(OutputMode outputMode) {
		if (outputMode != null && this.outputMode != outputMode) {
			NotificationArgs args = new NotificationArgs(this, "OutputMode", this.outputMode, outputMode);
			PropertyManager.notifyPropertyChanging(this, "OutputMode", args);
			this.outputMode = outputMode;
			PropertyManager.notifyPropertyChanged(this, "OutputMode", args);
			PlaybackServer.propertyChanged(0, Schema.PROP_OUTPUT_MODE, this.outputMode);
			updateVolume(null);
		}
	}
	
	/**
	 * Updates the volume to be the volume defined in the user's settings. If the volume
	 * setting is negative, then the volume isn't changed. If the volume is greater than 
	 * 100, then the volume isn't changed either. If the current output mode isn't the 
	 * specified output mode, then the volume won't be updated either.
	 * @param outputMode The output mode that the audio must be playing to in order for
	 * the volume to be updated. Specifying null will bypass this check.
	 */
	public void updateVolume(OutputMode outputMode) {
		// Ensure we are in the right mode
		if (outputMode != null && outputMode != this.outputMode) {
			return;
		}
		
		// Get the volume preference
		int volume = -1;
		if (this.outputMode == OutputMode.Speaker) {
			volume = PreferenceManager.getSettingInt(R.string.pref_volume_speaker);
		} else if (this.outputMode == OutputMode.Headphones) {
			volume = PreferenceManager.getSettingInt(R.string.pref_volume_headphones);
		} else if (this.outputMode == OutputMode.Bluetooth) {
			volume = PreferenceManager.getSettingInt(R.string.pref_volume_bluetooth);
		}
		
		// Update the volume
		if (volume >= 0 && volume <= 100) {
			AudioManager manager = getManager();
			if (manager != null) {
				double percent = volume / 100d;
				int max = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
				volume = (int)(percent * max);
				manager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
			}
		}
	}
	
	public boolean isRoutingToBluetooth() {
		AudioManager manager = getManager();
		return manager != null && (manager.isBluetoothA2dpOn() || manager.isBluetoothScoOn());
	}
	
	public boolean isRoutingToHeadphones() {
		AudioManager manager = getManager();
		return manager != null && manager.isWiredHeadsetOn();
	}
	
	public boolean isRoutingToSpeaker() {
		return !isRoutingToBluetooth() && !isRoutingToHeadphones();
	}
	
	public void routeToBluetooth() {
		if (App.getHeadset().isBluetoothConnected()) {
			AudioManager manager = getManager();
			if (manager != null) {
				manager.setBluetoothScoOn(false);
				manager.setSpeakerphoneOn(false);
				manager.setMode(AudioManager.MODE_NORMAL);
				updateOutputMode(OutputMode.Bluetooth);
			}
		}
	}
	
	public void routeToHeadphones() {
		if (App.getHeadset().isHeadphonesConnected()) {
			AudioManager manager = getManager();
			if (manager != null) {
				manager.setBluetoothScoOn(false);
				manager.setSpeakerphoneOn(false);
				manager.setMode(AudioManager.MODE_NORMAL);
				updateOutputMode(OutputMode.Headphones);
			}
		}
	}
	
	public void routeToSpeaker() {
		AudioManager manager = getManager();
		if (manager != null) {
			if (App.getHeadset().isBluetoothConnected() || App.getHeadset().isHeadphonesConnected()) {
				manager.setBluetoothScoOn(false);
				manager.setSpeakerphoneOn(true);
				manager.setMode(AudioManager.MODE_IN_CALL);
			} else {
				manager.setBluetoothScoOn(false);
				manager.setSpeakerphoneOn(false);
				manager.setMode(AudioManager.MODE_NORMAL);
			}
			updateOutputMode(OutputMode.Speaker);
		}
	}
	
	@Override
	public IntentFilter getIntentFilter() {
		return null;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
	}
}