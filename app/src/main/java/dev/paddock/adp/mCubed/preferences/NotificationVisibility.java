package dev.paddock.adp.mCubed.preferences;

public enum NotificationVisibility implements PreferenceEnum.IPreference {
	Always,
	Never,
	OnlyWhilePlaying;
	
	private String display;
	
	private NotificationVisibility() {
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