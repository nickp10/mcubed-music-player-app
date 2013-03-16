package dev.paddock.adp.mCubed.compatability;

import android.view.KeyEvent;
import dev.paddock.adp.mCubed.utilities.CompatibilityUtilities;

public class KeyEventCompat {
	// Public fields which map to constants in KeyEvent
	public static int KEYCODE_MEDIA_PAUSE;
	public static int KEYCODE_MEDIA_PLAY;
	
	static {
		CompatibilityUtilities.loadStaticFields(KeyEvent.class, KeyEventCompat.class);
	}
}
