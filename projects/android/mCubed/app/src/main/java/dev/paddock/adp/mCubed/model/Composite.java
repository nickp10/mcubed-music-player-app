package dev.paddock.adp.mCubed.model;

import dev.paddock.adp.mCubed.utilities.Utilities;

public class Composite {
	private final MediaGrouping grouping;
	private final ListAction action;
	
	public static Composite parse(String str) {
		if (!Utilities.isNullOrEmpty(str)) {
			String[] parts = str.split(",");
			if (parts.length == 3) {
				try {
					MediaGroup group = MediaGroup.valueOf(parts[0]);
					long id = Utilities.parseLong(parts[1]);
					MediaGrouping grouping = group.getGrouping(id);
					ListAction action = ListAction.valueOf(parts[2]);
					return new Composite(grouping, action);
				} catch (Exception e) { }
			}
		}
		return null;
	}
	
	public Composite(MediaGrouping grouping) {
		this(grouping, ListAction.Add);
	}
	
	public Composite(MediaGrouping grouping, ListAction action) {
		this.grouping = grouping;
		this.action = action;
	}
	
	public MediaGrouping getGrouping() {
		return grouping;
	}
	
	public ListAction getAction() {
		return action;
	}
	
	public boolean isMediaGroupAll() {
		MediaGrouping grouping = getGrouping();
		return grouping != null && grouping.isMediaGroupAll();
	}
	
	public static boolean equals(Composite first, Composite second) {
		if (first == null) {
			return second == null;
		} else if (second == null) {
			return false;
		} else {
			return first.getAction() == second.getAction() && MediaGrouping.equals(first.getGrouping(), second.getGrouping());
		}
	}
	
	@Override
	public String toString() {
		return getGrouping().getGroup().name() + "," + getGrouping().getID() + "," + getAction().name();
	}
}