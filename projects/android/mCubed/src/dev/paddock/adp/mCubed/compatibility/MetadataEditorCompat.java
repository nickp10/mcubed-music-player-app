package dev.paddock.adp.mCubed.compatibility;

import java.lang.reflect.Method;

import android.graphics.Bitmap;

/**
 * Class used to modify meta-data in a {@link android.media.RemoteControlClient}
 * object. Use {@link android.media.RemoteControlClient#editMetadata(boolean)}
 * to create an instance of an editor, on which you set the meta-data for the
 * RemoteControlClient instance. Once all the information has been set, use
 * {@link #apply()} to make it the new meta-data that should be displayed for
 * the associated client. Once the meta-data has been "applied", you cannot
 * reuse this instance of the MetadataEditor.
 */
public class MetadataEditorCompat {
	private Method mPutStringMethod;
	private Method mPutBitmapMethod;
	private Method mPutLongMethod;
	private Method mClearMethod;
	private Method mApplyMethod;
	private boolean sHasRemoteControlAPIs;

	private Object mActualMetadataEditor;

	/**
	 * The meta-data key for the content artwork / album art.
	 */
	public final static int BITMAP_KEY_ARTWORK = 100;

	public MetadataEditorCompat(boolean hasRemoteControlAPIs, Object actualMetadataEditor) {
		sHasRemoteControlAPIs = hasRemoteControlAPIs;
		mActualMetadataEditor = actualMetadataEditor;
		
		if (sHasRemoteControlAPIs && actualMetadataEditor != null) {
			Class<?> metadataEditorClass = actualMetadataEditor.getClass();
			try {
				mPutStringMethod = metadataEditorClass.getMethod("putString", int.class, String.class);
				mPutBitmapMethod = metadataEditorClass.getMethod("putBitmap", int.class, Bitmap.class);
				mPutLongMethod = metadataEditorClass.getMethod("putLong", int.class, long.class);
				mClearMethod = metadataEditorClass.getMethod("clear", new Class[0]);
				mApplyMethod = metadataEditorClass.getMethod("apply", new Class[0]);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Adds textual information to be displayed. Note that none of the
	 * information added after {@link #apply()} has been called, will be
	 * displayed.
	 * 
	 * @param key
	 *            The identifier of a the meta-data field to set. Valid values are
	 *            {@link android.media.MediaMetadataRetriever#METADATA_KEY_ALBUM},
	 *            {@link android.media.MediaMetadataRetriever#METADATA_KEY_ALBUMARTIST},
	 *            {@link android.media.MediaMetadataRetriever#METADATA_KEY_TITLE},
	 *            {@link android.media.MediaMetadataRetriever#METADATA_KEY_ARTIST},
	 *            {@link android.media.MediaMetadataRetriever#METADATA_KEY_AUTHOR},
	 *            {@link android.media.MediaMetadataRetriever#METADATA_KEY_COMPILATION},
	 *            {@link android.media.MediaMetadataRetriever#METADATA_KEY_COMPOSER},
	 *            {@link android.media.MediaMetadataRetriever#METADATA_KEY_DATE},
	 *            {@link android.media.MediaMetadataRetriever#METADATA_KEY_GENRE},
	 *            {@link android.media.MediaMetadataRetriever#METADATA_KEY_TITLE},
	 *            {@link android.media.MediaMetadataRetriever#METADATA_KEY_WRITER}.
	 * @param value
	 *            The text for the given key, or {@code null} to signify there
	 *            is no valid information for the field.
	 * @return Returns a reference to the same MetadataEditor object, so you can
	 *         chain put calls together.
	 */
	public MetadataEditorCompat putString(int key, String value) {
		if (sHasRemoteControlAPIs) {
			try {
				mPutStringMethod.invoke(mActualMetadataEditor, key, value);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return this;
	}

	/**
	 * Sets the album / artwork picture to be displayed on the remote control.
	 * 
	 * @param key
	 *            the identifier of the bitmap to set. The only valid value is
	 *            {@link #METADATA_KEY_ARTWORK}
	 * @param bitmap
	 *            The bitmap for the artwork, or null if there isn't any.
	 * @return Returns a reference to the same MetadataEditor object, so you can
	 *         chain put calls together.
	 * @throws IllegalArgumentException
	 * @see android.graphics.Bitmap
	 */
	public MetadataEditorCompat putBitmap(int key, Bitmap bitmap) {
		if (sHasRemoteControlAPIs) {
			try {
				mPutBitmapMethod.invoke(mActualMetadataEditor, key, bitmap);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return this;
	}

	/**
	 * Adds numerical information to be displayed. Note that none of the
	 * information added after {@link #apply()} has been called, will be
	 * displayed.
	 * 
	 * @param key
	 *            the identifier of a the meta-data field to set. Valid values are
	 *            {@link android.media.MediaMetadataRetriever#METADATA_KEY_CD_TRACK_NUMBER},
	 *            {@link android.media.MediaMetadataRetriever#METADATA_KEY_DISC_NUMBER},
	 *            {@link android.media.MediaMetadataRetriever#METADATA_KEY_DURATION} (with a value expressed in milliseconds),
	 *            {@link android.media.MediaMetadataRetriever#METADATA_KEY_YEAR}.
	 * @param value
	 *            The long value for the given key
	 * @return Returns a reference to the same MetadataEditor object, so you can
	 *         chain put calls together.
	 * @throws IllegalArgumentException
	 */
	public MetadataEditorCompat putLong(int key, long value) {
		if (sHasRemoteControlAPIs) {
			try {
				mPutLongMethod.invoke(mActualMetadataEditor, key, value);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return this;
	}

	/**
	 * Clears all the meta-data that has been set since the MetadataEditor
	 * instance was created with
	 * {@link android.media.RemoteControlClient#editMetadata(boolean)}.
	 */
	public void clear() {
		if (sHasRemoteControlAPIs) {
			try {
				mClearMethod.invoke(mActualMetadataEditor);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Associates all the meta-data that has been set since the MetadataEditor
	 * instance was created with
	 * {@link android.media.RemoteControlClient#editMetadata(boolean)}, or since
	 * {@link #clear()} was called, with the RemoteControlClient. Once
	 * "applied", this MetadataEditor cannot be reused to edit the
	 * RemoteControlClient's meta-data.
	 */
	public void apply() {
		if (sHasRemoteControlAPIs) {
			try {
				mApplyMethod.invoke(mActualMetadataEditor);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}