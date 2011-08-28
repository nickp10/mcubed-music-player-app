package dev.paddock.adp.mCubed.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import dev.paddock.adp.mCubed.utilities.Utilities;

import android.net.Uri;

public class MediaGrouping implements IMediaFileProvider, Serializable {
	private static final long serialVersionUID = 0;
	private MediaGroup group;
	private long id;
	private String name;
	
	public MediaGrouping() { }
	
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
	
	public Uri getAlbumArt() {
		return group.getAlbumArt(id);
	}
	
	public MediaGroup getGroup() {
		return group;
	}
	
	public void setGroup(MediaGroup group) {
		this.group = group;
	}
	
	public long getID() {
		return id;
	}
	
	public void setID(long id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
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
	
	public static int compare(MediaGrouping first, MediaGrouping second) {
		if (first == null) {
			return second == null ? 0 : -1;
		} else if (second == null) {
			return 1;
		} else {
			return Utilities.stringCompare(first.getName(), second.getName(), true);
		}
	}
}