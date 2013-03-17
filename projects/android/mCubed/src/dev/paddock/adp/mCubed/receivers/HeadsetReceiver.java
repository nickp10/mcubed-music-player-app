package dev.paddock.adp.mCubed.receivers;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.compatability.BluetoothA2dpCompat;
import dev.paddock.adp.mCubed.compatability.BluetoothProfileCompat;
import dev.paddock.adp.mCubed.model.DelayedTask;
import dev.paddock.adp.mCubed.model.NotificationArgs;
import dev.paddock.adp.mCubed.services.PlaybackServer;
import dev.paddock.adp.mCubed.utilities.PropertyManager;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class HeadsetReceiver extends BroadcastReceiver implements IReceiver {
	private static final HeadsetReceiver instance = new HeadsetReceiver();
	private boolean isBluetoothConnected;
	private boolean isHeadphonesConnected;
	
	/**
	 * Prevents external instances of a HeadsetReceiver.
	 */
	private HeadsetReceiver() { }
	
	public static HeadsetReceiver getInstance() {
		return instance;
	}
	
	public boolean isHeadphonesConnected() {
		return isHeadphonesConnected;
	}
	private void setHeadphonesConnected(boolean isHeadphonesConnected) {
		if (this.isHeadphonesConnected != isHeadphonesConnected) {
			NotificationArgs args = new NotificationArgs(this, "HeadphonesConnected", this.isHeadphonesConnected, isHeadphonesConnected);
			PropertyManager.notifyPropertyChanging(args);
			this.isHeadphonesConnected = isHeadphonesConnected;
			PropertyManager.notifyPropertyChanged(args);
			PlaybackServer.propertyChanged(0, Schema.PROP_HEADPHONE, this.isHeadphonesConnected);
		}
	}
	
	public boolean isBluetoothConnected() {
		return isBluetoothConnected;
	}
	private void setBluetoothConnectedDelayed(final boolean isBluetoothConnected) {
		if (isBluetoothConnected) {
			new DelayedTask(Utilities.getContext(), new Runnable() {
				public void run() {
					setBluetoothConnected(isBluetoothConnected);
				}
			}, 7000);
		} else {
			setBluetoothConnected(isBluetoothConnected);
		}
	}
	private void setBluetoothConnected(boolean isBluetoothConnected) {
		if (this.isBluetoothConnected != isBluetoothConnected) {
			NotificationArgs args = new NotificationArgs(this, "BluetoothConnected", this.isBluetoothConnected, isBluetoothConnected);
			PropertyManager.notifyPropertyChanging(args);
			this.isBluetoothConnected = isBluetoothConnected;
			PropertyManager.notifyPropertyChanged(args);
			PlaybackServer.propertyChanged(0, Schema.PROP_BLUETOOTH, this.isBluetoothConnected);
		}
	}
	
	private boolean isBluetoothAudio(BluetoothDevice device) {
		if (device != null) {
			BluetoothClass bluetoothClass = device.getBluetoothClass();
			if (bluetoothClass != null) {
				return bluetoothClass.hasService(BluetoothClass.Service.AUDIO);
			}
		}
		return false;
	}
	
	@Override
	public IntentFilter getIntentFilter() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
		if (BluetoothA2dpCompat.ACTION_CONNECTION_STATE_CHANGED == null) {
			intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
			intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		} else {
			intentFilter.addAction(BluetoothA2dpCompat.ACTION_CONNECTION_STATE_CHANGED);
		}
		intentFilter.setPriority(Integer.MAX_VALUE);
		return intentFilter;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Utilities.pushContext(context);
		try {
			String action = intent.getAction();
			if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
				Bundle extras = intent.getExtras();
				if (extras != null) {
					int headphoneState = extras.getInt("state");
					setHeadphonesConnected(headphoneState != 0);
				}
			} else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (isBluetoothAudio(device)) {
					setBluetoothConnectedDelayed(true);
				}
			} else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (isBluetoothAudio(device)) {
					setBluetoothConnectedDelayed(false);
				}
			} else if (action != null && action.equals(BluetoothA2dpCompat.ACTION_CONNECTION_STATE_CHANGED)) {
				Bundle extras = intent.getExtras();
				if (extras != null) {
					int bluetoothState = extras.getInt(BluetoothProfileCompat.EXTRA_STATE);
					if (bluetoothState == BluetoothProfileCompat.STATE_CONNECTED) {
						setBluetoothConnected(true);
					} else if (bluetoothState == BluetoothProfileCompat.STATE_DISCONNECTED) {
						setBluetoothConnected(false);
					}
				}
			}
		} finally {
			Utilities.popContext();
		}
	}
}