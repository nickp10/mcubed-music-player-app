package dev.paddock.adp.mCubed.model;

import java.util.ArrayList;
import java.util.List;

import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.utilities.ICursor;
import dev.paddock.adp.mCubed.utilities.ProgressManager;
import dev.paddock.adp.mCubed.utilities.Utilities;

import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore.Audio.Albums;
import android.provider.MediaStore.Audio.Artists;
import android.provider.MediaStore.Audio.Genres;
import android.provider.MediaStore.Audio.Media;
import android.provider.MediaStore.Audio.Playlists;

public enum MediaGroup {
	Artist(Artists.EXTERNAL_CONTENT_URI, Artists.ARTIST, Media.ARTIST_ID),
	Album(Albums.EXTERNAL_CONTENT_URI, Albums.ALBUM, Media.ALBUM_ID),
	Genre(Genres.EXTERNAL_CONTENT_URI, Genres.NAME),
	Playlist(Playlists.EXTERNAL_CONTENT_URI, Playlists.NAME),
	Song(Media.EXTERNAL_CONTENT_URI, Media.TITLE),
	All(Media.EXTERNAL_CONTENT_URI, Media.TITLE);
	
	private Uri queryUri;
	private String nameColumn, nameID;
	
	private MediaGroup(Uri queryUri, String nameColumn, String nameID) {
		this(queryUri, nameColumn);
		this.nameID = nameID;
	}
	
	private MediaGroup(Uri queryUri, String nameColumn) {
		this.queryUri = queryUri;
		this.nameColumn = nameColumn;
	}
	
	public List<MediaGrouping> getGroupings() {
		final Progress progress = ProgressManager.startProgress(Schema.PROG_MEDIAGROUP_GETGROUPINGS, "Loading groupings...");
		final List<MediaGrouping> groupings = new ArrayList<MediaGrouping>();
		if (this == MediaGroup.All) {
			groupings.add(new MediaGrouping(MediaGroup.this, 0, null));
		} else {
			Utilities.query(getQueryUri(), getQueryProjection(), new ICursor() {
				@Override
				public boolean run(Cursor cursor) {
					// Create the grouping
					long id = Utilities.getCursorLongValue(cursor, BaseColumns._ID);
					String name = Utilities.getCursorStringValue(cursor, nameColumn);
					groupings.add(new MediaGrouping(MediaGroup.this, id, name));
					
					// Update the progress
					double value = ((double)cursor.getPosition() + 1d) / (double)cursor.getCount();
					progress.setValue(value);
					return false;
				}
			});
		}
		ProgressManager.endProgress(progress);
		return groupings;
	}
	
	public MediaGrouping getGrouping(final long id) {
		if (this == MediaGroup.All) {
			return new MediaGrouping(MediaGroup.this, 0, null);
		} else {
			final Holder<MediaGrouping> grouping = new Holder<MediaGrouping>();
			Utilities.query(getQueryUri(), id, getQueryProjection(), new ICursor() {
				@Override
				public boolean run(Cursor cursor) {
					String name = Utilities.getCursorStringValue(cursor, nameColumn);
					grouping.setValue(new MediaGrouping(MediaGroup.this, id, name));
					return false;
				}
			});
			return grouping.getValue();
		}
	}
	
	public List<MediaFile> getMediaFilesForGrouping(final MediaGrouping grouping) {
		final Progress progress = ProgressManager.startProgress(Schema.PROG_MEDIAGROUP_GETFILES, "Loading files...");
		final long id = grouping.getID(); 
		final List<MediaFile> mediaFiles = new ArrayList<MediaFile>();
		ICursor cursor = new ICursor() {
			@Override
			public boolean run(Cursor cursor) {
				// Create the file
				MediaFile file = MediaFile.get(cursor);
				if (file != null) {
					file.loadGenre(grouping);
					mediaFiles.add(file);
				}
				
				// Update the progress
				double value = ((double)cursor.getPosition() + 1d) / (double)cursor.getCount();
				progress.setValue(value);
				return false;
			}
		};
		if (this == MediaGroup.Genre || this == MediaGroup.Playlist) {
			String volume = "external";
			Uri queryUri = this == MediaGroup.Genre ? Genres.Members.getContentUri(volume, id) : Playlists.Members.getContentUri(volume, id);
			Utilities.query(queryUri, MediaFile.DATA_PROJECTION, cursor);
		} else if (this == MediaGroup.Album || this == MediaGroup.Artist) {
			Utilities.query(Media.EXTERNAL_CONTENT_URI, MediaFile.DATA_PROJECTION, nameID + " = ?", new String[] { Long.toString(id) }, null, cursor);
		} else if (this == MediaGroup.Song) {
			Utilities.query(Media.EXTERNAL_CONTENT_URI, id, MediaFile.DATA_PROJECTION, cursor);
		} else if (this == MediaGroup.All) {
			Utilities.query(Media.EXTERNAL_CONTENT_URI, MediaFile.DATA_PROJECTION, cursor);
		}
		ProgressManager.endProgress(progress);
		return mediaFiles;
	}
	
	public Uri getQueryUri() {
		return queryUri;
	}
	
	public String[] getQueryProjection() {
		return new String[] { BaseColumns._ID, nameColumn };
	}
}