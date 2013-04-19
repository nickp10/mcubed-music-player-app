package dev.paddock.adp.mCubed.activities;

import java.util.Arrays;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.controls.MountDisplay;
import dev.paddock.adp.mCubed.controls.PlayerControls;
import dev.paddock.adp.mCubed.controls.ProgressDisplay;
import dev.paddock.adp.mCubed.receivers.IProvideClientReceiver;

public class OverlayActivity extends FragmentActivity implements IActivity {
	private ImageView closeButton;
	private PlayerControls playerControls;
	private MountDisplay mountDisplay;
	private ProgressDisplay progressDisplay;

//	/**
//	 * Click listener for the open button.
//	 */
//	private OnClickListener openClickListener = new OnClickListener() {
//		@Override
//		public void onClick(View v) {
//			Utilities.pushContext(OverlayActivity.this);
//			try {
//				ActivityUtils.startMainActivity();
//				OverlayActivity.this.finish();
//			} finally {
//				Utilities.popContext();
//			}
//		}
//	};

	/**
	 * Click listener for the close button.
	 */
	private OnClickListener closeClickListener = new OnClickListener() {
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
		return Arrays.asList();
	}

	@Override
	public int getLayoutID() {
		return R.layout.overlay_activity;
	}

	@Override
	public void findViews() {
		closeButton = (ImageView)findViewById(R.id.oa_close_button);
		playerControls = (PlayerControls)findViewById(R.id.oa_player_controls);
		mountDisplay = (MountDisplay)findViewById(R.id.oa_mount_display);
		progressDisplay = (ProgressDisplay)findViewById(R.id.oa_progress_display);
	}

	@Override
	public void setupViews() {
	}

	@Override
	public void updateViews() {
	}

	@Override
	public void registerListeners() {
		closeButton.setOnClickListener(closeClickListener);
	}

	@Override
	public void handleExtras(Bundle extras) {
	}

	@Override
	public List<IProvideClientReceiver> getClientReceivers() {
		return Arrays.<IProvideClientReceiver>asList(playerControls, mountDisplay, progressDisplay);
	}
}