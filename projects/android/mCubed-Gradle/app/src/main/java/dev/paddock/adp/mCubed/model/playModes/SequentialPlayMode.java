package dev.paddock.adp.mCubed.model.playModes;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import dev.paddock.adp.mCubed.model.MediaFile;

public class SequentialPlayMode implements IPlayMode {
	private final List<MediaFile> remainingFiles = new LinkedList<MediaFile>();
	
	@Override
	public void addedToPlaylist(MediaFile file) {
		remainingFiles.add(file);
	}

	@Override
	public void removedFromPlaylist(MediaFile file) {
		remainingFiles.remove(file);
	}

	@Override
	public void addedToQueue(MediaFile file) {
		remainingFiles.remove(file);
	}

	@Override
	public void removedFromQueue(MediaFile file) {
		remainingFiles.add(file);
	}

	@Override
	public MediaFile getNext() {
		if (remainingFiles.isEmpty()) {
			return null;
		}
		return remainingFiles.remove(0);
	}

	@Override
	public void reset(Collection<MediaFile> allFiles) {
		reset(allFiles, Collections.<MediaFile>emptyList(), null, Collections.<MediaFile>emptyList());
	}

	@Override
	public void reset(Collection<MediaFile> allFiles, Collection<MediaFile> history, MediaFile current, Collection<MediaFile> queue) {
		remainingFiles.clear();
		for (MediaFile file : allFiles) {
			if (current != file && !history.contains(file) && !queue.contains(file)) {
				remainingFiles.add(file);
			}
		}
	}
}