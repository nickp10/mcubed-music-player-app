package dev.paddock.adp.mCubed.scrobble;

import java.util.Locale;

import dev.paddock.adp.mCubed.listeners.IListener;
import dev.paddock.adp.mCubed.model.MediaFile;
import dev.paddock.adp.mCubed.model.MediaStatus;
import dev.paddock.adp.mCubed.model.NotificationArgs;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.INotifyListener;
import dev.paddock.adp.mCubed.utilities.Log;
import dev.paddock.adp.mCubed.utilities.PropertyManager;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class ScrobbleListener implements IListener {
	private boolean hasUpdatedNowPlaying;
	private final INotifyListener mediaFileIDChanged = new INotifyListener() {
		@Override
		public void propertyChanging(Object instance, NotificationArgs args) {
		}

		@Override
		public void propertyChanged(Object instance, NotificationArgs args) {
			onMediaFileChanged(Utilities.cast(MediaFile.class, args.getOldValue()), Utilities.cast(MediaFile.class, args.getNewValue()));
		}
	};

	private final INotifyListener seekChanged = new INotifyListener() {
		@Override
		public void propertyChanging(Object instance, NotificationArgs args) {
		}

		@Override
		public void propertyChanged(Object instance, NotificationArgs args) {
			onSeekChanged(Utilities.cast(Integer.class, args.getOldValue()), Utilities.cast(Integer.class, args.getNewValue()));
		}
	};

	private final INotifyListener statusChanged = new INotifyListener() {
		@Override
		public void propertyChanging(Object instance, NotificationArgs args) {
		}

		@Override
		public void propertyChanged(Object instance, NotificationArgs args) {
			onStatusChanged(Utilities.cast(MediaStatus.class, args.getOldValue()), Utilities.cast(MediaStatus.class, args.getNewValue()));
		}
	};

	@Override
	public void register() {
		PropertyManager.register(App.getPlayer(), "MediaFile", mediaFileIDChanged);
		PropertyManager.register(App.getPlayer(), "SeekListening", seekChanged);
		PropertyManager.register(App.getPlayer(), "Status", statusChanged);
		onMediaFileChanged(null, App.getPlayingMedia());
	}

	@Override
	public void unregister() {
		PropertyManager.unregister(mediaFileIDChanged);
		PropertyManager.unregister(seekChanged);
		PropertyManager.unregister(statusChanged);
	}

	private void onMediaFileChanged(MediaFile oldFile, MediaFile newFile) {
		hasUpdatedNowPlaying = false;
		updateNowPlaying(newFile, App.getPlayer().getStatus());
	}

	private void onSeekChanged(int oldSeek, int newSeek) {
	}

	private void onStatusChanged(MediaStatus oldStatus, MediaStatus newStatus) {
		updateNowPlaying(App.getPlayingMedia(), newStatus);
	}

	private void updateNowPlaying(final MediaFile file, final MediaStatus status) {
		if (ScrobbleService.isTurnedOn() && ScrobbleService.isLoggedIn()) {
			if (file != null && status == MediaStatus.Play) {
				Utilities.dispatchToBackgroundThread(Utilities.getContext(), new Runnable() {

					@Override
					public void run() {
						if (!hasUpdatedNowPlaying) {
							synchronized (ScrobbleListener.this) {
								if (!hasUpdatedNowPlaying) {
									UpdateNowPlayingRequest request = new UpdateNowPlayingRequest(file.getArtist(), file.getTitle());
									request.setAlbum(file.getAlbum());
									request.setDuration((int) (file.getDuration() / 1000L));
									request.setTrackNumber(file.getTrack());
									try {
										UpdateNowPlayingResponse response = ScrobbleService.sendRequest(request);
										if (response != null) {
											hasUpdatedNowPlaying = true;
											Log.i(String.format(Locale.US, "Scrobble: Update now playing request successful [Artist=%s, Title=%s]", response.getArtist(), response.getTrack()));
										}
									} catch (ScrobbleException e) {
										Log.e(e);
									}
								}
							}
						}
					}
				});
			}
		}
	}
}
