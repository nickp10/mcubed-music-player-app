package dev.paddock.adp.mCubed.controls;

import android.content.Context;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.lists.BindingListAdapter;
import dev.paddock.adp.mCubed.lists.IViewItem;
import dev.paddock.adp.mCubed.lists.IViewItemFactory;
import dev.paddock.adp.mCubed.model.MediaFile;

public class PlaylistViewAdapter extends BindingListAdapter<MediaFile> {
	public PlaylistViewAdapter(Context context) {
		super(context);
	}
	
	@Override
	protected void onBeforeInitialize() {
		setItemViewResource(R.layout.media_file_view_item);
		setItemViewItemFactory(new IViewItemFactory<MediaFile>() {
			@Override
			public IViewItem<MediaFile> createViewItem() {
				return new PlaylistViewItem();
			}
		});
	}
}
