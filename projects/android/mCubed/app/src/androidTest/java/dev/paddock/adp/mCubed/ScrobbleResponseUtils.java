package dev.paddock.adp.mCubed;

import java.io.InputStream;

import dev.paddock.adp.mCubed.scrobble.ScrobbleException;
import dev.paddock.adp.mCubed.scrobble.ScrobbleResponse;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class ScrobbleResponseUtils {
	public static <TResponse extends ScrobbleResponse> TResponse parseResponse(Class<TResponse> clazz, String assetName) throws ScrobbleException {
		InputStream in = ScrobbleResponseUtils.class.getClassLoader().getResourceAsStream("assets/" + assetName);
		String response = Utilities.loadStream(in);
		return ScrobbleResponse.parse(clazz, response);
	}
}
