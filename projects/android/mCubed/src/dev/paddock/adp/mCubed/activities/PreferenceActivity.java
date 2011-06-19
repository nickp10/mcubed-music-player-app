package dev.paddock.adp.mCubed.activities;

import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.preferences.NotificationVisibility;
import dev.paddock.adp.mCubed.preferences.PlayModeEnum;
import dev.paddock.adp.mCubed.preferences.PlaybackAction;
import dev.paddock.adp.mCubed.preferences.PreferenceEnum;
import dev.paddock.adp.mCubed.preferences.PreviousAction;
import dev.paddock.adp.mCubed.preferences.RepeatStatus;
import dev.paddock.adp.mCubed.utilities.PreferenceManager;
import dev.paddock.adp.mCubed.utilities.Utilities;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;

public class PreferenceActivity extends android.preference.PreferenceActivity {	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Setup the context
		Utilities.pushContext(this);
		try {
			// Setup the defaults
			PreferenceManager.setupDefaults();
			
			// Create the preferences
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preferences);
			
			// Get the previous smart condition preference
			String preferenceKey = getString(R.string.pref_previous_smart_condition);
			final Preference smartConditionPref = findPreference(preferenceKey);
			
			// Add a listener to the previous action changed
			preferenceKey = getString(R.string.pref_previous_action);
			findPreference(preferenceKey).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					smartConditionPref.setEnabled(newValue.equals(PreviousAction.Smart.name()));
					return true;
				}
			});
			
			// Get the overlay preference
			preferenceKey = getString(R.string.pref_open_overlay_player);
			final Preference overlayPref = findPreference(preferenceKey);
			
			// Add a listener to the notification visibility changed
			preferenceKey = getString(R.string.pref_notification_visibility);
			findPreference(preferenceKey).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					overlayPref.setEnabled(!newValue.equals(NotificationVisibility.Never.name()));
					return true;
				}
			});
			
			// Add options for the list preferences
			setListPreferences(R.string.pref_repeat_status, RepeatStatus.values());
			setListPreferences(R.string.pref_play_mode, PlayModeEnum.values());
			setListPreferences(R.string.pref_previous_action, PreviousAction.values());
			setListPreferences(R.string.pref_notification_visibility, NotificationVisibility.values());
			setListPreferences(R.string.pref_headphones_connected, PlaybackAction.values());
			setListPreferences(R.string.pref_headphones_disconnected, PlaybackAction.values());
			setListPreferences(R.string.pref_bluetooth_connected, PlaybackAction.values());
			setListPreferences(R.string.pref_bluetooth_disconnected, PlaybackAction.values());
		} finally {
			Utilities.popContext();
		}
	}
	
	private <T extends Enum<T> & PreferenceEnum.IPreference> void setListPreferences(int preferenceResource, T[] enumValues) {
		// Get the list preference view
		String preferenceKey = getString(preferenceResource);
		ListPreference list = (ListPreference)findPreference(preferenceKey);
		
		// Get the options
		String[] entryKeys = new String[enumValues.length];
		String[] entryValues = new String[enumValues.length];
		int current = 0;
		for (T enumValue : enumValues) {
			entryKeys[current] = enumValue.name();
			entryValues[current] = enumValue.getDisplay();
			current++;
		}
		
		// Set the options and the default value
		list.setEntries(entryValues);
		list.setEntryValues(entryKeys);
		
		// Send the initial update event
		OnPreferenceChangeListener listener = list.getOnPreferenceChangeListener();
		if (listener != null) {
			Object value = list.getValue();
			if (value != null) {
				listener.onPreferenceChange(list, value);
			}
		}
	}
}