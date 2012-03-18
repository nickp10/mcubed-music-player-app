package dev.paddock.adp.mCubed.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.services.PlaybackClient;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class NowPlayingView extends LinearLayout {
	private ImageButton actionButton, nextButton, prevButton;
	private MediaFileDetailsView mediaFileView;
	
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
	
	public NowPlayingView(Context context) {
		super(context);
		initView(context);
	}
	
	public NowPlayingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}
	
	private void initView(Context context) {
		// Inflate the layout
		LayoutInflater inflater = App.getSystemService(LayoutInflater.class, context, Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.now_playing_view, this, true);
		
		// Find the views
		mediaFileView = (MediaFileDetailsView)findViewById(R.id.npv_media_file_details_view);
		actionButton = (ImageButton)findViewById(R.id.npv_action_button);
		prevButton = (ImageButton)findViewById(R.id.npv_prev_button);
		nextButton = (ImageButton)findViewById(R.id.npv_next_button);
		
		// Register listeners
		actionButton.setOnClickListener(actionClickListener);
		prevButton.setOnClickListener(prevClickListener);
		nextButton.setOnClickListener(nextClickListener);
		
		// Initialize the views
		updateViews();
	}
	
	public void updateViews() {
		mediaFileView.setMediaFile(App.getPlayingMedia());
		if (App.isInitialized() && App.getPlayer().isPlaying()) {
			actionButton.setImageResource(R.drawable.ic_media_pause);
		} else {
			actionButton.setImageResource(R.drawable.ic_media_play);
		}
	}
}
