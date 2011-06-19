package dev.paddock.adp.mCubed.model.playModes;

import java.util.Collection;

import dev.paddock.adp.mCubed.model.MediaFile;

public interface IPlayMode {
	void addedToPlaylist(MediaFile file);
	void removedFromPlaylist(MediaFile file);
	void addedToQueue(MediaFile file);
	void removedFromQueue(MediaFile file);
	MediaFile getNext();
	void reset(Collection<MediaFile> allFiles);
	void reset(Collection<MediaFile> allFiles, Collection<MediaFile> history, MediaFile current, Collection<MediaFile> queue);
}