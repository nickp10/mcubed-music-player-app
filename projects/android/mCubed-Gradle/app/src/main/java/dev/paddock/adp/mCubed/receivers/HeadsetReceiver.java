package dev.paddock.adp.mCubed.receivers;

import java.util.Locale;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import dev.paddock.adp.mCubed.compatibility.BluetoothA2dpCompat;
import dev.paddock.adp.mCubed.compatibility.BluetoothProfileCompat;
import dev.paddock.adp.mCubed.model.DelayedTask;
import dev.paddock.adp.mCubed.model.NotificationArgs;
import dev.paddock.adp.mCubed.model.OutputMode;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.Log;
import dev.paddock.adp.mCubed.utilities.PropertyManager;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class HeadsetReceiver extends BroadcastReceiver implements IReceiver {
	private static boolean isBluetoothConnected, isHeadphonesConnected, isBluetoothOn;
	private static OutputMode outputMode = OutputMode.Speaker;
	private static BluetoothDevice lastDevice;
	
	static {
		Utilities.pushContext(App.getAppContext());
		try {
			AudioManager manager = App.getSystemService(AudioManager.class, Context.AUDIO_SERVICE);
			if (manager != null) {
				setBluetoothConnected(manager.isBluetoothA2dpOn());
				setHeadphonesConnected(manager.isWiredHeadsetOn());
			}
			BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
			if (adapter != null) {
				setBluetoothOn(adapter.isEnabled());
			}
		} finally {
			Utilities.popContext();
		}
	}

	/**
	 * Returns the last Bluetooth device that was used. If a Bluetooth device is disconnected, then this
	 * will store a reference to that device. This does not return the currently connected device. Rather
	 * it returns the Bluetooth device that received the last event.
	 * @returns The last Bluetooth device that was used.
	 */
	public static BluetoothDevice getLastDevice() {
		return lastDevice;
	}

	/**
	 * Retrieve whether or not bluetooth is turned on (not necessarily whether a device is connected)
	 * @return True if bluetooth is turned on, or false otherwise.
	 */
	public static boolean isBluetoothOn() {
		return isBluetoothOn;
	}
	
	private static void setBluetoothOn(boolean isBluetoothOn) {
		if (HeadsetReceiver.isBluetoothOn != isBluetoothOn) {
			NotificationArgs args = new NotificationArgs(HeadsetReceiver.class, "BluetoothOn", HeadsetReceiver.isBluetoothOn, isBluetoothOn);
			PropertyManager.notifyPropertyChanging(args);
			HeadsetReceiver.isBluetoothOn = isBluetoothOn;
			PropertyManager.notifyPropertyChanged(args);
		}
	}
	
	/**
	 * Retrieve whether or not a wired-headphone is connected to the device.
	 * @return True if a wired-headphone is connected to the device, or false otherwise.
	 */
	public static boolean isHeadphonesConnected() {
		return isHeadphonesConnected;
	}
	
	private static void setHeadphonesConnected(boolean isHeadphonesConnected) {
		if (HeadsetReceiver.isHeadphonesConnected != isHeadphonesConnected) {
			NotificationArgs args = new NotificationArgs(HeadsetReceiver.class, "HeadphonesConnected", HeadsetReceiver.isHeadphonesConnected, isHeadphonesConnected);
			PropertyManager.notifyPropertyChanging(args);
			HeadsetReceiver.isHeadphonesConnected = isHeadphonesConnected;
			updateOutputMode();
			PropertyManager.notifyPropertyChanged(args);
		}
	}
	
	/**
	 * Retrieve whether or not a bluetooth audio device is connected to the device.
	 * @return True if a bluetooth audio device is connected to the device, or false otherwise.
	 */
	public static boolean isBluetoothConnected() {
		return isBluetoothConnected;
	}
	
	private static void setBluetoothConnected(boolean isBluetoothConnected) {
		if (HeadsetReceiver.isBluetoothConnected != isBluetoothConnected) {
			NotificationArgs args = new NotificationArgs(HeadsetReceiver.class, "BluetoothConnected", HeadsetReceiver.isBluetoothConnected, isBluetoothConnected);
			PropertyManager.notifyPropertyChanging(args);
			HeadsetReceiver.isBluetoothConnected = isBluetoothConnected;
			updateOutputMode();
			PropertyManager.notifyPropertyChanged(args);
		}
	}
	
	private static void delayBluetoothConnected() {
		new DelayedTask(Utilities.getContext(), new Runnable() {
			public void run() {
				setBluetoothConnected(true);
			}
		}, 7000);
	}
	
	private static boolean isBluetoothAudio(BluetoothDevice device) {
		if (device != null) {
			BluetoothClass bluetoothClass = device.getBluetoothClass();
			if (bluetoothClass != null) {
				return bluetoothClass.hasService(BluetoothClass.Service.AUDIO);
			}
		}
		return false;
	}
	
	/**
	 * Get the output mode which is where the audio is currently routing.
	 * @return The output mode that identifies where the audio is currently routing.
	 */
	public static OutputMode getOutputMode() {
		return outputMode;
	}
	
	private static void setOutputMode(OutputMode outputMode) {
		if (outputMode != null && HeadsetReceiver.outputMode != outputMode) {
			NotificationArgs args = new NotificationArgs(HeadsetReceiver.class, "OutputMode", HeadsetReceiver.outputMode, outputMode);
			PropertyManager.notifyPropertyChanging(args);
			HeadsetReceiver.outputMode = outputMode;
			PropertyManager.notifyPropertyChanged(args);
		}
	}

	private static void updateOutputMode() {
		if (isHeadphonesConnected()) {
			setOutputMode(OutputMode.Headphones);
		} else if (isBluetoothConnected()) {
			setOutputMode(OutputMode.Bluetooth);
		} else {
			setOutputMode(OutputMode.Speaker);
		}
	}

	@Override
	public IntentFilter getIntentFilter() {
		// The ACTION_HEADSET_PLUG action must be registered via registerReceiver.
		// It cannot be registered in the manifest XML file.
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
		intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
		intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		if (BluetoothA2dpCompat.ACTION_CONNECTION_STATE_CHANGED != null) {
			intentFilter.addAction(BluetoothA2dpCompat.ACTION_CONNECTION_STATE_CHANGED);
		}
		intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		return intentFilter;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Utilities.pushContext(context);
		try {
			String action = intent.getAction();
			Log.i(String.format(Locale.US, "Handle HeadsetReceiver broadcast [Action=%s]", action));
			if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
				Bundle extras = intent.getExtras();
				if (extras != null) {
					int headphoneState = extras.getInt("state");
					setHeadphonesConnected(headphoneState != 0);
				}
			} else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
				Bundle extras = intent.getExtras();
				if (extras != null) {
					int bluetoothState = extras.getInt(BluetoothAdapter.EXTRA_STATE);
					setBluetoothOn(bluetoothState == BluetoothAdapter.STATE_ON);
				}
			} else if (BluetoothA2dpCompat.ACTION_CONNECTION_STATE_CHANGED == null) {
				if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					if (isBluetoothAudio(device)) {
						lastDevice = device;
						delayBluetoothConnected();
					}
				} else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					if (isBluetoothAudio(device)) {
						lastDevice = device;
						setBluetoothConnected(false);
					}
				}
			} else if (BluetoothA2dpCompat.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
				Bundle extras = intent.getExtras();
				if (extras != null) {
					int bluetoothState = extras.getInt(BluetoothProfileCompat.EXTRA_STATE);
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					if (bluetoothState == BluetoothProfile.STATE_CONNECTED) {
						lastDevice = device;
						setBluetoothConnected(true);
					} else if (bluetoothState == BluetoothProfile.STATE_DISCONNECTED) {
						lastDevice = device;
						setBluetoothConnected(false);
					}
				}
			}
		} finally {
			Utilities.popContext();
		}
	}
}