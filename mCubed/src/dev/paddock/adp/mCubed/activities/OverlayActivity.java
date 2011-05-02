package dev.paddock.adp.mCubed.activities;

import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.model.MediaStatus;
import dev.paddock.adp.mCubed.receivers.ClientReceiver;
import dev.paddock.adp.mCubed.services.ClientCallback;
import dev.paddock.adp.mCubed.services.IClientCallback;
import dev.paddock.adp.mCubed.services.PlaybackClient;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.Utilities;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class OverlayActivity extends Activity {
	private ClientReceiver clientReceiver;
	private IClientCallback clientCallback;
	private Button playButton, openButton, dismissButton;
	private boolean isInitialized;
	
	/**
	 * Click listener for the play/pause button
	 */
	private OnClickListener clickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Utilities.pushContext(OverlayActivity.this);
			try {
				if (App.getPlayer().getStatus() == MediaStatus.Play) {
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
	 * Click listener for the open button
	 */
	private OnClickListener openListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Utilities.pushContext(OverlayActivity.this);
			try {
				Intent intent = new Intent(Utilities.getContext(), LibraryActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				OverlayActivity.this.finish();
			} finally {
				Utilities.popContext();
			}
		}
	};
	
	/**
	 * Click listener for the dismiss button
	 */
	private OnClickListener dismissListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			OverlayActivity.this.finish();
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Initialize the creation
		Utilities.pushContext(this);
		try {
			super.onCreate(savedInstanceState);
			
			// Set the content view and retrieve the views
			setContentView(R.layout.overlay);
			playButton = (Button)findViewById(R.id.ov_play_button);
			openButton = (Button)findViewById(R.id.ov_open_button);
			dismissButton = (Button)findViewById(R.id.ov_dismiss_button);
			updateViews();
			
			// Register receiver and listeners
			if (!isInitialized) {
				// Listen for the click event
				isInitialized = true;
				playButton.setOnClickListener(clickListener);
				openButton.setOnClickListener(openListener);
				dismissButton.setOnClickListener(dismissListener);
				
				// Listen for service updates
				ClientReceiver receiver = getClientReceiver();
				IntentFilter filter = receiver.getIntentFilter();
				if (filter != null) {
					registerReceiver(receiver, filter);
				}
				
				// Notify the service of the new client
				PlaybackClient.startService();
			}
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
	
	private void updateViews() {
		playButton.setText(App.getPlayer().getStatus() == MediaStatus.Play ? "Pause" : "Play");
	}
	
	private ClientReceiver getClientReceiver() {
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