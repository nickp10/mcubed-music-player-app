package dev.paddock.adp.mCubed.scrobble;

import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;

import dev.paddock.adp.mCubed.listeners.IListener;
import dev.paddock.adp.mCubed.model.DelayedTask;
import dev.paddock.adp.mCubed.model.MediaFile;
import dev.paddock.adp.mCubed.model.MediaStatus;
import dev.paddock.adp.mCubed.model.NotificationArgs;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.INotifyListener;
import dev.paddock.adp.mCubed.utilities.Log;
import dev.paddock.adp.mCubed.utilities.PropertyManager;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class ScrobbleListener implements IListener {
	private final ConcurrentLinkedQueue<ScrobbleTrackRequest> scrobbleRequests = new ConcurrentLinkedQueue<ScrobbleTrackRequest>();
	private ScrobbleTrackRequest currentScrobbleRequest;
	private DelayedTask scrobbleTask;
	private DelayedTask updateNowPlayingTask;
	private boolean[] seekFlags;
	private final INotifyListener mediaFileChanged = new INotifyListener() {
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
		PropertyManager.register(App.getPlayer(), "MediaFile", mediaFileChanged);
		PropertyManager.register(App.getPlayer(), "SeekListening", seekChanged);
		PropertyManager.register(App.getPlayer(), "Status", statusChanged);
		onMediaFileChanged(null, App.getPlayingMedia());
	}

	@Override
	public void unregister() {
		PropertyManager.unregister(mediaFileChanged);
		PropertyManager.unregister(seekChanged);
		PropertyManager.unregister(statusChanged);
	}

	private void onMediaFileChanged(MediaFile oldFile, MediaFile newFile) {
		Utilities.pushContext(App.getAppContext());
		try {
			if (oldFile != null && hasPlayed(oldFile)) {
				scrobble(oldFile);
			}
			if (newFile != null) {
				int fileDuration = (int) Math.ceil(newFile.getDuration() / 1000d);
				seekFlags = new boolean[fileDuration];
			}
			updateNowPlaying();
		} finally {
			Utilities.popContext();
		}
	}

	private void onSeekChanged(final int oldSeek, final int newSeek) {
		Utilities.dispatchToBackgroundThread(App.getAppContext(), new Runnable() {
			@Override
			public void run() {
				boolean[] seekFlags = ScrobbleListener.this.seekFlags;
				if (seekFlags != null) {
					int from = (int) Math.floor(oldSeek / 1000d);
					int to = (int) Math.floor(newSeek / 1000d);
					for (; from <= to; from++) {
						if (from < seekFlags.length) {
							seekFlags[from] = true;
						}
					}
				}
			}
		});
	}

	private void onStatusChanged(MediaStatus oldStatus, MediaStatus newStatus) {
		Utilities.pushContext(App.getAppContext());
		try {
			updateNowPlaying();
		} finally {
			Utilities.popContext();
		}
	}

	private void updateNowPlaying() {
		Utilities.dispatchToBackgroundThread(Utilities.getContext(), new Runnable() {

			@Override
			public void run() {
				final MediaFile file = App.getPlayingMedia();
				final boolean isPlaying = App.getPlayer().isPlaying();
				cancelUpdateNowPlayingTask();
				if (ScrobbleService.isTurnedOn() && ScrobbleService.isLoggedIn() && file != null && isPlaying) {
					synchronized (ScrobbleListener.this) {
						UpdateNowPlayingRequest request = new UpdateNowPlayingRequest(file.getArtist(), file.getTitle());
						request.setAlbum(file.getAlbum());
						request.setDuration((int) (file.getDuration() / 1000L));
						request.setTrackNumber(file.getTrack());
						try {
							UpdateNowPlayingResponse response = ScrobbleService.sendRequest(request);
							if (response != null) {
								Log.i(String.format(Locale.US, "Scrobble: Update now playing request successful [Artist=%s, Title=%s]", response.getArtist(), response.getTrack()));
							} else {
								Log.e(String.format(Locale.US, "Scrobble: No response was returned for now playing request [Artist=%s, Title=%s]", request.getArtist(), request.getTrack()));
							}
						} catch (ScrobbleException e) {
							if (e.isNoConnection()) {
								Log.i(String.format(Locale.US, "Scrobble: No connection was established for now playing request [Artist=%s, Title=%s]", request.getArtist(), request.getTrack()));
								retryUpdateNowPlaying();
							} else {
								Log.e(String.format(Locale.US, "Scrobble: An error occurred for now playing request [Artist=%s, Title=%s]", request.getArtist(), request.getTrack()), e);
							}
						}
					}
				}
			}
		});
	}

	private void cancelUpdateNowPlayingTask() {
		DelayedTask updateNowPlayingTask = this.updateNowPlayingTask;
		if (updateNowPlayingTask != null) {
			updateNowPlayingTask.cancel(false);
		}
	}

	private void retryUpdateNowPlaying() {
		updateNowPlayingTask = new DelayedTask(Utilities.getContext(), new Runnable() {

			@Override
			public void run() {
				ScrobbleListener.this.updateNowPlaying();
			}
		}, 30000);
	}

	private int countSeekFlags() {
		int count = 0;
		boolean[] seekFlags = this.seekFlags;
		if (seekFlags != null) {
			for (boolean seekFlag : seekFlags) {
				if (seekFlag) {
					count++;
				}
			}
		}
		return count;
	}

	private boolean hasPlayed(MediaFile file) {
		int fileDuration = (int) (file.getDuration() / 1000L);
		// Track must be at least 30 seconds long (defined by last.fm)
		if (fileDuration >= 30) {
			int listenDuration = countSeekFlags();
			// Player for half its duration or for 4 minutes (defined by last.fm)
			if (listenDuration >= (fileDuration / 2) || listenDuration >= 240) {
				return true;
			}
		}
		return false;
	}

	private void scrobble(final MediaFile file) {
		Utilities.dispatchToBackgroundThread(Utilities.getContext(), new Runnable() {

			@Override
			public void run() {
				synchronized (ScrobbleListener.this) {
					if (currentScrobbleRequest == null || currentScrobbleRequest.isFull()) {
						currentScrobbleRequest = new ScrobbleTrackRequest();
						scrobbleRequests.offer(currentScrobbleRequest);
					}
					currentScrobbleRequest.addTrack(file.getArtist(), null, file.getAlbum(), file.getTitle(), file.getTrack(), (int) (file.getDuration() / 1000L), Utilities.currentUnixTimeSeconds());
					sendScrobbles();
				}
			}
		});
	}

	private void sendScrobbles() {
		Utilities.dispatchToBackgroundThread(Utilities.getContext(), new Runnable() {

			@Override
			public void run() {
				cancelScrobbleTask();
				if (ScrobbleService.isTurnedOn() && ScrobbleService.isLoggedIn()) {
					synchronized (ScrobbleListener.this) {
						while (!scrobbleRequests.isEmpty()) {
							ScrobbleTrackRequest request = scrobbleRequests.peek();
							if (sendScrobbleRequest(request)) {
								scrobbleRequests.poll();
							} else {
								return;
							}
						}
						currentScrobbleRequest = null;
					}
				}
			}
		});
	}

	private boolean sendScrobbleRequest(ScrobbleTrackRequest request) {
		try {
			ScrobbleTrackResponse response = ScrobbleService.sendRequest(request);
			if (response != null) {
				Log.i(String.format(Locale.US, "Scrobble: Scrobble successful [TrackCount=%d]", response.getTrackCount()));
			} else {
				Log.e(String.format(Locale.US, "Scrobble: No response was returned for scrobble request [TrackCount=%d]", request.getTrackCount()));
			}
			return true;
		} catch (ScrobbleException e) {
			if (e.isNoConnection()) {
				Log.i(String.format(Locale.US, "Scrobble: No connection was established for scrobble request [TrackCount=%d]", request.getTrackCount()));
				retryScrobble();
				return false;
			} else {
				Log.e(String.format(Locale.US, "Scrobble: An error occurred for scrobble request [TrackCount=%d]", request.getTrackCount()), e);
				return true;
			}
		}
	}

	private void cancelScrobbleTask() {
		DelayedTask scrobbleTask = this.scrobbleTask;
		if (scrobbleTask != null) {
			scrobbleTask.cancel(false);
		}
	}

	private void retryScrobble() {
		scrobbleTask = new DelayedTask(Utilities.getContext(), new Runnable() {

			@Override
			public void run() {
				ScrobbleListener.this.sendScrobbles();
			}
		}, 30000);
	}
}
