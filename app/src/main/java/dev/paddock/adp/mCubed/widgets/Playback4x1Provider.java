package dev.paddock.adp.mCubed.widgets;

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
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class Playback4x1Provider extends PlaybackProvider {
	@Override
	protected int getLayoutID() {
		return R.layout.playback4x1_widget;
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
				if (App.isScanRequired() || App.getInitStatus() == InitStatus.Initializing) {
					String text = Utilities.getResourceString(App.isScanRequired() ? R.string.prog_scanning : R.string.prog_initializing);
					views.setTextViewText(R.id.w41_init_text, text);
					views.setViewVisibility(R.id.w41_init_layout, View.VISIBLE);
					views.setViewVisibility(R.id.w41_info_layout, View.GONE);
				} else {
					views.setTextViewText(R.id.w41_init_text, Utilities.getResourceString(R.string.prog_scanning));
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
			
			@Override
			public void propertyScanRequiredChanged(boolean isScanRequired) {
				invalidate(Schema.WI_INV_SCAN_REQUIRED_CHANGED);
			}
		};
	}
}