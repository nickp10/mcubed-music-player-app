package dev.paddock.adp.mCubed.activities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.receivers.ClientReceiver;
import dev.paddock.adp.mCubed.services.PlaybackClient;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class ActivityUtils {
	private static final List<Activity> activities = new ArrayList<Activity>();
	
	public static <E extends Activity & IActivity> void onCreate(E activity, Bundle savedInstanceState) {
		// Initialize the creation
		Utilities.pushContext(activity);
		try {
			// Set the content view and retrieve the views
			activity.setContentView(activity.getLayoutID());
			activity.findViews();
			
			// Register receiver and listeners
			if (!activities.contains(activity)) {
				// Do some initializations
				activities.add(activity);
				activity.setupViews();
				activity.registerListeners();
				
				// Listen for service updates
				ClientReceiver receiver = activity.getClientReceiver();
				if (receiver != null) {
					IntentFilter filter = receiver.getIntentFilter();
					if (filter != null) {
						activity.registerReceiver(receiver, filter);
					}
				}
				
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
			ClientReceiver receiver = activity.getClientReceiver();
			if (receiver != null) {
				activity.unregisterReceiver(receiver);
			}
		} finally {
			Utilities.popContext();
		}
	}
	
	public static <E extends Activity & IActivity> boolean onCreateOptionsMenu(E activity, Menu menu) {
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
	}
	
	public static <E extends Activity & IActivity> boolean onPrepareOptionsMenu(E activity, Menu menu) {
		return App.isInitialized();
	}
	
	public static <E extends Activity & IActivity> boolean onOptionsItemSelected(E activity, MenuItem item) {
		Utilities.pushContext(activity);
		try {
			return ActivityMenu.runMenuItem(activity, item.getItemId());
		} finally {
			Utilities.popContext();
		}
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
}