package dev.paddock.adp.mCubed.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.util.SparseArray;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.preferences.NotificationVisibility;
import dev.paddock.adp.mCubed.preferences.PlayModeEnum;
import dev.paddock.adp.mCubed.preferences.PlaybackAction;
import dev.paddock.adp.mCubed.preferences.PreferenceEnum.IPreference;
import dev.paddock.adp.mCubed.preferences.PreviousAction;
import dev.paddock.adp.mCubed.preferences.RepeatStatus;

public class PreferenceManager {
	private static final SparseArray<Object> defaultValues = new SparseArray<Object>();
	private static final List<OnSharedPreferenceChangeListener> sharedPreferenceChangeListeners = new ArrayList<OnSharedPreferenceChangeListener>();

	static {
		defaultValues.put(R.string.pref_bluetooth_connected, PlaybackAction.DoNothing.name());
		defaultValues.put(R.string.pref_bluetooth_disconnected, PlaybackAction.DoNothing.name());
		defaultValues.put(R.string.pref_clear_queue_with_play_mode, false);
		defaultValues.put(R.string.pref_headphones_connected, PlaybackAction.Play.name());
		defaultValues.put(R.string.pref_headphones_disconnected, PlaybackAction.Pause.name());
		defaultValues.put(R.string.pref_light_up_screen, false);
		defaultValues.put(R.string.pref_notification_visibility, NotificationVisibility.OnlyWhilePlaying.name());
		defaultValues.put(R.string.pref_open_overlay_player, true);
		defaultValues.put(R.string.pref_play_mode, PlayModeEnum.Sequential.name());
		defaultValues.put(R.string.pref_previous_action, PreviousAction.Smart.name());
		defaultValues.put(R.string.pref_previous_smart_condition, "2%");
		defaultValues.put(R.string.pref_record_logs, true);
		defaultValues.put(R.string.pref_repeat_status, RepeatStatus.NoRepeat.name());
		defaultValues.put(R.string.pref_resume_automatically, true);
		defaultValues.put(R.string.pref_volume_speaker, -1);
		defaultValues.put(R.string.pref_volume_headphones, -1);
		defaultValues.put(R.string.pref_volume_bluetooth, -1);
		defaultValues.put(R.string.pref_defaults_loaded, false);
	}

	public static void setupDefaults() {
		if (!getSettingBoolean(R.string.pref_defaults_loaded)) {
			// Get the settings to modify
			SharedPreferences preferences = Utilities.getPreferences();
			if (preferences != null) {
				// Get the editor
				Editor editor = preferences.edit();
				
				// Set each of the default values in the editor
				for (int i = 0; i < defaultValues.size(); i++) {
					// Setup the default key/value
					String defaultKey = Utilities.getResourceString(defaultValues.keyAt(i));
					Object defaultValue = defaultValues.valueAt(i);
					
					// Switch over each type the setting value can be
					if (defaultValue instanceof String) {
						String val = preferences.getString(defaultKey, (String)defaultValue);
						editor.putString(defaultKey, val);
					} else if (defaultValue instanceof Boolean) {
						boolean val = preferences.getBoolean(defaultKey, (Boolean)defaultValue);
						editor.putBoolean(defaultKey, val);
					} else if (defaultValue instanceof Integer) {
						int val = preferences.getInt(defaultKey, (Integer)defaultValue);
						editor.putInt(defaultKey, val);
					} else if (defaultValue instanceof Long) {
						long val = preferences.getLong(defaultKey, (Long)defaultValue);
						editor.putLong(defaultKey, val);
					} else if (defaultValue instanceof Float) {
						float val = preferences.getFloat(defaultKey, (Float)defaultValue);
						editor.putFloat(defaultKey, val);
					}
				}
				
				// Commit the changes
				String defaultsLoadedKey = Utilities.getResourceString(R.string.pref_defaults_loaded);
				editor.putBoolean(defaultsLoadedKey, true);
				editor.commit();
			}
		}
	}

	public static Object getDefaultValue(int setting) {
		return defaultValues.get(setting);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getSetting(Class<T> clazz, int setting) {
		// Get the default value
		T defValue = (T)null;
		Object defaultValue = getDefaultValue(setting);
		if (defaultValue != null) {
			if (clazz.isAssignableFrom(defaultValue.getClass())) {
				defValue = (T)defaultValue;
			} else {
				Log.w("getSetting was called for a non-existent setting or incompatible class type.");
				return defValue;
			}
		}
		
		// Return the setting value
		return getSetting(clazz, setting, defValue);
	}

	public static <T> T getSetting(Class<T> clazz, int setting, T defValue) {
		String settingKey = Utilities.getResourceString(setting);
		return getSetting(clazz, settingKey, defValue);
	}

	public static <T> T getSetting(Class<T> clazz, String settingKey) {
		return getSetting(clazz, settingKey, (T)null);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getSetting(Class<T> clazz, String settingKey, T defValue) {
		// Retrieve the preferences
		SharedPreferences preferences = Utilities.getPreferences();
		if (preferences == null) {
			return defValue;
		}
		
		// Return the setting value
		if (clazz.equals(String.class)) {
			return (T)preferences.getString(settingKey, (String)defValue);
		} else if (clazz.equals(Boolean.class)) {
			if (defValue == null) {
				defValue = (T)(Object)false;
			}
			return (T)(Object)preferences.getBoolean(settingKey, (Boolean)defValue);
		} else if (clazz.equals(Integer.class)) {
			if (defValue == null) {
				defValue = (T)(Object)0;
			}
			return (T)(Object)preferences.getInt(settingKey, (Integer)defValue);
		} else if (clazz.equals(Long.class)) {
			if (defValue == null) {
				defValue = (T)(Object)0L;
			}
			return (T)(Object)preferences.getLong(settingKey, (Long)defValue);
		} else if (clazz.equals(Float.class)) {
			if (defValue == null) {
				defValue = (T)(Object)0f;
			}
			return (T)(Object)preferences.getFloat(settingKey, (Float)defValue);
		} else if (clazz.equals(Object.class)) {
			Map<String, ?> all = preferences.getAll();
			if (all != null && all.containsKey(settingKey)) {
				return (T)(Object)preferences.getAll().get(settingKey);
			}
		}
		return defValue;
	}

	public static boolean getSettingBoolean(int setting) {
		return getSetting(Boolean.class, setting);
	}

	public static boolean getSettingBoolean(String settingKey) {
		return getSetting(Boolean.class, settingKey);
	}

	public static float getSettingFloat(int setting) {
		return getSetting(Float.class, setting);
	}

	public static float getSettingFloat(String settingKey) {
		return getSetting(Float.class, settingKey);
	}

	public static int getSettingInt(int setting) {
		return getSetting(Integer.class, setting);
	}

	public static int getSettingInt(String settingKey) {
		return getSetting(Integer.class, settingKey);
	}

	public static long getSettingLong(int setting) {
		return getSetting(Long.class, setting);
	}

	public static long getSettingLong(String settingKey) {
		return getSetting(Long.class, settingKey);
	}

	public static String getSettingString(int setting) {
		return getSetting(String.class, setting);
	}

	public static Object getSettingObject(String settingKey) {
		return getSetting(Object.class, settingKey);
	}

	public static Object getSettingObject(int setting) {
		return getSetting(Object.class, setting);
	}

	public static String getSettingString(String settingKey) {
		return getSetting(String.class, settingKey);
	}

	public static <T extends Enum<T> & IPreference> T getSettingEnum(Class<T> clazz, int setting) {
		String settingValue = getSettingString(setting);
		return convertSettingValueToEnum(clazz, settingValue);
	}

	public static <T extends Enum<T> & IPreference> T getSettingEnum(Class<T> clazz, String settingKey) {
		String settingValue = getSettingString(settingKey);
		return convertSettingValueToEnum(clazz, settingValue);
	}

	private static <T extends Enum<T> & IPreference> T convertSettingValueToEnum(Class<T> clazz, String settingValue) {
		// Ensure a value is specified
		if (settingValue == null) {
			return null;
		}
		
		// Attempt to parse it
		return Enum.<T>valueOf(clazz, settingValue);
	}

	public static boolean setSettingBoolean(int setting, boolean settingValue) {
		String settingKey = Utilities.getResourceString(setting);
		return setSettingBoolean(settingKey, settingValue);
	}

	public static boolean setSettingBoolean(String settingKey, boolean settingValue) {
		// Retrieve the preferences
		SharedPreferences preferences = Utilities.getPreferences();
		if (preferences == null) {
			return false;
		}
		
		// Edit the value
		Editor editor = preferences.edit();
		editor.putBoolean(settingKey, settingValue);
		return editor.commit();
	}

	public static boolean setSettingFloat(int setting, float settingValue) {
		String settingKey = Utilities.getResourceString(setting);
		return setSettingFloat(settingKey, settingValue);
	}

	public static boolean setSettingFloat(String settingKey, float settingValue) {
		// Retrieve the preferences
		SharedPreferences preferences = Utilities.getPreferences();
		if (preferences == null) {
			return false;
		}
		
		// Edit the value
		Editor editor = preferences.edit();
		editor.putFloat(settingKey, settingValue);
		return editor.commit();
	}

	public static boolean setSettingInt(int setting, int settingValue) {
		String settingKey = Utilities.getResourceString(setting);
		return setSettingInt(settingKey, settingValue);
	}

	public static boolean setSettingInt(String settingKey, int settingValue) {
		// Retrieve the preferences
		SharedPreferences preferences = Utilities.getPreferences();
		if (preferences == null) {
			return false;
		}
		
		// Edit the value
		Editor editor = preferences.edit();
		editor.putInt(settingKey, settingValue);
		return editor.commit();
	}

	public static boolean setSettingLong(int setting, long settingValue) {
		String settingKey = Utilities.getResourceString(setting);
		return setSettingLong(settingKey, settingValue);
	}

	public static boolean setSettingLong(String settingKey, long settingValue) {
		// Retrieve the preferences
		SharedPreferences preferences = Utilities.getPreferences();
		if (preferences == null) {
			return false;
		}
		
		// Edit the value
		Editor editor = preferences.edit();
		editor.putLong(settingKey, settingValue);
		return editor.commit();
	}

	public static boolean setSettingObject(int setting, Object settingValue) {
		String settingKey = Utilities.getResourceString(setting);
		return setSettingObject(settingKey, settingValue);
	}

	public static boolean setSettingObject(String settingKey, Object settingValue) {
		if (settingValue == null || settingValue instanceof String) {
			return setSettingString(settingKey, (String)settingValue);
		} else if (settingValue instanceof Boolean) {
			return setSettingBoolean(settingKey, (Boolean)settingValue);
		} else if (settingValue instanceof Integer) {
			return setSettingInt(settingKey, (Integer)settingValue);
		} else if (settingValue instanceof Long) {
			return setSettingLong(settingKey, (Long)settingValue);
		} else if (settingValue instanceof Float) {
			return setSettingFloat(settingKey, (Float)settingValue);
		}
		return false;
	}

	public static boolean setSettingString(int setting, String settingValue) {
		String settingKey = Utilities.getResourceString(setting);
		return setSettingString(settingKey, settingValue);
	}

	public static boolean setSettingString(String settingKey, String settingValue) {
		// Retrieve the preferences
		SharedPreferences preferences = Utilities.getPreferences();
		if (preferences == null) {
			return false;
		}
		
		// Edit the value
		Editor editor = preferences.edit();
		editor.putString(settingKey, settingValue);
		return editor.commit();
	}

	public static <T extends Enum<T> & IPreference> boolean setSettingEnum(int setting, T settingValue) {
		return settingValue != null && setSettingString(setting, settingValue.name());
	}

	public static <T extends Enum<T> & IPreference> boolean setSettingEnum(String settingKey, T settingValue) {
		return settingValue != null && setSettingString(settingKey, settingValue.name());
	}

	public static void registerPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
		SharedPreferences preferences = Utilities.getPreferences();
		if (preferences != null && listener != null) {
			sharedPreferenceChangeListeners.add(listener);
			preferences.registerOnSharedPreferenceChangeListener(listener);
		}
	}

	public static void unregisterPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
		SharedPreferences preferences = Utilities.getPreferences();
		if (preferences != null && listener != null) {
			preferences.unregisterOnSharedPreferenceChangeListener(listener);
			sharedPreferenceChangeListeners.remove(listener);
		}
	}
}