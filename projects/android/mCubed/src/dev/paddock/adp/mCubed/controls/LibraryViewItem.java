package dev.paddock.adp.mCubed.controls;

import android.view.View;
import android.widget.TextView;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.lists.IViewItem;
import dev.paddock.adp.mCubed.model.MediaGrouping;

public class LibraryViewItem implements IViewItem<MediaGrouping> {
	public TextView textView;
	
	@Override
	public void findViews(View rootView) {
		textView = (TextView)rootView.findViewById(R.id.lvi_textView);
	}

	@Override
	public void updateViews(MediaGrouping item) {
		textView.setText(item.getName());
	}

	@Override
	public void onViewClick(MediaGrouping item) {
	}

	@Override
	public boolean onViewLongClick(MediaGrouping item) {
		return false;
	}
}