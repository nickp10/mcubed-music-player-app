package dev.paddock.adp.mCubed.scrobble;

import android.test.AndroidTestCase;
import dev.paddock.adp.mCubed.ScrobbleResponseUtils;

public class MobileSessionResponseTest extends AndroidTestCase {
	public void testSuccess() throws Exception {
		MobileSessionResponse response = ScrobbleResponseUtils.parseResponse(MobileSessionResponse.class, "MobileSessionResponseSuccess.xml");
		assertNotNull(response);
		assertEquals("12345abcde", response.getKey());
		assertEquals("mCubed", response.getName());
	}

	public void testFailInvalidPassword() throws Exception {
		try {
			ScrobbleResponseUtils.parseResponse(MobileSessionResponse.class, "MobileSessionResponseInvalidPassword.xml");
			fail("Scrobble exception expected");
		} catch (ScrobbleException e) {
			assertEquals("4", e.getErrorCode());
			assertNotNull(e.getErrorMessage());
		}
	}
	
	public void test() throws Exception {
		//MobileSessionRequest request = new MobileSessionRequest("nickp_10", "invalid");
		//ScrobbleService.sendRequest(request);
	}
}
