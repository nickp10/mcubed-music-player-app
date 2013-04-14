package dev.paddock.adp.mCubed.compatibility;

import dev.paddock.adp.mCubed.utilities.CompatibilityUtilities;

public class MediaMetadataRetrieverCompat {
	// Public fields which map to constants in MediaMetadataRetriever
	public static int METADATA_KEY_ALBUM;
	public static int METADATA_KEY_ALBUMARTIST;
	public static int METADATA_KEY_ARTIST;
	public static int METADATA_KEY_AUTHOR;
	public static int METADATA_KEY_CD_TRACK_NUMBER;
	public static int METADATA_KEY_COMPILATION;
	public static int METADATA_KEY_COMPOSER;
	public static int METADATA_KEY_DATE;
	public static int METADATA_KEY_DISC_NUMBER;
	public static int METADATA_KEY_DURATION;
	public static int METADATA_KEY_GENRE;
	public static int METADATA_KEY_MIMETYPE;
	public static int METADATA_KEY_NUM_TRACKS;
	public static int METADATA_KEY_TITLE;
	public static int METADATA_KEY_WRITER;
	public static int METADATA_KEY_YEAR;
	
	static {
		CompatibilityUtilities.loadStaticFields("android.media.MediaMetadataRetriever", MediaMetadataRetrieverCompat.class);
	}
}
