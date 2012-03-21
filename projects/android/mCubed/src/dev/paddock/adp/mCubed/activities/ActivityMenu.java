package dev.paddock.adp.mCubed.activities;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.view.Menu;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.model.Composite;
import dev.paddock.adp.mCubed.model.MediaGroup;
import dev.paddock.adp.mCubed.services.PlaybackClient;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.Delegate.Action;

public class ActivityMenu {
	private static final Map<Integer, ActivityMenuItem> menuItems = new HashMap<Integer, ActivityMenuItem>();
	
	static {
		createMenuItem(Schema.MN_LIBRARY, "Library", R.drawable.menu_library, new Action<Activity>() {
			public void act(Activity activity) {
				ActivityUtils.startActivity(activity, LibraryActivity.class);
			}
			
		});
		createMenuItem(Schema.MN_NOWPLAYING, "Now Playing", R.drawable.menu_nowplaying, new Action<Activity>() {
			public void act(Activity activity) {
				ActivityUtils.startActivity(activity, NowPlayingActivity.class);
			}
			
		});
		createMenuItem(Schema.MN_PLAYALL, "Play All", R.drawable.menu_playall, new Action<Activity>() {
			public void act(Activity activity) {
				App.getNowPlaying().playComposite(new Composite(MediaGroup.All.getGrouping(0)));
			}
		});
		createMenuItem(Schema.MN_SETTINGS, "Settings", R.drawable.menu_settings, new Action<Activity>() {
			public void act(Activity activity) {
				ActivityUtils.startActivity(activity, PreferenceActivity.class);
			}
		});
		createMenuItem(Schema.MN_HELP, "Help", R.drawable.menu_help, new Action<Activity>() {
			public void act(Activity activity) {
				ActivityUtils.startActivity(activity, HelpActivity.class);
			}
		});
		createMenuItem(Schema.MN_EXIT, "Exit", R.drawable.menu_exit, new Action<Activity>() {
			public void act(Activity activity) {
				PlaybackClient.stopService();
				activity.finish();
			}
		});
		createMenuItem(Schema.MN_ABOUT, "About", R.drawable.menu_about, new Action<Activity>() {
			public void act(Activity activity) {
				ActivityUtils.startActivity(activity, AboutActivity.class);
			}
		});
		createMenuItem(Schema.MN_FEEDBACK, "Feedback", R.drawable.menu_feedback, new Action<Activity>() {
			public void act(Activity activity) {
				ActivityUtils.startActivity(activity, FeedbackActivity.class);
			}
		});
	}
	
	private static void createMenuItem(int itemID, String title, int iconResource, Action<Activity> action) {
		menuItems.put(itemID, new ActivityMenuItem(itemID, title, iconResource, action));
	}
	
	private static ActivityMenuItem getMenuItem(int itemID) {
		if (menuItems.containsKey(itemID)) {
			return menuItems.get(itemID);
		}
		return null;
	}
	
	public static void addMenuItem(Menu menu, int itemID, int order) {
		ActivityMenuItem item = getMenuItem(itemID);
		if (item != null) {
			menu.add(Menu.NONE, item.getItemID(), order, item.getTitle()).setIcon(item.getIconResource());
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
