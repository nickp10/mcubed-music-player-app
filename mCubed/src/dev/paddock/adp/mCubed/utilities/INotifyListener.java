package dev.paddock.adp.mCubed.utilities;

import dev.paddock.adp.mCubed.model.NotificationArgs;

public interface INotifyListener {
	void propertyChanging(Object instance, NotificationArgs args);
	void propertyChanged(Object instance, NotificationArgs args);
}