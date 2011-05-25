package dev.paddock.adp.mCubed.widgets;

import android.content.Context;
import android.net.Uri;
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
	protected IRemoteViewsUpdater onUpdate(WidgetUpdater updater) {
		updater.setImageViewResource(R.id.w41_play_button, App.isInitialized() && App.getPlayer().isPlaying() ? R.drawable.ic_media_pause : R.drawable.ic_media_play);
		updater.setOnClickIntent(R.id.w41_play_button, new ClickIntent(Utilities.getContext(), getClass(), Schema.WI_PLAY_CLICK));
		updater.setOnClickIntent(R.id.w41_prev_button, new ClickIntent(Utilities.getContext(), getClass(), Schema.WI_PREV_CLICK));
		updater.setOnClickIntent(R.id.w41_next_button, new ClickIntent(Utilities.getContext(), getClass(), Schema.WI_NEXT_CLICK));
		updater.setOnClickIntent(R.id.w41_cover_image, new ClickIntent(Utilities.getContext(), getClass(), Schema.WI_OPEN_APP_CLICK));
		updater.setProgressBar(R.id.w41_seek_bar, App.getPlayer().getDuration(), App.getPlayer().getSeek(), false);
		MediaFile file = App.getPlayingMedia();
		Uri art = file == null ? null : file.getAlbumArt();
		if (art == null) {
			updater.setImageViewResource(R.id.w41_cover_image, R.drawable.img_cover_missing);
		} else {
			updater.setImageViewUri(R.id.w41_cover_image, art);
		}
		return null;
	}
	
	@Override
	protected IClientCallback generateClientCallback() {
		return new ClientCallback() {
			@Override
			public void propertyInitStatusChanged(InitStatus initStatus) {
				invalidate();
				if (initStatus == InitStatus.Initialized) {
					PlaybackClient.setSeekListener(true);
				}
			}
			
			@Override
			public void propertyPlaybackStatusChanged(MediaStatus playbackStatus) {
				invalidate();
			}
			
			@Override
			public void propertyPlaybackSeekChanged(int playbackSeek) {
				invalidate();
			}
		};
	}
}