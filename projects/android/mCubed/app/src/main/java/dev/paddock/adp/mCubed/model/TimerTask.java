package dev.paddock.adp.mCubed.model;

import android.os.Handler;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class TimerTask {
	private Runnable task;
	private long repeatMillis;
	private boolean isStarted;
	private final Handler handler = Utilities.getHandler();
	private final Runnable handlerTask = new Runnable() {
		@Override
		public void run() {
			executeTask();
			handler.postDelayed(this, repeatMillis);
		}
	};
	
	public TimerTask(Runnable task, long repeatMillis) {
		this.task = task;
		this.repeatMillis = repeatMillis;
	}
	
	public long getRepeatMillis() {
		return repeatMillis;
	}
	
	public void setRepeatMillis(long repeatMillis) {
		this.repeatMillis = repeatMillis;
	}
	
	public Runnable getTask() {
		return task;
	}
	
	public void setTask(Runnable task) {
		this.task = task;
		if (task == null) {
			stop();
		}
	}
	
	public void executeTask() {
		Runnable task = this.task;
		if (task != null) {
			task.run();
		}
	}
	
	public boolean isStarted() {
		return isStarted;
	}
	
	public void start() {
		start(false);
	}
	
	public void start(boolean executeTask) {
		if (!isStarted) {
			isStarted = true;
			handler.removeCallbacks(handlerTask);
			if (executeTask) {
				handlerTask.run();
			} else {
				handler.postDelayed(handlerTask, repeatMillis);
			}
		}
	}
	
	public void stop() {
		stop(false);
	}
	
	public void stop(boolean executeTask) {
		if (isStarted) {
			isStarted = false;
			handler.removeCallbacks(handlerTask);
			if (executeTask) {
				executeTask();
			}
		}
	}
}