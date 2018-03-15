package dev.paddock.adp.mCubed.listeners;

import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.model.NotificationArgs;
import dev.paddock.adp.mCubed.receivers.MountReceiver;
import dev.paddock.adp.mCubed.services.PlaybackServer;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.INotifyListener;
import dev.paddock.adp.mCubed.utilities.PropertyManager;

public class MountListener implements IListener {
	private final INotifyListener isMountedListener = new INotifyListener() {
		@Override
		public void propertyChanging(Object instance, NotificationArgs args) { }

		@Override
		public void propertyChanged(Object instance, NotificationArgs args) {
			onIsMountedChanged(args);
		}
	};

	private final INotifyListener isScanRequiredListener = new INotifyListener() {
		@Override
		public void propertyChanging(Object instance, NotificationArgs args) { }

		@Override
		public void propertyChanged(Object instance, NotificationArgs args) {
			onIsScanRequiredChanged(args);
		}
	};

	@Override
	public void register() {
		PropertyManager.register(MountReceiver.class, "IsMounted", isMountedListener);
		PropertyManager.register(MountReceiver.class, "IsScanRequired", isScanRequiredListener);
	}

	@Override
	public void unregister() {
		PropertyManager.unregister(isMountedListener);
		PropertyManager.unregister(isScanRequiredListener);
	}

	private void onIsMountedChanged(NotificationArgs args) {
		if (App.isInitialized()) {
			if (App.isMounted()) {
				App.initMount();
			} else {
				App.deinitMount();
			}
		} else if (App.isMounted()) {
			App.initialize();
		}
		PlaybackServer.propertyChanged(0, Schema.PROP_MOUNTED, App.isMounted());
	}

	private void onIsScanRequiredChanged(NotificationArgs args) {
		if (App.isInitialized()) {
			if (App.isMounted()) {
				if (App.isScanRequired()) {
					App.deinitScanned();
				} else {
					App.initScanned();
				}
			}
		} else if (!App.isScanRequired()) {
			App.initialize();
		}
		PlaybackServer.propertyChanged(0, Schema.PROP_SCAN_REQUIRED, App.isScanRequired());
	}
}
