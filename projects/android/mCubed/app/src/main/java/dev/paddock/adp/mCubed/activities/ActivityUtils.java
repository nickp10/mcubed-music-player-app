package dev.paddock.adp.mCubed.activities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.compatibility.ActivityCompat;
import dev.paddock.adp.mCubed.listeners.MediaAssociateListener;
import dev.paddock.adp.mCubed.receivers.ClientReceiver;
import dev.paddock.adp.mCubed.receivers.IProvideClientReceiver;
import dev.paddock.adp.mCubed.services.PlaybackClient;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class ActivityUtils {
	private static final List<Activity> activities = new ArrayList<Activity>();

	public static <E extends Activity & IActivity> void onCreate(E activity, Bundle savedInstanceState) {
		// Initialize the creation
		Utilities.pushContext(activity);
		try {
			// Enable the back button on the action bar
			ActivityCompat.getActionBar(activity).setDisplayHomeAsUpEnabled(true);
			
			// Set the content view and retrieve the views
			int layoutID = activity.getLayoutID();
			if (layoutID != 0) {
				activity.setContentView(layoutID);
			}
			activity.findViews();
			
			// Register receiver and listeners
			if (!activities.contains(activity)) {
				// Do some initializations
				activities.add(activity);
				activity.setupViews();
				activity.registerListeners();
				
				// Listen for service updates
				registerClientReceivers(activity, activity.getClientReceivers());
				
				// Notify the service of the new client
				PlaybackClient.startService();
			}
			
			// Handle the intent's extra data
			Intent intent = activity.getIntent();
			if (intent != null) {
				Bundle extras = intent.getExtras();
				if (extras != null) {
					activity.handleExtras(extras);
				}
			}
			
			// Update the views
			activity.updateViews();
		} finally {
			Utilities.popContext();
		}
	}

	public static <E extends Activity & IActivity> void onDestroy(E activity) {
		// Start the destroy process
		Utilities.pushContext(activity);
		try {
			// De-initialize
			activities.remove(activity);
			
			// Unregister receivers and listeners
			unregisterClientReceivers(activity, activity.getClientReceivers());
		} finally {
			Utilities.popContext();
		}
	}

	public static <E extends Activity & IActivity> void onResume(E activity) {
		// Start the resume process
		Utilities.pushContext(activity);
		try {
			// Re-associate the media player
			new MediaAssociateListener().register();
		} finally {
			Utilities.popContext();
		}
	}

	public static <E extends Activity & IActivity> boolean onCreateOptionsMenu(E activity, Menu menu) {
		Utilities.pushContext(activity);
		try {
			List<Integer> menuOptions = activity.getMenuOptions();
			if (menuOptions != null) {
				int order = 1;
				for (Integer menuOption : menuOptions) {
					int itemID = menuOption.intValue();
					ActivityMenu.addMenuItem(menu, itemID, order);
					order++;
				}
			}
			return true;
		} finally {
			Utilities.popContext();
		}
	}

	public static <E extends Activity & IActivity> boolean onOptionsItemSelected(E activity, MenuItem item) {
		Utilities.pushContext(activity);
		try {
			if (item.getItemId() == android.R.id.home) {
				activity.onBackPressed();
				return true;
			}
			return ActivityMenu.runMenuItem(activity, item.getItemId());
		} finally {
			Utilities.popContext();
		}
	}

	public static List<Integer> getMenuOptions(int exceptMenu) {
		if (Build.VERSION.SDK_INT >= 11) {
			return getMenuOptionsAPI11AndUp(exceptMenu);
		} else {
			return getMenuOptionsAPIBefore11(exceptMenu);
		}
	}

	private static List<Integer> getMenuOptionsAPI11AndUp(int exceptMenu) {
		List<Integer> options = new ArrayList<Integer>(Arrays.asList(
			Schema.MN_NOWPLAYING, Schema.MN_LIBRARY, Schema.MN_PLAYALL,
			Schema.MN_SETTINGS, Schema.MN_ABOUT, Schema.MN_FEEDBACK,
			Schema.MN_HELP, Schema.MN_EXIT
		));
		options.remove(Integer.valueOf(exceptMenu));
		return options;
	}

	private static List<Integer> getMenuOptionsAPIBefore11(int exceptMenu) {
		List<Integer> options = null;
		if (exceptMenu == Schema.MN_ABOUT || exceptMenu == Schema.MN_FEEDBACK || exceptMenu == Schema.MN_HELP) {
			options = new ArrayList<Integer>(Arrays.asList(
				Schema.MN_NOWPLAYING, Schema.MN_PLAYALL, Schema.MN_SETTINGS,
				Schema.MN_LIBRARY, Schema.MN_EXIT, Schema.MN_ABOUT,
				Schema.MN_FEEDBACK, Schema.MN_HELP
			));
		} else {
			options = new ArrayList<Integer>(Arrays.asList(
				Schema.MN_NOWPLAYING, Schema.MN_LIBRARY, Schema.MN_PLAYALL,
				Schema.MN_SETTINGS, Schema.MN_HELP, Schema.MN_EXIT,
				Schema.MN_ABOUT, Schema.MN_FEEDBACK
			));
		}
		options.remove(Integer.valueOf(exceptMenu));
		return options;
	}

	public static <E extends Activity> void startActivity(Class<E> clazz) {
		startActivity(clazz, null);
	}

	public static <E extends Activity> void startActivity(Class<E> clazz, Serializable data) {
		startActivity(Utilities.getContext(), clazz, data);
	}

	public static <E extends Activity> void startActivity(Context context, Class<E> clazz) {
		startActivity(context, clazz, null);
	}

	public static <E extends Activity> void startActivity(Context context, Class<E> clazz, Serializable data) {
		Intent intent = new Intent(context, clazz);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		if (data != null) {
			intent.putExtra(Schema.I_PARAM_ACTIVITY_DATA, data);
		}
		context.startActivity(intent);
	}

	public static void startMainActivity() {
		startActivity(LibraryActivity.class);
	}

	public static void finishAllActivities() {
		for (Activity activity : activities) {
			activity.finish();
		}
	}

	public static void registerClientReceivers(Context context, IProvideClientReceiver... receivers) {
		registerClientReceivers(context, Arrays.asList(receivers));
	}

	public static void registerClientReceivers(Context context, List<IProvideClientReceiver> receivers) {
		if (receivers != null) {
			for (IProvideClientReceiver receiver : receivers) {
				ClientReceiver clientReceiver = receiver.getClientReceiver();
				if (clientReceiver != null) {
					IntentFilter filter = clientReceiver.getIntentFilter();
					if (filter != null) {
						context.registerReceiver(clientReceiver, filter);
					}
				}
			}
		}
	}

	public static void unregisterClientReceivers(Context context, IProvideClientReceiver... receivers) {
		unregisterClientReceivers(context, Arrays.asList(receivers));
	}

	public static void unregisterClientReceivers(Context context, List<IProvideClientReceiver> receivers) {
		if (receivers != null) {
			for (IProvideClientReceiver receiver : receivers) {
				ClientReceiver clientReceiver = receiver.getClientReceiver();
				if (clientReceiver != null) {
					context.unregisterReceiver(clientReceiver);
				}
			}
		}
	}
}