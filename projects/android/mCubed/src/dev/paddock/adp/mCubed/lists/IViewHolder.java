package dev.paddock.adp.mCubed.lists;

import android.view.View;

public interface IViewHolder<E> {
	void findViews(View rootView);
	void updateViews(E item);
}
