package dev.paddock.adp.mCubed.model;

import dev.paddock.adp.mCubed.utilities.Utilities;
import dev.paddock.adp.mCubed.utilities.XMLNode;

public class MediaPlayerState {
	private int seek;
	private MediaStatus status;
	private boolean isSeekValueAcquired, isSeekLockAcquired, isStatusValueAcquired, isStatusLockAcquired;
	
	public MediaPlayerState(int seek, boolean isSeekValueAcquired, boolean isSeekLockAcquired,
			MediaStatus status, boolean isStatusValueAcquired, boolean isStatusLockAcquired) {
		this.seek = seek;
		this.isSeekValueAcquired = isSeekValueAcquired;
		this.isSeekLockAcquired = isSeekLockAcquired;
		this.status = status;
		this.isStatusValueAcquired = isStatusValueAcquired;
		this.isStatusLockAcquired = isStatusLockAcquired;
	}
	
	public int getSeek() {
		return seek;
	}
	
	public MediaStatus getStatus() {
		return status;
	}
	
	public boolean isSeekValueAcquired() {
		return isSeekValueAcquired;
	}

	public boolean isSeekLockAcquired() {
		return isSeekLockAcquired;
	}

	public boolean isStatusValueAcquired() {
		return isStatusValueAcquired;
	}

	public boolean isStatusLockAcquired() {
		return isStatusLockAcquired;
	}
	
	public void setIsSeekValueAcquired(boolean isSeekValueAcquired) {
		this.isSeekValueAcquired = isSeekValueAcquired;
	}
	
	public void setIsStatusValueAcquired(boolean isStatusValueAcquired) {
		this.isStatusValueAcquired = isSeekValueAcquired;
	}
	
	public static MediaPlayerState fromXML(XMLNode node) {
		XMLNode seekNode = node.getChildNode("Seek");
		int seek = Utilities.parseInt(seekNode.getNodeText());
		boolean isSeekLockAcquired = Boolean.parseBoolean(seekNode.getAttribute("Lock"));
		boolean isSeekValueAcquired = Boolean.parseBoolean(seekNode.getAttribute("Acquired"));
		XMLNode statusNode = node.getChildNode("Status");
		String statusText = statusNode.getNodeText();
		MediaStatus status = null;
		if (!Utilities.isNullOrEmpty(statusText)) {
			status = MediaStatus.valueOf(statusText);
		}
		boolean isStatusLockAcquired = Boolean.parseBoolean(statusNode.getAttribute("Lock"));
		boolean isStatusValueAcquired = Boolean.parseBoolean(statusNode.getAttribute("Acquired"));
		return new MediaPlayerState(seek, isSeekValueAcquired, isSeekLockAcquired, status, isStatusValueAcquired, isStatusLockAcquired);
	}
	
	public XMLNode toXML(String nodeName) {
		XMLNode node = new XMLNode(nodeName);
		XMLNode seekNode = node.addChildNode("Seek");
		seekNode.setAttribute("Lock", Boolean.toString(isSeekLockAcquired()));
		seekNode.setAttribute("Acquired", Boolean.toString(isSeekValueAcquired()));
		seekNode.setNodeText(Integer.toString(getSeek()));
		XMLNode statusNode = node.addChildNode("Status");
		statusNode.setAttribute("Lock", Boolean.toString(isStatusLockAcquired()));
		statusNode.setAttribute("Acquired", Boolean.toString(isStatusValueAcquired()));
		if (status != null) {
			statusNode.setNodeText(status.name());
		}
		return node;
	}
}