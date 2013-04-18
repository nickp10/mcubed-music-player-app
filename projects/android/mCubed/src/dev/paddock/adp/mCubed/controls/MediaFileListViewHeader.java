package dev.paddock.adp.mCubed.controls;

import android.view.ContextMenu;
import android.view.View;
import android.widget.TextView;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.lists.IViewItem;

public class MediaFileListViewHeader implements IViewItem<String> {
	private TextView textView;

	@Override
	public void findViews(View rootView) {
		textView = (TextView)rootView.findViewById(R.id.mfvh_text_view);
	}

	@Override
	public void updateViews(String header) {
		textView.setText(header);
	}

	@Override
	public void onViewClick(String header) {
	}

	@Override
	public boolean onViewLongClick(String header) {
		return false;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, String header) {
	}

	@Override
	public boolean onContextItemClick(int menuId, String header) {
		return false;
	}
}
