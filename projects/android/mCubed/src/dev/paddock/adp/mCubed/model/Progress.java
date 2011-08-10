package dev.paddock.adp.mCubed.model;

import java.util.ArrayList;
import java.util.List;

import dev.paddock.adp.mCubed.utilities.Utilities;

public class Progress {
	private boolean allowChildTitle, isStarted, isFinished, isBlocking;
	private double value;
	private String id, title;
	private Progress parent;
	private final List<Progress> subProgress = new ArrayList<Progress>();
	
	public Progress() { }
	
	public boolean allowChildTitle() {
		return allowChildTitle;
	}
	public void setAllowChildTitle(boolean allowChildTitle) {
		this.allowChildTitle = allowChildTitle;
	}
	
	public boolean isActive() {
		if (isStarted() && !isFinished()) {
			return true;
		}
		for (Progress progress : subProgress) {
			if (progress.isActive()) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isStarted() {
		return isStarted;
	}
	private void setIsStarted(boolean isStarted) {
		this.isStarted = isStarted;
	}
	
	public boolean isFinished() {
		return isFinished;
	}
	private void setIsFinished(boolean isFinished) {
		this.isFinished = isFinished;
	}
	
	public boolean isBlocking() {
		if (isBlocking) {
			return true;
		}
		for (Progress progress : subProgress) {
			if (progress.isBlocking()) {
				return true;
			}
		}
		return false;
	}
	public void setIsBlocking(boolean isBlocking) {
		this.isBlocking = isBlocking;
	}
	
	public String getID() {
		return id;
	}
	public void setID(String id) {
		this.id = id;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public double getValue() {
		int count = subProgress.size();
		if (count == 0) {
			return value;
		}
		double total = 0d;
		for (Progress progress : subProgress) {
			total += progress.getValue();
		}
		return total / (double)count;
	}
	public void setValue(double value) {
		if (0d <= value && value <= 1d) {
			if (this.value != value) {
				this.value = value;
				boolean doNotify = subProgress.size() == 0;
				if (this.value == 1d) {
					end();
				}
				if (doNotify) {
					notify(getTitle());
				}
			}
		}
	}
	
	private void setParent(Progress parent) {
		this.parent = parent;
	}
	
	public List<Progress> getSubProgress() {
		return subProgress;
	}
	
	public void setSubIDs(String... subIDs) {
		subProgress.clear();
		appendSubIDs(subIDs);
	}
	
	public void appendSubIDs(String... subIDs) {
		for (String subID : subIDs) {
			Progress progress = new Progress();
			progress.setID(subID);
			progress.setParent(this);
			subProgress.add(progress);
		}
	}
	
	public void appendSubID(String subID, int count) {
		for (int i = 0; i < count; i++) {
			appendSubIDs(subID);
		}
	}
	
	public void start() {
		setIsStarted(true);
		setValue(0d);
	}
	
	public void end() {
		subProgress.clear();
		setIsFinished(true);
		setValue(1d);
	}
	
	private void notify(String title) {
		title = allowChildTitle() ? title : getTitle();
		if (parent == null) {
			double value = getValue() * 100d;
			value = isFinished() ? 100d : Math.max(0d, Math.min(value, 99d));
			Utilities.publishProgress(new PublishProgress(getID(), title, (byte)value, isBlocking()));
		} else {
			parent.notify(title);
		}
	}
}