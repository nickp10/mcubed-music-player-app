package dev.paddock.adp.mCubed.receivers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.model.NotificationArgs;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.ICursor;
import dev.paddock.adp.mCubed.utilities.Log;
import dev.paddock.adp.mCubed.utilities.PropertyManager;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class MountReceiver extends BroadcastReceiver {
	private static final String EXTERNAL_VOLUME = "external"; // Should replicate the private MediaProvider.EXTERNAL_VOLUME
	private static final String INTERNAL_VOLUME = "internal"; // Should replicate the private MediaProvider.INTERNAL_VOLUME
	private static boolean isMounted, isScanRequired;
	private static List<String> scanRequiredMounts = new ArrayList<String>();
	private static List<String> unmountedMounts = new ArrayList<String>();
	
	static {
		Utilities.pushContext(App.getAppContext());
		try {
			isMounted = detectIsPrimaryDeviceMounted();
			isScanRequired = loadUnmounted() || detectIsScanning();
		} finally {
			Utilities.popContext();
		}
	}
	
	/**
	 * Retrieve whether or not the external storage device is mounted, meaning the phone is able to read contents from it.
	 * @return True if the external storage device is mounted, or false otherwise.
	 */
	public static boolean isMounted() {
		return isMounted;
	}
	
	private static void setMounted(boolean isMounted) {
		if (MountReceiver.isMounted != isMounted) {
			NotificationArgs args = new NotificationArgs(MountReceiver.class, "IsMounted", MountReceiver.isMounted, isMounted);
			PropertyManager.notifyPropertyChanging(args);
			MountReceiver.isMounted = isMounted;
			PropertyManager.notifyPropertyChanged(args);
		}
	}
	
	/**
	 * Retrieve whether or not the internal/external storage device will require the media content to be scanned.
	 * @return True if the internal/external storage device will require the media content to be scanned, or false otherwise.
	 */
	public static boolean isScanRequired() {
		return isScanRequired;
	}
	
	private static void setScanRequired(boolean isScanRequired) {
		if (MountReceiver.isScanRequired != isScanRequired) {
			NotificationArgs args = new NotificationArgs(MountReceiver.class, "IsScanRequired", MountReceiver.isScanRequired, isScanRequired);
			PropertyManager.notifyPropertyChanging(args);
			MountReceiver.isScanRequired = isScanRequired;
			PropertyManager.notifyPropertyChanged(args);
		}
	}
	
	private static boolean detectIsPrimaryDeviceMounted() {
		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
	}
	
	private static boolean detectIsScanning() {
		final List<String> scanning = new ArrayList<String>();
		Utilities.query(MediaStore.getMediaScannerUri(), new String[]{MediaStore.MEDIA_SCANNER_VOLUME}, new ICursor() {
			@Override
			public boolean run(Cursor cursor) {
				scanning.add(cursor.getString(0));
				return false;
			}
		});
		return scanning.contains(EXTERNAL_VOLUME) || scanning.contains(INTERNAL_VOLUME);
	}
	
	private static boolean loadUnmounted() {
		boolean result = false;
		final Context context = Utilities.getContext();
		String contents = Utilities.loadFile(Schema.FILE_MOUNTS);
		Utilities.deleteFile(Schema.FILE_MOUNTS);
		if (!Utilities.isNullOrEmpty(contents)) {
			String[] mounts = contents.split("\n");
			if (mounts != null) {
				result = true;
				for (int i = 0; i < mounts.length; i++) {
					Uri mountUri = Uri.parse(mounts[i]);
					if (Utilities.fileExists(mountUri)) {
						try {
							context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, mountUri));
						} catch (SecurityException e) {
							Log.e(e);
							result = false;
						}
					}
				}
			}
		}
		return result;
	}
	
	private static void saveUnmounted() {
		StringBuilder builder = new StringBuilder();
		for (String mount : unmountedMounts) {
			if (!Utilities.isNullOrEmpty(mount)) {
				if (builder.length() > 0) {
					builder.append("\n");
				}
				builder.append(mount);
			}
		}
		Utilities.saveFile(Schema.FILE_MOUNTS, builder.toString());
	}
	
	/**
	 * A storage device has been mounted. Android will begin scanning the storage device.
	 * @param mount The mount that was just mounted.
	 */
	private static void storageMounted(String mount) {
		// Update scan required. A mount has been mounted, Android will have to scan it for media.
		scanRequiredMounts.add(mount);
		setScanRequired(true);
		
		// A mount has been mounted, check to see if any other mounts are unmounted.
		if (unmountedMounts.remove(mount))
		{
			saveUnmounted();
		}
		setMounted(unmountedMounts.size() == 0);
	}
	
	/**
	 * A storage device has been unmounted. Treat the application as being unmounted altogether.
	 * @param mount The mount that was just unmounted.
	 */
	private static void storageUnmounted(String mount) {
		if (!unmountedMounts.contains(mount))
		{
			unmountedMounts.add(mount);
			saveUnmounted();
		}
		setMounted(false);
	}
	
	/**
	 * A mount has started scanning.
	 * @param mount The mount that just started scanning.
	 */
	private static void scanStarted(String mount) {
	}
	
	/**
	 * A mount has finished scanning, check to see if any other mounts need to be scanned.
	 * @param mount The mount that just finished scanning.
	 */
	private static void scanFinished(String mount) {
		scanRequiredMounts.remove(mount);
		setScanRequired(scanRequiredMounts.size() != 0);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Utilities.pushContext(context);
		try {
			String action = intent.getAction();
			String data = intent.getDataString();
			Log.i(String.format(Locale.US, "Handle MountReceiver broadcast [Action=%s, Data=%s]", action, data));
			if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
				storageMounted(data);
			} else if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
				storageUnmounted(data);
			} else if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)) {
				scanStarted(data);
			} else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
				scanFinished(data);
			}
		} finally {
			Utilities.popContext();
		}
	}
}