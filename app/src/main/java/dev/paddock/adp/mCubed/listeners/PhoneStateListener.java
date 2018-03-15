package dev.paddock.adp.mCubed.listeners;

import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.model.MediaPlayerState;
import dev.paddock.adp.mCubed.model.NotificationArgs;
import dev.paddock.adp.mCubed.receivers.PhoneStateReceiver;
import dev.paddock.adp.mCubed.services.PlaybackServer;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.INotifyListener;
import dev.paddock.adp.mCubed.utilities.PropertyManager;

public class PhoneStateListener implements IListener {
	private MediaPlayerState savedState;
	private final INotifyListener isPhoneCallActiveListener = new INotifyListener() {
		@Override
		public void propertyChanging(Object instance, NotificationArgs args) { }

		@Override
		public void propertyChanged(Object instance, NotificationArgs args) {
			onIsPhoneCallActiveChanged(args);
		}
	};

	@Override
	public void register() {
		PropertyManager.register(PhoneStateReceiver.class, "PhoneCallActive", isPhoneCallActiveListener);
	}

	@Override
	public void unregister() {
		PropertyManager.unregister(isPhoneCallActiveListener);
	}

	private void onIsPhoneCallActiveChanged(NotificationArgs args) {
		// Update the media player state accordingly
		if (PhoneStateReceiver.isPhoneCallActive()) {
			savedState = App.getPlayer().getMediaPlayerStateWithLocks(false, true, true);
		} else {
			App.getPlayer().setMediaPlayerState(savedState);
		}
		
		// Re-route the event
		PlaybackServer.propertyChanged(0, Schema.PROP_PHONE_CALL_ACTIVE, PhoneStateReceiver.isPhoneCallActive());
	}
}
