package dev.paddock.adp.mCubed.scrobble;

import android.test.AndroidTestCase;
import dev.paddock.adp.mCubed.ScrobbleResponseUtils;

public class MobileSessionResponseTest extends AndroidTestCase {
	public void testSuccessResponse() throws Exception {
		MobileSessionResponse response = ScrobbleResponseUtils.parseResponse(MobileSessionResponse.class, "MobileSessionResponseSuccess.xml");
		assertNotNull(response);
		assertEquals("12345abcde", response.getKey());
		assertEquals("mCubed", response.getName());
	}
}
