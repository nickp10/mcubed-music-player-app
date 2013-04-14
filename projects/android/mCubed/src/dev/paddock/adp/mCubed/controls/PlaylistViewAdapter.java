package dev.paddock.adp.mCubed.controls;

import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.lists.BindingListAdapter;
import dev.paddock.adp.mCubed.lists.IViewItem;
import dev.paddock.adp.mCubed.lists.IViewItemFactory;
import dev.paddock.adp.mCubed.model.MediaFile;

public class PlaylistViewAdapter extends BindingListAdapter<MediaFile> {
	@Override
	protected void onBeforeInitialize() {
		setItemViewResource(R.layout.media_file_list_view_item);
		setItemViewItemFactory(new IViewItemFactory<MediaFile>() {
			@Override
			public IViewItem<MediaFile> createViewItem() {
				return new PlaylistViewItem();
			}
		});
	}
}
