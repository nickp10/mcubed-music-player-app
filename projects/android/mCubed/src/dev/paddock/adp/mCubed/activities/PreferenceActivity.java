package dev.paddock.adp.mCubed.activities;

import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuItem;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.model.NotificationArgs;
import dev.paddock.adp.mCubed.preferences.NotificationVisibility;
import dev.paddock.adp.mCubed.preferences.PlayModeEnum;
import dev.paddock.adp.mCubed.preferences.PlaybackAction;
import dev.paddock.adp.mCubed.preferences.PreferenceEnum;
import dev.paddock.adp.mCubed.preferences.PreviousAction;
import dev.paddock.adp.mCubed.preferences.RepeatStatus;
import dev.paddock.adp.mCubed.receivers.HeadsetReceiver;
import dev.paddock.adp.mCubed.receivers.IProvideClientReceiver;
import dev.paddock.adp.mCubed.scrobble.MobileSessionRequest;
import dev.paddock.adp.mCubed.scrobble.ScrobbleException;
import dev.paddock.adp.mCubed.scrobble.ScrobbleService;
import dev.paddock.adp.mCubed.utilities.INotifyListener;
import dev.paddock.adp.mCubed.utilities.PreferenceManager;
import dev.paddock.adp.mCubed.utilities.PropertyManager;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class PreferenceActivity extends android.preference.PreferenceActivity implements IActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityUtils.onCreate(this, savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ActivityUtils.onDestroy(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		ActivityUtils.onResume(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu) &&
				ActivityUtils.onCreateOptionsMenu(this, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return ActivityUtils.onOptionsItemSelected(this, item) ||
				super.onOptionsItemSelected(item);
	}

	@Override
	public List<Integer> getMenuOptions() {
		return ActivityUtils.getMenuOptions(Schema.MN_SETTINGS);
	}

	@Override
	public int getLayoutID() {
		return 0;
	}

	@Override
	public void findViews() {
	}

	@Override
	public void setupViews() {
	}

	@Override
	public void updateViews() {
		// Create the preferences
		PreferenceManager.setupDefaults();
		addPreferencesFromResource(R.xml.preferences);

		setupBluetoothPreferences();

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

		// Get the Scrobble preferences
		preferenceKey = getString(R.string.pref_scrobble_screen);
		final PreferenceScreen scrobbleScreenPref = (PreferenceScreen) findPreference(preferenceKey);
		preferenceKey = getString(R.string.pref_scrobble_login);
		final Preference scrobbleLoginPref = findPreference(preferenceKey);
		preferenceKey = getString(R.string.pref_scrobble_logout);
		final Preference scrobbleLogoutPref = findPreference(preferenceKey);

		// Add a listener to the Scrobble key changed
		PreferenceManager.registerPreferenceChangeListener(new OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				Utilities.pushContext(PreferenceActivity.this);
				try {
					if (key.equals(Utilities.getResourceString(R.string.pref_scrobble_key))) {
						updateScrobbleVisibilities(scrobbleScreenPref, scrobbleLoginPref, scrobbleLogoutPref);
					}
				} finally {
					Utilities.popContext();
				}
			}
		});
		updateScrobbleVisibilities(scrobbleScreenPref, scrobbleLoginPref, scrobbleLogoutPref);

		// Add click listeners to the login and logout preferences
		scrobbleLoginPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Utilities.dispatchToBackgroundThread(PreferenceActivity.this, new Runnable() {
					@Override
					public void run() {
						try {
							ScrobbleService.sendRequest(new MobileSessionRequest("", ""));
						} catch (ScrobbleException e) {
							e.printStackTrace();
						}
						PreferenceManager.setSettingString(R.string.pref_scrobble_key, "bahbahbah");
					}
				});
				return true;
			}
		});
		scrobbleLogoutPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Utilities.pushContext(PreferenceActivity.this);
				try {
					PreferenceManager.setSettingString(R.string.pref_scrobble_key, null);
					return true;
				} finally {
					Utilities.popContext();
				}
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
	}

	private void setupBluetoothPreferences() {
		// Add a preference screen for each bluetooth device
		final BluetoothPreferences bluetoothPreferences = new BluetoothPreferences(this);
		final PreferenceScreen bluetoothScreen = (PreferenceScreen) findPreference(getString(R.string.pref_bluetooth_screen));
		final PreferenceCategory bluetoothDeviceCategory = new PreferenceCategory(this);
		bluetoothDeviceCategory.setTitle(R.string.pref_bluetooth_device_category_title);
		bluetoothScreen.removeAll();
		bluetoothScreen.addPreference(bluetoothDeviceCategory);
		if (HeadsetReceiver.isBluetoothOn()) {
			for (BluetoothDevice device : BluetoothAdapter.getDefaultAdapter().getBondedDevices()) {
				// Setup the default preferences
				bluetoothPreferences.setupDefaults(device);

				// Create a screen for the bluetooth device
				PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(this);
				screen.setTitle(device.getName());
				bluetoothDeviceCategory.addPreference(screen);

				// Add the default checkbox preference
				Preference defaultPreference = bluetoothPreferences.createDefaultPreference(device);
				screen.addPreference(defaultPreference);

				// Add each bluetooth preference
				for (Preference preference : bluetoothPreferences.createBluetoothPreferences(device)) {
					screen.addPreference(preference);
					preference.setDependency(defaultPreference.getKey());
				}
			}
		} else {
			bluetoothScreen.addPreference(bluetoothPreferences.createBluetoothSwitchPreference());
		}

		// Add each bluetooth preference for the default settings
		final PreferenceCategory bluetoothDefaultsCategory = new PreferenceCategory(this);
		bluetoothDefaultsCategory.setTitle(R.string.pref_bluetooth_defaults_category_title);
		bluetoothScreen.addPreference(bluetoothDefaultsCategory);
		for (Preference preference : bluetoothPreferences.createBluetoothPreferences()) {
			bluetoothDefaultsCategory.addPreference(preference);
		}
	}

	private void updateScrobbleVisibilities(PreferenceScreen scrobbleScreenPref, Preference scrobbleLoginPref, Preference scrobbleLogoutPref) {
		if (ScrobbleService.isLoggedIn()) {
			scrobbleScreenPref.removePreference(scrobbleLoginPref);
			scrobbleScreenPref.addPreference(scrobbleLogoutPref);
		} else {
			scrobbleScreenPref.addPreference(scrobbleLoginPref);
			scrobbleScreenPref.removePreference(scrobbleLogoutPref);
		}
	}

	@Override
	public void registerListeners() {
		PropertyManager.register(HeadsetReceiver.class, "BluetoothOn", new INotifyListener() {

			@Override
			public void propertyChanging(Object instance, NotificationArgs args) {
			}

			@Override
			public void propertyChanged(Object instance, NotificationArgs args) {
				PreferenceActivity.this.setupBluetoothPreferences();
			}
		});
	}

	@Override
	public void handleExtras(Bundle extras) {
	}

	@Override
	public List<IProvideClientReceiver> getClientReceivers() {
		return null;
	}

	private <T extends Enum<T> & PreferenceEnum.IPreference> void setListPreferences(int preferenceResource, T[] enumValues) {
		// Get the list preference view
		String preferenceKey = getString(preferenceResource);
		ListPreference list = (ListPreference) findPreference(preferenceKey);
		setListPreferences(list, enumValues);
	}

	public <T extends Enum<T> & PreferenceEnum.IPreference> void setListPreferences(ListPreference list, T[] enumValues) {
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