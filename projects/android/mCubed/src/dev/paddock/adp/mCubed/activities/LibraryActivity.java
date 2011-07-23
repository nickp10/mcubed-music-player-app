package dev.paddock.adp.mCubed.activities;

import android.app.TabActivity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.controls.LibraryView;
import dev.paddock.adp.mCubed.model.MediaStatus;
import dev.paddock.adp.mCubed.receivers.ClientReceiver;
import dev.paddock.adp.mCubed.services.ClientCallback;
import dev.paddock.adp.mCubed.services.IClientCallback;
import dev.paddock.adp.mCubed.services.PlaybackClient;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class LibraryActivity extends TabActivity {
	private ClientReceiver clientReceiver;
	private IClientCallback clientCallback;
	private boolean isInitialized;
	private TabHost tabHost;
	
	/**
	 * Click listener for the play/pause button
	 */
//	private OnClickListener clickListener = new OnClickListener() {
//		@Override
//		public void onClick(View v) {
//			Utilities.pushContext(LibraryActivity.this);
//			try {
//				if (App.getPlayer().getStatus() == MediaStatus.Play) {
//					PlaybackClient.pause();
//				} else {
//					PlaybackClient.play();
//				}
//			} finally {
//				Utilities.popContext();
//			}
//		}
//	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Initialize the creation
		Utilities.pushContext(this);
		try {
			super.onCreate(savedInstanceState);
			
			// Set the content view and retrieve the views
			setContentView(R.layout.library);
			findViews();
			
			// Register receiver and listeners
			if (!isInitialized) {
				// Do some initializations
				isInitialized = true;
				setupViews();
				registerListeners();
				
				// Listen for service updates
				ClientReceiver receiver = getClientReceiver();
				IntentFilter filter = receiver.getIntentFilter();
				if (filter != null) {
					registerReceiver(receiver, filter);
				}
				
				// Notify the service of the new client
				PlaybackClient.startService();
			}
			
			// Update the views
			updateViews();
		} finally {
			Utilities.popContext();
		}
	}
	
	@Override
	protected void onDestroy() {
		// Start the destroy process
		Utilities.pushContext(this);
		try {
			super.onDestroy();
			
			// Unregister receivers and listeners
			isInitialized = false;
			if (clientReceiver != null) {
				unregisterReceiver(clientReceiver);
			}
		} finally {
			Utilities.popContext();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, Schema.MN_SETTINGS, 1, "Settings").setIcon(R.drawable.menu_settings);
		menu.add(Menu.NONE, Schema.MN_EXIT, 2, "Exit").setIcon(R.drawable.menu_exit);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Utilities.pushContext(this);
		try {
			switch (item.getItemId()) {
			case Schema.MN_SETTINGS:
				Intent intent = new Intent(this, PreferenceActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				return true;
			case Schema.MN_EXIT:
				PlaybackClient.stopService();
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
			}
		} finally {
			Utilities.popContext();
		}
	}
	
	/**
	 * Finds the views from in the layout and assigns them to member variables.
	 */
	private void findViews() {
		tabHost = (TabHost)findViewById(android.R.id.tabhost);
	}
	
	/**
	 * Sets up the views for the first time. Once setup, the views will never be setup again.
	 */
	private void setupViews() {
		createTabSpec("Artists");
		createTabSpec("Albums");
		createTabSpec("Genres");
		createTabSpec("Songs");
		createTabSpec("Playlists");
	}
	
	/**
	 * Updates all the views so that they have the latest information.
	 */
	private void updateViews() {
	}
	
	/**
	 * Registers onclick and other listeners to the views associated with the layout.
	 */
	private void registerListeners() {
	}
	
	private void createTabSpec(String display) {
		TabSpec tabSpec = tabHost.newTabSpec(display).setIndicator(display).setContent(new TabHost.TabContentFactory() {
			@Override
			public View createTabContent(String tag) {
				Utilities.pushContext(LibraryActivity.this);
				try {
					LibraryView view = new LibraryView(LibraryActivity.this);
					view.setMediaGroup(tag.substring(0, tag.length() - 1));
					return view;
				} finally {
					Utilities.popContext();
				}
			}
		});
		tabHost.addTab(tabSpec);
	}
	
	private ClientReceiver getClientReceiver() {
		if (clientReceiver == null) {
			clientReceiver = new ClientReceiver(getClientCallback(), false);
			clientReceiver.addAction(Schema.I_MCUBED_PROGRESS);
		}
		return clientReceiver;
	}
	
	private IClientCallback getClientCallback() {
		if (clientCallback == null) {
			clientCallback = new ClientCallback() {
				public void propertyMountChanged(boolean isMounted) {
					updateViews();
				}
				
				public void propertyBlueoothChanged(boolean isBluetoothConnected) {
					updateViews();
				}
				
				public void propertyHeadphoneChanged(boolean isHeadphoneConnected) {
					updateViews();
				}
				
				public void propertyPlaybackStatusChanged(MediaStatus playbackStatus) {
					updateViews();
				}
			};
		}
		return clientCallback;
	}
}