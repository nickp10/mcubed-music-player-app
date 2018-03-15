package dev.paddock.adp.mCubed.model;

import android.content.Context;
import android.os.Handler;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class DelayedTask implements Runnable {
	private Context context;
	private Runnable task;
	private final Handler handler = Utilities.getHandler();
	private final Object lock = new Object();
	
	public DelayedTask(Context context, Runnable task, long delayMillis) {
		this.context = context.getApplicationContext();
		this.task = task;
		handler.postDelayed(this, delayMillis);
	}
	
	public void cancel(boolean doRun) {
		runAndCancel(doRun);
	}
	
	public void run() {
		runAndCancel(true);
	}
	
	private void runAndCancel(boolean doRun) {
		if (task != null) {
			synchronized (lock) {
				if (doRun) {
					runTask();
				}
				disposeTask();
			}
		}
	}
	
	private void disposeTask() {
		task = null;
		handler.removeCallbacks(this);
	}
	
	private void runTask() {
		if (task != null) {
			Utilities.pushContext(context);
			try {
				task.run();
			} finally {
				Utilities.popContext();
			}
		}
	}
}
