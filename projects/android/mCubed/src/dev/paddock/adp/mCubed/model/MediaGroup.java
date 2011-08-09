package dev.paddock.adp.mCubed.model;

import java.util.ArrayList;
import java.util.List;

import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.lists.BindingList;
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
	private BindingList<MediaGrouping> groupings;
	
	private MediaGroup(Uri queryUri, String nameColumn, String nameID) {
		this(queryUri, nameColumn);
		this.nameID = nameID;
	}
	
	private MediaGroup(Uri queryUri, String nameColumn) {
		this.queryUri = queryUri;
		this.nameColumn = nameColumn;
	}
	
	public static void refreshAll() {
		final Progress progress = ProgressManager.startProgress(Schema.PROG_MEDIAGROUP_REFRESHALL, "Refreshing groupings...");
		try {
			progress.appendSubID(Schema.PROG_MEDIAGROUP_GETGROUPINGS, values().length);
			for (MediaGroup group : values()) {
				group.refresh();
			}
		} finally {
			ProgressManager.endProgress(progress);
		}
	}
	
	public void refresh() {
		if (groupings != null) {
			groupings.clear();
			fillInGroupings();
		}
	}
	
	public BindingList<MediaGrouping> getGroupings() {
		if (groupings == null) {
			groupings = new BindingList<MediaGrouping>();
			fillInGroupings();
		}
		return groupings;
	}
	
	private void fillInGroupings() {
		final Progress progress = ProgressManager.startProgress(Schema.PROG_MEDIAGROUP_GETGROUPINGS, "Loading groupings...");
		try {
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
		} finally {
			ProgressManager.endProgress(progress);
		}
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
	
	public List<MediaFile> getMediaFilesForGrouping(final MediaGrouping grouping, WhereClause where, SortClause sort) {
		final Progress progress = ProgressManager.startProgress(Schema.PROG_MEDIAGROUP_GETFILES, "Loading files...");
		try {
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
				Utilities.query(queryUri, MediaFile.DATA_PROJECTION, where, sort, cursor);
			} else if (this == MediaGroup.Album || this == MediaGroup.Artist) {
				WhereClause groupWhere = WhereClause.create(nameID + " = ?", Long.toString(id));
				if (where != null) {
					groupWhere = groupWhere.and(where);
				}
				Utilities.query(Media.EXTERNAL_CONTENT_URI, MediaFile.DATA_PROJECTION, groupWhere, sort, cursor);
			} else if (this == MediaGroup.Song) {
				// NOTE: we'll ignore the where/sort clauses since this will only return one song
				Utilities.query(Media.EXTERNAL_CONTENT_URI, id, MediaFile.DATA_PROJECTION, cursor);
			} else if (this == MediaGroup.All) {
				Utilities.query(Media.EXTERNAL_CONTENT_URI, MediaFile.DATA_PROJECTION, where, sort, cursor);
			}
			return mediaFiles;
		} finally {
			ProgressManager.endProgress(progress);
		}
	}
	
	public Uri getQueryUri() {
		return queryUri;
	}
	
	public String[] getQueryProjection() {
		return new String[] { BaseColumns._ID, nameColumn };
	}
}