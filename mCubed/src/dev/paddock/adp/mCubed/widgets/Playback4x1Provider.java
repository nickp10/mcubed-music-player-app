package dev.paddock.adp.mCubed.widgets;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.model.InitStatus;
import dev.paddock.adp.mCubed.model.MediaFile;
import dev.paddock.adp.mCubed.model.MediaStatus;
import dev.paddock.adp.mCubed.services.ClientCallback;
import dev.paddock.adp.mCubed.services.IClientCallback;
import dev.paddock.adp.mCubed.services.PlaybackClient;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class Playback4x1Provider extends PlaybackProvider {
	@Override
	protected int getLayoutID() {
		return R.layout.playback4x1widget;
	}
	
	@Override
	public void onEnabled(Context context) {
		Utilities.pushContext(context);
		try {
			super.onEnabled(context);
			if (App.isInitialized()) {
				PlaybackClient.setSeekListener(true);
			}
		} finally {
			Utilities.popContext();
		}
	}
	
	@Override
	public void onDisabled(Context context) {
		Utilities.pushContext(context);
		try {
			super.onDisabled(context);
			if (App.isInitialized()) {
				PlaybackClient.setSeekListener(false);
			}
		} finally {
			Utilities.popContext();
		}
	}
	
	@Override
	protected IRemoteViewsUpdater generateUpdater() {
		return new IRemoteViewsUpdater() {
			@Override
			public void updateView(RemoteViews views, int flags) {
				// Update the click events
				views.setOnClickPendingIntent(R.id.w41_play_button, generateClickIntent(Schema.WI_PLAY_CLICK));
				views.setOnClickPendingIntent(R.id.w41_prev_button, generateClickIntent(Schema.WI_PREV_CLICK));
				views.setOnClickPendingIntent(R.id.w41_next_button, generateClickIntent(Schema.WI_NEXT_CLICK));
				views.setOnClickPendingIntent(R.id.w41_cover_image, generateClickIntent(Schema.WI_OPEN_APP_CLICK));
				
				// Update the play/pause button
				views.setImageViewResource(R.id.w41_play_button, App.isInitialized() && App.getPlayer().isPlaying() ? R.drawable.ic_media_pause : R.drawable.ic_media_play);
				
				// Update the seek bar
				views.setProgressBar(R.id.w41_seek_bar, App.getPlayer().getDuration(), App.getPlayer().getSeek(), false);
				
				// Update the scroll information
				Uri art = null;
				MediaFile file = App.getPlayingMedia();
				if (file == null) {
					views.setTextViewText(R.id.w41_playing_info, "");
				} else {
					art = file.getAlbumArt();
					String info = String.format("%s - %s", file.getArtist(), file.getTitle());
					views.setTextViewText(R.id.w41_playing_info, info);
				}
				
				// Update the album art
				if (art == null) {
					views.setImageViewResource(R.id.w41_cover_image, R.drawable.img_cover_missing);
				} else {
					views.setImageViewUri(R.id.w41_cover_image, art);
				}
				
				// Update the visibilities
				if (App.getInitStatus() == InitStatus.Initializing) {
					views.setViewVisibility(R.id.w41_init_layout, View.VISIBLE);
					views.setViewVisibility(R.id.w41_info_layout, View.GONE);
				} else {
					views.setViewVisibility(R.id.w41_init_layout, View.GONE);
					views.setViewVisibility(R.id.w41_info_layout, View.VISIBLE);
				}
			}
		};
	}
	
	@Override
	protected IClientCallback generateClientCallback() {
		return new ClientCallback() {
			@Override
			public void propertyInitStatusChanged(InitStatus initStatus) {
				invalidate(Schema.WI_INV_INIT_CHANGED);
				if (initStatus == InitStatus.Initialized) {
					PlaybackClient.setSeekListener(true);
				}
			}
			
			@Override
			public void propertyPlaybackIDChanged(long playbackID) {
				invalidate(Schema.WI_INV_FILE_CHANGED);
			}
			
			@Override
			public void propertyPlaybackStatusChanged(MediaStatus playbackStatus) {
				invalidate(Schema.WI_INV_STATUS_CHANGED);
			}
			
			@Override
			public void propertyPlaybackSeekChanged(int playbackSeek) {
				invalidate(Schema.WI_INV_SEEK_CHANGED);
			}
		};
	}
}