package dev.paddock.adp.mCubed.lists;

import android.view.ContextMenu;
import android.view.View;

public interface IViewItem<E> {
	void findViews(View rootView);
	void updateViews(E item);
	void onViewClick(E item);
	boolean onViewLongClick(E item);
	void onCreateContextMenu(ContextMenu menu, E item);
	boolean onContextItemClick(int menuId, E item);
}
