package dev.paddock.adp.mCubed.controls;

import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.lists.IViewItem;
import dev.paddock.adp.mCubed.model.Composite;
import dev.paddock.adp.mCubed.model.ListAction;
import dev.paddock.adp.mCubed.model.MediaGroup;
import dev.paddock.adp.mCubed.model.MediaGrouping;
import dev.paddock.adp.mCubed.utilities.App;

public class LibraryViewItem implements IViewItem<MediaGrouping> {
	public TextView textView;
	public ImageView coverView;
	
	@Override
	public void findViews(View rootView) {
		textView = (TextView)rootView.findViewById(R.id.lvi_text_view);
		coverView = (ImageView)rootView.findViewById(R.id.lvi_cover_image);
	}

	@Override
	public void updateViews(MediaGrouping item) {
		textView.setText(item.getName());
		if (item.getGroup() == MediaGroup.Album) {
			coverView.setImageURI(item.getAlbumArt());
		} else {
			coverView.setVisibility(View.GONE);
		}
	}

	@Override
	public void onViewClick(MediaGrouping item) {
		if (item.getGroup() == MediaGroup.Song) {
			onContextItemClick(Schema.MN_CTX_LVI_VIEWDETAILS, item);
		} else {
			onContextItemClick(Schema.MN_CTX_LVI_VIEWFILES, item);
		}
	}

	@Override
	public boolean onViewLongClick(MediaGrouping item) {
		return false;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, MediaGrouping item) {
		menu.setHeaderTitle(item.getName());
		if (item.getGroup() == MediaGroup.Song) {
			menu.add(ContextMenu.NONE, Schema.MN_CTX_LVI_VIEWDETAILS, 1, "View Details");
		} else {
			menu.add(ContextMenu.NONE, Schema.MN_CTX_LVI_VIEWFILES, 1, "View Files");
		}
		menu.add(ContextMenu.NONE, Schema.MN_CTX_LVI_PLAY, 2, "Play");
		menu.add(ContextMenu.NONE, Schema.MN_CTX_LVI_ADDTOQUEUE, 3, "Add to Queue");
		menu.add(ContextMenu.NONE, Schema.MN_CTX_LVI_ADDTOPLAYLIST, 4, "Add to Playlist");
		menu.add(ContextMenu.NONE, Schema.MN_CTX_LVI_REMOVEFROMPLAYLIST, 5, "Remove from Playlist");
	}

	@Override
	public boolean onContextItemClick(int menuId, MediaGrouping item) {
		switch (menuId) {
		case Schema.MN_CTX_LVI_VIEWDETAILS:
			// TODO Launch activity for MediaFile details
			break;
		case Schema.MN_CTX_LVI_VIEWFILES:
			// TODO Launch activity for a List<MediaFile> using MediaFileView/Adapter
			break;
		case Schema.MN_CTX_LVI_PLAY:
			App.getNowPlaying().playComposite(new Composite(item));
			break;
		case Schema.MN_CTX_LVI_ADDTOPLAYLIST:
			App.getNowPlaying().addComposite(new Composite(item));
			break;
		case Schema.MN_CTX_LVI_REMOVEFROMPLAYLIST:
			App.getNowPlaying().addComposite(new Composite(item, ListAction.Remove));
			break;
		case Schema.MN_CTX_LVI_ADDTOQUEUE:
			App.getNowPlaying().addFilesToQueue(item.getMediaFiles());
			break;
		}
		return false;
	}
}