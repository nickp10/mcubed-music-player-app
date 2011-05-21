package dev.paddock.adp.mCubed.model.playModes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import dev.paddock.adp.mCubed.model.MediaFile;

public class PerpetualShufflePlayMode implements IPlayMode {
	private final List<MediaFile> remainingFiles = new ArrayList<MediaFile>();
	private final Random random = new Random();

	@Override
	public void addedToPlaylist(MediaFile file) {
		remainingFiles.add(file);
	}

	@Override
	public void removedFromPlaylist(MediaFile file) {
		remainingFiles.remove(file);
	}

	@Override
	public void addedToQueue(MediaFile file) { }

	@Override
	public void removedFromQueue(MediaFile file) { }

	@Override
	public MediaFile getNext() {
		if (remainingFiles.isEmpty()) {
			return null;
		}
		int index = random.nextInt(remainingFiles.size());
		return remainingFiles.get(index);
	}

	@Override
	public void reset(Collection<MediaFile> allFiles) {
		reset(allFiles, Collections.<MediaFile>emptyList(), null, Collections.<MediaFile>emptyList());
	}

	@Override
	public void reset(Collection<MediaFile> allFiles, Collection<MediaFile> history, MediaFile current, Collection<MediaFile> queue) {
		remainingFiles.clear();
		remainingFiles.addAll(allFiles);
	}
}