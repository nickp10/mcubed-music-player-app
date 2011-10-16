package dev.paddock.adp.mCubed.activities;

import android.app.TabActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.controls.LibraryView;
import dev.paddock.adp.mCubed.model.Composite;
import dev.paddock.adp.mCubed.model.MediaGroup;
import dev.paddock.adp.mCubed.model.MediaStatus;
import dev.paddock.adp.mCubed.receivers.ClientReceiver;
import dev.paddock.adp.mCubed.services.ClientCallback;
import dev.paddock.adp.mCubed.services.IClientCallback;
import dev.paddock.adp.mCubed.services.PlaybackClient;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class LibraryActivity extends TabActivity implements IActivity {
	private ClientReceiver clientReceiver;
	private IClientCallback clientCallback;
	private TabHost tabHost;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityUtils.onCreate(this, savedInstanceState);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		ActivityUtils.onDestroy(this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, Schema.MN_NOWPLAYING, 1, "Now Playing").setIcon(R.drawable.menu_nowplaying);
		menu.add(Menu.NONE, Schema.MN_PLAYALL, 2, "Play All").setIcon(R.drawable.menu_playall);
		menu.add(Menu.NONE, Schema.MN_SETTINGS, 3, "Settings").setIcon(R.drawable.menu_settings);
		menu.add(Menu.NONE, Schema.MN_HELP, 4, "Help").setIcon(R.drawable.menu_help);
		menu.add(Menu.NONE, Schema.MN_EXIT, 5, "Exit").setIcon(R.drawable.menu_exit);
		menu.add(Menu.NONE, Schema.MN_ABOUT, 6, "About").setIcon(R.drawable.menu_about);
		menu.add(Menu.NONE, Schema.MN_FEEDBACK, 7, "Feedback").setIcon(R.drawable.menu_feedback);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		return App.isInitialized();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Utilities.pushContext(this);
		try {
			switch (item.getItemId()) {
			case Schema.MN_NOWPLAYING:
				ActivityUtils.startActivity(this, NowPlayingActivity.class);
				return true;
			case Schema.MN_PLAYALL:
				App.getNowPlaying().playComposite(new Composite(MediaGroup.All.getGrouping(0)));
				return true;
			case Schema.MN_SETTINGS:
				ActivityUtils.startActivity(this, PreferenceActivity.class);
				return true;
			case Schema.MN_EXIT:
				PlaybackClient.stopService();
				finish();
				return true;
			case Schema.MN_HELP:
				// TODO Launch activity for the help
				return true;
			case Schema.MN_ABOUT:
				// TODO Launch activity for the about
				return true;
			case Schema.MN_FEEDBACK:
				// TODO Launch activity for the feedback
				return true;
			default:
				return super.onOptionsItemSelected(item);
			}
		} finally {
			Utilities.popContext();
		}
	}
	
	@Override
	public int getLayoutID() {
		return R.layout.library_activity;
	}
	
	@Override
	public void findViews() {
		tabHost = (TabHost)findViewById(android.R.id.tabhost);
	}
	
	@Override
	public void setupViews() {
		createTabSpec("Artists");
		createTabSpec("Albums");
		createTabSpec("Genres");
		createTabSpec("Songs");
		createTabSpec("Playlists");
	}
	
	@Override
	public void updateViews() {
	}
	
	@Override
	public void registerListeners() {
	}
	
	@Override
	public void handleExtras(Bundle extras) {
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
	
	@Override
	public ClientReceiver getClientReceiver() {
		if (clientReceiver == null) {
			clientReceiver = new ClientReceiver(getClientCallback(), false);
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