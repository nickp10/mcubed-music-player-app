package dev.paddock.adp.mCubed.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.preferences.PlaybackAction;
import dev.paddock.adp.mCubed.preferences.SeekBarPreference;
import dev.paddock.adp.mCubed.receivers.HeadsetReceiver;
import dev.paddock.adp.mCubed.utilities.PreferenceManager;
import dev.paddock.adp.mCubed.utilities.Utilities;

/**
 * How to add new bluetooth preferences:
 * 
 * - Change createBluetoothPreferences() to create the preference
 * - Add necessary entries to strings.xml for the key, title, and summary
 * - Possibly change refresh() to handle the type of preference being created
 * - Change setupDefaults() to set the default value for the bluetooth preference
 */
public class BluetoothPreferences {
	private PreferenceActivity preferences;

	public BluetoothPreferences(PreferenceActivity preferences) {
		this.preferences = preferences;
	}

	public static PlaybackAction getAction(int resource) {
		Context context = Utilities.getContext();
		BluetoothDevice device = HeadsetReceiver.getLastDevice();
		String useDefaultsKey = getDeviceString(context, device, R.string.pref_bluetooth_defaults);
		if (PreferenceManager.getSettingBoolean(useDefaultsKey)) {
			return PreferenceManager.getSettingEnum(PlaybackAction.class, resource);
		}
		String resourceKey = getDeviceString(context, device, resource);
		return PreferenceManager.getSettingEnum(PlaybackAction.class, resourceKey);
	}

	private static String getDeviceID(BluetoothDevice device) {
		return device == null ? "" : device.getAddress();
	}

	private static String getDeviceString(Context context, BluetoothDevice device, int resource) {
		return getDeviceString(context, getDeviceID(device), resource);
	}

	private static String getDeviceString(Context context, String deviceID, int resource) {
		if (Utilities.isNullOrEmpty(deviceID)) {
			return getString(context, resource);
		} else {
			return String.format("%s_%s", getString(context, resource), deviceID);
		}
	}

	private static String getString(Context context, int resource) {
		return context.getString(resource);
	}

	private void refresh(String deviceID, int resource) {
		String deviceString = getDeviceString(preferences, deviceID, resource);
		Preference preference = preferences.findPreference(deviceString);
		if (preference instanceof ListPreference) {
			((ListPreference)preference).setValue(PreferenceManager.getSettingString(deviceString));
		} else if (preference instanceof SeekBarPreference) {
			((SeekBarPreference)preference).setValue(PreferenceManager.getSettingInt(deviceString));
		} else if (preference instanceof CheckBoxPreference) {
			((CheckBoxPreference)preference).setChecked(PreferenceManager.getSettingBoolean(deviceString));
		}
	}

	private void setupDefault(String deviceID, int resource) {
		setupDefault(deviceID, resource, PreferenceManager.getSettingObject(resource));
	}

	private void setupDefault(String deviceID, int resource, Object value) {
		PreferenceManager.setSettingObject(getDeviceString(preferences, deviceID, resource), value);
		refresh(deviceID, resource);
	}

	public Preference createBluetoothSwitchPreference() {
		final PreferenceScreen bluetoothSwitch = preferences.getPreferenceManager().createPreferenceScreen(preferences);
		bluetoothSwitch.setTitle(R.string.pref_bluetooth_switch_title);
		bluetoothSwitch.setSummary(R.string.pref_bluetooth_switch_summary_off);
		bluetoothSwitch.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				bluetoothSwitch.setEnabled(false);
				bluetoothSwitch.setSummary(R.string.pref_bluetooth_switch_summary_on);
				BluetoothAdapter.getDefaultAdapter().enable();
				return false;
			}
		});
		return bluetoothSwitch;
	}

	public Preference createDefaultPreference(final BluetoothDevice device) {
		final CheckBoxPreference defaultPreference = new CheckBoxPreference(preferences);
		defaultPreference.setTitle(R.string.pref_bluetooth_defaults_title);
		defaultPreference.setSummary(R.string.pref_bluetooth_defaults_summary);
		defaultPreference.setKey(getDeviceString(preferences, device, R.string.pref_bluetooth_defaults));
		defaultPreference.setDisableDependentsState(true);
		defaultPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				// If using the default bluetooth settings, then re-setup the initial values to the defaults.
				if (defaultPreference.isChecked()) {
					setupDefaults(device, true);
				}
				return true;
			}
		});
		return defaultPreference;
	}

	public Preference[] createBluetoothPreferences() {
		return createBluetoothPreferences(null);
	}

	public Preference[] createBluetoothPreferences(BluetoothDevice device) {
		ListPreference connectedPreference = new ListPreference(preferences);
		connectedPreference.setDialogTitle(R.string.pref_bluetooth_connected_title);
		connectedPreference.setTitle(R.string.pref_bluetooth_connected_title);
		connectedPreference.setSummary(R.string.pref_bluetooth_connected_summary);
		connectedPreference.setKey(getDeviceString(preferences, device, R.string.pref_bluetooth_connected));
		
		ListPreference disconnectedPreference = new ListPreference(preferences);
		disconnectedPreference.setDialogTitle(R.string.pref_bluetooth_disconnected_title);
		disconnectedPreference.setTitle(R.string.pref_bluetooth_disconnected_title);
		disconnectedPreference.setSummary(R.string.pref_bluetooth_disconnected_summary);
		disconnectedPreference.setKey(getDeviceString(preferences, device, R.string.pref_bluetooth_disconnected));
		
		preferences.setListPreferences(connectedPreference, PlaybackAction.values());
		preferences.setListPreferences(disconnectedPreference, PlaybackAction.values());
		
		return new Preference[] { connectedPreference, disconnectedPreference };
	}

	public void setupDefaults(BluetoothDevice device) {
		setupDefaults(device, false);
	}

	private void setupDefaults(BluetoothDevice device, boolean forceRefresh) {
		if (device != null) {
			String deviceID = getDeviceID(device);
			if (forceRefresh || !PreferenceManager.getSettingBoolean(getDeviceString(preferences, deviceID, R.string.pref_bluetooth_defaults_loaded))) {
				setupDefault(deviceID, R.string.pref_bluetooth_connected);
				setupDefault(deviceID, R.string.pref_bluetooth_disconnected);
				setupDefault(deviceID, R.string.pref_bluetooth_defaults, true);
				setupDefault(deviceID, R.string.pref_bluetooth_defaults_loaded, true);
			}
		}
	}
}