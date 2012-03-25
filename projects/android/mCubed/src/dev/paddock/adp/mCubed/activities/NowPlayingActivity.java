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
import dev.paddock.adp.mCubed.controls.NowPlayingView;
import dev.paddock.adp.mCubed.controls.ProgressDisplay;
import dev.paddock.adp.mCubed.model.MediaStatus;
import dev.paddock.adp.mCubed.receivers.ClientReceiver;
import dev.paddock.adp.mCubed.receivers.IProvideClientReceiver;
import dev.paddock.adp.mCubed.services.ClientCallback;
import dev.paddock.adp.mCubed.services.IClientCallback;

public class NowPlayingActivity extends TabActivity implements IActivity, IProvideClientReceiver {
	private ClientReceiver clientReceiver;
	private ClientCallback clientCallback;
	private NowPlayingView nowPlayingView;
	private ProgressDisplay progressDisplay;
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
		return Arrays.asList(Schema.MN_LIBRARY, Schema.MN_PLAYALL,
				Schema.MN_SETTINGS, Schema.MN_HELP, Schema.MN_EXIT,
				Schema.MN_ABOUT, Schema.MN_FEEDBACK);
	}

	@Override
	public int getLayoutID() {
		return R.layout.now_playing_activity;
	}

	@Override
	public void findViews() {
		tabHost = (TabHost)findViewById(android.R.id.tabhost);
		progressDisplay = (ProgressDisplay)findViewById(R.id.npa_progress_display);
	}

	@Override
	public void setupViews() {
		nowPlayingView = new NowPlayingView(this);
		createTabSpec("Now Playing", nowPlayingView);
		createTabSpec("History", new View(this));
		createTabSpec("Queue", new View(this));
	}

	@Override
	public void updateViews() {
		nowPlayingView.updateViews();
	}

	@Override
	public void registerListeners() {
	}
	
	@Override
	public void handleExtras(Bundle extras) {
	}
	
	@Override
	public List<IProvideClientReceiver> getClientReceivers() {
		return Arrays.<IProvideClientReceiver>asList(this, progressDisplay);
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
				public void propertyPlaybackIDChanged(long playbackID) {
					updateViews();
				}
				
				public void propertyPlaybackStatusChanged(MediaStatus playbackStatus) {
					updateViews();
				}
			};
		}
		return clientCallback;
	}
	
	private void createTabSpec(String display, final View contentView) {
		TabSpec tabSpec = tabHost.newTabSpec(display).setIndicator(display).setContent(new TabHost.TabContentFactory() {
			@Override
			public View createTabContent(String tag) {
				return contentView;
			}
		});
		tabHost.addTab(tabSpec);
	}
}