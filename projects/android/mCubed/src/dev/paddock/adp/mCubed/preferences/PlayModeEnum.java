package dev.paddock.adp.mCubed.preferences;

import dev.paddock.adp.mCubed.model.playModes.IPlayMode;
import dev.paddock.adp.mCubed.model.playModes.PerpetualShufflePlayMode;
import dev.paddock.adp.mCubed.model.playModes.PersistedShufflePlayMode;
import dev.paddock.adp.mCubed.model.playModes.SequentialPlayMode;

public enum PlayModeEnum implements PreferenceEnum.IPreference {
	Sequential(new SequentialPlayMode()),
	PersistedShuffle(new PersistedShufflePlayMode()),
	PerpetualShuffle(new PerpetualShufflePlayMode());
	
	private String display;
	private IPlayMode playMode;
	
	private PlayModeEnum(IPlayMode playMode) {
		PreferenceEnum.setDisplay(this);
		this.playMode = playMode;
	}
	
	public IPlayMode getPlayMode() {
		return playMode;
	}

	@Override
	public String getDisplay() {
		return display;
	}
	
	@Override
	public void setDisplay(String display) {
		this.display = display;
	}
}