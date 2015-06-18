package dev.paddock.adp.mCubed;

import java.util.List;

import org.easymock.IAnswer;

import com.google.android.testing.mocking.AndroidMock;
import com.google.android.testing.mocking.UsesMocks;

import dev.paddock.adp.mCubed.model.MediaFile;
import dev.paddock.adp.mCubed.model.MediaGroup;
import dev.paddock.adp.mCubed.model.MediaGrouping;

@UsesMocks(MediaGrouping.class)
public class MediaGroupingUtils {
	public static MediaGrouping createMock(MediaGroup group, long id) {
		String name = group + ":" + id;
		final MediaGrouping grouping = AndroidMock.createMock(MediaGrouping.class, group, id, name);
		AndroidMock.expect(grouping.isMediaGroupAll()).andReturn(group == MediaGroup.All).anyTimes();
		AndroidMock.expect(grouping.getGroup()).andReturn(group).anyTimes();
		AndroidMock.expect(grouping.getID()).andReturn(id).anyTimes();
		AndroidMock.expect(grouping.getName()).andReturn(name).anyTimes();
		AndroidMock.expect(grouping.getMediaFiles()).andAnswer(new IAnswer<List<MediaFile>>() {
			@Override
			public List<MediaFile> answer() throws Throwable {
				return MediaFileUtils.getMediaFilesForGrouping(grouping);
			}
		}).anyTimes();
		AndroidMock.replay(grouping);
		return grouping;
	}
	
	public static void verifyMock(MediaGrouping grouping) {
		AndroidMock.verify(grouping);
	}
}