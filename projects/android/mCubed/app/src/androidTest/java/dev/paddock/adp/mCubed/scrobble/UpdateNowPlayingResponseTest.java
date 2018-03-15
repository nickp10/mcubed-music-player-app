package dev.paddock.adp.mCubed.scrobble;

import android.test.AndroidTestCase;
import dev.paddock.adp.mCubed.ScrobbleResponseUtils;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class UpdateNowPlayingResponseTest extends AndroidTestCase {

	public void testSuccess() throws Exception {
		UpdateNowPlayingResponse response = ScrobbleResponseUtils.parseResponse(UpdateNowPlayingResponse.class, "UpdateNowPlayingResponseSuccess.xml");
		assertNotNull(response);
		assertEquals("Eminem", response.getArtist());
		assertEquals("Eminem", response.getAlbumArtist());
		assertEquals("Recovery", response.getAlbum());
		assertEquals("Not Afraid", response.getTrack());
		assertTrue(Utilities.isNullOrEmpty(response.getIgnoredMessage()));
	}

	public void testFailInvalidSignature() throws Exception {
		try {
			ScrobbleResponseUtils.parseResponse(UpdateNowPlayingResponse.class, "UpdateNowPlayingResponseInvalidSignature.xml");
			fail("Scrobble exception expected");
		} catch (ScrobbleException e) {
			assertEquals("13", e.getErrorCode());
			assertNotNull(e.getErrorMessage());
		}
	}

	public void testFailMissingParameter() throws Exception {
		try {
			ScrobbleResponseUtils.parseResponse(UpdateNowPlayingResponse.class, "UpdateNowPlayingResponseMissingParameter.xml");
			fail("Scrobble exception expected");
		} catch (ScrobbleException e) {
			assertEquals("6", e.getErrorCode());
			assertNotNull(e.getErrorMessage());
		}
	}
}
