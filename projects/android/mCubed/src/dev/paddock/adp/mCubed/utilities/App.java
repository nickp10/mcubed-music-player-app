package dev.paddock.adp.mCubed.utilities;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask.Status;

import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.model.AsyncTask;
import dev.paddock.adp.mCubed.model.Composite;
import dev.paddock.adp.mCubed.model.InitStatus;
import dev.paddock.adp.mCubed.model.ListAction;
import dev.paddock.adp.mCubed.model.MediaFile;
import dev.paddock.adp.mCubed.model.MediaGroup;
import dev.paddock.adp.mCubed.model.MediaPlayer;
import dev.paddock.adp.mCubed.model.MediaPlayerState;
import dev.paddock.adp.mCubed.model.MediaStatus;
import dev.paddock.adp.mCubed.model.NotificationArgs;
import dev.paddock.adp.mCubed.model.Playlist;
import dev.paddock.adp.mCubed.model.Progress;
import dev.paddock.adp.mCubed.model.TimerTask;
import dev.paddock.adp.mCubed.preferences.PlaybackAction;
import dev.paddock.adp.mCubed.preferences.PreviousAction;
import dev.paddock.adp.mCubed.receivers.HeadsetReceiver;
import dev.paddock.adp.mCubed.receivers.MountReceiver;
import dev.paddock.adp.mCubed.receivers.OutputReceiver;
import dev.paddock.adp.mCubed.receivers.PhoneStateReceiver;
import dev.paddock.adp.mCubed.services.PlaybackServer;

public class App extends Application {
	private static boolean isServiceRunning, isStaticallyInitialized;
	private static InitStatus initStatus = InitStatus.Deinitialized;
	private static AsyncTask initTask, deinitTask, initScannedTask;
	private static Playlist nowPlaying;
	private static Context appContext;
	private static MediaPlayerState mountState;
	private static final List<Runnable> initCallbacks = new ArrayList<Runnable>();
	private static final List<Runnable> deinitCallbacks = new ArrayList<Runnable>();
	private static final TimerTask appStateSaver = new TimerTask(new Runnable() {
		@Override
		public void run() {
			Utilities.pushContext(getAppContext());
			try {
				saveAppStateXML();
				Log.i("Application state saved");
			} finally {
				Utilities.popContext();
			}
		}
	}, 600000);
	
	private static void staticInitialization() {
		// Make sure this only gets run once
		if (isStaticallyInitialized) {
			return;
		}
		isStaticallyInitialized = true;
		
		// Initialize the playlist
		nowPlaying = new Playlist();
		PropertyManager.register(getNowPlaying(), "Current", new INotifyListener() {
			@Override
			public void propertyChanging(Object instance, NotificationArgs args) { }
			
			@Override
			public void propertyChanged(Object instance, NotificationArgs args) {
				getPlayer().setMediaFile(getNowPlaying().getCurrent());
			}
		});
		
		// Register to the headphone/bluetooth property changes
		PropertyManager.register(getHeadset(), "HeadphonesConnected", new INotifyListener() {
			@Override
			public void propertyChanging(Object instance, NotificationArgs args) { }
			
			@Override
			public void propertyChanged(Object instance, NotificationArgs args) {
				// Retrieve the action
				PlaybackAction action = null;
				if (getHeadset().isHeadphonesConnected()) {
					action = PreferenceManager.getSettingEnum(PlaybackAction.class, R.string.pref_headphones_connected);
				} else {
					action = PreferenceManager.getSettingEnum(PlaybackAction.class, R.string.pref_headphones_disconnected);
				}
				
				// Perform the appropriate action
				if (action == PlaybackAction.Play) {
					getPlayer().setStatus(MediaStatus.Play);
				} else if (action == PlaybackAction.Pause) {
					getPlayer().setStatus(MediaStatus.Pause);
				}
			}
		});
		PropertyManager.register(getHeadset(), "BluetoothConnected", new INotifyListener() {
			@Override
			public void propertyChanging(Object instance, NotificationArgs args) { }
			
			@Override
			public void propertyChanged(Object instance, NotificationArgs args) {
				// Retrieve the action
				PlaybackAction action = null;
				if (getHeadset().isBluetoothConnected()) {
					action = PreferenceManager.getSettingEnum(PlaybackAction.class, R.string.pref_bluetooth_connected);
				} else {
					action = PreferenceManager.getSettingEnum(PlaybackAction.class, R.string.pref_bluetooth_disconnected);
				}
				
				// Perform the appropriate action
				if (action == PlaybackAction.Play) {
					getPlayer().setStatus(MediaStatus.Play);
				} else if (action == PlaybackAction.Pause) {
					getPlayer().setStatus(MediaStatus.Pause);
				}
			}
		});
	}
	
	public static InitStatus getInitStatus() {
		return initStatus;
	}
	
	public static boolean isInitialized() {
		return initStatus == InitStatus.Initialized;
	}
	
	public static boolean isServiceRunning() {
		return isServiceRunning;
	}
	
	public static Playlist getNowPlaying() {
		return nowPlaying;
	}
	
	public static MediaPlayer getPlayer() {
		return MediaPlayer.getInstance();
	}
	
	public static MediaFile getPlayingMedia() {
		return getPlayer().getMediaFile();
	}
	
	public static HeadsetReceiver getHeadset() {
		return HeadsetReceiver.getInstance();
	}
	
	public static MountReceiver getMount() {
		return MountReceiver.getInstance();
	}
	
	public static OutputReceiver getOutput() {
		return OutputReceiver.getInstance();
	}
	
	public static PhoneStateReceiver getPhoneState() {
		return PhoneStateReceiver.getInstance();
	}
	
	public static Context getAppContext() {
		return appContext;
	}
	
	@Override
	public void onCreate() {
		// Create the app
		super.onCreate();
		App.appContext = getApplicationContext();
		
		// Initialize the static members
		Utilities.pushContext(this);
		try {
			staticInitialization();
		} finally {
			Utilities.popContext();
		}
	}
	
	public static void movePlaybackNext() {
		getNowPlaying().next();
	}
	
	public static void movePlaybackPrev() {
		// Determine if the song should restart or play the previous song
		PreviousAction action = PreferenceManager.getSettingEnum(PreviousAction.class, R.string.pref_previous_action);
		boolean restartSong = false;
		if (action == PreviousAction.AlwaysRestartSong) {
			restartSong = true;
		} else if (action == PreviousAction.Smart) {
			String condition = PreferenceManager.getSettingString(R.string.pref_previous_smart_condition);
			if (!Utilities.isNullOrEmpty(condition)) {
				char unit = condition.charAt(condition.length() - 1);
				int value = Utilities.parseInt(condition.substring(0, condition.length() - 1));
				int currentSeek = getPlayer().getSeek();
				int currentDuration = getPlayer().getDuration();
				if (unit == '%') {
					if (value >= 0 && value <= 100) {
						int checkSeek = (int)((value / 100d) * currentDuration);
						restartSong = currentSeek >= checkSeek;
					}
				} else if (unit == 's') {
					if (value >= 0) {
						int checkSeek = value * 1000;
						restartSong = currentSeek >= checkSeek;
					}
				}
			}
		}
		
		// Play the previous song or restart accordingly
		if (restartSong) {
			getPlayer().setSeek(0);
		} else {
			getNowPlaying().previous();
		}
	}
	
	private static void saveAppStateXML() {
		// Create the root
		XMLDocument rootNode = XMLDocument.newDocument("AppState");
		
		// Create the now playing node
		Playlist nowPlaying = getNowPlaying();
		long currentID = nowPlaying.getCurrent() == null ? 0 : nowPlaying.getCurrent().getID();
		rootNode.setNodePathValue("NowPlaying/History", nowPlaying.getHistoryList());
		rootNode.setNodePathValue("NowPlaying/Current", Long.toString(currentID));
		rootNode.setNodePathValue("NowPlaying/Queue", nowPlaying.getQueueList());
		XMLNode compositionNode = rootNode.getNodePath("NowPlaying/Composition", true);
		for (Composite composite : nowPlaying.getComposition()) {
			compositionNode.addChildNode("Item").setNodeText(composite.toString());
		}
		
		// Create the player node
		MediaPlayer player = getPlayer();
		MediaPlayerState playerState = player.getMediaPlayerState(true, false, false);
		rootNode.addChildNode(playerState.toXML("Player"));
		
		// Save the XML
		Utilities.saveFile(Schema.FILE_APP_STATE, rootNode.toXML(false));
	}
	
	private static XMLDocument loadAppStateXML() {
		String contents = Utilities.loadFile(Schema.FILE_APP_STATE);
		return XMLDocument.read(contents);
	}
	
	private static void loadAppStateXML(XMLDocument rootNode) {
		// Setup the progress
		Progress progress = ProgressManager.startProgress(Schema.PROG_APP_LOADXML, "Restoring application state...");
		progress.setSubIDs(Schema.PROG_PLAYLIST_VALIDATE);
		
		// Load the now playing playlist
		Playlist nowPlaying = getNowPlaying();
		String historyIDs = rootNode.getNodePathValue("NowPlaying/History");
		String queueIDs = rootNode.getNodePathValue("NowPlaying/Queue");
		long currentID = Utilities.parseLong(rootNode.getNodePathValue("NowPlaying/Current"));
		nowPlaying.reset(historyIDs, queueIDs, currentID);
		XMLNode compositionNode = rootNode.getNodePath("NowPlaying/Composition", false);
		if (compositionNode != null) {
			// Setup the composition
			List<XMLNode> itemNodes = compositionNode.getChildNodes();
			
			// Adjust the progress
			String[] subIDs = new String[itemNodes.size()];
			for (int i = 0; i < subIDs.length; i++) {
				subIDs[i] = Schema.PROG_PLAYLIST_ADDCOMPOSITE;
			}
			progress.appendSubIDs(subIDs);
			
			// Add the composition
			for (XMLNode itemNode : compositionNode.getChildNodes()) {
				nowPlaying.addComposite(Composite.parse(itemNode.getNodeText()));
			}
		}
		
		// Validate the playlist
		boolean currentChanged = nowPlaying.validate();
		
		// Load the player
		MediaPlayer player = getPlayer();
		XMLNode playerNode = rootNode.getChildNode("Player");
		MediaPlayerState playerState = MediaPlayerState.fromXML(playerNode);
		if (currentChanged) {
			playerState.setIsSeekValueAcquired(false);
		}
		player.setMediaPlayerState(playerState);
		
		// End the progress
		ProgressManager.endProgress(progress);
	}
	
	public static void setIsServiceRunning(boolean isServiceRunning) {
		if (App.isServiceRunning != isServiceRunning) {
			App.isServiceRunning = isServiceRunning;
			PlaybackServer.propertyChanged(0, Schema.PROP_IS_SERVICE_RUNNING, App.isServiceRunning);
		}
	}
	
	public static void addInitCallback(Runnable callback) {
		initCallbacks.add(callback);
	}
	
	public static void removeInitCallback(Runnable callback) {
		initCallbacks.remove(callback);
	}
	
	public static void addDeinitCallback(Runnable callback) {
		deinitCallbacks.add(callback);
	}
	
	public static void removeDeinitCallback(Runnable callback) {
		deinitCallbacks.remove(callback);
	}
	
	public static synchronized void initialize() {
		if (initStatus == InitStatus.Deinitialized && getMount().isMounted() && (initTask == null || initTask.getStatus() == Status.FINISHED)) {
			initTask = new AsyncTask(Utilities.getContext()) {
				@Override
				public void run() {
					// Begin initialization
					Log.i("Application initialization started");
					Progress progress = ProgressManager.startProgress(Schema.PROG_APP_INIT, "Initializing...", true);
					try {
						// Send the initializing property changed
						initStatus = InitStatus.Initializing;
						PlaybackServer.propertyChanged(0, Schema.PROP_INIT_STATUS, initStatus);
						
						// Start up the player
						getPlayer().open();
						
						// Retrieve its previous state
						XMLDocument rootNode = loadAppStateXML();
						if (rootNode == null) {
							// Load the initial playlist as all files
							progress.setSubIDs(Schema.PROG_PLAYLIST_ADDCOMPOSITE);
							// TODO IMPROVE
							getNowPlaying().addComposite(new Composite(MediaGroup.All.getGrouping(0), ListAction.Add));
						} else {
							// Load from its previous state
							progress.setSubIDs(Schema.PROG_APP_LOADXML);
							loadAppStateXML(rootNode);
						}
						
						// Start the application state saver thread
						appStateSaver.start();
						
						// Send the initialized property changed
						initStatus = InitStatus.Initialized;
						PlaybackServer.propertyChanged(0, Schema.PROP_INIT_STATUS, initStatus);
						
						// Call the callbacks
						for (Runnable callback : initCallbacks) {
							callback.run();
						}
					} catch (Throwable t) {
						Log.e(t);
					} finally {
						// End initialization
						ProgressManager.endProgress(progress);
						Log.i("Application initialization ended");
					}
				}
			};
			initTask.execute();
		}
	}
	
	public static synchronized void deinitialize() {
		if (initStatus == InitStatus.Initialized && (deinitTask == null || deinitTask.getStatus() == Status.FINISHED)) {
			deinitTask = new AsyncTask(Utilities.getContext()) {
				@Override
				public void run() {
					// Begin de-initialization
					Log.i("Application de-initialization started");
					try {
						// Send the de-initializing property changed
						initStatus = InitStatus.Deinitializing;
						PlaybackServer.propertyChanged(0, Schema.PROP_INIT_STATUS, initStatus);
						
						// Stop the application state saver thread
						appStateSaver.stop();
						
						// Attempt to save its state
						saveAppStateXML();
						
						// Destroy the player
						getPlayer().unregisterAllSeekListeners();
						getPlayer().close();
						
						// Clear the media file cache
						MediaFile.clearCache();
						
						// Send the de-initialized property changed
						initStatus = InitStatus.Deinitialized;
						PlaybackServer.propertyChanged(0, Schema.PROP_INIT_STATUS, initStatus);
						
						// Call the callbacks
						for (Runnable callback : deinitCallbacks) {
							callback.run();
						}
					} catch (Throwable t) {
						Log.e(t);
					} finally {
						// End de-initialization
						Log.i("Application de-initialization ended");
					}
				}
			};
			deinitTask.execute();
		}
	}
	
	/**
	 * Initialize the portion of the application that was affected by the SD card being unmounted previously.
	 * The app must already have gone through the initialize phase, must not have been deinitialized, and
	 * the SD card must be mounted. If any of those cases are not true, then this method will not be called.
	 * This method is called when the SD card has been mounted and the SD card has begun scanning.
	 */
	public static synchronized void initMount() {
		// Restore the media player state (if the file still exists)
		MediaFile file = getPlayingMedia();
		if (file != null && file.fileExists() && mountState != null) {
			getPlayer().setMediaPlayerState(mountState);
			mountState.setIsStatusValueAcquired(false);
		}
	}
	
	/**
	 * Initialize the portion of the application that was affected by the SD card being unmounted previously.
	 * The app must already have gone through the initialize phase, must not have been deinitialized, and
	 * the SD card must be mounted. If any of those cases are not true, then this method will not be called.
	 * This method is called when the SD card has finished scanning.
	 */
	public static synchronized void initScanned() {
		if (initStatus == InitStatus.Initialized && getMount().isMounted() && (initScannedTask == null || initScannedTask.getStatus() == Status.FINISHED)) {
			initScannedTask = new AsyncTask(Utilities.getContext()) {
				@Override
				protected void run() {
					// Begin re-initialization
					Log.i("Application scanned-finished initialization started");
					try {
						// Send the initializing property changed
						initStatus = InitStatus.Initializing;
						PlaybackServer.propertyChanged(0, Schema.PROP_INIT_STATUS, initStatus);
						
						// Check for updates in the media files
						getNowPlaying().validate();
						
						// Restore the media player state
						if (mountState != null) {
							mountState.setIsSeekValueAcquired(false);
							getPlayer().setMediaPlayerState(mountState);
							mountState = null;
						}
						
						// Send the initialized property changed
						initStatus = InitStatus.Initialized;
						PlaybackServer.propertyChanged(0, Schema.PROP_INIT_STATUS, initStatus);
					} catch (Throwable t) {
						Log.e(t);
					} finally {
						// End re-initialization
						Log.i("Application scanned-finished initialization ended");
					}
				}
			};
			initScannedTask.execute();
		}
	}
	
	/**
	 * Deinitializes the portion of the application that will be affected by the SD card being unmounted.
	 * The app must be initialized and the SD card must not be mounted. If any of those cases are not true,
	 * then this method will not be called.
	 */
	public static synchronized void deinitMount() {
		// Save off the media player state
		mountState = getPlayer().getMediaPlayerStateWithLocks(true, true, true);
		
		// Clear the media file cache (since files may be removed/added/modified while the SD card isn't mounted)
		MediaFile.clearCache();
	}
}