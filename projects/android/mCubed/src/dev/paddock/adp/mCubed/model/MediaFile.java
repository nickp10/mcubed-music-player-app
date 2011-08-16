package dev.paddock.adp.mCubed.model;

import java.util.HashMap;
import java.util.Map;

import dev.paddock.adp.mCubed.utilities.ICursor;
import dev.paddock.adp.mCubed.utilities.Log;
import dev.paddock.adp.mCubed.utilities.PropertyManager;
import dev.paddock.adp.mCubed.utilities.Utilities;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class MediaFile {
	public static final String[] DATA_PROJECTION = new String[] {
		MediaStore.Audio.Media.ALBUM,
		MediaStore.Audio.Media.ALBUM_ID,
		MediaStore.Audio.Media.ARTIST,
		MediaStore.Audio.Media.ARTIST_ID,
		MediaStore.Audio.Media.DURATION,
		MediaStore.Audio.Media.DATA,
		MediaStore.Audio.Media.DISPLAY_NAME,
		MediaStore.Audio.Media._ID,
		MediaStore.Audio.Media.SIZE,
		MediaStore.Audio.Media.TITLE,
		MediaStore.Audio.Media.TRACK,
		MediaStore.Audio.Media.YEAR
	};
	private static final Map<Long, MediaFile> activeCache = new HashMap<Long, MediaFile>();
	private static final Map<Long, MediaFile> instanceCache = new HashMap<Long, MediaFile>();
	private final Map<String, Object> values = new HashMap<String, Object>();
	private boolean isAlbumArtLoaded, isDataLoaded, isGenreLoaded, isInitialized, isPlaying;
	
	public static MediaFile get(Cursor cursor) {
		long id = Utilities.getCursorLongValue(cursor, MediaStore.Audio.Media._ID);
		return get(cursor, id);
	}
	
	public static MediaFile get(long id) {
		// Check the cache first
		if (activeCache.containsKey(id)) {
			return activeCache.get(id);
		}
		
		// Get an instance
		MediaFile file = null;
		if (instanceCache.containsKey(id)) {
			file = instanceCache.get(id);
			file.reloadData();
		} else {
			file = new MediaFile(id);
			instanceCache.put(id, file);
		}
		
		// Store and return it
		if (file.isInitialized) {
			activeCache.put(id, file);
			return file;
		}
		return null;
	}
	
	public static MediaFile get(Cursor cursor, long id) {
		// Check the cache first
		if (activeCache.containsKey(id)) {
			return activeCache.get(id);
		}
		
		// Get an instance
		MediaFile file = null;
		if (instanceCache.containsKey(id)) {
			file = instanceCache.get(id);
			file.reloadData(cursor);
		} else {
			file = new MediaFile(cursor);
			instanceCache.put(id, file);
		}
		
		// Store and return it
		if (file != null && file.isInitialized) {
			activeCache.put(id, file);
			return file;
		}
		return null;
	}
	
	public static void forceRefresh(long id) {
		// Get the file from the cache
		if (instanceCache.containsKey(id)) {
			MediaFile file = instanceCache.get(id);
			if (file != null) {
				file.reloadData();
			}
		}
	}
	
	public static void clearCache() {
		activeCache.clear();
	}
	
	{
		values.put("AlbumArt", (Uri)null);
		values.put("Genre", "");
		values.put("GenreID", 0l);
	}
	
	/**
	 * Create a media file with the cursor containing the data to load from.
	 * @param cursor The cursor that contains the data to load from.
	 */
	private MediaFile(Cursor cursor) {
		isInitialized = loadData(cursor);
	}
	
	/**
	 * Create a media file with the ID to load the data from.
	 * @param id The ID of the media file to load the data from.
	 */
	private MediaFile(long id) {
		isInitialized = loadData(id);
	}
	
	/**
	 * FOR TESTING PURPOSES ONLY!!!
	 */
	protected MediaFile() { }
	
	/**
	 * Reloads the data for the media file.
	 */
	private void reloadData() {
		if (isDataLoaded) {
			long id = getID();
			isDataLoaded = false;
			isInitialized = loadData(id);
		}
	}
	
	/**
	 * Reloads the data for the media file from the given cursor.
	 * @param cursor The cursor containing the information for the media file to reload from.
	 */
	private void reloadData(Cursor cursor) {
		if (isDataLoaded) {
			isDataLoaded = false;
			isInitialized = loadData(cursor);
		}
	}
	
	/**
	 * Loads the data for the media file.
	 * @param id The ID of the media file to query the information for.
	 * @return True if the data was loaded successfully, or false otherwise.
	 */
	private boolean loadData(long id) {
		if (!isDataLoaded) {
			final Holder<Boolean> exists = new Holder<Boolean>(false);
			Utilities.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id, DATA_PROJECTION, new ICursor() {
				@Override
				public boolean run(Cursor cursor) {
					exists.setValue(loadData(cursor));
					return false;
				}
			});
			return exists.getValue();
		}
		return true;
	}
	
	/**
	 * Loads the data for the media file.
	 * @param cursor The cursor to read the information from.
	 * @return True if the data was loaded successfully, or false otherwise.
	 */
	private boolean loadData(Cursor cursor) {
		if (!isDataLoaded) {
			// Update all the information
			setAlbum(Utilities.getCursorStringValue(cursor, MediaStore.Audio.Media.ALBUM));
			setAlbumID(Utilities.getCursorLongValue(cursor, MediaStore.Audio.Media.ALBUM_ID));
			setArtist(Utilities.getCursorStringValue(cursor, MediaStore.Audio.Media.ARTIST));
			setArtistID(Utilities.getCursorLongValue(cursor, MediaStore.Audio.Media.ARTIST_ID));
			setDuration(Utilities.getCursorLongValue(cursor, MediaStore.Audio.Media.DURATION));
			setFileLocation(Utilities.getCursorUriValue(cursor, MediaStore.Audio.Media.DATA));
			setFileName(Utilities.getCursorStringValue(cursor, MediaStore.Audio.Media.DISPLAY_NAME));
			setID(Utilities.getCursorLongValue(cursor, MediaStore.Audio.Media._ID));
			setSize(Utilities.getCursorLongValue(cursor, MediaStore.Audio.Media.SIZE));
			setTitle(Utilities.getCursorStringValue(cursor, MediaStore.Audio.Media.TITLE));
			setTrack(Utilities.getCursorIntValue(cursor, MediaStore.Audio.Media.TRACK));
			setYear(Utilities.getCursorIntValue(cursor, MediaStore.Audio.Media.YEAR));
			
			// Make sure the file exists
			if (!Utilities.fileExists(getFileLocation())) {
				return false;
			}
			
			// All is well
			isDataLoaded = true;
		}
		return true;
	}
	
	/**
	 * Loads the album art for the media file.
	 */
	private void loadAlbumArt() {
		if (!isAlbumArtLoaded) {
			setAlbumArt(MediaGroup.Album.getAlbumArt(getAlbumID()));
			isAlbumArtLoaded = true;
		}
	}
	
	/**
	 * Loads the genre for the media file.
	 */
	private void loadGenre() {
		if (!isGenreLoaded) {
			final Holder<Boolean> found = new Holder<Boolean>(false);
			String[] genreProjection = new String[] { MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME };
			Utilities.query(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, genreProjection, new ICursor() {
				@Override
				public boolean run(final Cursor genreCursor) {
					long id = Utilities.getCursorLongValue(genreCursor, MediaStore.Audio.Genres._ID);
					Uri uri = MediaStore.Audio.Genres.Members.getContentUri("external", id);
					String[] projection = new String[] { MediaStore.Audio.Media._ID };
					WhereClause where = WhereClause.create(MediaStore.Audio.Media._ID + " = ?", Long.toString(getID()));
					Utilities.query(uri, projection, where, null, new ICursor() {
						@Override
						public boolean run(Cursor cursor) {
							found.setValue(true);
							loadGenre(genreCursor);
							return true;
						}
					});
					return found.getValue();
				}
			});
		}
	}
	
	/**
	 * Loads the genre for the media file.
	 * @param cursor The cursor to read the genre information from.
	 */
	public void loadGenre(Cursor cursor) {
		if (!isGenreLoaded) {
			setGenre(Utilities.getCursorStringValue(cursor, MediaStore.Audio.Genres.NAME));
			setGenreID(Utilities.getCursorLongValue(cursor, MediaStore.Audio.Genres._ID));
			isGenreLoaded = true;
		}
	}
	
	/**
	 * Loads the genre for the media file.
	 * @param grouping The grouping that contains the genre information to read from.
	 */
	public void loadGenre(MediaGrouping grouping) {
		if (!isGenreLoaded && grouping != null && grouping.getGroup() == MediaGroup.Genre) {
			setGenre(grouping.getName());
			setGenreID(grouping.getID());
			isGenreLoaded = true;
		}
	}
	
	private <T> T getValue(Class<T> clazz, String property) {
		return getValue(clazz, property, true);
	}
	
	@SuppressWarnings("unchecked")
	private <T> T getValue(Class<T> clazz, String property, boolean logError) {
		// See if the values contains the property
		if (values.containsKey(property)) {
			return (T)values.get(property);
		}
		
		// Otherwise, return a null value
		if (logError) {
			Log.e(String.format("Values doesn't contain property: %s", property));
		}
		return (T)null;
	}
	
	private <T> void setValue(Class<T> clazz, String property, T value) {
		if (isInitialized) {
			T oldValue = getValue(clazz, property, false);
			if ((oldValue == null && value != null) || (value == null) || (!oldValue.equals(value))) {
				NotificationArgs args = new NotificationArgs(this, property, oldValue, value);
				PropertyManager.notifyPropertyChanging(this, property, args);
				values.put(property, value);
				PropertyManager.notifyPropertyChanged(this, property, args);
			}
		} else {
			values.put(property, value);
		}
	}

	/**
	 * Retrieve the album title for the media file.
	 * @return The album title for the media file.
	 */
	public String getAlbum() {
		return getValue(String.class, "Album");
	}
	private void setAlbum(String album) {
		setValue(String.class, "Album", album);
	}
	
	/**
	 * Retrieve the URI to the cached album art for the media file.
	 * @return The URI to the cached album art for the media file.
	 */
	public Uri getAlbumArt() {
		if (!isAlbumArtLoaded) {
			loadAlbumArt();
		}
		return getValue(Uri.class, "AlbumArt");
	}
	private void setAlbumArt(Uri albumArt) {
		setValue(Uri.class, "AlbumArt", albumArt);
	}
	
	/**
	 * Retrieve the ID of the album for the media file.
	 * @return The ID of the album for the media file.
	 */
	public long getAlbumID() {
		return getValue(Long.class, "AlbumID");
	}
	private void setAlbumID(long albumID) {
		setValue(Long.class, "AlbumID", albumID);
	}
	
	/**
	 * Retrieve the artist for the media file.
	 * @return The artist for the media file.
	 */
	public String getArtist() {
		return getValue(String.class, "Artist");
	}
	private void setArtist(String artist) {
		setValue(String.class, "Artist", artist);
	}
	
	/**
	 * Retrieve the artists for the media file (which is the artist value separated on a "/").
	 * @return The artists for the media file.
	 */
	public String[] getArtists() {
		String artist = getArtist();
		return artist == null ? null : artist.split("/");
	}
	
	/**
	 * Retrieve the ID of the artist for the media file.
	 * @return The ID of the artist for the media file.
	 */
	public long getArtistID() {
		return getValue(Long.class, "ArtistID");
	}
	private void setArtistID(long artistID) {
		setValue(Long.class, "ArtistID", artistID);
	}
	
	/**
	 * Retrieve the duration of the media file (in milliseconds).
	 * @return The duration of the media file.
	 */
	public long getDuration() {
		return getValue(Long.class, "Duration");
	}
	private void setDuration(long duration) {
		setValue(Long.class, "Duration", duration);
	}
	
	/**
	 * Retrieve the URI for the location to the media file.
	 * @return The URI for the location to the media file.
	 */
	public Uri getFileLocation() {
		return getValue(Uri.class, "FileLocation");
	}
	private void setFileLocation(Uri fileLocation) {
		setValue(Uri.class, "FileLocation", fileLocation);
	}
	
	/**
	 * Retrieve the filename for the media file.
	 * @return The filename for the media file.
	 */
	public String getFileName() {
		return getValue(String.class, "FileName");
	}
	private void setFileName(String fileName) {
		setValue(String.class, "FileName", fileName);
	}

	/**
	 * Retrieve the genre for the media file.
	 * @return The genre for the media file.
	 */
	public String getGenre() {
		if (!isGenreLoaded) {
			loadGenre();
		}
		return getValue(String.class, "Genre");
	}
	private void setGenre(String genre) {
		setValue(String.class, "Genre", genre);
	}
	
	/**
	 * Retrieve the genres for the media file (which is the genre value separated on a "/").
	 * @return The genres for the media file.
	 */
	public String[] getGenres() {
		String genre = getGenre();
		return genre == null ? null : genre.split("/");
	}
	
	/**
	 * Retrieve the ID of the genre for the media file.
	 * @return The ID of the genre for the media file.
	 */
	public long getGenreID() {
		return getValue(Long.class, "GenreID");
	}
	private void setGenreID(long genreID) {
		setValue(Long.class, "GenreID", genreID);
	}
	
	/**
	 * Retrieve the ID of the media file.
	 * @return The ID of the media file.
	 */
	public long getID() {
		return getValue(Long.class, "ID");
	}
	private void setID(long id) {
		setValue(Long.class, "ID", id);
	}

	/**
	 * Retrieve the size of the media file (in bytes).
	 * @return The size of the media file.
	 */
	public long getSize() {
		return getValue(Long.class, "Size");
	}
	private void setSize(long size) {
		setValue(Long.class, "Size", size);
	}
	
	/**
	 * Retrieve the title of the media file.
	 * @return The title of the media file.
	 */
	public String getTitle() {
		return getValue(String.class, "Title");
	}
	private void setTitle(String title) {
		setValue(String.class, "Title", title);
	}

	/**
	 * Retrieve the track number for the media file.
	 * @return The track number for the media file.
	 */
	public int getTrack() {
		return getValue(Integer.class, "Track");
	}
	private void setTrack(int track) {
		setValue(Integer.class, "Track", track);
	}

	/**
	 * Retrieve the year for the media file.
	 * @return The year for the media file.
	 */
	public int getYear() {
		return getValue(Integer.class, "Year");
	}
	private void setYear(int year) {
		setValue(Integer.class, "Year", year);
	}
	
	/**
	 * Retrieve whether or not the media file is currently playing.
	 * @return True if the media file is currently playing, or false otherwise.
	 */
	public boolean isPlaying() {
		return isPlaying;
	}
	public void setPlaying(boolean isPlaying) {
		if (this.isPlaying != isPlaying) {
			NotificationArgs args = new NotificationArgs(this, "IsPlaying", this.isPlaying, isPlaying);
			PropertyManager.notifyPropertyChanging(this, "IsPlaying", args);
			this.isPlaying = isPlaying;
			PropertyManager.notifyPropertyChanged(this, "IsPlaying", args);
		}
	}
	
	/**
	 * Determine whether or not this media file exists.
	 * @return True if the media file exists, or false otherwise.
	 */
	public boolean fileExists() {
		return Utilities.fileExists(getFileLocation());
	}
	
	/**
	 * Refreshes the media file to ensure that is using the file in the active cache. This
	 * will not refresh the data within file if it already exists in the active cache. To
	 * force a full refresh regardless of whether or not it's in the active cache, then call
	 * forceRefresh() instead.
	 * @return True if the media file exists in the active cache and is using the data from that cache, or false otherwise.
	 */
	public boolean refresh() {
		return get(getID()) == this;
	}
	
	/**
	 * Forces the media file to refresh itself to contain the latest information from the database.
	 * This will refresh the media file's information but will not place it in the active cache.
	 * To ensure this file is placed on the active cache, then call refresh() instead.
	 */
	public void forceRefresh() {
		forceRefresh(getID());
	}
}