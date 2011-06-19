package dev.paddock.adp.mCubed.preferences;

public enum RepeatStatus implements PreferenceEnum.IPreference {
	NoRepeat,
	RepeatPlaylist,
	RepeatSong;
	
	private String display;
	
	private RepeatStatus() {
		PreferenceEnum.setDisplay(this);
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