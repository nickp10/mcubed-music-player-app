package dev.paddock.adp.mCubed.activities;

import java.util.Arrays;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.controls.LibraryView;
import dev.paddock.adp.mCubed.controls.MountDisplay;
import dev.paddock.adp.mCubed.controls.ProgressDisplay;
import dev.paddock.adp.mCubed.model.MediaGroup;
import dev.paddock.adp.mCubed.receivers.IProvideClientReceiver;

public class LibraryActivity extends FragmentActivity implements IActivity {
	private FragmentTabHost tabHost;
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
	public boolean onOptionsItemSelected(MenuItem item) {
		return ActivityUtils.onOptionsItemSelected(this, item) ||
				super.onOptionsItemSelected(item);
	}

	@Override
	public List<Integer> getMenuOptions() {
		return Arrays.asList(Schema.MN_NOWPLAYING, Schema.MN_PLAYALL,
				Schema.MN_SETTINGS, Schema.MN_ABOUT, Schema.MN_FEEDBACK,
				Schema.MN_HELP, Schema.MN_EXIT);
	}

	@Override
	public int getLayoutID() {
		return R.layout.library_activity;
	}

	@Override
	public void findViews() {
		tabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
		mountDisplay = (MountDisplay)findViewById(R.id.la_mount_display);
		progressDisplay = (ProgressDisplay)findViewById(R.id.la_progress_display);
	}

	@Override
	public void setupViews() {
		tabHost.setup(this, getSupportFragmentManager(), R.id.la_tabcontent);
		createTabSpec("Artists", MediaGroup.Artist);
		createTabSpec("Albums", MediaGroup.Album);
		createTabSpec("Genres", MediaGroup.Genre);
		createTabSpec("Songs", MediaGroup.Song);
		createTabSpec("Playlists", MediaGroup.Playlist);
		trimTabPadding(tabHost);
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

	private Bundle createBundle(MediaGroup mediaGroup) {
		Bundle bundle = new Bundle();
		bundle.putSerializable(Schema.BUNDLE_MEDIA_GROUP, mediaGroup);
		return bundle;
	}

	private void createTabSpec(String display, MediaGroup mediaGroup) {
		TabSpec tabSpec = tabHost.newTabSpec(display).setIndicator(display);
		tabHost.addTab(tabSpec, LibraryView.class, createBundle(mediaGroup));
	}

	private void trimTabPadding(FragmentTabHost tabHost) {
		TabWidget tabWidget = tabHost.getTabWidget();
		for (int i = 0; i < tabWidget.getChildCount(); i++) {
			View child = tabWidget.getChildAt(i);
			if (child != null) {
				child.setPadding(0, child.getPaddingTop(), 0, child.getPaddingBottom());
			}
		}
	}
}