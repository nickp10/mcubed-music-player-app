package dev.paddock.adp.mCubed.controls;

import java.util.Comparator;

import android.content.Context;

import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.lists.BindingListAdapter;
import dev.paddock.adp.mCubed.lists.IViewItem;
import dev.paddock.adp.mCubed.lists.IViewItemFactory;
import dev.paddock.adp.mCubed.model.MediaFile;
import dev.paddock.adp.mCubed.model.MediaFileValue;

public class MediaFileViewAdapter extends BindingListAdapter<MediaFile> {
	public MediaFileViewAdapter(Context context) {
		super(context);
	}
	
	@Override
	protected void onBeforeInitialize() {
		setItemViewResource(R.layout.media_file_view_item);
		setItemViewItemFactory(new IViewItemFactory<MediaFile>() {
			@Override
			public IViewItem<MediaFile> createViewItem() {
				return new MediaFileViewItem();
			}
		});
		setSorter(new Comparator<MediaFile>() {
			@Override
			public int compare(MediaFile leftFile, MediaFile rightFile) {
				return MediaFile.compare(leftFile, rightFile, MediaFileValue.Title);
			}
		});
		setGrouper(MediaFileValue.Artist);
	}
}