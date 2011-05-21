package dev.paddock.adp.mCubed;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;

import java.util.ArrayList;
import java.util.List;

import com.google.android.testing.mocking.AndroidMock;
import com.google.android.testing.mocking.UsesMocks;

import dev.paddock.adp.mCubed.model.MediaFile;
import dev.paddock.adp.mCubed.model.MediaGroup;
import dev.paddock.adp.mCubed.model.MediaGrouping;

@UsesMocks(MediaFile.class)
public class MediaFileUtils {
	private static final String[] artists = new String[] { "Eminem", "Eminem/Dr. Dre", "Dr. Dre/Snoop Dogg" };
	private static final String[] albums = new String[] { "Recovery", "Relapse", "2001" };
	private static final String[] genres = new String[] { "Shady Records", "Aftermath Records", "Rap" };
	private static final MediaFile[] mediaFiles;
	private static byte verified;
	
	static {
		mediaFiles = new MediaFile[5];
		mediaFiles[0] = createMock(0, 0, 0, 0, "Not Afraid");
		mediaFiles[1] = createMock(1, 1, 1, 0, "Old Time's Sake");
		mediaFiles[2] = createMock(2, 2, 2, 1, "Still D.R.E.");
		mediaFiles[3] = createMock(3, 1, 2, 1, "Forgot About Dre");
		mediaFiles[4] = createMock(4, 1, 0, 2, "I Need A Doctor");
	}
	
	private static MediaFile createMock(int id, int artistID, int albumID, int genreID, String title) {
		MediaFile file = AndroidMock.createMock(MediaFile.class);
		AndroidMock.expect(file.fileExists()).andReturn(true).anyTimes();
		AndroidMock.expect(file.getID()).andReturn((long)id + 1).anyTimes();
		AndroidMock.expect(file.getAlbum()).andReturn(albums[albumID]).anyTimes();
		AndroidMock.expect(file.getAlbumID()).andReturn((long)albumID + 1).anyTimes();
		AndroidMock.expect(file.getArtist()).andReturn(artists[artistID]).anyTimes();
		AndroidMock.expect(file.getArtistID()).andReturn((long)artistID + 1).anyTimes();
		AndroidMock.expect(file.getGenre()).andReturn(genres[genreID]).anyTimes();
		AndroidMock.expect(file.getGenreID()).andReturn((long)genreID + 1).anyTimes();
		AndroidMock.expect(file.getTitle()).andReturn(title).anyTimes();
		AndroidMock.replay(file);
		return file;
	}
	
	public static void verifyArrange() {
		if (verified == 0) {
			verified = -1;
			for (int i = 0; i < mediaFiles.length; i++) {
				MediaFile file = mediaFiles[i];
				assertNotNull(file);
				for (int j = 0; j < mediaFiles.length; j++) {
					if (i != j) {
						assertNotSame(mediaFiles[j], file);
					}
				}
			}
			verified = 1;
		} else {
			assertEquals("A previous verification failed", 1, verified);
		}
	}
	
	public static void verifyMocks() {
		for (MediaFile file : mediaFiles) {
			AndroidMock.verify(file);
		}
	}
	
	public static MediaFile[] getMocks() {
		return mediaFiles;
	}
	
	public static MediaFile get(int id) {
		return mediaFiles[id - 1];
	}
	
	public static List<MediaFile> getMediaFilesForGrouping(MediaGrouping grouping) {
		MediaGroup group = grouping.getGroup();
		long id = grouping.getID();
		List<MediaFile> files = new ArrayList<MediaFile>();
		for (MediaFile file : mediaFiles) {
			if (group == MediaGroup.All) {
				files.add(file);
			} else if (group == MediaGroup.Album) {
				if (file.getAlbumID() == id) {
					files.add(file);
				}
			} else if (group == MediaGroup.Artist) {
				if (file.getArtistID() == id) {
					files.add(file);
				}
			} else if (group == MediaGroup.Genre) {
				if (file.getGenreID() == id) {
					files.add(file);
				}
			} else if (group == MediaGroup.Song) {
				if (file.getID() == id) {
					files.add(file);
				}
			}
		}
		return files;
	}
}