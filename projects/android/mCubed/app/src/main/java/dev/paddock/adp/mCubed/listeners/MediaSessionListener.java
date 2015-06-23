package dev.paddock.adp.mCubed.listeners;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.listeners.IListener;
import dev.paddock.adp.mCubed.model.MediaFile;
import dev.paddock.adp.mCubed.receivers.MediaKeyReceiver;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class MediaSessionListener implements IListener {
	private final static MediaSessionListener instance = new MediaSessionListener();
	private MediaSessionCompat mediaSession;
	private final MediaKeyReceiver keyReceiver = new MediaKeyReceiver();

	private MediaSessionListener() {
	}

	public static MediaSessionListener getInstance() {
		return instance;
	}

	@Override
	public void register() {
		final Context context = Utilities.getContext();
		if (context != null) {
			ComponentName component = new ComponentName(context, MediaKeyReceiver.class);
			Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
			intent.setComponent(component);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
			mediaSession = new MediaSessionCompat(context, Schema.TAG, component, pendingIntent);
			mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
			mediaSession.setCallback(new MediaSessionCompat.Callback() {
				@Override
				public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
					keyReceiver.onReceive(context, mediaButtonEvent);
					return super.onMediaButtonEvent(mediaButtonEvent);
				}
			});
			mediaSession.setActive(true);
			updatePlaybackState();
			updateCurrentMetadata();
		}
	}

	@Override
	public void unregister() {
		mediaSession.setActive(false);
		mediaSession.release();
	}

	public void updatePlaybackState() {
		int state = App.getPlayer().isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
		int ms = App.getPlayer().getSeek();
		PlaybackStateCompat.Builder bob = new PlaybackStateCompat.Builder()
				.setActions(
						PlaybackStateCompat.ACTION_PLAY |
						PlaybackStateCompat.ACTION_PAUSE |
						PlaybackStateCompat.ACTION_PLAY_PAUSE |
						PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
						PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
						PlaybackStateCompat.ACTION_STOP
				);
		mediaSession.setPlaybackState(bob
				.setState(PlaybackStateCompat.STATE_NONE, ms, 1f)
				.build());
		mediaSession.setPlaybackState(bob
				.setState(state, ms, 1f)
				.build());
	}

	public void updateCurrentMetadata() {
		MediaFile media = App.getPlayingMedia();
		MediaMetadataCompat.Builder bob = new MediaMetadataCompat.Builder();
		if (media == null) {
			mediaSession.setMetadata(bob.build());
		} else {
			mediaSession.setMetadata(bob
					.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, media.getAlbum())
					.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, media.getArtist())
					.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, Utilities.loadBitmap(media.getAlbumArt()))
					.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, media.getArtist())
					.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, media.getDuration())
					.putString(MediaMetadataCompat.METADATA_KEY_GENRE, media.getGenre())
					.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, Long.toString(media.getID()))
					.putString(MediaMetadataCompat.METADATA_KEY_TITLE, media.getTitle())
					.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, media.getTrack())
					.putLong(MediaMetadataCompat.METADATA_KEY_YEAR, media.getYear())
					.build());
		}
	}
}
