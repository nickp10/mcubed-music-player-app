package dev.paddock.adp.mCubed.model;

import android.test.AndroidTestCase;

import dev.paddock.adp.mCubed.MediaFileUtils;
import dev.paddock.adp.mCubed.MediaGroupingUtils;
import dev.paddock.adp.mCubed.utilities.Utilities;
import static dev.paddock.adp.mCubed.TestUtils.assertSequenceEmpty;
import static dev.paddock.adp.mCubed.TestUtils.assertSequenceEquals;

public class PlaylistTest extends AndroidTestCase {
	private final MediaFile[] mediaFiles = MediaFileUtils.getMocks();
	
	private static Composite createComposite(ListAction action, MediaGroup group, long id) {
		return new Composite(MediaGroupingUtils.createMock(group, id), action);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Utilities.pushContext(getContext());
		MediaFileUtils.verifyArrange();
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		Utilities.popContext();
	}
	
	public void testAddComposite() {
		// Setup the composites
		Composite addCompEminem = createComposite(ListAction.Add, MediaGroup.Artist, 1);
		Composite addCompEmDre = createComposite(ListAction.Add, MediaGroup.Artist, 2);
		Composite remCompRecovery = createComposite(ListAction.Remove, MediaGroup.Album, 1);
		Composite addAll = createComposite(ListAction.Add, MediaGroup.All, 0);
		Composite remAll = createComposite(ListAction.Remove, MediaGroup.All, 0);
		Playlist playlist = new Playlist();
		
		// Add the Eminem composite and assert
		playlist.addComposite(addCompEminem);
		assertSequenceEquals(new Composite[] { addCompEminem }, playlist.getComposition());
		assertSequenceEquals(new MediaFile[] { mediaFiles[0] }, playlist.getFiles());
		
		// Add the Eminem/Dr. Dre composite and assert
		playlist.addComposite(addCompEmDre);
		assertSequenceEquals(new Composite[] { addCompEminem, addCompEmDre }, playlist.getComposition());
		assertSequenceEquals(new MediaFile[] { mediaFiles[0], mediaFiles[1], mediaFiles[3], mediaFiles[4] }, playlist.getFiles());
		
		// Add the Relapse composite and assert
		playlist.addComposite(remCompRecovery);
		assertSequenceEquals(new Composite[] { addCompEminem, addCompEmDre, remCompRecovery }, playlist.getComposition());
		assertSequenceEquals(new MediaFile[] { mediaFiles[1], mediaFiles[3] }, playlist.getFiles());
		
		// Add the Add All composite and assert
		playlist.addComposite(addAll);
		assertSequenceEquals(new Composite[] { addAll }, playlist.getComposition());
		assertSequenceEquals(new MediaFile[] { mediaFiles[1], mediaFiles[3], mediaFiles[0], mediaFiles[2], mediaFiles[4] }, playlist.getFiles());
		
		// Add the Remove All composite and assert
		playlist.addComposite(remAll);
		assertSequenceEquals(new Composite[] { remAll }, playlist.getComposition());
		assertSequenceEmpty(playlist.getFiles());
	}
	
	public void testRemoveComposite() {
		// Setup the composites
		Composite addCompEminem = createComposite(ListAction.Add, MediaGroup.Artist, 1);
		Composite addCompShady = createComposite(ListAction.Add, MediaGroup.Genre, 1);
		Composite remCompEminem = createComposite(ListAction.Remove, MediaGroup.Artist, 1);
		Composite remCompShady = createComposite(ListAction.Remove, MediaGroup.Genre, 1);
		Composite addAll = createComposite(ListAction.Add, MediaGroup.All, 0);
		Composite remAll = createComposite(ListAction.Remove, MediaGroup.All, 0);
		Playlist playlist = new Playlist();
		
		// Add then remove the Add All composite and assert
		playlist.addComposite(addAll);
		assertSequenceEquals(mediaFiles, playlist.getFiles());
		playlist.removeComposite(addAll);
		assertSequenceEmpty(playlist.getFiles());
		
		// Add the Add All composite back
		playlist.addComposite(addAll);
		assertSequenceEquals(mediaFiles, playlist.getFiles());
		
		// Add the Remove All composite
		playlist.addComposite(remAll);
		assertSequenceEmpty(playlist.getFiles());
		playlist.removeComposite(remAll);
		assertSequenceEmpty(playlist.getFiles());
		
		// Add the Shady and Eminem composites
		playlist.addComposite(addCompShady);
		playlist.addComposite(addCompEminem);
		assertSequenceEquals(new MediaFile[] { mediaFiles[0], mediaFiles[1] }, playlist.getFiles());
		
		// Remove the Shady composite (which should keep Eminem)
		playlist.removeComposite(addCompShady);
		assertSequenceEquals(new MediaFile[] { mediaFiles[0] }, playlist.getFiles());
		
		// Remove the Eminem composite
		playlist.removeComposite(addCompEminem);
		assertSequenceEmpty(playlist.getFiles());
		
		// Add the Add All composite and some remove composites
		playlist.addComposite(addAll);
		playlist.addComposite(remCompShady);
		playlist.addComposite(remCompEminem);
		assertSequenceEquals(new MediaFile[] { mediaFiles[2], mediaFiles[3], mediaFiles[4] }, playlist.getFiles());
		
		// Remove the Shady composite (which should still have Eminem removed)
		playlist.removeComposite(remCompShady);
		assertSequenceEquals(new MediaFile[] { mediaFiles[1], mediaFiles[2], mediaFiles[3], mediaFiles[4] }, playlist.getFiles());
		
		// remove the Eminem composite
		playlist.removeComposite(remCompEminem);
		assertSequenceEquals(mediaFiles, playlist.getFiles());
	}
}