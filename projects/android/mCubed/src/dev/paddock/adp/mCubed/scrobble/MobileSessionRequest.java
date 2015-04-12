package dev.paddock.adp.mCubed.scrobble;

public class MobileSessionRequest extends ScrobbleRequest {

	public MobileSessionRequest(String username, String password) {
		setUsername(username);
		setPassword(password);
	}

	public String getUsername() {
		return getValue(String.class, "username");
	}

	public void setUsername(String username) {
		setValue("username", username);
	}

	public String getPassword() {
		return getValue(String.class, "password");
	}

	public void setPassword(String password) {
		setValue("password", password);
	}

	@Override
	public String getMethod() {
		return "auth.getMobileSession";
	}

	@Override
	protected boolean requiresSessionKey() {
		return false;
	}
}
