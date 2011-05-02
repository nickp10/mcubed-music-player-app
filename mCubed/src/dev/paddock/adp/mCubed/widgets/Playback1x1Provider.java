package dev.paddock.adp.mCubed.widgets;

import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.model.InitStatus;
import dev.paddock.adp.mCubed.model.MediaStatus;
import dev.paddock.adp.mCubed.services.ClientCallback;
import dev.paddock.adp.mCubed.services.IClientCallback;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class Playback1x1Provider extends PlaybackProvider {
	@Override
	protected int getLayoutID() {
		return R.layout.playback1x1widget;
	}
	
	@Override
	protected IRemoteViewsUpdater onUpdate(WidgetUpdater updater) {
		updater.setTextViewText(R.id.w11_play_button, App.isInitialized() && App.getPlayer().getStatus() == MediaStatus.Play ? "Pause" : "Play");
		updater.setOnClickIntent(R.id.w11_play_button, new ClickIntent(Utilities.getContext(), getClass(), Schema.WI_PLAY_CLICK));
		return null;
	}
	
	@Override
	protected IClientCallback generateClientCallback() {
		return new ClientCallback() {
			@Override
			public void propertyInitStatusChanged(InitStatus initStatus) {
				invalidate();
			}
			
			@Override
			public void propertyPlaybackStatusChanged(MediaStatus playbackStatus) {
				invalidate();
			}
		};
	}
}