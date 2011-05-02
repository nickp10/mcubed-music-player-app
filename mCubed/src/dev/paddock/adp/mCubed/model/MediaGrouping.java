package dev.paddock.adp.mCubed.model;

import java.util.Collections;
import java.util.List;

import dev.paddock.adp.mCubed.utilities.Utilities;

public class MediaGrouping {
	private MediaGroup group;
	private long id;
	private String name;
	
	public static MediaGrouping parse(String str) {
		if (!Utilities.isNullOrEmpty(str)) {
			String[] parts = str.split(",");
			if (parts.length == 2) {
				try {
					MediaGroup group = MediaGroup.valueOf(parts[0]);
					long id = Long.parseLong(parts[1]);
					return group.getGrouping(id);
				} catch (Exception e) { }
			}
		}
		return null;
	}
	
	public MediaGrouping(MediaGroup group, long id, String name) {
		this.group = group;
		this.id = id;
		this.name = name;
	}
	
	public List<MediaFile> getMediaFiles() {
		if (group == null) {
			return Collections.<MediaFile>emptyList();
		}
		return group.getMediaFilesForGrouping(this);
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
	
	@Override
	public String toString() {
		return group.toString() + "," + id;
	}
}