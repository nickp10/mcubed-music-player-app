package dev.paddock.adp.mCubed.model;

import java.util.ArrayList;
import java.util.List;

import android.test.AndroidTestCase;

import com.google.android.testing.mocking.AndroidMock;
import com.google.android.testing.mocking.UsesMocks;

public class PlayModeTest extends AndroidTestCase{
	private final MediaFile[] mediaFiles = new MediaFile[5];
	private final List<MediaFile> files = new ArrayList<MediaFile>();
	private Playlist playlist;
	
	@UsesMocks({ MediaFile.class, Playlist.class })
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		for (int i = 0; i < mediaFiles.length; i++) {
			MediaFile file = AndroidMock.createMock(MediaFile.class);
			AndroidMock.expect(file.fileExists()).andReturn(true).anyTimes();
			AndroidMock.expect(file.getID()).andReturn((long)i).anyTimes();
			AndroidMock.replay(file);
			mediaFiles[i] = file;
		}
		playlist = AndroidMock.createMock(Playlist.class, true);
		AndroidMock.expect(playlist.getFiles()).andReturn(files).anyTimes();
		AndroidMock.replay(playlist);
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		for (int i = 0; i < mediaFiles.length; i++) {
			AndroidMock.verify(mediaFiles[i]);
		}
		AndroidMock.verify(playlist);
	}
	
	public void testAddedToPlaylist() {
		PlayMode playMode = new PlayMode(playlist, false);
		files.add(mediaFiles[0]);
		playMode.addedToPlaylist(mediaFiles[0]);
		assertEquals(mediaFiles[0], playMode.getCurrent());
		assertTrue(playMode.getHistory().isEmpty());
		assertTrue(playMode.getQueue().isEmpty());
		
		files.add(mediaFiles[1]);
		playMode.addedToPlaylist(mediaFiles[1]);
		assertEquals(mediaFiles[0], playMode.getCurrent());
		assertTrue(playMode.getHistory().isEmpty());
		assertEquals(mediaFiles[1], playMode.getQueue().get(0));
		
		files.add(mediaFiles[2]);
		playMode.addedToPlaylist(mediaFiles[2]);
		assertEquals(mediaFiles[0], playMode.getCurrent());
		assertTrue(playMode.getHistory().isEmpty());
		assertEquals(mediaFiles[1], playMode.getQueue().get(0));
	}
	
	public void testRemovedFromPlaylist() {
		// Setup the play mode
		PlayMode playMode = new PlayMode(playlist, false);
		files.add(mediaFiles[0]);
		playMode.addedToPlaylist(mediaFiles[0]);
		files.add(mediaFiles[1]);
		playMode.addedToPlaylist(mediaFiles[1]);
		files.add(mediaFiles[2]);
		playMode.addedToPlaylist(mediaFiles[2]);
		
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
		playMode.addedToPlaylist(mediaFiles[0]);
		files.add(mediaFiles[1]);
		playMode.addedToPlaylist(mediaFiles[1]);
		files.add(mediaFiles[2]);
		playMode.addedToPlaylist(mediaFiles[2]);
		
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
		playMode.addedToPlaylist(mediaFiles[0]);
		files.add(mediaFiles[1]);
		playMode.addedToPlaylist(mediaFiles[1]);
		files.add(mediaFiles[2]);
		playMode.addedToPlaylist(mediaFiles[2]);
		
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
	
	public void testNext() {
		// Setup the play mode
		PlayMode playMode = new PlayMode(playlist, false);
		files.add(mediaFiles[0]);
		playMode.addedToPlaylist(mediaFiles[0]);
		files.add(mediaFiles[1]);
		playMode.addedToPlaylist(mediaFiles[1]);
		files.add(mediaFiles[2]);
		playMode.addedToPlaylist(mediaFiles[2]);
		
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
		playMode.addedToPlaylist(mediaFiles[0]);
		files.add(mediaFiles[1]);
		playMode.addedToPlaylist(mediaFiles[1]);
		files.add(mediaFiles[2]);
		playMode.addedToPlaylist(mediaFiles[2]);
		
		// Assert the state before
		assertTrue(playMode.getHistory().isEmpty());
		assertEquals(mediaFiles[0], playMode.getCurrent());
		assertEquals(1, playMode.getQueue().size());
		assertEquals(mediaFiles[1], playMode.getQueue().get(0));
		assertFalse(playMode.getCurrentRequiresRepeat());
		
		// Call previous to make sure nothing happens
		playMode.previous();
		assertTrue(playMode.getHistory().isEmpty());
		assertEquals(mediaFiles[0], playMode.getCurrent());
		assertEquals(1, playMode.getQueue().size());
		assertEquals(mediaFiles[1], playMode.getQueue().get(0));
		assertFalse(playMode.getCurrentRequiresRepeat());
		
		// Call next two times to reach the end and call previous once
		playMode.next();
		playMode.next();
		playMode.previous();
		assertEquals(1, playMode.getHistory().size());
		assertEquals(mediaFiles[0], playMode.getHistory().get(0));
		assertEquals(mediaFiles[1], playMode.getCurrent());
		assertEquals(1, playMode.getQueue().size());
		assertEquals(mediaFiles[2], playMode.getQueue().get(0));
		assertFalse(playMode.getCurrentRequiresRepeat());
		
		// Call previous to reach the beginning again
		playMode.previous();
		assertTrue(playMode.getHistory().isEmpty());
		assertEquals(mediaFiles[0], playMode.getCurrent());
		assertEquals(2, playMode.getQueue().size());
		assertEquals(mediaFiles[1], playMode.getQueue().get(0));
		assertEquals(mediaFiles[2], playMode.getQueue().get(1));
		assertFalse(playMode.getCurrentRequiresRepeat());
		
		// Call previous and make sure nothing happens again
		playMode.previous();
		assertTrue(playMode.getHistory().isEmpty());
		assertEquals(mediaFiles[0], playMode.getCurrent());
		assertEquals(2, playMode.getQueue().size());
		assertEquals(mediaFiles[1], playMode.getQueue().get(0));
		assertEquals(mediaFiles[2], playMode.getQueue().get(1));
		assertFalse(playMode.getCurrentRequiresRepeat());
		
		// Call next three times to loop around
		playMode.next();
		playMode.next();
		playMode.next();
		assertTrue(playMode.getCurrentRequiresRepeat());
		
		// Call previous and make sure nothing changed
		playMode.previous();
		assertTrue(playMode.getHistory().isEmpty());
		assertEquals(mediaFiles[0], playMode.getCurrent());
		assertEquals(2, playMode.getQueue().size());
		assertEquals(mediaFiles[1], playMode.getQueue().get(0));
		assertEquals(mediaFiles[2], playMode.getQueue().get(1));
		assertFalse(playMode.getCurrentRequiresRepeat());
	}
}