package dev.paddock.adp.mCubed.scrobble;

public class UpdateNowPlayingRequest extends ScrobbleRequest<UpdateNowPlayingResponse> {

	public UpdateNowPlayingRequest(String artist, String track) {
		super(UpdateNowPlayingResponse.class);
		setArtist(artist);
		setTrack(track);
	}

	public String getAlbum() {
		return getValue(String.class, "album");
	}

	public void setAlbum(String album) {
		setValue("album", album);
	}

	public String getAlbumArtist() {
		return getValue(String.class, "albumArtist");
	}

	public void setAlbumArtist(String albumArtist) {
		setValue("albumArtist", albumArtist);
	}

	public String getArtist() {
		return getValue(String.class, "artist");
	}

	public void setArtist(String artist) {
		setValue("artist", artist);
	}

	/**
	 * Gets the duration of the track in seconds.
	 * 
	 * @return The duration of the track in seconds.
	 */
	public int getDuration() {
		return getValue(Integer.class, "duration");
	}

	/**
	 * Sets the duration of the track in seconds.
	 * 
	 * @param duration The duration of the track in seconds.
	 */
	public void setDuration(int duration) {
		setValue("duration", duration);
	}

	public String getTrack() {
		return getValue(String.class, "track");
	}

	public void setTrack(String track) {
		setValue("track", track);
	}

	public int getTrackNumber() {
		return getValue(Integer.class, "trackNumber");
	}

	public void setTrackNumber(int trackNumber) {
		setValue("trackNumber", trackNumber);
	}

	@Override
	public String getMethod() {
		return "track.updateNowPlaying";
	}
}
