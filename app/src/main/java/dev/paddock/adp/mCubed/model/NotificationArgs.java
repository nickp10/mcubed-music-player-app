package dev.paddock.adp.mCubed.model;

public class NotificationArgs {
	private Object instance, oldValue, newValue;
	private String property;
	
	public NotificationArgs(Object instance, String property, Object oldValue, Object newValue) {
		this.instance = instance;
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.property = property;
	}

	public Object getInstance() {
		return instance;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public Object getNewValue() {
		return newValue;
	}

	public String getProperty() {
		return property;
	}
}