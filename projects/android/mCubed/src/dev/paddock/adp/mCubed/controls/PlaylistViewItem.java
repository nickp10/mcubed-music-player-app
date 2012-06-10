package dev.paddock.adp.mCubed.controls;

import android.view.ContextMenu;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.model.MediaFile;

public class PlaylistViewItem extends MediaFileViewItem {
	@Override
	public void updateViews(MediaFile item) {
		textView.setText(String.format("%s - %s", item.getArtist(), item.getTitle()));
	}

	@Override
	public void onViewClick(MediaFile item) {
		onContextItemClick(Schema.MN_CTX_MFVI_VIEWDETAILS, item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, MediaFile item) {
		menu.setHeaderTitle(item.getTitle());
		menu.add(ContextMenu.NONE, Schema.MN_CTX_MFVI_VIEWDETAILS, 1, "View Details");
		menu.add(ContextMenu.NONE, Schema.MN_CTX_MFVI_PLAY, 2, "Play");
	}
}
