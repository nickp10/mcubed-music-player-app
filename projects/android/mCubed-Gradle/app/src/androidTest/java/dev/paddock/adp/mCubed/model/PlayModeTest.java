package dev.paddock.adp.mCubed.model;

import static dev.paddock.adp.mCubed.TestUtils.assertSequenceEmpty;
import static dev.paddock.adp.mCubed.TestUtils.assertSequenceEquals;

import java.util.ArrayList;
import java.util.List;

import android.test.AndroidTestCase;

import com.google.android.testing.mocking.AndroidMock;
import com.google.android.testing.mocking.UsesMocks;

import dev.paddock.adp.mCubed.MediaFileUtils;

@UsesMocks(Playlist.class)
public class PlayModeTest extends AndroidTestCase {
	private final MediaFile[] mediaFiles = MediaFileUtils.getMocks();
	private final List<MediaFile> files = new ArrayList<MediaFile>();
	private Playlist playlist;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		MediaFileUtils.verifyArrange();
		files.clear();
		playlist = AndroidMock.createMock(Playlist.class, true);
		AndroidMock.expect(playlist.getFiles()).andReturn(files).anyTimes();
		AndroidMock.replay(playlist);
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		MediaFileUtils.verifyMocks();
		AndroidMock.verify(playlist);
	}
	
	private void addedToPlaylist(PlayMode playMode, MediaFile file) {
		playMode.addedToPlaylist(file);
		playMode.resetCurrent();
	}
	
	public void testAddedToPlaylist() {
		PlayMode playMode = new PlayMode(playlist, false);
		files.add(mediaFiles[0]);
		addedToPlaylist(playMode, mediaFiles[0]);
		assertEquals(mediaFiles[0], playMode.getCurrent());
		assertTrue(playMode.getHistory().isEmpty());
		assertTrue(playMode.getQueue().isEmpty());
		
		files.add(mediaFiles[1]);
		addedToPlaylist(playMode, mediaFiles[1]);
		assertEquals(mediaFiles[0], playMode.getCurrent());
		assertTrue(playMode.getHistory().isEmpty());
		assertEquals(mediaFiles[1], playMode.getQueue().get(0));
		
		files.add(mediaFiles[2]);
		addedToPlaylist(playMode, mediaFiles[2]);
		assertEquals(mediaFiles[0], playMode.getCurrent());
		assertTrue(playMode.getHistory().isEmpty());
		assertEquals(mediaFiles[1], playMode.getQueue().get(0));
	}
	
	public void testRemovedFromPlaylist() {
		// Setup the play mode
		PlayMode playMode = new PlayMode(playlist, false);
		files.add(mediaFiles[0]);
		addedToPlaylist(playMode, mediaFiles[0]);
		files.add(mediaFiles[1]);
		addedToPlaylist(playMode, mediaFiles[1]);
		files.add(mediaFiles[2]);
		addedToPlaylist(playMode, mediaFiles[2]);
		
		// Assert the state before
		assertTrue(playMode.getHistory().isEmpty());
		assertEquals(mediaFiles[0], playMode.getCurrent());
		assertEquals(1, playMode.getQueue().size());
		assertEquals(mediaFiles[1], playMode.getQueue().get(0));
		
		// Remove file 1 and assert
		files.remove(mediaFiles[1]);
		playMode.removedFromPlaylist(mediaFiles[1]);
		assertTrue(playMode.getHistory().isEmpty());
		assertEquals(mediaFiles[0], playMode.getCurrent());
		assertEquals(1, playMode.getQueue().size());
		assertEquals(mediaFiles[2], playMode.getQueue().get(0));
		
		// Remove file 2 and assert
		files.remove(mediaFiles[2]);
		playMode.removedFromPlaylist(mediaFiles[2]);
		assertTrue(playMode.getHistory().isEmpty());
		assertEquals(mediaFiles[0], playMode.getCurrent());
		assertTrue(playMode.getQueue().isEmpty());
		
		// Remove file 0 and assert
		files.remove(mediaFiles[0]);
		playMode.removedFromPlaylist(mediaFiles[0]);
		assertTrue(playMode.getHistory().isEmpty());
		assertEquals(null, playMode.getCurrent());
		assertTrue(playMode.getQueue().isEmpty());
		
		// Add the files back
		files.add(mediaFiles[0]);
		addedToPlaylist(playMode, mediaFiles[0]);
		files.add(mediaFiles[1]);
		addedToPlaylist(playMode, mediaFiles[1]);
		files.add(mediaFiles[2]);
		addedToPlaylist(playMode, mediaFiles[2]);
		
		// Let's move to the next item and redo the removes
		playMode.next();
		assertEquals(1, playMode.getHistory().size());
		assertEquals(mediaFiles[0], playMode.getHistory().get(0));
		assertEquals(mediaFiles[1], playMode.getCurrent());
		assertEquals(1, playMode.getQueue().size());
		assertEquals(mediaFiles[2], playMode.getQueue().get(0));
		
		// Remove file 1 and assert
		files.remove(mediaFiles[1]);
		playMode.removedFromPlaylist(mediaFiles[1]);
		assertEquals(1, playMode.getHistory().size());
		assertEquals(mediaFiles[0], playMode.getHistory().get(0));
		assertEquals(mediaFiles[2], playMode.getCurrent());
		assertTrue(playMode.getQueue().isEmpty());
		assertFalse(playMode.getCurrentRequiresRepeat());
		
		// Remove file 2 and assert
		files.remove(mediaFiles[2]);
		playMode.removedFromPlaylist(mediaFiles[2]);
		assertTrue(playMode.getHistory().isEmpty());
		assertEquals(mediaFiles[0], playMode.getCurrent());
		assertTrue(playMode.getQueue().isEmpty());
		assertTrue(playMode.getCurrentRequiresRepeat());
		
		// Remove file 0 and assert
		files.remove(mediaFiles[0]);
		playMode.removedFromPlaylist(mediaFiles[0]);
		assertTrue(playMode.getHistory().isEmpty());
		assertEquals(null, playMode.getCurrent());
		assertTrue(playMode.getQueue().isEmpty());
		assertFalse(playMode.getCurrentRequiresRepeat());
		
		// Add the files back
		files.add(mediaFiles[0]);
		addedToPlaylist(playMode, mediaFiles[0]);
		files.add(mediaFiles[1]);
		addedToPlaylist(playMode, mediaFiles[1]);
		files.add(mediaFiles[2]);
		addedToPlaylist(playMode, mediaFiles[2]);
		
		// Let's move to the last item and redo the removes
		playMode.next();
		playMode.next();
		assertEquals(2, playMode.getHistory().size());
		assertEquals(mediaFiles[0], playMode.getHistory().get(0));
		assertEquals(mediaFiles[1], playMode.getHistory().get(1));
		assertEquals(mediaFiles[2], playMode.getCurrent());
		assertTrue(playMode.getQueue().isEmpty());
		assertFalse(playMode.getCurrentRequiresRepeat());
		
		// Remove file 2 and assert
		files.remove(mediaFiles[2]);
		playMode.removedFromPlaylist(mediaFiles[2]);
		assertTrue(playMode.getHistory().isEmpty());
		assertEquals(mediaFiles[0], playMode.getCurrent());
		assertEquals(1, playMode.getQueue().size());
		assertEquals(mediaFiles[1], playMode.getQueue().get(0));
		assertTrue(playMode.getCurrentRequiresRepeat());
		
		// Remove file 0 and assert
		files.remove(mediaFiles[0]);
		playMode.removedFromPlaylist(mediaFiles[0]);
		assertTrue(playMode.getHistory().isEmpty());
		assertEquals(mediaFiles[1], playMode.getCurrent());
		assertTrue(playMode.getQueue().isEmpty());
		assertFalse(playMode.getCurrentRequiresRepeat());
		
		// Remove file 1 and assert
		files.remove(mediaFiles[1]);
		playMode.removedFromPlaylist(mediaFiles[1]);
		assertTrue(playMode.getHistory().isEmpty());
		assertEquals(null, playMode.getCurrent());
		assertTrue(playMode.getQueue().isEmpty());
		assertFalse(playMode.getCurrentRequiresRepeat());
	}
	
	/**
	 * The following tests are to test the removedFromPlaylist functionality
	 * when one MediaFile instance appears multiple times. Here are all possibilities:
	 * - The history contains the MediaFile two or more times (testRemovedFromPlaylist_MultipleInstanceInHistory)
	 * - The queue contains the MediaFile two or more times (testRemovedFromPlaylist_MultipleInstanceInQueue)
	 * - The history and queue both contain the MediaFile one or more times (testRemovedFromPlaylist_MultipleInstanceInHistoryAndQueue)
	 * - The history contains the MediaFile one or more times, and it is the current MediaFile (testRemovedFromPlaylist_MultipleInstanceInHistoryAndIsCurrent)
	 * - The queue contains the MediaFile one or more times, and it is the current MediaFile (testRemovedFromPlaylist_MultipleInstanceInQueueAndIsCurrent)
	 * - The history and queue both contain the MediaFile one or more times, and it is the current MediaFile (testRemovedFromPlaylist_MultipleInstanceInHistoryAndQueueAndIsCurrent)
	 */
	public void testRemovedFromPlaylist_MultipleInstanceInHistory() {
		// Setup the play mode
		PlayMode playMode = new PlayMode(playlist, false);
		files.add(mediaFiles[0]);
		addedToPlaylist(playMode, mediaFiles[0]);
		playMode.appendToQueue(mediaFiles[0]);
		playMode.appendToQueue(mediaFiles[0]);
		playMode.appendToQueue(mediaFiles[0]);
		playMode.appendToQueue(mediaFiles[0]);
		files.add(mediaFiles[1]);
		addedToPlaylist(playMode, mediaFiles[1]);
		files.add(mediaFiles[2]);
		addedToPlaylist(playMode, mediaFiles[2]);
		playMode.next();
		playMode.next();
		playMode.next();
		playMode.next();
		playMode.next();
		
		// Assert the state before
		assertSequenceEquals(new MediaFile[] { mediaFiles[0], mediaFiles[0], mediaFiles[0], mediaFiles[0], mediaFiles[0] }, playMode.getHistory());
		assertEquals(mediaFiles[1], playMode.getCurrent());
		assertSequenceEquals(new MediaFile[] { mediaFiles[2] }, playMode.getQueue());
		
		// Remove the instance
		files.remove(mediaFiles[0]);
		playMode.removedFromPlaylist(mediaFiles[0]);
		
		// Assert the state after
		assertSequenceEmpty(playMode.getHistory());
		assertEquals(mediaFiles[1], playMode.getCurrent());
		assertSequenceEquals(new MediaFile[] { mediaFiles[2] }, playMode.getQueue());
	}
	
	public void testRemovedFromPlaylist_MultipleInstanceInQueue() {
		// Setup the play mode
		PlayMode playMode = new PlayMode(playlist, false);
		files.add(mediaFiles[0]);
		addedToPlaylist(playMode, mediaFiles[0]);
		files.add(mediaFiles[1]);
		addedToPlaylist(playMode, mediaFiles[1]);
		playMode.appendToQueue(mediaFiles[1]);
		playMode.appendToQueue(mediaFiles[1]);
		playMode.appendToQueue(mediaFiles[1]);
		playMode.appendToQueue(mediaFiles[1]);
		files.add(mediaFiles[2]);
		addedToPlaylist(playMode, mediaFiles[2]);
		
		// Assert the state before
		assertSequenceEmpty(playMode.getHistory());
		assertEquals(mediaFiles[0], playMode.getCurrent());
		assertSequenceEquals(new MediaFile[] { mediaFiles[1], mediaFiles[1], mediaFiles[1], mediaFiles[1], mediaFiles[1] }, playMode.getQueue());
		
		// Remove the instance
		files.remove(mediaFiles[1]);
		playMode.removedFromPlaylist(mediaFiles[1]);
		
		// Assert the state after
		assertSequenceEmpty(playMode.getHistory());
		assertEquals(mediaFiles[0], playMode.getCurrent());
		assertSequenceEquals(new MediaFile[] { mediaFiles[2] }, playMode.getQueue());
	}
	
	public void testRemovedFromPlaylist_MultipleInstanceInHistoryAndQueue() {
		// Setup the play mode
		PlayMode playMode = new PlayMode(playlist, false);
		files.add(mediaFiles[0]);
		addedToPlaylist(playMode, mediaFiles[0]);
		playMode.appendToQueue(mediaFiles[0]);
		playMode.appendToQueue(mediaFiles[0]);
		playMode.appendToQueue(mediaFiles[0]);
		files.add(mediaFiles[1]);
		addedToPlaylist(playMode, mediaFiles[1]);
		playMode.appendToQueue(mediaFiles[1]);
		playMode.appendToQueue(mediaFiles[0]);
		playMode.appendToQueue(mediaFiles[0]);
		playMode.appendToQueue(mediaFiles[0]);
		playMode.appendToQueue(mediaFiles[0]);
		files.add(mediaFiles[2]);
		addedToPlaylist(playMode, mediaFiles[2]);
		playMode.next();
		playMode.next();
		playMode.next();
		playMode.next();
		
		// Assert the state before
		assertSequenceEquals(new MediaFile[] { mediaFiles[0], mediaFiles[0], mediaFiles[0], mediaFiles[0] }, playMode.getHistory());
		assertEquals(mediaFiles[1], playMode.getCurrent());
		assertSequenceEquals(new MediaFile[] { mediaFiles[0], mediaFiles[0], mediaFiles[0], mediaFiles[0] }, playMode.getQueue());
		
		// Remove the instance
		files.remove(mediaFiles[0]);
		playMode.removedFromPlaylist(mediaFiles[0]);
		
		// Assert the state after
		assertSequenceEmpty(playMode.getHistory());
		assertEquals(mediaFiles[1], playMode.getCurrent());
		assertSequenceEquals(new MediaFile[] { mediaFiles[2] }, playMode.getQueue());
	}
	
	public void testRemovedFromPlaylist_MultipleInstanceInHistoryAndIsCurrent() {
		// Setup the play mode
		PlayMode playMode = new PlayMode(playlist, false);
		files.add(mediaFiles[0]);
		addedToPlaylist(playMode, mediaFiles[0]);
		playMode.appendToQueue(mediaFiles[0]);
		playMode.appendToQueue(mediaFiles[0]);
		playMode.appendToQueue(mediaFiles[0]);
		playMode.appendToQueue(mediaFiles[0]);
		files.add(mediaFiles[1]);
		addedToPlaylist(playMode, mediaFiles[1]);
		files.add(mediaFiles[2]);
		addedToPlaylist(playMode, mediaFiles[2]);
		playMode.next();
		playMode.next();
		playMode.next();
		playMode.next();
		
		// Assert the state before
		assertSequenceEquals(new MediaFile[] { mediaFiles[0], mediaFiles[0], mediaFiles[0], mediaFiles[0] }, playMode.getHistory());
		assertEquals(mediaFiles[0], playMode.getCurrent());
		assertSequenceEquals(new MediaFile[] { mediaFiles[1] }, playMode.getQueue());
		
		// Remove the instance
		files.remove(mediaFiles[0]);
		playMode.removedFromPlaylist(mediaFiles[0]);
		
		// Assert the state after
		assertSequenceEmpty(playMode.getHistory());
		assertEquals(mediaFiles[1], playMode.getCurrent());
		assertSequenceEquals(new MediaFile[] { mediaFiles[2] }, playMode.getQueue());
	}
	
	public void testRemovedFromPlaylist_MultipleInstanceInQueueAndIsCurrent() {
		// Setup the play mode
		PlayMode playMode = new PlayMode(playlist, false);
		files.add(mediaFiles[0]);
		addedToPlaylist(playMode, mediaFiles[0]);
		files.add(mediaFiles[1]);
		addedToPlaylist(playMode, mediaFiles[1]);
		playMode.appendToQueue(mediaFiles[1]);
		playMode.appendToQueue(mediaFiles[1]);
		playMode.appendToQueue(mediaFiles[1]);
		playMode.appendToQueue(mediaFiles[1]);
		files.add(mediaFiles[2]);
		addedToPlaylist(playMode, mediaFiles[2]);
		playMode.next();
		
		// Assert the state before
		assertSequenceEquals(new MediaFile[] { mediaFiles[0] }, playMode.getHistory());
		assertEquals(mediaFiles[1], playMode.getCurrent());
		assertSequenceEquals(new MediaFile[] { mediaFiles[1], mediaFiles[1], mediaFiles[1], mediaFiles[1] }, playMode.getQueue());
		
		// Remove the instance
		files.remove(mediaFiles[1]);
		playMode.removedFromPlaylist(mediaFiles[1]);
		
		// Assert the state after
		assertSequenceEquals(new MediaFile[] { mediaFiles[0] }, playMode.getHistory());
		assertEquals(mediaFiles[2], playMode.getCurrent());
		assertSequenceEmpty(playMode.getQueue());
	}
	
	public void testRemovedFromPlaylist_MultipleInstanceInHistoryAndQueueAndIsCurrent() {
		// Setup the play mode
		PlayMode playMode = new PlayMode(playlist, false);
		files.add(mediaFiles[0]);
		addedToPlaylist(playMode, mediaFiles[0]);
		playMode.appendToQueue(mediaFiles[0]);
		playMode.appendToQueue(mediaFiles[0]);
		playMode.appendToQueue(mediaFiles[0]);
		playMode.appendToQueue(mediaFiles[0]);
		playMode.appendToQueue(mediaFiles[0]);
		playMode.appendToQueue(mediaFiles[0]);
		playMode.appendToQueue(mediaFiles[0]);
		playMode.appendToQueue(mediaFiles[0]);
		files.add(mediaFiles[1]);
		addedToPlaylist(playMode, mediaFiles[1]);
		files.add(mediaFiles[2]);
		addedToPlaylist(playMode, mediaFiles[2]);
		playMode.next();
		playMode.next();
		playMode.next();
		playMode.next();
		
		// Assert the state before
		assertSequenceEquals(new MediaFile[] { mediaFiles[0], mediaFiles[0], mediaFiles[0], mediaFiles[0] }, playMode.getHistory());
		assertEquals(mediaFiles[0], playMode.getCurrent());
		assertSequenceEquals(new MediaFile[] { mediaFiles[0], mediaFiles[0], mediaFiles[0], mediaFiles[0] }, playMode.getQueue());
		
		// Remove the instance
		files.remove(mediaFiles[0]);
		playMode.removedFromPlaylist(mediaFiles[0]);
		
		// Assert the state after
		assertSequenceEmpty(playMode.getHistory());
		assertEquals(mediaFiles[1], playMode.getCurrent());
		assertSequenceEquals(new MediaFile[] { mediaFiles[2] }, playMode.getQueue());
	}
	
	public void testNext() {
		// Setup the play mode
		PlayMode playMode = new PlayMode(playlist, false);
		files.add(mediaFiles[0]);
		addedToPlaylist(playMode, mediaFiles[0]);
		files.add(mediaFiles[1]);
		addedToPlaylist(playMode, mediaFiles[1]);
		files.add(mediaFiles[2]);
		addedToPlaylist(playMode, mediaFiles[2]);
		
		// Assert the state before
		assertTrue(playMode.getHistory().isEmpty());
		assertEquals(mediaFiles[0], playMode.getCurrent());
		assertEquals(1, playMode.getQueue().size());
		assertEquals(mediaFiles[1], playMode.getQueue().get(0));
		assertFalse(playMode.getCurrentRequiresRepeat());
		
		// Call next and assert
		playMode.next();
		assertEquals(1, playMode.getHistory().size());
		assertEquals(mediaFiles[0], playMode.getHistory().get(0));
		assertEquals(mediaFiles[1], playMode.getCurrent());
		assertEquals(1, playMode.getQueue().size());
		assertEquals(mediaFiles[2], playMode.getQueue().get(0));
		assertFalse(playMode.getCurrentRequiresRepeat());
		
		// Call next and assert
		playMode.next();
		assertEquals(2, playMode.getHistory().size());
		assertEquals(mediaFiles[0], playMode.getHistory().get(0));
		assertEquals(mediaFiles[1], playMode.getHistory().get(1));
		assertEquals(mediaFiles[2], playMode.getCurrent());
		assertTrue(playMode.getQueue().isEmpty());
		assertFalse(playMode.getCurrentRequiresRepeat());
		
		// Call next and assert (should reset back to the beginning)
		playMode.next();
		assertTrue(playMode.getHistory().isEmpty());
		assertEquals(mediaFiles[0], playMode.getCurrent());
		assertEquals(2, playMode.getQueue().size());
		assertEquals(mediaFiles[1], playMode.getQueue().get(0));
		assertEquals(mediaFiles[2], playMode.getQueue().get(1));
		assertTrue(playMode.getCurrentRequiresRepeat());
		
		// Call next and assert (should clear out the currentRequiresRepeat)
		playMode.next();
		assertEquals(1, playMode.getHistory().size());
		assertEquals(mediaFiles[0], playMode.getHistory().get(0));
		assertEquals(mediaFiles[1], playMode.getCurrent());
		assertEquals(1, playMode.getQueue().size());
		assertEquals(mediaFiles[2], playMode.getQueue().get(0));
		assertFalse(playMode.getCurrentRequiresRepeat());
	}
	
	public void testPrevious() {
		// Setup the play mode
		PlayMode playMode = new PlayMode(playlist, false);
		files.add(mediaFiles[0]);
		addedToPlaylist(playMode, mediaFiles[0]);
		files.add(mediaFiles[1]);
		addedToPlaylist(playMode, mediaFiles[1]);
		files.add(mediaFiles[2]);
		addedToPlaylist(playMode, mediaFiles[2]);
		
		// Assert the state before
		assertSequenceEmpty(playMode.getHistory());
		assertEquals(mediaFiles[0], playMode.getCurrent());
		assertSequenceEquals(new MediaFile[] { mediaFiles[1] }, playMode.getQueue());
		assertFalse(playMode.getCurrentRequiresRepeat());
		
		// Call previous to make sure nothing happens
		playMode.previous();
		assertSequenceEmpty(playMode.getHistory());
		assertEquals(mediaFiles[0], playMode.getCurrent());
		assertSequenceEquals(new MediaFile[] { mediaFiles[1] }, playMode.getQueue());
		assertFalse(playMode.getCurrentRequiresRepeat());
		
		// Call next two times to reach the end and call previous once
		playMode.next();
		playMode.next();
		playMode.previous();
		assertSequenceEquals(new MediaFile[] { mediaFiles[0] }, playMode.getHistory());
		assertEquals(mediaFiles[1], playMode.getCurrent());
		assertSequenceEquals(new MediaFile[] { mediaFiles[2] }, playMode.getQueue());
		assertFalse(playMode.getCurrentRequiresRepeat());
		
		// Call previous to reach the beginning again
		playMode.previous();
		assertSequenceEmpty(playMode.getHistory());
		assertEquals(mediaFiles[0], playMode.getCurrent());
		assertSequenceEquals(new MediaFile[] { mediaFiles[1], mediaFiles[2] }, playMode.getQueue());
		assertFalse(playMode.getCurrentRequiresRepeat());
		
		// Call previous and make sure nothing happens again
		playMode.previous();
		assertSequenceEmpty(playMode.getHistory());
		assertEquals(mediaFiles[0], playMode.getCurrent());
		assertSequenceEquals(new MediaFile[] { mediaFiles[1], mediaFiles[2] }, playMode.getQueue());
		assertFalse(playMode.getCurrentRequiresRepeat());
		
		// Call next three times to loop around
		playMode.next();
		playMode.next();
		playMode.next();
		assertTrue(playMode.getCurrentRequiresRepeat());
		
		// Call previous and make sure nothing changed
		playMode.previous();
		assertSequenceEmpty(playMode.getHistory());
		assertEquals(mediaFiles[0], playMode.getCurrent());
		assertSequenceEquals(new MediaFile[] { mediaFiles[1], mediaFiles[2] }, playMode.getQueue());
		assertFalse(playMode.getCurrentRequiresRepeat());
	}
	
	public void testMoveWithinQueue() {
		// Setup the play mode
		PlayMode playMode = new PlayMode(playlist, false);
		files.add(mediaFiles[0]);
		addedToPlaylist(playMode, mediaFiles[0]);
		files.add(mediaFiles[1]);
		addedToPlaylist(playMode, mediaFiles[1]);
		files.add(mediaFiles[2]);
		addedToPlaylist(playMode, mediaFiles[2]);
		playMode.appendToQueue(mediaFiles[0]);
		playMode.appendToQueue(mediaFiles[0]);
		playMode.appendToQueue(mediaFiles[2]);
		playMode.appendToQueue(mediaFiles[2]);
		playMode.appendToQueue(mediaFiles[1]);
		
		// Assert the setup
		assertSequenceEquals(new MediaFile[] { mediaFiles[1], mediaFiles[0], mediaFiles[0], mediaFiles[2], mediaFiles[2], mediaFiles[1] }, playMode.getQueue());
		
		// Move file 0 to the end
		playMode.moveWithinQueue(mediaFiles[0], 6);
		
		// Assert the move
		assertSequenceEquals(new MediaFile[] { mediaFiles[1], mediaFiles[2], mediaFiles[2], mediaFiles[1], mediaFiles[0] }, playMode.getQueue());
		
		// Move file 2 to the beginning
		playMode.moveWithinQueue(mediaFiles[2], 0);
		
		// Assert the move
		assertSequenceEquals(new MediaFile[] { mediaFiles[2], mediaFiles[1], mediaFiles[1], mediaFiles[0] }, playMode.getQueue());
		
		// Move file 1 nowhere, but should remove the duplicate
		playMode.moveWithinQueue(mediaFiles[1], 1);
		
		// Assert the move
		assertSequenceEquals(new MediaFile[] { mediaFiles[2], mediaFiles[1], mediaFiles[0] }, playMode.getQueue());
		
		// Move file 1 nowhere, again
		playMode.moveWithinQueue(mediaFiles[1], 1);
		
		// Assert the move
		assertSequenceEquals(new MediaFile[] { mediaFiles[2], mediaFiles[1], mediaFiles[0] }, playMode.getQueue());
		
		// Move file 0 nowhere
		playMode.moveWithinQueue(mediaFiles[0], 2);
		
		// Assert the move and setup again
		assertSequenceEquals(new MediaFile[] { mediaFiles[2], mediaFiles[1], mediaFiles[0] }, playMode.getQueue());
		playMode.appendToQueue(mediaFiles[2]);
		assertSequenceEquals(new MediaFile[] { mediaFiles[2], mediaFiles[1], mediaFiles[0], mediaFiles[2] }, playMode.getQueue());
		
		// Move file 2 between files 1 and 0
		playMode.moveWithinQueue(mediaFiles[2], 2);
		
		// Assert the move
		assertSequenceEquals(new MediaFile[] { mediaFiles[1], mediaFiles[2], mediaFiles[0] }, playMode.getQueue());
		
		// Attempt to move file 3
		playMode.moveWithinQueue(mediaFiles[3], 2);
		
		// Assert that it didn't move
		assertSequenceEquals(new MediaFile[] { mediaFiles[1], mediaFiles[2], mediaFiles[0] }, playMode.getQueue());
	}
}