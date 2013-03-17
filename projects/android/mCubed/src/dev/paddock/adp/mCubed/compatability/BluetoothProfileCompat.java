package dev.paddock.adp.mCubed.compatability;

import dev.paddock.adp.mCubed.utilities.CompatibilityUtilities;

public class BluetoothProfileCompat {
	// Public fields which map to constants in BluetoothProfile
	public static String EXTRA_PREVIOUS_STATE;
	public static String EXTRA_STATE;
	public static int STATE_CONNECTED;
	public static int STATE_CONNECTING;
	public static int STATE_DISCONNECTED;
	public static int STATE_DISCONNECTING;
	
	static {
		CompatibilityUtilities.loadStaticFields("android.bluetooth.BluetoothProfile", BluetoothProfileCompat.class);
	}
}
