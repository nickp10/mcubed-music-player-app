package dev.paddock.adp.mCubed.model;

import java.util.Collections;
import java.util.List;

public class MediaGrouping {
	private MediaGroup group;
	private long id;
	private String name;
	
	public MediaGrouping(MediaGroup group, long id, String name) {
		this.group = group;
		this.id = id;
		this.name = name;
	}
	
	public List<MediaFile> getMediaFiles() {
		return getMediaFiles(null, null);
	}
	
	public List<MediaFile> getMediaFiles(WhereClause where, SortClause sort) {
		if (group == null) {
			return Collections.<MediaFile>emptyList();
		}
		return group.getMediaFilesForGrouping(this, where, sort);
	}
	
	public MediaGroup getGroup() {
		return group;
	}
	
	public long getID() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isMediaGroupAll() {
		return getGroup() == MediaGroup.All;
	}
	
	public static boolean equals(MediaGrouping first, MediaGrouping second) {
		if (first == null) {
			return second == null;
		} else if (second == null) {
			return false;
		} else {
			return first.getGroup() == second.getGroup() && first.getID() == second.getID();
		}
	}
}