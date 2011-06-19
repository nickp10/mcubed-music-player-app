package dev.paddock.adp.mCubed.receivers;

import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.model.NotificationArgs;
import dev.paddock.adp.mCubed.services.PlaybackServer;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.Log;
import dev.paddock.adp.mCubed.utilities.PropertyManager;
import dev.paddock.adp.mCubed.utilities.Utilities;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;

public class MountReceiver extends BroadcastReceiver implements IReceiver {
	private static final MountReceiver instance = new MountReceiver();
	private boolean isMounted;
	
	/** 
	 * Prevent external instances of a MountReceiver
	 */
	private MountReceiver() {
		isMounted = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
	}
	
	public static MountReceiver getInstance() {
		return instance;
	}
	
	/**
	 * Retrieve whether or not the external storage device is mounted, meaning the phone is able to read contents from it.
	 * @return True if the external storage device is mounted, or false otherwise.
	 */
	public boolean isMounted() {
		return isMounted;
	}
	
	private void setMounted(boolean isMounted) {
		if (this.isMounted != isMounted) {
			NotificationArgs args = new NotificationArgs(this, "Mounted", this.isMounted, isMounted);
			PropertyManager.notifyPropertyChanging(this, "Mount", args);
			this.isMounted = isMounted;
			updatePlayer();
			PropertyManager.notifyPropertyChanged(this, "Mount", args);
			PlaybackServer.propertyChanged(0, Schema.PROP_MOUNT, this.isMounted);
		}
	}
	
	private void updatePlayer() {
		if (App.isInitialized()) {
			if (isMounted()) {
				App.initMount();
			} else {
				App.deinitMount();
			}
		}
	}
	
	private void updatePlayerScanned() {
		if (isMounted()) {
			if (App.isInitialized()) {
				App.initScanned();
			} else {
				App.initialize();
			}
		}
	}
	
	@Override
	public IntentFilter getIntentFilter() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		intentFilter.addDataScheme("file");
		return intentFilter;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Utilities.pushContext(context);
		try {
			String action = intent.getAction();
			if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
				setMounted(true);
			} else if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
				setMounted(false);
			} else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
				if (intent.getDataString().contains(Environment.getExternalStorageDirectory().toString())) {
					Log.i("Media scanner finished");
					updatePlayerScanned();
				}
			}
		} finally {
			Utilities.popContext();
		}
	}
}