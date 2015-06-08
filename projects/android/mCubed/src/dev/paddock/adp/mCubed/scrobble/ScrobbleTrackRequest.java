package dev.paddock.adp.mCubed.scrobble;

public class ScrobbleTrackRequest extends ScrobbleRequest<ScrobbleTrackResponse> {

	private int trackCount;

	public ScrobbleTrackRequest() {
		super(ScrobbleTrackResponse.class);
	}

	public int getTrackCount() {
		return trackCount;
	}

	/**
	 * Adds a track to be scrobbled. At most 50 scrobbles can be added to one request.
	 * 
	 * @param artist The artist of the track to scrobble.
	 * @param albumArtist The album artist of the track to scrobble.
	 * @param album The album of the track to scrobble.
	 * @param track The tile of the track to scrobble.
	 * @param trackNumber The track number of the track to scrobble
	 * @param duration The duration of the track to scrobble in seconds.
	 * @param timestamp The time the track to scrobble started playing. Should be the number of seconds since Jan. 1, 1970 UTC and should be calculated in the UTC time zone.
	 * @throws IllegalStateException Thrown if 50 scrobbles already exist in the request.
	 */
	public void addTrack(String artist, String albumArtist, String album, String track, int trackNumber, int duration, int timestamp) {
		if (trackCount >= 50) {
			throw new IllegalStateException("Cannot add more than 50 tracks per request.");
		}
		String index = String.format("[%s]", trackCount++);
		setValue("artist" + index, artist);
		setValue("albumArtist" + index, albumArtist);
		setValue("album" + index, album);
		setValue("track" + index, track);
		setValue("trackNumber" + index, trackNumber);
		setValue("duration" + index, duration);
		setValue("timestamp" + index, timestamp);
	}

	@Override
	public String getMethod() {
		return "track.scrobble";
	}
}
