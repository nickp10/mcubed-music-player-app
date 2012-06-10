package dev.paddock.adp.mCubed.controls;

import android.view.ContextMenu;
import android.view.View;
import android.widget.TextView;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.activities.ActivityUtils;
import dev.paddock.adp.mCubed.activities.MediaFileDetailsActivity;
import dev.paddock.adp.mCubed.lists.IViewItem;
import dev.paddock.adp.mCubed.model.Composite;
import dev.paddock.adp.mCubed.model.ListAction;
import dev.paddock.adp.mCubed.model.MediaFile;
import dev.paddock.adp.mCubed.utilities.App;

public class MediaFileViewItem implements IViewItem<MediaFile> {
	protected TextView textView;
	
	@Override
	public void findViews(View rootView) {
		textView = (TextView)rootView.findViewById(R.id.mfvi_text_view);
	}

	@Override
	public void updateViews(MediaFile item) {
		textView.setText(item.getTitle());
	}

	@Override
	public void onViewClick(MediaFile item) {
		onContextItemClick(Schema.MN_CTX_MFVI_PLAY, item);
	}

	@Override
	public boolean onViewLongClick(MediaFile item) {
		return false;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, MediaFile item) {
		menu.setHeaderTitle(item.getTitle());
		menu.add(ContextMenu.NONE, Schema.MN_CTX_MFVI_PLAY, 1, "Play");
		menu.add(ContextMenu.NONE, Schema.MN_CTX_MFVI_VIEWDETAILS, 2, "View Details");
		menu.add(ContextMenu.NONE, Schema.MN_CTX_MFVI_ADDTOQUEUE, 3, "Add to Queue");
		menu.add(ContextMenu.NONE, Schema.MN_CTX_MFVI_PREPENDTOQUEUE, 4, "Prepend to Queue");
		menu.add(ContextMenu.NONE, Schema.MN_CTX_MFVI_ADDTONOWPLAYING, 5, "Add to Now Playing");
		menu.add(ContextMenu.NONE, Schema.MN_CTX_MFVI_REMOVEFROMNOWPLAYING, 6, "Remove from Now Playing");
	}

	@Override
	public boolean onContextItemClick(int menuId, MediaFile item) {
		switch (menuId) {
		case Schema.MN_CTX_MFVI_PLAY:
			App.getNowPlaying().playFile(item);
			break;
		case Schema.MN_CTX_MFVI_VIEWDETAILS:
			ActivityUtils.startActivity(MediaFileDetailsActivity.class, item.getID());
			break;
		case Schema.MN_CTX_MFVI_ADDTOQUEUE:
			App.getNowPlaying().addFilesToQueue(item);
			break;
		case Schema.MN_CTX_MFVI_PREPENDTOQUEUE:
			App.getNowPlaying().prependFilesToQueue(item);
			break;
		case Schema.MN_CTX_MFVI_ADDTONOWPLAYING:
			App.getNowPlaying().addComposite(new Composite(item.getMediaGrouping()));
			break;
		case Schema.MN_CTX_LVI_REMOVEFROMNOWPLAYING:
			App.getNowPlaying().addComposite(new Composite(item.getMediaGrouping(), ListAction.Remove));
			break;
		}
		return false;
	}
}