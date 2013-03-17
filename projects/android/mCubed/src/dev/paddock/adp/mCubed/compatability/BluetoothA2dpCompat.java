package dev.paddock.adp.mCubed.compatability;

import dev.paddock.adp.mCubed.utilities.CompatibilityUtilities;

public class BluetoothA2dpCompat {
	// Public fields which map to constants in BluetoothA2dp
	public static String ACTION_CONNECTION_STATE_CHANGED;
	public static String ACTION_PLAYING_STATE_CHANGED;
	public static int STATE_NOT_PLAYING;
	public static int STATE_PLAYING;
	
	static {
		CompatibilityUtilities.loadStaticFields("android.bluetooth.BluetoothA2dp", BluetoothA2dpCompat.class);
	}
}
