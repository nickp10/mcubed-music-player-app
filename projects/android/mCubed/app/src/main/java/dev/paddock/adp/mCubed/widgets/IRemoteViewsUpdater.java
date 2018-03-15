package dev.paddock.adp.mCubed.widgets;

import android.widget.RemoteViews;

public interface IRemoteViewsUpdater {
	void updateView(RemoteViews views, int flags);
}