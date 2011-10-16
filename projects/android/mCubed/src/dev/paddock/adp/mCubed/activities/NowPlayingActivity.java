package dev.paddock.adp.mCubed.activities;

import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.model.MediaFile;
import dev.paddock.adp.mCubed.model.MediaStatus;
import dev.paddock.adp.mCubed.receivers.ClientReceiver;
import dev.paddock.adp.mCubed.services.ClientCallback;
import dev.paddock.adp.mCubed.services.IClientCallback;
import dev.paddock.adp.mCubed.services.PlaybackClient;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.Utilities;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class NowPlayingActivity extends Activity implements IActivity {
	private ClientReceiver clientReceiver;
	private ClientCallback clientCallback;
	private TextView artistTextView, titleTextView;
	private Button actionButton;
	
	/**
	 * Click listener for the play/pause action button.
	 */
	private OnClickListener actionClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Utilities.pushContext(NowPlayingActivity.this);
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
	public int getLayoutID() {
		return R.layout.now_playing_activity;
	}

	@Override
	public void findViews() {
		artistTextView = (TextView)findViewById(R.id.np_artist_textView);
		titleTextView = (TextView)findViewById(R.id.np_title_textView);
		actionButton = (Button)findViewById(R.id.np_action_button);
	}

	@Override
	public void setupViews() {
	}

	@Override
	public void updateViews() {
		MediaFile mediaFile = App.getPlayingMedia();
		if (mediaFile == null) {
			artistTextView.setText("Unknown");
			titleTextView.setText("Unknown");
		} else {
			artistTextView.setText(mediaFile.getArtist());
			titleTextView.setText(mediaFile.getTitle());
		}
		if (App.getPlayer().isPlaying()) {
			actionButton.setText("Pause");
		} else {
			actionButton.setText("Play");
		}
	}

	@Override
	public void registerListeners() {
		actionButton.setOnClickListener(actionClickListener);
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
}