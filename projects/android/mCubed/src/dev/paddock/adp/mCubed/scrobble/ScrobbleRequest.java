package dev.paddock.adp.mCubed.scrobble;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.NameValuePair;

import dev.paddock.adp.mCubed.utilities.Log;
import dev.paddock.adp.mCubed.utilities.Utilities;

public abstract class ScrobbleRequest {
	private final List<ScrobbleKeyValuePair> scrobblePairs = new ArrayList<ScrobbleKeyValuePair>();
	private static final String API_KEY = "06bd23c697c83e151b2447c204f9c9bb";
	private static final String API_SECRET = "7763946374c097697b86e23d148cf46b";

	public ScrobbleRequest() {
		setupCommonPairs();
	}

	private void setupCommonPairs() {
		setValue("api_key", API_KEY);
		setValue("method", getMethod());
		if (requiresSessionKey()) {
			String sessionKey = ScrobbleService.getSessionKey();
			if (!Utilities.isNullOrEmpty(sessionKey)) {
				setValue("sk", sessionKey);
			}
		}
	}

	private String calculateSignature() {
		StringBuilder builder = new StringBuilder();
		Collections.sort(scrobblePairs, new ScrobbleKeyValuePairComparator());
		for (ScrobbleKeyValuePair pair : scrobblePairs) {
			if (!pair.getKey().equals("api_sig")) {
				Object value = pair.getValue();
				builder.append(pair.getKey());
				builder.append(value == null ? "" : value.toString());
			}
		}
		builder.append(API_SECRET);
		try {
			byte[] bytes = builder.toString().getBytes("UTF-8");
			byte[] hashedBytes = MessageDigest.getInstance("MD5").digest(bytes);
			StringBuilder hashedBuilder = new StringBuilder(2 * hashedBytes.length);
			for (byte hashedByte : hashedBytes) {
				hashedBuilder.append(String.format("%02x", hashedByte & 0xff));
			}
			return hashedBuilder.toString();
		} catch (Exception e) {
			Log.e(e);
			return null;
		}
	}

	private void updateSignature() {
		setValue("api_sig", calculateSignature());
	}

	public List<NameValuePair> createParameters() {
		List<NameValuePair> parameters = new ArrayList<NameValuePair>();
		updateSignature();
		for (ScrobbleKeyValuePair pair : scrobblePairs) {
			parameters.add(pair.createNameValuePair());
		}
		return parameters;
	}

	public List<ScrobbleKeyValuePair> getScrobblePairs() {
		return Collections.unmodifiableList(scrobblePairs);
	}

	public <T> T getValue(Class<T> clazz, String key) {
		for (ScrobbleKeyValuePair pair : scrobblePairs) {
			if (pair.getKey().equals(key)) {
				return Utilities.cast(clazz, pair.getValue());
			}
		}
		return null;
	}

	public void setValue(String key, Object value) {
		for (ScrobbleKeyValuePair pair : scrobblePairs) {
			if (pair.getKey().equals(key)) {
				pair.setValue(value);
				return;
			}
		}
		scrobblePairs.add(new ScrobbleKeyValuePair(key, value));
	}

	protected boolean requiresSessionKey() {
		return true;
	}

	public abstract String getMethod();
}
