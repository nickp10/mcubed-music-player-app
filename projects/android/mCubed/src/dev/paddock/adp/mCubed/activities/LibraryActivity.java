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
import dev.paddock.adp.mCubed.controls.MountDisplay;
import dev.paddock.adp.mCubed.controls.ProgressDisplay;
import dev.paddock.adp.mCubed.receivers.IProvideClientReceiver;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class LibraryActivity extends TabActivity implements IActivity {
	private TabHost tabHost;
	private MountDisplay mountDisplay;
	private ProgressDisplay progressDisplay;
	
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
	protected void onResume() {
		super.onResume();
		ActivityUtils.onResume(this);
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
		mountDisplay = (MountDisplay)findViewById(R.id.la_mount_display);
		progressDisplay = (ProgressDisplay)findViewById(R.id.la_progress_display);
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
	public List<IProvideClientReceiver> getClientReceivers() {
		return Arrays.<IProvideClientReceiver>asList(mountDisplay, progressDisplay);
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