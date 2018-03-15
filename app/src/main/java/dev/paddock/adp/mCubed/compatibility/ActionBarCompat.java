package dev.paddock.adp.mCubed.compatibility;

import java.lang.reflect.Method;

public class ActionBarCompat {
	private Method setDisplayHomeAsUpEnabledMethod;
	private Object actualActionBar;
	
	public ActionBarCompat(Object actionBar) {
		actualActionBar = actionBar;
		
		if (actionBar != null) {
			Class<?> actionBarClass = actionBar.getClass();
			try {
				setDisplayHomeAsUpEnabledMethod = actionBarClass.getMethod("setDisplayHomeAsUpEnabled", boolean.class);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public void setDisplayHomeAsUpEnabled(boolean showHomeAsUp) {
		if (actualActionBar != null) {
			try {
				setDisplayHomeAsUpEnabledMethod.invoke(actualActionBar, showHomeAsUp);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
