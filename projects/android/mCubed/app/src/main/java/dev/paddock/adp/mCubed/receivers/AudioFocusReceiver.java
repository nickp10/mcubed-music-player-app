package dev.paddock.adp.mCubed.receivers;

import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.model.AudioFocusState;
import dev.paddock.adp.mCubed.model.NotificationArgs;
import dev.paddock.adp.mCubed.services.PlaybackServer;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.PropertyManager;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class AudioFocusReceiver implements OnAudioFocusChangeListener {
	private static AudioFocusReceiver instance;
	private AudioFocusState audioFocusState = AudioFocusState.NoAudioFocus;
	
	@Override
	public void onAudioFocusChange(int focusChange) {
		Utilities.pushContext(App.getAppContext());
		try {
			AudioFocusState audioFocusState = AudioFocusState.getStateFromFlag(focusChange);
			audioFocusState = audioFocusState == null ? AudioFocusState.NoAudioFocus : audioFocusState;
			setAudioFocusState(audioFocusState);
		} finally {
			Utilities.popContext();
		}
	}
	
	public AudioFocusState getAudioFocusState() {
		return audioFocusState;
	}
	
	public void setAudioFocusState(AudioFocusState audioFocusState) {
		if (audioFocusState != null && this.audioFocusState != audioFocusState) {
			NotificationArgs args = new NotificationArgs(this, "AudioFocusState", this.audioFocusState, audioFocusState);
			PropertyManager.notifyPropertyChanging(args);
			this.audioFocusState = audioFocusState;
			PlaybackServer.propertyChanged(0, Schema.PROP_AUDIO_FOCUS_STATE, this.audioFocusState);
			PropertyManager.notifyPropertyChanged(args);
		}
	}
	
	public static AudioFocusReceiver getAudioFocusReceiver() {
		return instance;
	}
	
	public static void registerAudioFocus(AudioManager manager) {
		AudioFocusReceiver audioFocus = getAudioFocusReceiver();
		if (audioFocus == null || audioFocus.getAudioFocusState() == AudioFocusState.NoAudioFocus || audioFocus.getAudioFocusState() == AudioFocusState.NoAudioFocusTemporary) {
			audioFocus = audioFocus == null ? new AudioFocusReceiver() : audioFocus;
			int result = manager.requestAudioFocus(audioFocus, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
			AudioFocusReceiver.instance = audioFocus;
			if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
				audioFocus.setAudioFocusState(AudioFocusState.AudioFocus);
			}
		}
	}
	
	public static void unregisterAudioFocus(AudioManager manager) {
		AudioFocusReceiver audioFocus = getAudioFocusReceiver();
		if (audioFocus != null) {
			manager.abandonAudioFocus(audioFocus);
			audioFocus.setAudioFocusState(AudioFocusState.NoAudioFocus);
			AudioFocusReceiver.instance = null;
		}
	}
}
