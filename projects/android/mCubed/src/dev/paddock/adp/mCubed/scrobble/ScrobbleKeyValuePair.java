package dev.paddock.adp.mCubed.scrobble;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class ScrobbleKeyValuePair {
	private String key;
	private Object value;

	public ScrobbleKeyValuePair() {
	}

	public ScrobbleKeyValuePair(String key, Object value) {
		setKey(key);
		setValue(value);
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public NameValuePair createNameValuePair() {
		Object value = getValue();
		return new BasicNameValuePair(getKey(), value == null ? "" : value.toString());
	}
}
