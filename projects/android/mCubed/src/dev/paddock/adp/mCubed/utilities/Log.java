package dev.paddock.adp.mCubed.utilities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import dev.paddock.adp.mCubed.Schema;

public class Log {
	private static final String TAG = Schema.TAG;
	private static final String NL = System.getProperty("line.separator");
	private static final DateFormat FORMAT = new SimpleDateFormat("M/d/yyyy h:mm:ss a", Locale.US);
	public static final int D = 4; // DEBUG
	public static final int E = 1; // ERROR
	public static final int I = 3; // INFO
	public static final int V = 5; // VERBOSE
	public static final int W = 2; // WARN
	public static final int SUPPRESS = 0; // SUPPRESS
	public static final int DEFAULT_FILE_LEVEL = W; // MINIMUM LEVEL THAT IS LOGGED TO A FILE
	private static int fileLevel = DEFAULT_FILE_LEVEL;
	
	public static void setFileLoggingEnabled(boolean isFileLoggingEnabled) {
		if (isFileLoggingEnabled) {
			fileLevel = DEFAULT_FILE_LEVEL;
		} else {
			fileLevel = SUPPRESS;
			clearLogFile();
		}
	}
	
	public static boolean isDebug() {
		return android.util.Log.isLoggable(TAG, android.util.Log.DEBUG);
	}
	
	public static boolean isError() {
		return android.util.Log.isLoggable(TAG, android.util.Log.ERROR);
	}
	
	public static boolean isInfo() {
		return android.util.Log.isLoggable(TAG, android.util.Log.INFO);
	}
	
	public static boolean isVerbose() {
		return android.util.Log.isLoggable(TAG, android.util.Log.VERBOSE);
	}
	
	public static boolean isWarn() {
		return android.util.Log.isLoggable(TAG, android.util.Log.WARN);
	}
	
	public static int d(String msg) {
		appendLogFile(D, msg);
		return isDebug() ? android.util.Log.d(TAG, msg) : 0;
	}
	
	public static int d(String msg, boolean showStackTrace) {
		if (showStackTrace) {
			try {
				throw new Exception();
			} catch (Exception e) {
				return d(msg, e);
			}
		} else {
			return d(msg);
		}
	}
	
	public static int d(Throwable tr) {
		return d(tr.getMessage(), tr);
	}
	
	public static int d(String msg, Throwable tr) {
		appendLogFile(D, msg, tr);
		return isDebug() ? android.util.Log.d(TAG, msg, tr) : 0;
	}
	
	public static int e(String msg) {
		appendLogFile(E, msg);
		return isError() ? android.util.Log.e(TAG, msg) : 0;
	}
	
	public static int e(String msg, boolean showStackTrace) {
		if (showStackTrace) {
			try {
				throw new Exception();
			} catch (Exception e) {
				return e(msg, e);
			}
		} else {
			return e(msg);
		}
	}
	
	public static int e(Throwable tr) {
		return e(tr.getMessage(), tr);
	}
	
	public static int e(String msg, Throwable tr) {
		appendLogFile(E, msg, tr);
		return isError() ? android.util.Log.e(TAG, msg, tr) : 0;
	}
	
	public static int i(String msg) {
		appendLogFile(I, msg);
		return isInfo() ? android.util.Log.i(TAG, msg) : 0;
	}
	
	public static int i(String msg, boolean showStackTrace) {
		if (showStackTrace) {
			try {
				throw new Exception();
			} catch (Exception e) {
				return i(msg, e);
			}
		} else {
			return i(msg);
		}
	}
	
	public static int i(Throwable tr) {
		return i(tr.getMessage(), tr);
	}
	
	public static int i(String msg, Throwable tr) {
		appendLogFile(I, msg, tr);
		return isInfo() ? android.util.Log.i(TAG, msg, tr) : 0;
	}
	
	public static int v(String msg) {
		appendLogFile(V, msg);
		return isVerbose() ? android.util.Log.v(TAG, msg) : 0;
	}
	
	public static int v(String msg, boolean showStackTrace) {
		if (showStackTrace) {
			try {
				throw new Exception();
			} catch (Exception e) {
				return v(msg, e);
			}
		} else {
			return v(msg);
		}
	}
	
	public static int v(Throwable tr) {
		return v(tr.getMessage(), tr);
	}
	
	public static int v(String msg, Throwable tr) {
		appendLogFile(V, msg, tr);
		return isVerbose() ? android.util.Log.v(TAG, msg, tr) : 0;
	}
	
	public static int w(String msg) {
		appendLogFile(W, msg);
		return isWarn() ? android.util.Log.w(TAG, msg) : 0;
	}
	
	public static int w(String msg, boolean showStackTrace) {
		if (showStackTrace) {
			try {
				throw new Exception();
			} catch (Exception e) {
				return w(msg, e);
			}
		} else {
			return w(msg);
		}
	}
	
	public static int w(Throwable tr) {
		return w(tr.getMessage(), tr);
	}
	
	public static int w(String msg, Throwable tr) {
		appendLogFile(W, msg, tr);
		return isWarn() ? android.util.Log.w(TAG, msg, tr) : 0;
	}
	
	private static String typeToString(int type) {
		switch(type) {
		case D: return "Debug";
		case E: return "Error";
		case I: return "Info";
		case V: return "Verbose";
		case W: return "Warn";
		}
		return "";
	}
	
	private static String buildFileLog(int type, String msg, Throwable tr) {
		StringBuilder builder = new StringBuilder(typeToString(type));
		builder.append(" (");
		builder.append(FORMAT.format(Calendar.getInstance().getTime()));
		builder.append("): ");
		builder.append(msg);
		builder.append(NL);
		builder.append(android.util.Log.getStackTraceString(tr));
		return builder.toString();
	}
	
	private static void appendLogFile(int type, String msg) {
		appendLogFile(type, msg, null);
	}
	
	private static void appendLogFile(int type, String msg, Throwable tr) {
		if (type <= fileLevel) {
			int tempLevel = fileLevel;
			fileLevel = SUPPRESS;
			Utilities.appendToFile(Schema.FILE_LOGS, buildFileLog(type, msg, tr));
			fileLevel = tempLevel;
		}
	}
	
	public static String readLogFile() {
		return Utilities.loadFile(Schema.FILE_LOGS);
	}
	
	public static boolean clearLogFile() {
		return Utilities.deleteFile(Schema.FILE_LOGS);
	}
}