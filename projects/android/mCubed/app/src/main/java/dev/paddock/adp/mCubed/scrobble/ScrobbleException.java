package dev.paddock.adp.mCubed.scrobble;

import java.util.Locale;

public class ScrobbleException extends Exception {
	private static final long serialVersionUID = 1L;
	private final String errorCode;
	private final String errorMessage;

	public ScrobbleException(Throwable cause) {
		this("1", cause.getMessage(), cause);
	}

	public ScrobbleException(String errorCode, String errorMessage) {
		this(errorCode, errorMessage, null);
	}

	public ScrobbleException(String errorCode, String errorMessage, Throwable cause) {
		super(String.format(Locale.US, "Error occurred with Scrobble [ErrorCode=%s, ErrorMessage=%s]", errorCode, errorMessage), cause);
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * Determines whether or not the exception occurred due to a connection failure.
	 * 
	 * @return True if the exception indicates a connection failure, or false otherwise.
	 */
	public boolean isNoConnection() {
		return "1".equals(getErrorCode());
	}
}
