package dev.paddock.adp.mCubed.activities;

import android.app.Activity;
import dev.paddock.adp.mCubed.utilities.Delegate.Action;

public class ActivityMenuItem {
	private final int itemID, iconResource, showAsActionFlags;
	private final String title;
	private final Action<Activity> action;
	
	public ActivityMenuItem(int itemID, String title, int iconResource, int showAsActionFlags, Action<Activity> action) {
		this.itemID = itemID;
		this.iconResource = iconResource;
		this.title = title;
		this.action = action;
		this.showAsActionFlags = showAsActionFlags;
	}
	
	public int getItemID() {
		return itemID;
	}
	
	public String getTitle() {
		return title;
	}
	
	public int getIconResource() {
		return iconResource;
	}
	
	public int getShowAsActionFlags() {
		return showAsActionFlags;
	}
	
	public void runAction(Activity activity) {
		action.act(activity);
	}
}