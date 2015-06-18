package dev.paddock.adp.mCubed.services;

import java.io.Serializable;

public interface IClientCallback {
	void progressChanged(int intentID, String progressID, String progressTitle, byte progressValue, boolean progressBlocking);
	void propertyChanged(int intentID, String propertyName, Serializable propertyValue);
	void preferenceChanged(int intentID, String preferenceName);
}