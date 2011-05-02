package dev.paddock.adp.mCubed.receivers;

import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.model.MediaStatus;
import dev.paddock.adp.mCubed.model.NotificationArgs;
import dev.paddock.adp.mCubed.services.PlaybackServer;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.PreferenceManager;
import dev.paddock.adp.mCubed.utilities.PropertyManager;
import dev.paddock.adp.mCubed.utilities.Utilities;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;

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
			PropertyManager.notifyPropertyChanging(this, "HeadphonesConnected", args);
			this.isHeadphonesConnected = isHeadphonesConnected;
			PropertyManager.notifyPropertyChanged(this, "HeadphonesConnected", args);
			PlaybackServer.propertyChanged(0, Schema.PROP_HEADPHONE, this.isHeadphonesConnected);
		}
	}
	
	public boolean isBluetoothConnected() {
		return isBluetoothConnected;
	}
	private void setBluetoothConnected(boolean isBluetoothConnected) {
		if (this.isBluetoothConnected != isBluetoothConnected) {
			NotificationArgs args = new NotificationArgs(this, "BluetoothConnected", this.isBluetoothConnected, isBluetoothConnected);
			PropertyManager.notifyPropertyChanging(this, "BluetoothConnected", args);
			this.isBluetoothConnected = isBluetoothConnected;
			PropertyManager.notifyPropertyChanged(this, "BluetoothConnected", args);
			PlaybackServer.propertyChanged(0, Schema.PROP_BLUETOOTH, this.isBluetoothConnected);
		}
	}
	
	@Override
	public IntentFilter getIntentFilter() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
		intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
		intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_BUTTON);
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
					setBluetoothConnected(true);
				}
			} else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (isBluetoothAudio(device)) {
					setBluetoothConnected(false);
				}
			} else if (Intent.ACTION_MEDIA_BUTTON.equals(action) && PreferenceManager.getSettingBoolean(R.string.pref_bluetooth_allow_controls)) {
				KeyEvent keyEvent = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
				if (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_UP) {
					int key = keyEvent.getKeyCode();
					boolean handled = true;
					if (key == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
						MediaStatus status = App.getPlayer().getStatus();
						if (status == MediaStatus.Play) {
							App.getPlayer().pause();
						} else {
							App.getPlayer().play();
						}
					} else if (key == KeyEvent.KEYCODE_MEDIA_NEXT) {
						App.movePlaybackNext();
					} else if (key == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
						App.movePlaybackPrev();
					} else if (key == KeyEvent.KEYCODE_MEDIA_STOP) {
						App.getPlayer().stop();
					} else {
						handled = false;
					}
					if (handled) {
						abortBroadcast();
					}
				}
			}
		} finally {
			Utilities.popContext();
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
}