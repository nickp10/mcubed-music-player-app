package dev.paddock.adp.mCubed.compatibility;

import java.lang.reflect.Method;

import android.app.Activity;

public class ActivityCompat {
	private static Method getActionBarMethod;
	private static boolean hasAPIs;

	static {
		try {
			getActionBarMethod = Activity.class.getMethod("getActionBar");
			hasAPIs = true;
		} catch (NoSuchMethodException e) {
			// Silently fail when running on an OS before API 11.
		}
	}
	
	public static ActionBarCompat getActionBar(Activity activity) {
		if (hasAPIs) {
			try {
				return new ActionBarCompat(getActionBarMethod.invoke(activity));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return new ActionBarCompat(null);
	}
}
