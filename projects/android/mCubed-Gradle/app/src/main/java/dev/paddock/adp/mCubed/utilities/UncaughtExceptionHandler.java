package dev.paddock.adp.mCubed.utilities;

import android.content.Context;

public class UncaughtExceptionHandler  implements java.lang.Thread.UncaughtExceptionHandler {
	private final Context context;
    private final java.lang.Thread.UncaughtExceptionHandler defaultHandler;

    public UncaughtExceptionHandler(Context context) {
    	this.context = context;
		this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
	}

	@Override
	public void uncaughtException(Thread thread, Throwable e) {
		Utilities.pushContext(context);
		try {
			Log.e(e);
			defaultHandler.uncaughtException(thread, e);
		} finally {
			Utilities.popContext();
		}
	}
}