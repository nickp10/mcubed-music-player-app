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
import dev.paddock.adp.mCubed.controls.MountDisplay;
import dev.paddock.adp.mCubed.controls.NowPlayingView;
import dev.paddock.adp.mCubed.controls.PlayerControls;
import dev.paddock.adp.mCubed.controls.PlaylistView;
import dev.paddock.adp.mCubed.controls.ProgressDisplay;
import dev.paddock.adp.mCubed.receivers.IProvideClientReceiver;

public class NowPlayingActivity extends FragmentActivity implements IActivity {
	private PlayerControls playerControls;
	private MountDisplay mountDisplay;
	private ProgressDisplay progressDisplay;
	private FragmentTabHost tabHost;

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
		return ActivityUtils.getMenuOptions(Schema.MN_NOWPLAYING);
	}

	@Override
	public int getLayoutID() {
		return R.layout.now_playing_activity;
	}

	@Override
	public void findViews() {
		tabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
		playerControls = (PlayerControls)findViewById(R.id.npa_player_controls);
		mountDisplay = (MountDisplay)findViewById(R.id.npa_mount_display);
		progressDisplay = (ProgressDisplay)findViewById(R.id.npa_progress_display);
	}

	@Override
	public void setupViews() {
		tabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);
		createTabSpec("Now Playing", NowPlayingView.class, null);
		createTabSpec("History", PlaylistView.class, createBundle(true));
		createTabSpec("Queue", PlaylistView.class, createBundle(false));
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
		return Arrays.<IProvideClientReceiver>asList(mountDisplay, progressDisplay, playerControls);
	}

	private Bundle createBundle(boolean isHistory) {
		Bundle bundle = new Bundle();
		bundle.putBoolean(Schema.BUNDLE_BOOLEAN_IS_HISTORY, isHistory);
		return bundle;
	}

	private void createTabSpec(String display, Class<?> fragmentClass, Bundle bundle) {
		TabSpec tabSpec = tabHost.newTabSpec(display).setIndicator(display);
		tabHost.addTab(tabSpec, fragmentClass, bundle);
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