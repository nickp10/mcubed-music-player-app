package dev.paddock.adp.mCubed.model;

public class PublishProgress {
	private String id, title;
	private byte value;
	private boolean isBlocking;
	
	public PublishProgress(String id, String title, byte value, boolean isBlocking) {
		this.id = id;
		this.title = title;
		this.value = value;
		this.isBlocking = isBlocking;
	}
	
	public String getID() {
		return id;
	}
	public String getTitle() {
		return title;
	}
	public byte getValue() {
		return value;
	}
	public boolean isBlocking() {
		return isBlocking;
	}
}