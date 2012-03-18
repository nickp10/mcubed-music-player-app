package dev.paddock.adp.mCubed.activities;

import java.util.Arrays;
import java.util.List;

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
import dev.paddock.adp.mCubed.model.MediaStatus;
import dev.paddock.adp.mCubed.receivers.ClientReceiver;
import dev.paddock.adp.mCubed.services.ClientCallback;
import dev.paddock.adp.mCubed.services.IClientCallback;
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
		return super.onCreateOptionsMenu(menu) &&
				ActivityUtils.onCreateOptionsMenu(this, menu);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu) &&
				ActivityUtils.onPrepareOptionsMenu(this, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return ActivityUtils.onOptionsItemSelected(this, item) ||
				super.onOptionsItemSelected(item);
	}
	
	@Override
	public List<Integer> getMenuOptions() {
		return Arrays.asList(Schema.MN_NOWPLAYING, Schema.MN_PLAYALL,
				Schema.MN_SETTINGS, Schema.MN_HELP, Schema.MN_EXIT,
				Schema.MN_ABOUT, Schema.MN_FEEDBACK);
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
}