package dev.paddock.adp.mCubed.receivers;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.media.AudioManager;
import dev.paddock.adp.mCubed.compatability.AudioManagerCompat;
import dev.paddock.adp.mCubed.compatability.MediaMetadataRetrieverCompat;
import dev.paddock.adp.mCubed.compatability.MetadataEditorCompat;
import dev.paddock.adp.mCubed.compatability.RemoteControlClientCompat;
import dev.paddock.adp.mCubed.model.MediaFile;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class RemoteControlReceiver {
	private static RemoteControlClientCompat remoteControl;
	
	public static RemoteControlClientCompat getRemoteControl() {
		return remoteControl;
	}
	
	public static void registerRemoteControl(AudioManager manager, ComponentName component) {
		if (RemoteControlReceiver.remoteControl == null) {
			RemoteControlClientCompat remoteControl = createRemoteControlClient(component);
			AudioManagerCompat.registerRemoteControlClient(manager, remoteControl);
			RemoteControlReceiver.remoteControl = remoteControl;
			setupRemoteControlClient();
		}
	}
	
	public static void unregisterRemoteControl(AudioManager manager) {
		RemoteControlClientCompat remoteControl = getRemoteControl();
		if (remoteControl != null) {
			AudioManagerCompat.unregisterRemoteControlClient(manager, remoteControl);
			RemoteControlReceiver.remoteControl = null;
		}
	}
	
	private static RemoteControlClientCompat createRemoteControlClient(ComponentName component) {
		Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		intent.setComponent(component);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(Utilities.getContext(), 0, intent, 0);
		return new RemoteControlClientCompat(pendingIntent);
	}
	
	public static void setupRemoteControlClient() {
		RemoteControlClientCompat remoteControl = getRemoteControl();
		remoteControl.setTransportControlFlags(
				RemoteControlClientCompat.FLAG_KEY_MEDIA_NEXT |
				RemoteControlClientCompat.FLAG_KEY_MEDIA_PAUSE |
				RemoteControlClientCompat.FLAG_KEY_MEDIA_PLAY |
				RemoteControlClientCompat.FLAG_KEY_MEDIA_PLAY_PAUSE |
				RemoteControlClientCompat.FLAG_KEY_MEDIA_PREVIOUS |
				RemoteControlClientCompat.FLAG_KEY_MEDIA_STOP
		);
		updatePlaybackState();
		updateCurrentMetadata();
	}
	
	public static void updatePlaybackState() {
		int state = App.getPlayer().isPlaying() ? RemoteControlClientCompat.PLAYSTATE_PLAYING : RemoteControlClientCompat.PLAYSTATE_PAUSED;
		RemoteControlClientCompat remoteControl = getRemoteControl();
		remoteControl.setPlaybackState(state);
	}
	
	public static void updateCurrentMetadata() {
		RemoteControlClientCompat remoteControl = getRemoteControl();
		MediaFile media = App.getPlayingMedia();
		if (media == null) {
			remoteControl.editMetadata(true).apply();
		} else {
			remoteControl.editMetadata(true).
					putString(MediaMetadataRetrieverCompat.METADATA_KEY_ALBUM, media.getAlbum()).
					putString(MediaMetadataRetrieverCompat.METADATA_KEY_ALBUMARTIST, media.getArtist()).
					putString(MediaMetadataRetrieverCompat.METADATA_KEY_ARTIST, media.getArtist()).
					putLong(MediaMetadataRetrieverCompat.METADATA_KEY_CD_TRACK_NUMBER, media.getTrack()).
					putLong(MediaMetadataRetrieverCompat.METADATA_KEY_DURATION, media.getDuration()).
					putString(MediaMetadataRetrieverCompat.METADATA_KEY_GENRE, media.getGenre()).
					putString(MediaMetadataRetrieverCompat.METADATA_KEY_TITLE, media.getTitle()).
					putBitmap(MetadataEditorCompat.BITMAP_KEY_ARTWORK, Utilities.loadBitmap(media.getAlbumArt())).
					apply();
		}
	}
}
