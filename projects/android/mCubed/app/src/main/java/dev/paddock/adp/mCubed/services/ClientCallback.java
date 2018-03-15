package dev.paddock.adp.mCubed.services;

import java.io.Serializable;

import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.model.AudioFocusState;
import dev.paddock.adp.mCubed.model.InitStatus;
import dev.paddock.adp.mCubed.model.MediaStatus;
import dev.paddock.adp.mCubed.model.OutputMode;

public abstract class ClientCallback implements IClientCallback {
	@Override
	public void preferenceChanged(int intentID, String preferenceName) { }
	
	@Override
	public void progressChanged(int intentID, String progressID, String progressTitle, byte progressValue, boolean progressBlocking) { }
	
	@Override
	public final void propertyChanged(int intentID, String propertyName, Serializable propertyValue) {
		// Call the generic property changed method
		prePropertyChanged(propertyName, propertyValue);
		
		// Now call the specific method
		if (Schema.PROP_AUDIO_FOCUS_STATE.equals(propertyName)) {
			propertyAudioFocusStateChanged((AudioFocusState)propertyValue);
		} else if (Schema.PROP_BLUETOOTH.equals(propertyName)) {
			propertyBlueoothChanged((Boolean)propertyValue);
		} else if (Schema.PROP_HEADPHONES.equals(propertyName)) { 
			propertyHeadphonesChanged((Boolean)propertyValue);
		} else if (Schema.PROP_MOUNTED.equals(propertyName)) {
			propertyMountedChanged((Boolean)propertyValue);
		} else if (Schema.PROP_SCAN_REQUIRED.equals(propertyName)) {
			propertyScanRequiredChanged((Boolean)propertyValue);
		} else if (Schema.PROP_OUTPUT_MODE.equals(propertyName)) {
			propertyOutputModeChanged((OutputMode)propertyValue);
		} else if (Schema.PROP_PHONE_CALL_ACTIVE.equals(propertyName)) {
			propertyPhoneCallActiveChanged((Boolean)propertyValue);
		} else if (Schema.PROP_PB_ID.equals(propertyName)) {
			propertyPlaybackIDChanged((Long)propertyValue);
		} else if (Schema.PROP_PB_SEEK.equals(propertyName)) {
			propertyPlaybackSeekChanged((Integer)propertyValue);
		} else if (Schema.PROP_PB_STATUS.equals(propertyName)) {
			propertyPlaybackStatusChanged((MediaStatus)propertyValue);
		} else if (Schema.PROP_INIT_STATUS.equals(propertyName)) {
			propertyInitStatusChanged((InitStatus)propertyValue);
		} else if (Schema.PROP_IS_SERVICE_RUNNING.equals(propertyName)) {
			propertyIsServiceRunningChanged((Boolean)propertyValue);
		}
	}

	public void prePropertyChanged(String propertyName, Serializable propertyValue) { }
	public void propertyAudioFocusStateChanged(AudioFocusState audioFocusState) { }
	public void propertyBlueoothChanged(boolean isBluetoothConnected) { }
	public void propertyHeadphonesChanged(boolean isHeadphonesConnected) { }
	public void propertyMountedChanged(boolean isMounted) { }
	public void propertyScanRequiredChanged(boolean isScanRequired) { }
	public void propertyOutputModeChanged(OutputMode outputMode) { }
	public void propertyPhoneCallActiveChanged(boolean isPhoneCallActive) { }
	public void propertyPlaybackIDChanged(long playbackID) { }
	public void propertyPlaybackSeekChanged(int playbackSeek) { }
	public void propertyPlaybackStatusChanged(MediaStatus playbackStatus) { }
	public void propertyInitStatusChanged(InitStatus initStatus) { }
	public void propertyIsServiceRunningChanged(boolean isServiceRunning) { }
}