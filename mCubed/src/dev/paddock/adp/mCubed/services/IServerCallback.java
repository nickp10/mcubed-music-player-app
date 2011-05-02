package dev.paddock.adp.mCubed.services;

import dev.paddock.adp.mCubed.model.MediaStatus;

public interface IServerCallback {
	void startService(int intentID);
	void stopService(int intentID);
	void setSeekListener(int intentID, boolean isSeekListener);
	void setPlaybackSeek(int intentID, int playbackSeek);
	void setPlaybackStatus(int intentID, MediaStatus playbackStatus);
	void movePlaybackNext(int intentID);
	void movePlaybackPrev(int intentID);
}