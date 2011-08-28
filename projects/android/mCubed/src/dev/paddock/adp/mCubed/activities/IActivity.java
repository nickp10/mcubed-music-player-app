package dev.paddock.adp.mCubed.activities;

import android.os.Bundle;
import dev.paddock.adp.mCubed.receivers.ClientReceiver;

public interface IActivity {
	/**
	 * Get the ID of the layout that the activity is associated with.
	 * @return The ID of the layout to inflate for the activity.
	 */
	int getLayoutID();
	
	/**
	 * Find the views from in the layout and assign them to member variables.
	 */
	void findViews();
	
	/**
	 * Setup the views for the first time. Once setup, the views will never be setup again.
	 */
	void setupViews();
	
	/**
	 * Update all the views so that they have the latest information.
	 */
	void updateViews();
	
	/**
	 * Register onclick and other listeners to the views associated with the activity.
	 */
	void registerListeners();
	
	/**
	 * Handle the intent extras bundle which will be called in the onCreate utility before updating the views.
	 * @param extras The bundle of extra bundle provided with the intent that started the activity.
	 */
	void handleExtras(Bundle extras);
	
	/**
	 * Get the receiver of the specified server intents. Return null to not register to any.
	 * @return The client receiver of server intents.
	 */
	ClientReceiver getClientReceiver();
}