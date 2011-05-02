package dev.paddock.adp.mCubed.widgets;

import android.content.Context;
import android.content.Intent;

public class ClickIntent extends Intent {
	private Context context;
	
	public ClickIntent(Context context, Class<?> clazz, String action) {
		super(context, clazz);
		setAction(action);
		setContext(context);
	}

	public Context getContext() {
		return context;
	}
	public void setContext(Context context) {
		this.context = context;
	}
}