package dev.paddock.adp.mCubed.preferences;

public class PreferenceEnum {
	public interface IPreference {
		String getDisplay();
		void setDisplay(String display);
	}
	
	public static <T extends Enum<T> & IPreference> void setDisplay(T enumValue) {
		enumValue.setDisplay(enumValue.name().replaceAll("(.)([A-Z])", "$1 $2"));
	}
}