package dev.paddock.adp.mCubed;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import dev.paddock.adp.mCubed.model.MediaFile;
import dev.paddock.adp.mCubed.model.MediaGroup;
import dev.paddock.adp.mCubed.model.MediaGrouping;

public class MediaGroupingUtils {
	public static MediaGrouping createMock(MediaGroup group, long id) {
		String name = group + ":" + id;
		final MediaGrouping grouping = mock(MediaGrouping.class);
		when(grouping.isMediaGroupAll()).thenReturn(group == MediaGroup.All);
		when(grouping.getGroup()).thenReturn(group);
		when(grouping.getID()).thenReturn(id);
		when(grouping.getName()).thenReturn(name);
		when(grouping.getMediaFiles()).thenAnswer(new Answer<List<MediaFile>>() {
			@Override
			public List<MediaFile> answer(InvocationOnMock invocation) throws Throwable {
				return MediaFileUtils.getMediaFilesForGrouping(grouping);
			}
		});
		return grouping;
	}
}