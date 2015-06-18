package dev.paddock.adp.mCubed.compatibility;

import java.lang.reflect.Method;

import android.app.Dialog;

public class DialogCompat {
	private static Method getActionBarMethod;
	private static boolean hasAPIs;

	static {
		try {
			getActionBarMethod = Dialog.class.getMethod("getActionBar");
			hasAPIs = true;
		} catch (NoSuchMethodException e) {
			// Silently fail when running on an OS before API 11.
		}
	}
	
	public static ActionBarCompat getActionBar(Dialog dialog) {
		if (hasAPIs) {
			try {
				return new ActionBarCompat(getActionBarMethod.invoke(dialog));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return new ActionBarCompat(null);
	}
}
