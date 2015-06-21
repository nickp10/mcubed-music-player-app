package dev.paddock.adp.mCubed.listeners;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.model.AudioFocusState;
import dev.paddock.adp.mCubed.model.NotificationArgs;
import dev.paddock.adp.mCubed.services.PlaybackServer;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.PropertyManager;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class AudioFocusListener implements IListener, OnAudioFocusChangeListener {
	private static final AudioFocusListener instance = new AudioFocusListener();
	private AudioFocusState audioFocusState = AudioFocusState.NoAudioFocus;

	private AudioFocusListener() {
	}

	public static AudioFocusListener getInstance() {
		return instance;
	}

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

	public void requestAudioFocus(Context context) {
		AudioManager manager = App.getSystemService(AudioManager.class, context, Context.AUDIO_SERVICE);
		if (getAudioFocusState() == AudioFocusState.NoAudioFocus || getAudioFocusState() == AudioFocusState.NoAudioFocusTemporary) {
			int result = manager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
			if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
				this.setAudioFocusState(AudioFocusState.AudioFocus);
			}
		}
	}

	@Override
	public void register() {
		Context context = Utilities.getContext();
		if (context != null) {
			requestAudioFocus(context);
		}
	}

	@Override
	public void unregister() {
		Context context = Utilities.getContext();
		if (context != null) {
			AudioManager manager = App.getSystemService(AudioManager.class, context, Context.AUDIO_SERVICE);
			manager.abandonAudioFocus(this);
			this.setAudioFocusState(AudioFocusState.NoAudioFocus);
		}
	}
}
