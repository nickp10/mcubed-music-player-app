package dev.paddock.adp.mCubed.activities;

import android.app.Activity;
import android.support.v4.view.MenuItemCompat;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.model.Composite;
import dev.paddock.adp.mCubed.model.IMediaFileProvider;
import dev.paddock.adp.mCubed.model.MediaFile;
import dev.paddock.adp.mCubed.model.MediaGroup;
import dev.paddock.adp.mCubed.services.PlaybackClient;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.Delegate.Action;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class ActivityMenu {
	private static final SparseArray<ActivityMenuItem> menuItems = new SparseArray<ActivityMenuItem>();
	
	static {
		createMenuItem(Schema.MN_LIBRARY, "Library", R.attr.action_library, MenuItem.SHOW_AS_ACTION_IF_ROOM, new Action<Activity>() {
			public void act(Activity activity) {
				ActivityUtils.startActivity(activity, LibraryActivity.class);
			}
		});
		createMenuItem(Schema.MN_NOWPLAYING, "Now Playing", R.attr.action_nowplaying, MenuItem.SHOW_AS_ACTION_IF_ROOM, new Action<Activity>() {
			public void act(Activity activity) {
				ActivityUtils.startActivity(activity, NowPlayingActivity.class);
			}
		});
		createMenuItem(Schema.MN_PLAYALL, "Play All", R.attr.action_playall, MenuItem.SHOW_AS_ACTION_IF_ROOM, new Action<Activity>() {
			public void act(Activity activity) {
				if (App.isInitialized() && App.isMounted()) {
					// Handle playing a particular artist/album/genre/playlist
					MediaFileListActivity fileList = Utilities.cast(MediaFileListActivity.class, activity);
					if (fileList != null) {
						IMediaFileProvider mediaFileProvider = fileList.getMediaFileProvider();
						if (mediaFileProvider != null) {
							App.getNowPlaying().playComposite(mediaFileProvider.createComposite());
							return;
						}
					}
					
					// Handle playing a particular song
					MediaFileDetailsActivity fileDetails = Utilities.cast(MediaFileDetailsActivity.class, activity);
					if (fileDetails != null) {
						MediaFile mediaFile = fileDetails.getMediaFile();
						if (mediaFile != null) {
							App.getNowPlaying().playFile(mediaFile);
							return;
						}
					}
					
					// Otherwise, play everything
					App.getNowPlaying().playComposite(new Composite(MediaGroup.All.getGrouping(0)));
				}
			}
		});
		createMenuItem(Schema.MN_SETTINGS, "Settings", R.attr.action_settings, MenuItem.SHOW_AS_ACTION_NEVER, new Action<Activity>() {
			public void act(Activity activity) {
				ActivityUtils.startActivity(activity, PreferenceActivity.class);
			}
		});
		createMenuItem(Schema.MN_HELP, "Help", R.attr.action_help, MenuItem.SHOW_AS_ACTION_NEVER, new Action<Activity>() {
			public void act(Activity activity) {
				ActivityUtils.startActivity(activity, HelpActivity.class);
			}
		});
		createMenuItem(Schema.MN_EXIT, "Exit", R.attr.action_exit, MenuItem.SHOW_AS_ACTION_NEVER, new Action<Activity>() {
			public void act(Activity activity) {
				if (App.isInitialized()) {
					PlaybackClient.stopService();
					ActivityUtils.finishAllActivities();
				}
			}
		});
		createMenuItem(Schema.MN_ABOUT, "About", R.attr.action_about, MenuItem.SHOW_AS_ACTION_NEVER, new Action<Activity>() {
			public void act(Activity activity) {
				ActivityUtils.startActivity(activity, AboutActivity.class);
			}
		});
		createMenuItem(Schema.MN_FEEDBACK, "Feedback", R.attr.action_feedback, MenuItem.SHOW_AS_ACTION_NEVER, new Action<Activity>() {
			public void act(Activity activity) {
				ActivityUtils.startActivity(activity, FeedbackActivity.class);
			}
		});
	}
	
	private static void createMenuItem(int itemID, String title, int iconResource, int showAsActionFlags, Action<Activity> action) {
		menuItems.put(itemID, new ActivityMenuItem(itemID, title, iconResource, showAsActionFlags, action));
	}
	
	private static ActivityMenuItem getMenuItem(int itemID) {
		return menuItems.get(itemID);
	}

	public static void addMenuItem(Menu menu, int itemID, int order) {
		ActivityMenuItem item = getMenuItem(itemID);
		if (item != null) {
			MenuItemCompat.setShowAsAction(
				menu.add(Menu.NONE, item.getItemID(), order, item.getTitle()).
					setIcon(Utilities.resolveAttribute(item.getIconResource())),
				item.getShowAsActionFlags()
			);
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
