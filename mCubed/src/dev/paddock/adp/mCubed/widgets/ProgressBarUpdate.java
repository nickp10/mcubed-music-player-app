package dev.paddock.adp.mCubed.widgets;

public class ProgressBarUpdate {
	private int max, progress;
	private boolean isIndeterminate;
	
	public ProgressBarUpdate(int max, int progress, boolean isIndeterminate) {
		this.max = max;
		this.progress = progress;
		this.isIndeterminate = isIndeterminate;
	}
	
	public int getMax() {
		return max;
	}
	public int getProgress() {
		return progress;
	}
	public boolean isIndeterminate() {
		return isIndeterminate;
	}
}