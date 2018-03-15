package dev.paddock.adp.mCubed.scrobble;

import dev.paddock.adp.mCubed.utilities.XMLNode;

public class UpdateNowPlayingResponse extends ScrobbleResponse {
	private String album, albumArtist, artist, track, ignoredMessage;

	public String getAlbum() {
		return album;
	}

	public String getAlbumArtist() {
		return albumArtist;
	}

	public String getArtist() {
		return artist;
	}

	public String getTrack() {
		return track;
	}

	public String getIgnoredMessage() {
		return ignoredMessage;
	}

	@Override
	protected void parse(XMLNode node) {
		album = node.getNodePathValue("nowplaying/album");
		albumArtist = node.getNodePathValue("nowplaying/albumArtist");
		artist = node.getNodePathValue("nowplaying/artist");
		track = node.getNodePathValue("nowplaying/track");
		ignoredMessage = node.getNodePathValue("nowplaying/ignoredMessage");
	}
}
