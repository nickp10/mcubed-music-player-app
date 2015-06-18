package dev.paddock.adp.mCubed.activities;

import java.util.List;

import android.os.Bundle;
import dev.paddock.adp.mCubed.receivers.IProvideClientReceiver;

public interface IActivity {
	/**
	 * Get the ID of the layout that the activity is associated with.
	 * @return The ID of the layout to inflate for the activity.
	 */
	int getLayoutID();
	
	/**
	 * Get the list of menu options that will be available with the activity.
	 * Each integer should represent an ID defined by ActivityMenu. Returning
	 * null or an empty list results in no menu options.
	 * @return The list of menu options that will be available with the activity.
	 */
	List<Integer> getMenuOptions();
	
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
	 * Get the receivers of the specified server intents. Return null or any empty list to not register to any.
	 * @return The client receivers of server intents.
	 */
	List<IProvideClientReceiver> getClientReceivers();
}