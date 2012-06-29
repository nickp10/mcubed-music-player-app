package dev.paddock.adp.mCubed.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.model.AsyncTask;
import dev.paddock.adp.mCubed.model.Holder;
import dev.paddock.adp.mCubed.model.PublishProgress;
import dev.paddock.adp.mCubed.model.SortClause;
import dev.paddock.adp.mCubed.model.WhereClause;
import dev.paddock.adp.mCubed.receivers.MediaKeyReceiver;

public class Utilities {
	private static final Map<Long, Stack<Context>> contextMap = new HashMap<Long, Stack<Context>>();
	private static final Map<Long, Stack<AsyncTask>> taskMap = new HashMap<Long, Stack<AsyncTask>>();
	private static Handler handler;
	
	/**
	 * Prevents an instance of Utilities
	 */
	private Utilities() { }
	
	public static boolean isNullOrEmpty(String str) {
		return str == null || str.length() == 0;
	}
	
	public static int stringCompare(String leftString, String rightString, boolean ignoreCase) {
		if (leftString == null) {
			return rightString == null ? 0 : -1;
		} else if (rightString == null) {
			return 1;
		} else {
			return ignoreCase ? leftString.compareToIgnoreCase(rightString) : leftString.compareTo(rightString);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T cast(Class<T> clazz, Object obj) {
		if (obj == null || !clazz.isAssignableFrom(obj.getClass())) {
			return null;
		} else {
			return (T)obj;
		}
	}
	
	public static boolean getCursorBooleanValue(Cursor cursor, String columnName) {
		return getCursorIntValue(cursor, columnName) != 0;
	}

	public static double getCursorDoubleValue(Cursor cursor, String columnName) {
		// Validate the parameters
		if (cursor != null && columnName != null) {
			// Get the column index, and then the double at the index
			int index = cursor.getColumnIndex(columnName);
			if (index != -1) {
				return cursor.getDouble(index);
			}
			return 0d;
		}
		return 0d;
	}
	
	public static int getCursorIntValue(Cursor cursor, String columnName) {
		// Validate the parameters
		if (cursor != null && columnName != null) {
			// Get the column index, and then the int at the index
			int index = cursor.getColumnIndex(columnName);
			if (index != -1) {
				return cursor.getInt(index);
			}
			return 0;
		}
		return 0;
	}
	
	public static long getCursorLongValue(Cursor cursor, String columnName) {
		// Validate the parameters
		if (cursor != null && columnName != null) {
			// Get the column index, and then the long at the index
			int index = cursor.getColumnIndex(columnName);
			if (index != -1) {
				return cursor.getLong(index);
			}
			return 0l;
		}
		return 0l;
	}

	public static String getCursorStringValue(Cursor cursor, String columnName) {
		// Validate the parameters
		if (cursor != null && columnName != null) {
			// Get the column index, and then the string at the index
			int index = cursor.getColumnIndex(columnName);
			if (index != -1) {
				return cursor.getString(index);
			}
			return null;
		}
		return null;
	}
	
	public static Uri getCursorUriValue(Cursor cursor, String columnName) {
		String uriPath = getCursorStringValue(cursor, columnName);
		if (uriPath != null) {
			return Uri.parse(uriPath);
		}
		return null;
	}
	
	public static ContentResolver getCR() {
		Context context = getContext();
		if (context != null) {
			return context.getContentResolver();
		}
		return null;
	}
	
	public static SharedPreferences getPreferences() {
		Context context = getContext();
		if (context != null) {
			return android.preference.PreferenceManager.getDefaultSharedPreferences(context);
		}
		return null;
	}
	
	public static String getResourceString(int resource) {
		Context context = getContext();
		if (context != null) {
			return context.getString(resource);
		}
		return null;
	}
	
	public static int getVersionCode() {
		Context context = getContext();
		if (context != null) {
			PackageManager manager = context.getPackageManager();
			if (manager != null) {
				try {
					PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
					if (info != null) {
						return info.versionCode;
					}
				} catch (NameNotFoundException e) {
					Log.e(e);
				}
			}
		}
		return 0;
	}
	
	private static <T> Stack<T> getStack(Map<Long, Stack<T>> map, boolean create) {
		Long currentThreadID = Thread.currentThread().getId();
		if (map.containsKey(currentThreadID)) {
			return map.get(currentThreadID);
		} else if (create) {
			Stack<T> stack = new Stack<T>();
			map.put(currentThreadID, stack);
			return stack;
		}
		return null;
	}
	
	private static <T> void removeStack(Map<Long, Stack<T>> map) {
		Long currentThreadID = Thread.currentThread().getId();
		if (map.containsKey(currentThreadID)) {
			Stack<T> stack = map.get(currentThreadID);
			if (stack == null || stack.isEmpty()) {
				map.remove(currentThreadID);
			}
		}
	}
	
	private static <T> T peekIntoStack(Map<Long, Stack<T>> map) {
		Stack<T> stack = getStack(map, false);
		if (stack != null && !stack.isEmpty()) {
			return stack.peek();
		}
		return null;
	}
	
	private static <T> void pushToStack(Map<Long, Stack<T>> map, T item) {
		Stack<T> stack = getStack(map, true);
		if (stack != null) {
			stack.push(item);
		}
	}
	
	private static <T> void popFromStack(Map<Long, Stack<T>> map) {
		Stack<T> stack = getStack(map, false);
		if (stack != null && !stack.empty()) {
			stack.pop();
			if (stack.isEmpty()) {
				removeStack(map);
			}
		}
	}
	
	public static Context getContext() {
		Context context = peekIntoStack(contextMap);
		if (context == null) {
			Log.w("Proper context was null, returning application context.", true);
			context = App.getAppContext();
		}
		return context;
	}
	
	public static void pushContext(Context c) {
		pushToStack(contextMap, c);
	}
	
	public static void popContext() {
		popFromStack(contextMap);
	}
	
	public static void publishProgress(PublishProgress progress) {
		AsyncTask task = getTask();
		if (task != null) {
			task.updateProgress(progress);
		}
	}
	
	public static AsyncTask getTask() {
		return peekIntoStack(taskMap);
	}
	
	public static void pushTask(AsyncTask task) {
		pushToStack(taskMap, task);
	}
	
	public static void popTask() {
		popFromStack(taskMap);
	}
	
	public static Handler getHandler() {
		if (handler == null) {
			handler = new Handler(Looper.getMainLooper());
		}
		return handler;
	}
	
	public static void dispatchToUIThread(final Context context, final Runnable action) {
		if (action != null) {
			getHandler().post(new Runnable() {
				@Override
				public void run() {
					Utilities.pushContext(context);
					try {
						action.run();
					} finally {
						Utilities.popContext();
					}
				}
			});
		}
	}
	
	public static void dispatchToBackgroundThread(final Context context, final Runnable action) {
		if (action != null) {
			new AsyncTask(context) {
				@Override
				protected void run() {
					action.run();
				}
			}.execute();
		}
	}
	
	public static void query(Uri uri, String[] projection, ICursor cursor) {
		query(uri, projection, null, null, cursor);
	}
	
	public static void query(Uri uri, long id, String[] projection, ICursor cursor) {
		query(ContentUris.withAppendedId(uri, id), projection, null, null, cursor);
	}
	
	public static void query(Uri uri, String[] projection, WhereClause where, SortClause sort, ICursor cursor) {
		ContentResolver cr = getCR();
		if (cursor != null && cr != null) {
			// Generate the selection and sort clauses
			String selection = null, sortOrder = null;
			String[] selectionArgs = null;
			if (where != null) {
				selection = where.getSelection();
				selectionArgs = where.getSelectionArgs();
			}
			if (sort != null) {
				sortOrder = sort.toSQL();
			}
			
			// Run the query
			Cursor queryCursor = null;
			try {
				queryCursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
				
				// Iterator over the results, running the ICursor for each item
				if (queryCursor != null && queryCursor.moveToFirst()) {
					while(!cursor.run(queryCursor) && queryCursor.moveToNext());
				}
			} finally {
				// Make sure we close the cursor
				if (queryCursor != null) {
					queryCursor.close();
				}
			}
		}
	}
	
	public static void registerToMediaKeys(Context context) {
		AudioManager manager = App.getSystemService(AudioManager.class, context, Context.AUDIO_SERVICE);
		if (manager != null) {
			manager.registerMediaButtonEventReceiver(new ComponentName(context, MediaKeyReceiver.class));
		}
	}
	
	public static void unregisterFromMediaKeys(Context context) {
		AudioManager manager = App.getSystemService(AudioManager.class, context, Context.AUDIO_SERVICE);
		if (manager != null) {
			manager.unregisterMediaButtonEventReceiver(new ComponentName(context, MediaKeyReceiver.class));
		}
	}
	
	public static boolean appendToFile(String filename, String contents) {
		return writeToFile(filename, contents, Context.MODE_APPEND);
	}
	
	public static boolean saveFile(String filename, String contents) {
		return writeToFile(filename, contents, Context.MODE_PRIVATE);
	}
	
	private static boolean writeToFile(String filename, String contents, int mode) {
		if (!isNullOrEmpty(filename) && contents != null) {
			FileOutputStream stream = null;
			try {
				stream = getContext().openFileOutput(filename, mode);
				stream.write(contents.getBytes());
				return true;
			} catch (Exception e) {
				Log.e(e);
			} finally {
				if (stream != null) {
					try {
						stream.close();
					} catch (IOException e1) {
						Log.e(e1);
					}
				}
			}
		}
		return false;
	}
	
	public static String loadFile(String filename) {
		if (!isNullOrEmpty(filename)) {
			FileInputStream stream = null;
			InputStreamReader streamReader = null;
			BufferedReader reader = null;
			try {
				stream = getContext().openFileInput(filename);
				streamReader = new InputStreamReader(stream);
				reader = new BufferedReader(streamReader);
				StringBuilder builder = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
					builder.append("\n");
				}
				return builder.toString();
			} catch (FileNotFoundException e) {
				// Ignore this exception since it's the only way to check if the file exists
			} catch (Exception e) {
				Log.e(e);
			} finally {
				try {
					if (reader != null) {
						reader.close();
					}
					if (streamReader != null) {
						streamReader.close();
					}
					if (stream != null) {
						stream.close();
					}
				} catch (IOException e1) {
					Log.e(e1);
				}
			}
		}
		return null;
	}
	
	public static boolean deleteFile(String filename) {
		if (!isNullOrEmpty(filename)) {
			return getContext().deleteFile(filename);
		}
		return false;
	}
	
	public static boolean fileExists(Uri uri) {
		// Perform null-checking
		if (uri == null) {
			return false;
		}
		
		// Check if the file straight up exists
		File file = new File(uri.toString());
		if (file.exists()) {
			return true;
		}
		
		// Get the content resolver
		ContentResolver contentResolver = getCR();
		if (contentResolver == null) {
			return false;
		}
		
		// Attempt to open the input stream
		InputStream stream = null;
		try {
			stream = contentResolver.openInputStream(uri);
			return true;
		} catch (FileNotFoundException e) {
			return false;
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					Log.e(e);
				}
			}
		}
	}
	
	public static boolean isScreenOn() {
		return isScreenOn(null);
	}
	
	public static boolean isScreenOn(Holder<PowerManager> holder) {
		Context context = getContext();
		if (context != null) {
			PowerManager manager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
			if (manager != null) {
				if (holder != null) {
					holder.setValue(manager);
				}
				return manager.isScreenOn();
			}
		}
		return true;
	}
	
	public static void turnScreenOn(int ms) {
		Holder<PowerManager> holder = new Holder<PowerManager>();
		if (!isScreenOn(holder)) {
			PowerManager manager = holder.getValue();
			if (manager != null) {
				WakeLock lock = manager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, Schema.TAG);
				if (lock != null) {
					lock.acquire(ms);
				}
			}
		}
	}
	
	public static int parseInt(String str) {
		Holder<Integer> value = new Holder<Integer>(0);
		tryParseInt(str, value);
		return value.getValue();
	}
	
	public static boolean tryParseInt(String str, Holder<Integer> holder) {
		try {
			int value = Integer.parseInt(str);
			holder.setValue(value);
			return true;
		} catch (Exception e) { }
		return false;
	}
	
	public static long parseLong(String str) {
		Holder<Long> value = new Holder<Long>(0L);
		tryParseLong(str, value);
		return value.getValue();
	}
	
	public static boolean tryParseLong(String str, Holder<Long> holder) {
		try {
			long value = Long.parseLong(str);
			holder.setValue(value);
			return true;
		} catch (Exception e) { }
		return false;
	}
	
	public static double parseDouble(String str) {
		Holder<Double> value = new Holder<Double>(0d);
		tryParseDouble(str, value);
		return value.getValue();
	}
	
	public static boolean tryParseDouble(String str, Holder<Double> holder) {
		try {
			double value = Double.parseDouble(str);
			holder.setValue(value);
			return true;
		} catch (Exception e) { }
		return false;
	}
	
	/**
	 * Format the time (in milliseconds) into a readable format.
	 * @param millis The milliseconds to format.
	 * @return The time in mm:ss format.
	 */
	public static String formatTime(long millis) {
		StringBuilder builder = new StringBuilder();
		long hours = millis / 3600000;
		millis %= 3600000;
		long minutes = millis / 60000;
		millis %= 60000;
		long seconds = millis / 1000;
		if (hours > 0) {
			builder.append(String.format("%02d:", hours));
		}
		builder.append(String.format("%02d:%02d", minutes, seconds));
		return builder.toString();
	}
}