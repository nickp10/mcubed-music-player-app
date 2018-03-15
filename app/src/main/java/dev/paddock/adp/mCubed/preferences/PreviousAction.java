package dev.paddock.adp.mCubed.preferences;

public enum PreviousAction implements PreferenceEnum.IPreference {
	Smart,
	AlwaysRestartSong,
	AlwaysPlayPreviousSong;
	
	private String display;
	
	private PreviousAction() {
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