package dev.paddock.adp.mCubed.compatibility;

import dev.paddock.adp.mCubed.utilities.CompatibilityUtilities;

public class BluetoothProfileCompat {
	// Public fields which map to constants in BluetoothProfile
	public static String EXTRA_STATE;
	
	static {
		CompatibilityUtilities.loadStaticFields("android.bluetooth.BluetoothProfile", BluetoothProfileCompat.class);
	}
}
