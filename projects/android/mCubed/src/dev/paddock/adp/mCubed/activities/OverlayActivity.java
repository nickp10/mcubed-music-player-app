package dev.paddock.adp.mCubed.activities;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.controls.MountDisplay;
import dev.paddock.adp.mCubed.controls.ProgressDisplay;
import dev.paddock.adp.mCubed.model.MediaStatus;
import dev.paddock.adp.mCubed.receivers.ClientReceiver;
import dev.paddock.adp.mCubed.receivers.IProvideClientReceiver;
import dev.paddock.adp.mCubed.services.ClientCallback;
import dev.paddock.adp.mCubed.services.IClientCallback;
import dev.paddock.adp.mCubed.services.PlaybackClient;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class OverlayActivity extends Activity implements IActivity, IProvideClientReceiver {
	private ClientReceiver clientReceiver;
	private IClientCallback clientCallback;
	private Button playButton, openButton, dismissButton;
	private MountDisplay mountDisplay;
	private ProgressDisplay progressDisplay;
	
	/**
	 * Click listener for the play/pause button.
	 */
	private OnClickListener actionClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Utilities.pushContext(OverlayActivity.this);
			try {
				if (App.getPlayer().isPlaying()) {
					PlaybackClient.pause();
				} else {
					PlaybackClient.play();
				}
			} finally {
				Utilities.popContext();
			}
		}
	};
	
	/**
	 * Click listener for the open button.
	 */
	private OnClickListener openClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Utilities.pushContext(OverlayActivity.this);
			try {
				ActivityUtils.startMainActivity();
				OverlayActivity.this.finish();
			} finally {
				Utilities.popContext();
			}
		}
	};
	
	/**
	 * Click listener for the dismiss button.
	 */
	private OnClickListener dismissClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			OverlayActivity.this.finish();
		}
	};
	
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
		return Arrays.asList();
	}

	@Override
	public int getLayoutID() {
		return R.layout.overlay_activity;
	}

	@Override
	public void findViews() {
		playButton = (Button)findViewById(R.id.ova_play_button);
		openButton = (Button)findViewById(R.id.ova_open_button);
		dismissButton = (Button)findViewById(R.id.ova_dismiss_button);
		mountDisplay = (MountDisplay)findViewById(R.id.ova_mount_display);
		progressDisplay = (ProgressDisplay)findViewById(R.id.ova_progress_display);
	}

	@Override
	public void setupViews() {
	}

	@Override
	public void updateViews() {
		playButton.setText(App.getPlayer().isPlaying() ? "Pause" : "Play");
	}

	@Override
	public void registerListeners() {
		playButton.setOnClickListener(actionClickListener);
		openButton.setOnClickListener(openClickListener);
		dismissButton.setOnClickListener(dismissClickListener);
	}
	
	@Override
	public void handleExtras(Bundle extras) {
	}
	
	@Override
	public List<IProvideClientReceiver> getClientReceivers() {
		return Arrays.<IProvideClientReceiver>asList(this, mountDisplay, progressDisplay);
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