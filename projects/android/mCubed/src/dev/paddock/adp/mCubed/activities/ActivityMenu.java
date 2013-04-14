package dev.paddock.adp.mCubed.activities;

import android.app.Activity;
import android.support.v4.view.MenuItemCompat;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.model.Composite;
import dev.paddock.adp.mCubed.model.MediaGroup;
import dev.paddock.adp.mCubed.services.PlaybackClient;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.Delegate.Action;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class ActivityMenu {
	private static final SparseArray<ActivityMenuItem> menuItems = new SparseArray<ActivityMenuItem>();
	
	static {
		createMenuItem(Schema.MN_LIBRARY, "Library", R.attr.action_library, new Action<Activity>() {
			public void act(Activity activity) {
				ActivityUtils.startActivity(activity, LibraryActivity.class);
			}
		});
		createMenuItem(Schema.MN_NOWPLAYING, "Now Playing", R.attr.action_nowplaying, new Action<Activity>() {
			public void act(Activity activity) {
				ActivityUtils.startActivity(activity, NowPlayingActivity.class);
			}
		});
		createMenuItem(Schema.MN_PLAYALL, "Play All", R.attr.action_playall, new Action<Activity>() {
			public void act(Activity activity) {
				if (App.isInitialized() && App.isMounted()) {
					App.getNowPlaying().playComposite(new Composite(MediaGroup.All.getGrouping(0)));
				}
			}
		});
		createMenuItem(Schema.MN_SETTINGS, "Settings", R.attr.action_settings, new Action<Activity>() {
			public void act(Activity activity) {
				ActivityUtils.startActivity(activity, PreferenceActivity.class);
			}
		});
		createMenuItem(Schema.MN_HELP, "Help", R.attr.action_help, new Action<Activity>() {
			public void act(Activity activity) {
				ActivityUtils.startActivity(activity, HelpActivity.class);
			}
		});
		createMenuItem(Schema.MN_EXIT, "Exit", R.attr.action_exit, new Action<Activity>() {
			public void act(Activity activity) {
				if (App.isInitialized()) {
					PlaybackClient.stopService();
					ActivityUtils.finishAllActivities();
				}
			}
		});
		createMenuItem(Schema.MN_ABOUT, "About", R.attr.action_about, new Action<Activity>() {
			public void act(Activity activity) {
				ActivityUtils.startActivity(activity, AboutActivity.class);
			}
		});
		createMenuItem(Schema.MN_FEEDBACK, "Feedback", R.attr.action_feedback, new Action<Activity>() {
			public void act(Activity activity) {
				ActivityUtils.startActivity(activity, FeedbackActivity.class);
			}
		});
	}
	
	private static void createMenuItem(int itemID, String title, int iconResource, Action<Activity> action) {
		menuItems.put(itemID, new ActivityMenuItem(itemID, title, iconResource, action));
	}
	
	private static ActivityMenuItem getMenuItem(int itemID) {
		return menuItems.get(itemID);
	}

	private static int resolveResource(int resource) {
		TypedValue value = new TypedValue();
		Utilities.getContext().getTheme().resolveAttribute(resource, value, true);
		return value.resourceId;
	}

	public static void addMenuItem(Menu menu, int itemID, int order) {
		ActivityMenuItem item = getMenuItem(itemID);
		if (item != null) {
			MenuItem menuItem = menu.add(Menu.NONE, item.getItemID(), order, item.getTitle()).setIcon(resolveResource(item.getIconResource()));
			MenuItemCompat.setShowAsAction(menuItem, 1);
		}
	}

	public static boolean runMenuItem(Activity activity, int itemID) {
		ActivityMenuItem item = getMenuItem(itemID);
		if (item != null) {
			item.runAction(activity);
			return true;
		}
		return false;
	}
}
