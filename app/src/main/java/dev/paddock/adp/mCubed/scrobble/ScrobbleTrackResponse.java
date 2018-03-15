package dev.paddock.adp.mCubed.scrobble;

import java.util.ArrayList;
import java.util.List;

import dev.paddock.adp.mCubed.utilities.XMLNode;

public class ScrobbleTrackResponse extends ScrobbleResponse {

	private final List<String> albums = new ArrayList<String>();
	private final List<String> albumArtists = new ArrayList<String>();
	private final List<String> artists = new ArrayList<String>();
	private final List<String> tracks = new ArrayList<String>();
	private final List<String> ignoredMessages = new ArrayList<String>();
	private int trackCount;

	public String getAlbum(int index) {
		return albums.get(index);
	}

	public String getAlbumArtist(int index) {
		return albumArtists.get(index);
	}

	public String getArtist(int index) {
		return artists.get(index);
	}

	public String getTrack(int index) {
		return tracks.get(index);
	}

	public String getIgnoredMessage(int index) {
		return ignoredMessages.get(index);
	}

	public int getTrackCount() {
		return trackCount;
	}

	@Override
	protected void parse(XMLNode node) {
		for (XMLNode scrobble : node.getChildNode("scrobbles").getChildNodes("scrobble")) {
			albums.add(scrobble.getNodePathValue("album"));
			albumArtists.add(scrobble.getNodePathValue("albumArtist"));
			artists.add(scrobble.getNodePathValue("artist"));
			tracks.add(scrobble.getNodePathValue("track"));
			ignoredMessages.add(scrobble.getNodePathValue("ignoredMessage"));
			trackCount++;
		}
	}
}
