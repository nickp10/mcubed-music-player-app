package dev.paddock.adp.mCubed.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.preferences.PlayModeEnum;
import dev.paddock.adp.mCubed.preferences.RepeatStatus;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.Log;
import dev.paddock.adp.mCubed.utilities.PreferenceManager;
import dev.paddock.adp.mCubed.utilities.ProgressManager;
import dev.paddock.adp.mCubed.utilities.PropertyManager;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class Playlist {
	private final BindingList<Composite> composition = new BindingList<Composite>(new ArrayList<Composite>());
	private final BindingList<MediaFile> files = new BindingList<MediaFile>(new ArrayList<MediaFile>());
	private final PlayMode playMode;
	private String name;
	private MediaFile current;
	
	/**
	 * FOR TESTING PURPOSES ONLY!!!
	 * @param unitTestOnly
	 */
	protected Playlist(Boolean unitTestOnly) {
		playMode = null;
	}

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
		addFilesInternal(false, files);
	}
	
	protected void addFiles(Collection<MediaFile> files) {
		addFilesInternal(false, files);
	}
	
	public void addFilesToQueue(MediaFile... files) {
		addFilesInternal(true, files);
	}
	
	public void addFilesToQueue(Collection<MediaFile> files) {
		addFilesInternal(true, files);
	}
	
	private void addFilesInternal(boolean addToQueue, MediaFile... files) {
		if (files != null) {
			String subject = addToQueue ? "queue" : "playlist";
			Progress progress = ProgressManager.startProgress(Schema.PROG_PLAYLIST_ADDFILES, "Adding files to " + subject + "...");
			int count = 0;
			for (MediaFile file : files) {
				if (file != null) {
					// Add the file to the playlist
					addFileInternal(true, file);
					
					// Add to the queue
					if (addToQueue) {
						playMode.appendToQueue(file);
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
	
	private void addFilesInternal(boolean addToQueue, Collection<MediaFile> files) {
		if (files != null) {
			addFilesInternal(addToQueue, files.toArray(new MediaFile[0]));
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
	
	public List<MediaFile> getFiles() {
		return files;
	}
	
	public List<Composite> getComposition() {
		return Collections.unmodifiableList(composition);
	}
	
	public void addComposite(Composite composite) {
		if (composite != null) {
			ListAction action = composite.getAction();
			MediaGrouping grouping = composite.getGrouping();
			Progress progress = ProgressManager.startProgress(Schema.PROG_PLAYLIST_ADDCOMPOSITE, "Adding composite to playlist...");
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
			ProgressManager.endProgress(progress);
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
		Collection<MediaFile> files = new ArrayList<MediaFile>();
		if (!Utilities.isNullOrEmpty(ids)) {
			String[] idArray = ids.split(",");
			for (String idString : idArray) {
				try {
					long id = Long.parseLong(idString);
					MediaFile file = MediaFile.get(id);
					if (file != null) {
						files.add(file);
					}
				} catch (Exception e) { }
			}
		}
		return files;
	}
	
	private static void setList(Collection<MediaFile> destinationList, String ids) {
		Collection<MediaFile> files = generateList(ids);
		destinationList.clear();
		destinationList.addAll(files);
	}
	
	public String getHistoryList() {
		return generateList(playMode.getHistory());
	}
	
	public String getQueueList() {
		return generateList(playMode.getQueue());
	}
	
	public void reset(String historyIDs, String queueIDs, long currentID) {
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