package dev.paddock.adp.mCubed.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.lists.BindingList;
import dev.paddock.adp.mCubed.preferences.PlayModeEnum;
import dev.paddock.adp.mCubed.preferences.RepeatStatus;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.Delegate;
import dev.paddock.adp.mCubed.utilities.Log;
import dev.paddock.adp.mCubed.utilities.PreferenceManager;
import dev.paddock.adp.mCubed.utilities.ProgressManager;
import dev.paddock.adp.mCubed.utilities.PropertyManager;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class Playlist {
	private final BindingList<Composite> composition = new BindingList<Composite>();
	private final BindingList<MediaFile> files = new BindingList<MediaFile>();
	private final PlayMode playMode;
	private String name;
	private MediaFile current;

	public Playlist() {
		this(Collections.<MediaFile>emptyList());
	}
	
	public Playlist(Collection<MediaFile> initialFiles) {
		files.addAll(initialFiles);
		playMode = new PlayMode(this);
		playMode.setPlaylist(this);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		if (this.name != name) {
			NotificationArgs args = new NotificationArgs(this, "Name", this.name, name);
			PropertyManager.notifyPropertyChanging(this, "Name", args);
			this.name = name;
			PropertyManager.notifyPropertyChanged(this, "Name", args);
		}
	}
	
	public MediaFile getCurrent() {
		return current;
	}
	private void setCurrent(MediaFile file, boolean forceNotify) {
		if (this.current != file || forceNotify) {
			if (Log.isDebug()) {
				Log.d("History: " + getHistoryList());
				Log.d("Queue: " + getQueueList());
				Log.d("Current: " + (file == null ? 0 : file.getID()));
			}
			NotificationArgs args = new NotificationArgs(this, "Current", this.current, file);
			PropertyManager.notifyPropertyChanging(this, "Current", args);
			this.current = file;
			PropertyManager.notifyPropertyChanged(this, "Current", args);
		}
	}
	
	private void resetCurrent() {
		resetCurrent(false);
	}
	
	private void resetCurrent(boolean forceNotify) {
		playMode.resetCurrent();
		setCurrent(playMode.getCurrent(), forceNotify);
		if (playMode.getCurrentRequiresRepeat()) {
			RepeatStatus repeat = PreferenceManager.getSettingEnum(RepeatStatus.class, R.string.pref_repeat_status);
			if (repeat != RepeatStatus.RepeatPlaylist) {
				App.getPlayer().stop();
			}
		}
	}
	
	public void next() {
		playMode.next();
		resetCurrent(true);
	}
	
	public void previous() {
		playMode.previous();
		resetCurrent(true);
	}
	
	protected void addFiles(MediaFile... files) {
		addFilesInternal(null, files);
	}
	
	protected void addFiles(Collection<MediaFile> files) {
		addFilesInternal(null, files);
	}
	
	public void addFilesToQueue(MediaFile... files) {
		addFilesInternal(new Delegate.Action<MediaFile>() {
			@Override
			public void act(MediaFile file) {
				playMode.appendToQueue(file);
			}
		}, files);
	}
	
	public void addFilesToQueue(Collection<MediaFile> files) {
		addFilesInternal(new Delegate.Action<MediaFile>() {
			@Override
			public void act(MediaFile file) {
				playMode.appendToQueue(file);
			}
		}, files);
	}
	
	public void prependFilesToQueue(MediaFile... files) {
		addFilesInternal(new Delegate.Action<MediaFile>() {
			@Override
			public void act(MediaFile file) {
				playMode.prependToQueue(file);
			}
		}, files);
	}
	
	public void prependFilesToQueue(Collection<MediaFile> files) {
		addFilesInternal(new Delegate.Action<MediaFile>() {
			@Override
			public void act(MediaFile file) {
				playMode.prependToQueue(file);
			}
		}, files);
	}
	
	private void addFilesInternal(Delegate.Action<MediaFile> queueAction, MediaFile... files) {
		if (files != null) {
			String subject = queueAction == null ? "playlist" : "queue";
			Progress progress = ProgressManager.startProgress(Schema.PROG_PLAYLIST_ADDFILES, "Adding files to " + subject + "...");
			int count = 0;
			for (MediaFile file : files) {
				if (file != null) {
					// Add the file to the playlist
					addFileInternal(true, file);
					
					// Add to the queue
					if (queueAction != null) {
						queueAction.act(file);
					}
				}
				
				// Update the progress
				count++;
				double value = (double)count / (double)files.length;
				progress.setValue(value);
			}
			resetCurrent();
			ProgressManager.endProgress(progress);
		}
	}
	
	private void addFilesInternal(Delegate.Action<MediaFile> queueAction, Collection<MediaFile> files) {
		if (files != null) {
			addFilesInternal(queueAction, files.toArray(new MediaFile[0]));
		}
	}
	
	private void addFileInternal(boolean doNotify, MediaFile file) {
		if (!this.files.contains(file)) {
			this.files.add(file);
			if (doNotify) {
				playMode.addedToPlaylist(file);
			}
		}
	}
	
	protected void removeFiles(MediaFile... files) {
		removeFilesInternal(false, files);
	}
	
	protected void removeFiles(Collection<MediaFile> files) {
		removeFilesInternal(false, files);
	}
	
	public void removeFilesFromQueue(MediaFile... files) {
		removeFilesInternal(true, files);
	}
	
	public void removeFilesFromQueue(Collection<MediaFile> files) {
		removeFilesInternal(true, files);
	}
	
	private void removeFilesInternal(boolean removeFromQueue, MediaFile... files) {
		if (files != null) {
			String subject = removeFromQueue ? "queue" : "playlist";
			Progress progress = ProgressManager.startProgress(Schema.PROG_PLAYLIST_REMOVEFILES, "Removing files from " + subject + "...");
			int count = 0;
			for (MediaFile file : files) {
				// Remove the file accordingly
				if (file != null) {
					if (removeFromQueue) {
						if (this.files.contains(file)) {
							playMode.removeFromQueue(file);
						}
					} else if (this.files.remove(file, true)) {
						playMode.removedFromPlaylist(file);
					}
				}
				
				// Update the progress
				count++;
				double value = (double)count / (double)files.length;
				progress.setValue(value);
			}
			resetCurrent();
			ProgressManager.endProgress(progress);
		}
	}
	
	private void removeFilesInternal(boolean removeFromQueue, Collection<MediaFile> files) {
		if (files != null) {
			removeFilesInternal(removeFromQueue, files.toArray(new MediaFile[0]));
		}
	}
	
	public void clear() {
		files.clear();
		composition.clear();
		playMode.clear();
		resetCurrent();
	}
	
	public List<MediaFile> getFiles() {
		return files;
	}
	
	public List<Composite> getComposition() {
		return Collections.unmodifiableList(composition);
	}
	
	public boolean containsComposite(Composite composite) {
		for (Composite comp : composition) {
			if (Composite.equals(comp, composite)) {
				return true;
			}
		}
		return false;
	}
	
	public void playFile(MediaFile file) {
		if (playMode.peekNext() != file) {
			prependFilesToQueue(file);
		}
		next();
		App.getPlayer().play();
	}
	
	public void playComposite(Composite composite) {
		clear();
		addComposite(composite);
		App.getPlayer().play();
	}
	
	public void addComposite(Composite composite) {
		if (composite != null) {
			ListAction action = composite.getAction();
			MediaGrouping grouping = composite.getGrouping();
			Progress progress = ProgressManager.startProgress(Schema.PROG_PLAYLIST_ADDCOMPOSITE, "Adding composite to playlist...");
			try {
				progress.setSubIDs(Schema.PROG_MEDIAGROUP_GETFILES);
				if (action == ListAction.Add) {
					progress.appendSubIDs(Schema.PROG_PLAYLIST_ADDFILES);
					if (composite.isMediaGroupAll()) {
						composition.clear();
					}
					composition.add(composite);
					addFiles(grouping.getMediaFiles());
				} else if (action == ListAction.Remove) {
					progress.appendSubIDs(Schema.PROG_PLAYLIST_REMOVEFILES);
					if (composite.isMediaGroupAll()) {
						composition.clear();
					}
					composition.add(composite);
					removeFiles(grouping.getMediaFiles());
				}
			} finally {
				ProgressManager.endProgress(progress);
			}
		}
	}
	
	public void removeComposite(Composite composite) {
		if (composition.remove(composite, true)) {
			Progress progress = ProgressManager.startProgress(Schema.PROG_PLAYLIST_REMOVECOMPOSITE, "Removing composite from playlist...", Schema.PROG_PLAYLIST_VALIDATE);
			progress.setAllowChildTitle(false);
			validate();
			ProgressManager.endProgress(progress);
		}
	}
	
	private static String generateList(Collection<MediaFile> files) {
		StringBuilder builder = new StringBuilder();
		for (MediaFile file : files) {
			if (builder.length() != 0) {
				builder.append(",");
			}
			builder.append(file.getID());
		}
		return builder.toString();
	}
	
	private static Collection<MediaFile> generateList(String ids) {
		Progress progress = ProgressManager.startProgress(Schema.PROG_PLAYLIST_GENERATELIST, "Loading files...");
		try {
			Collection<MediaFile> files = new ArrayList<MediaFile>();
			if (!Utilities.isNullOrEmpty(ids)) {
				String[] idArray = ids.split(",");
				int current = 0;
				for (String idString : idArray) {
					try {
						long id = Long.parseLong(idString);
						MediaFile file = MediaFile.get(id);
						if (file != null) {
							files.add(file);
						}
					} finally {
						current++;
						progress.setValue((double)current / (double)idArray.length);
					}
				}
			}
			return files;
		} finally {
			ProgressManager.endProgress(progress);
		}
	}
	
	private static void setList(Collection<MediaFile> destinationList, String ids) {
		Collection<MediaFile> files = generateList(ids);
		destinationList.clear();
		destinationList.addAll(files);
	}
	
	public BindingList<MediaFile> getHistory() {
		return playMode.getHistory();
	}
	
	public BindingList<MediaFile> getQueue() {
		return playMode.getQueue();
	}
	
	public String getHistoryList() {
		return generateList(getHistory());
	}
	
	public String getQueueList() {
		return generateList(getQueue());
	}
	
	public void reset(String historyIDs, String queueIDs, long currentID) {
		Progress progress = ProgressManager.startProgress(Schema.PROG_PLAYLIST_RESET, "Restoring the playlist");
		progress.appendSubID(Schema.PROG_PLAYLIST_GENERATELIST, 2);
		try {
			// Update the history
			setList(playMode.getHistory(), historyIDs);
			for (MediaFile file : playMode.getHistory()) {
				addFileInternal(false, file);
			}
			
			// Update the queue
			setList(playMode.getQueue(), queueIDs);
			for (MediaFile file : playMode.getQueue()) {
				addFileInternal(false, file);
			}
			
			// Update the current
			MediaFile file = MediaFile.get(currentID);
			if (file != null) {
				addFileInternal(false, file);
			}
			playMode.setCurrent(file);
			playMode.reset(false);
			resetCurrent(true);
		} finally {
			ProgressManager.endProgress(progress);
		}
	}
	
	public void resetPlayMode(PlayModeEnum playMode, boolean clearQueue) {
		this.playMode.setPlayModeEnum(playMode, clearQueue);
	}
	
	private void validateList(BindingList<MediaFile> destinationList) {
		MediaFile[] destArray = destinationList.toArray(new MediaFile[0]);
		for (MediaFile file : destArray) {
			if (file == null || !files.contains(file)) {
				destinationList.remove(file, true);
			}
		}
	}
	
	/**
	 * Validates that all the media files in the playlist (containing the history,
	 * queue, current, and remaining files) still exist on the device and still fall
	 * in one of the compositions that make up the playlist. If the current file changed
	 * during this process, then this  method will true. Otherwise, false will be returned.
	 * @return True if the current media file changed, or false otherwise.
	 */
	public boolean validate() {
		// Setup the progress
		Progress progress = ProgressManager.startProgress(Schema.PROG_PLAYLIST_VALIDATE, "Validating the playlist...");
		String[] subIDs = new String[composition.size()];
		for (int i = 0; i < composition.size(); i++) {
			subIDs[i] = Schema.PROG_PLAYLIST_ADDCOMPOSITE;
		}
		progress.setSubIDs(subIDs);
		progress.setAllowChildTitle(false);
		
		// Clear and re-add the media files
		MediaFile beginCurrent = getCurrent();
		Composite[] tempComposition = composition.toArray(new Composite[0]);
		composition.clear();
		files.clear();
		for (Composite composite : tempComposition) {
			addComposite(composite);
		}
		
		// Validate the history and queue
		validateList(playMode.getHistory());
		validateList(playMode.getQueue());
		
		// Validate the current
		MediaFile current = playMode.getCurrent();
		if (current == null || !files.contains(current)) {
			playMode.setCurrent(null);
		}
		
		// Notify the play mode
		playMode.reset(false);
		resetCurrent();
		
		// End the progress
		ProgressManager.endProgress(progress);
		return beginCurrent != getCurrent();
	}
}