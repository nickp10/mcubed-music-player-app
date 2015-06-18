package dev.paddock.adp.mCubed.scrobble;

import android.test.AndroidTestCase;
import dev.paddock.adp.mCubed.ScrobbleResponseUtils;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class ScrobbleTrackResponseTest extends AndroidTestCase {

	public void testOneTrackSuccess() throws Exception {
		ScrobbleTrackResponse response = ScrobbleResponseUtils.parseResponse(ScrobbleTrackResponse.class, "ScrobbleTrackResponseOneTrackSuccess.xml");
		assertNotNull(response);
		assertEquals(1, response.getTrackCount());
		assertEquals("Eminem", response.getArtist(0));
		assertEquals("Eminem", response.getAlbumArtist(0));
		assertEquals("Recovery", response.getAlbum(0));
		assertEquals("Not Afraid", response.getTrack(0));
		assertTrue(Utilities.isNullOrEmpty(response.getIgnoredMessage(0)));
	}

	public void testTenTrackSuccess() throws Exception {
		ScrobbleTrackResponse response = ScrobbleResponseUtils.parseResponse(ScrobbleTrackResponse.class, "ScrobbleTrackResponseTenTrackSuccess.xml");
		assertNotNull(response);
		assertEquals(10, response.getTrackCount());
		for (int i = 0; i < 10; i++) {
			assertEquals("Eminem", response.getArtist(i));
			assertEquals("Eminem", response.getAlbumArtist(i));
			assertEquals("Recovery", response.getAlbum(i));
			assertTrue(Utilities.isNullOrEmpty(response.getIgnoredMessage(i)));
		}
		assertEquals("Cold Wind Blows", response.getTrack(0));
		assertEquals("Talkin' 2 Myself", response.getTrack(1));
		assertEquals("On Fire", response.getTrack(2));
		assertEquals("Won't Back Down", response.getTrack(3));
		assertEquals("W.T.P.", response.getTrack(4));
		assertEquals("Going Through Changes", response.getTrack(5));
		assertEquals("Not Afraid", response.getTrack(6));
		assertEquals("Seduction", response.getTrack(7));
		assertEquals("No Love", response.getTrack(8));
		assertEquals("Space Bound", response.getTrack(9));
	}

	public void testFailInvalidSignature() throws Exception {
		try {
			ScrobbleResponseUtils.parseResponse(ScrobbleTrackResponse.class, "ScrobbleTrackResponseInvalidSignature.xml");
			fail("Scrobble exception expected");
		} catch (ScrobbleException e) {
			assertEquals("13", e.getErrorCode());
			assertNotNull(e.getErrorMessage());
		}
	}

	public void testFailMissingParameter() throws Exception {
		try {
			ScrobbleResponseUtils.parseResponse(ScrobbleTrackResponse.class, "ScrobbleTrackResponseMissingParameter.xml");
			fail("Scrobble exception expected");
		} catch (ScrobbleException e) {
			assertEquals("6", e.getErrorCode());
			assertNotNull(e.getErrorMessage());
		}
	}

	public void testFailTwoTrackMissingParameter() throws Exception {
		try {
			ScrobbleResponseUtils.parseResponse(ScrobbleTrackResponse.class, "ScrobbleTrackResponseTwoTrackMissingParameter.xml");
			fail("Scrobble exception expected");
		} catch (ScrobbleException e) {
			assertEquals("6", e.getErrorCode());
			assertNotNull(e.getErrorMessage());
		}
	}
}
