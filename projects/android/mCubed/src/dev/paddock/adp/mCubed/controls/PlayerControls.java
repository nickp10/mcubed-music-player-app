package dev.paddock.adp.mCubed.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.model.MediaStatus;
import dev.paddock.adp.mCubed.receivers.ClientReceiver;
import dev.paddock.adp.mCubed.receivers.IProvideClientReceiver;
import dev.paddock.adp.mCubed.services.ClientCallback;
import dev.paddock.adp.mCubed.services.IClientCallback;
import dev.paddock.adp.mCubed.services.PlaybackClient;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class PlayerControls extends LinearLayout implements IProvideClientReceiver {
	private ClientReceiver clientReceiver;
	private ClientCallback clientCallback;
	private ImageButton actionButton, nextButton, prevButton;
	private ProgressBar seekBar;
	
	/**
	 * Click listener for the play/pause action button.
	 */
	private OnClickListener actionClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Utilities.pushContext(getContext());
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
	 * Click listener for the next button.
	 */
	private OnClickListener nextClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Utilities.pushContext(getContext());
			try {
				PlaybackClient.movePlaybackNext();
			} finally {
				Utilities.popContext();
			}
		}
	};
	
	/**
	 * Click listener for the previous button.
	 */
	private OnClickListener prevClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Utilities.pushContext(getContext());
			try {
				PlaybackClient.movePlaybackPrev();
			} finally {
				Utilities.popContext();
			}
		}
	};
	
	public PlayerControls(Context context) {
		super(context);
		initView(context);
	}
	
	public PlayerControls(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}
	
	private void initView(Context context) {
		// Inflate the layout
		LayoutInflater inflater = App.getSystemService(LayoutInflater.class, context, Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.player_controls, this, true);
		
		// Find the views
		actionButton = (ImageButton)findViewById(R.id.pc_action_button);
		prevButton = (ImageButton)findViewById(R.id.pc_prev_button);
		nextButton = (ImageButton)findViewById(R.id.pc_next_button);
		seekBar = (ProgressBar)findViewById(R.id.pc_seek_bar);
		
		// Register listeners
		actionButton.setOnClickListener(actionClickListener);
		prevButton.setOnClickListener(prevClickListener);
		nextButton.setOnClickListener(nextClickListener);
		
		// Initialize the views
		updateViews();
	}
	
	public void updateViews() {
		// Update the action icon
		if (App.isInitialized() && App.getPlayer().isPlaying()) {
			actionButton.setImageResource(R.drawable.ic_media_pause);
		} else {
			actionButton.setImageResource(R.drawable.ic_media_play);
		}
		
		// Update the seek progress
		seekBar.setMax(App.getPlayer().getDuration());
		seekBar.setProgress(App.getPlayer().getSeek());
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
				@Override
				public void propertyPlaybackIDChanged(long playbackID) {
					updateViews();
				}
				
				@Override
				public void propertyPlaybackStatusChanged(MediaStatus playbackStatus) {
					updateViews();
				}
				
				@Override
				public void propertyPlaybackSeekChanged(int playbackSeek) {
					updateViews();
				}
			};
		}
		return clientCallback;
	}
}
