package dev.paddock.adp.mCubed.compatibility;

import java.lang.reflect.Method;

public class ActionBarCompat {
	private Method setDisplayHomeAsUpEnabledMethod;
	private boolean hasAPIs;
	private Object actualActionBar;
	
	public ActionBarCompat(boolean hasAPIs, Object actionBar) {
		this.hasAPIs = hasAPIs;
		actualActionBar = actionBar;
		
		if (hasAPIs && actionBar != null) {
			Class<?> actionBarClass = actionBar.getClass();
			try {
				setDisplayHomeAsUpEnabledMethod = actionBarClass.getMethod("setDisplayHomeAsUpEnabled", boolean.class);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public void setDisplayHomeAsUpEnabled(boolean showHomeAsUp) {
		if (hasAPIs) {
			try {
				setDisplayHomeAsUpEnabledMethod.invoke(actualActionBar, showHomeAsUp);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
