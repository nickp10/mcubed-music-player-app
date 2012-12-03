package dev.paddock.adp.mCubed.model;

import android.content.Context;
import android.os.Handler;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class DelayedTask implements Runnable {
	private Context context;
	private Runnable task;
	private final Handler handler = Utilities.getHandler();
	
	public DelayedTask(Context context, Runnable task, long delayMillis) {
		this.context = context.getApplicationContext();
		this.task = task;
		handler.postDelayed(this, delayMillis);
	}
	
	public void run() {
		Utilities.pushContext(context);
		try {
			task.run();
		} finally {
			Utilities.popContext();
		}
	}
}
