package dev.paddock.adp.mCubed.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.model.playModes.IPlayMode;
import dev.paddock.adp.mCubed.preferences.PlayModeEnum;
import dev.paddock.adp.mCubed.utilities.PreferenceManager;

public class PlayMode {
	private final Stack<MediaFile> history = new Stack<MediaFile>();
	private final List<MediaFile> queue = new LinkedList<MediaFile>();
	private MediaFile current;
	private boolean currentRequiresRepeat;
	private IPlayMode playMode;
	private PlayModeEnum playModeEnum;
	private Playlist playlist;
	
	public PlayMode(Playlist playlist) {
		this(playlist, PlayModeEnum.Sequential);
	}
	
	public PlayMode(Playlist playlist, PlayModeEnum playMode) {
		setPlayModeEnum(playMode);
		setPlaylist(playlist);
	}
	
	public PlayMode(Playlist playlist, boolean clearQueue) {
		this(playlist, PlayModeEnum.Sequential, clearQueue);
	}
	
	public PlayMode(Playlist playlist, PlayModeEnum playMode, boolean clearQueue) {
		setPlayModeEnum(playMode, clearQueue);
		setPlaylist(playlist);
	}
	
	public List<MediaFile> getAllFiles() {
		return playlist == null ? Collections.<MediaFile>emptyList() : playlist.getFiles();
	}
	
	public boolean isEmpty() {
		return getAllFiles().isEmpty();
	}
	
	public List<MediaFile> getHistory() {
		return history;
	}
	
	public MediaFile getCurrent() {
		return current;
	}
	
	public void setCurrent(MediaFile file) {
		this.current = file;
	}
	
	public List<MediaFile> getQueue() {
		return queue;
	}
	
	public boolean getCurrentRequiresRepeat() {
		return currentRequiresRepeat;
	}
	
	public void reset(boolean clearQueue) {
		if (playMode != null) {
			if (clearQueue) {
				queue.clear();
			}
			playMode.reset(getAllFiles(), history, current, queue);
			resetCurrent();
			generateNext();
		}
	}
	
	private void setPlayMode(IPlayMode playMode, boolean clearQueue) {
		if (playMode != null && this.playMode != playMode) {
			this.playMode = playMode;
			reset(clearQueue);
		}
	}
	
	public void setPlayModeEnum(PlayModeEnum playModeEnum) {
		boolean clearQueue = PreferenceManager.getSettingBoolean(R.string.pref_clear_queue_with_play_mode);
		setPlayModeEnum(playModeEnum, clearQueue);
	}
	
	public void setPlayModeEnum(PlayModeEnum playModeEnum, boolean clearQueue) {
		if (playModeEnum != null && this.playModeEnum != playModeEnum) {
			this.playModeEnum = playModeEnum;
			setPlayMode(this.playModeEnum.getPlayMode(), clearQueue);
		}
	}
	
	public void setPlaylist(Playlist playlist) {
		if (playlist != null && this.playlist != playlist) {
			this.playlist = playlist;
			playMode.reset(getAllFiles());
			history.clear();
			queue.clear();
			current = null;
			currentRequiresRepeat = false;
			resetCurrent();
		}
	}
	
	public void addedToPlaylist(MediaFile file) {
		playMode.addedToPlaylist(file);
		resetCurrent();
	}
	
	public void removedFromPlaylist(MediaFile file) {
		playMode.removedFromPlaylist(file);
		queue.remove(file);
		history.remove(file);
		if (current == file) {
			next();
		}
		if (current == file) {
			current = null;
			currentRequiresRepeat = false;
		}
		queue.remove(file);
		history.remove(file);
		generateNext();
	}
	
	public void appendToQueue(MediaFile file) {
		insertIntoQueue(file, queue.size());
	}
	
	public void insertIntoQueue(MediaFile file, int index) {
		if (current != file && index >= 0 && index <= queue.size() && !queue.contains(file)) {
			history.remove(file);
			queue.add(index, file);
			playMode.addedToQueue(file);
		}
	}
	
	public void moveWithinQueue(MediaFile file, int newIndex) {
		if (newIndex >= 0 && newIndex < queue.size() && queue.remove(file)) {
			queue.add(newIndex, file);
		}
	}
	
	public void removeFromQueue(MediaFile file) {
		if (queue.remove(file)) {
			playMode.removedFromQueue(file);
			generateNext();
		}
	}
	
	private void generateNext() {
		if (queue.isEmpty()) {
			MediaFile next = playMode.getNext();
			if (next != null) {
				queue.add(next);
			}
		}
	}
	
	public void next() {
		currentRequiresRepeat = false;
		if (isEmpty()) {
			current = null;
		} else {
			if (current != null) {
				history.push(current);
			}
			generateNext();
			if (queue.isEmpty()) {
				queue.addAll(history);
				history.clear();
				currentRequiresRepeat = true;
			}
			current = queue.remove(0);
			generateNext();
		}
	}
	
	public void previous() {
		currentRequiresRepeat = false;
		if (isEmpty()) {
			current = null;
		} else if (!history.isEmpty()) {
			if (current != null) {
				queue.add(0, current);
			}
			current = history.pop();
		}
	}
	
	private void resetCurrent() {
		if (current == null) {
			next();
		} else {
			generateNext();
		}
	}
}