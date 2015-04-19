package dev.paddock.adp.mCubed.scrobble;

public class ScrobbleResponse {
	public static <T extends ScrobbleResponse> T parse(Class<T> clazz, String response) throws ScrobbleException {
		try {
			return clazz.newInstance();
		} catch (Exception e) {
			throw new ScrobbleException(e);
		}
	}
}
