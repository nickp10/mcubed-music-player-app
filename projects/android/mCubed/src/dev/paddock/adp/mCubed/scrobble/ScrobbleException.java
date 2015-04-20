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
		super(String.format(Locale.US, "Error occurred with Scrobble [ErrorCode=%s, ErrorMessage=%s]", errorCode, errorMessage));
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
}
