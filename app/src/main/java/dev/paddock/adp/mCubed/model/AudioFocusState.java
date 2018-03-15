package dev.paddock.adp.mCubed.model;

import android.media.AudioManager;

public enum AudioFocusState {
	NoAudioFocus(AudioManager.AUDIOFOCUS_LOSS),
	NoAudioFocusTemporary(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT),
	AudioFocusDuck(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK),
	AudioFocus(AudioManager.AUDIOFOCUS_GAIN);
	
	private final int _audioFocusFlag;
	
	private AudioFocusState(int audioFocusFlag) {
		_audioFocusFlag = audioFocusFlag;
	}
	
	public int getAudioFocusFlag() {
		return _audioFocusFlag;
	}
	
	public static AudioFocusState getStateFromFlag(int flag) {
		for (AudioFocusState state : AudioFocusState.values()) {
			if (state._audioFocusFlag == flag) {
				return state;
			}
		}
		return null;
	}
}
