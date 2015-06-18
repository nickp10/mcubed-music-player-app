package dev.paddock.adp.mCubed.widgets;

import android.view.View;
import android.widget.RemoteViews;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.model.InitStatus;
import dev.paddock.adp.mCubed.model.MediaStatus;
import dev.paddock.adp.mCubed.services.ClientCallback;
import dev.paddock.adp.mCubed.services.IClientCallback;
import dev.paddock.adp.mCubed.utilities.App;

public class Playback1x1Provider extends PlaybackProvider {
	@Override
	protected int getLayoutID() {
		return R.layout.playback1x1_widget;
	}
	
	@Override
	protected IRemoteViewsUpdater generateUpdater() {
		return new IRemoteViewsUpdater() {
			@Override
			public void updateView(RemoteViews views, int flags) {
				// Update the click events
				views.setOnClickPendingIntent(R.id.w11_play_button, generateClickIntent(Schema.WI_PLAY_CLICK));
				
				// Update the play/pause button
				views.setImageViewResource(R.id.w11_play_button, App.isInitialized() && App.getPlayer().isPlaying() ? R.drawable.widget_pause : R.drawable.widget_play);
				
				// Update the visibilities
				if (App.isScanRequired() || App.getInitStatus() == InitStatus.Initializing) {
					views.setViewVisibility(R.id.w11_init_layout, View.VISIBLE);
					views.setViewVisibility(R.id.w11_info_layout, View.GONE);
				} else {
					views.setViewVisibility(R.id.w11_init_layout, View.GONE);
					views.setViewVisibility(R.id.w11_info_layout, View.VISIBLE);
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
			public void propertyPlaybackStatusChanged(MediaStatus playbackStatus) {
				invalidate(Schema.WI_INV_STATUS_CHANGED);
			}
			
			@Override
			public void propertyScanRequiredChanged(boolean isScanRequired) {
				invalidate(Schema.WI_INV_SCAN_REQUIRED_CHANGED);
			}
		};
	}
}