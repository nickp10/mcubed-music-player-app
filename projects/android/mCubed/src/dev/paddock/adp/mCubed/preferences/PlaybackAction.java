package dev.paddock.adp.mCubed.preferences;

public enum PlaybackAction implements PreferenceEnum.IPreference {
	DoNothing,
	Pause,
	Play;
	
	private String display;
	
	private PlaybackAction() {
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