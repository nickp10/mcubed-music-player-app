package dev.paddock.adp.mCubed.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
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
	private TextView seekText, durationText;
	private SeekBar seekBar;
	private boolean autoUpdateSeek = true;
	
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
	
	/**
	 * Listener for keeping track of the user seeking.
	 */
	private OnSeekBarChangeListener seekBarChangeListener = new OnSeekBarChangeListener() {
		private int lastUserSeek = Integer.MIN_VALUE;
		
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			if (lastUserSeek >= 0) {
				Utilities.pushContext(getContext());
				try {
					App.getPlayer().setSeek(lastUserSeek);
				} finally {
					Utilities.popContext();
				}
			}
			autoUpdateSeek = true;
		}
		
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			autoUpdateSeek = false;
			lastUserSeek = Integer.MIN_VALUE;
		}
		
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (fromUser) {
				lastUserSeek = progress;
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
		// Ensure the seek events are being published
		Utilities.pushContext(context);
		try {
			PlaybackClient.setSeekListener(true);
		} finally {
			Utilities.popContext();
		}
		
		// Inflate the layout
		LayoutInflater inflater = App.getSystemService(LayoutInflater.class, context, Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.player_controls, this, true);
		
		// Find the views
		actionButton = (ImageButton)findViewById(R.id.pc_action_button);
		prevButton = (ImageButton)findViewById(R.id.pc_prev_button);
		nextButton = (ImageButton)findViewById(R.id.pc_next_button);
		seekBar = (SeekBar)findViewById(R.id.pc_seek_bar);
		seekText = (TextView)findViewById(R.id.pc_seek_text);
		durationText = (TextView)findViewById(R.id.pc_duration_text);
		
		// Register listeners
		actionButton.setOnClickListener(actionClickListener);
		prevButton.setOnClickListener(prevClickListener);
		nextButton.setOnClickListener(nextClickListener);
		seekBar.setOnSeekBarChangeListener(seekBarChangeListener);
		
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
		
		// Update the seek text
		int seek = App.getPlayer().getSeek();
		int duration = App.getPlayer().getDuration();
		seekText.setText(Utilities.formatTime(seek));
		durationText.setText(Utilities.formatTime(duration));
		
		// Update the seek progress
		seekBar.setMax(duration);
		if (autoUpdateSeek) {
			seekBar.setProgress(seek);
		}
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
