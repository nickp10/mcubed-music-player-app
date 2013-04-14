package dev.paddock.adp.mCubed.compatibility;

import dev.paddock.adp.mCubed.utilities.CompatibilityUtilities;

public class BluetoothA2dpCompat {
	// Public fields which map to constants in BluetoothA2dp
	public static String ACTION_CONNECTION_STATE_CHANGED;
	
	static {
		CompatibilityUtilities.loadStaticFields("android.bluetooth.BluetoothA2dp", BluetoothA2dpCompat.class);
	}
}
