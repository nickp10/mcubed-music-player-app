package dev.paddock.adp.mCubed.model;

import android.content.Context;
import dev.paddock.adp.mCubed.services.PlaybackServer;
import dev.paddock.adp.mCubed.utilities.Utilities;

public abstract class AsyncTask extends android.os.AsyncTask<Void, PublishProgress, Void> {
	private Context context;
	
	public AsyncTask(Context context) {
		this.context = context.getApplicationContext();
	}
	
	public final void updateProgress(PublishProgress... progress) {
		super.publishProgress(progress);
	}
	
	protected abstract void run();
	
	@Override
	protected final Void doInBackground(Void... params) {
		Utilities.pushContext(context);
		Utilities.pushTask(this);
		try {
			run();
			return null;
		} finally {
			Utilities.popTask();
			Utilities.popContext();
		}
	}
	
	@Override
	protected final void onProgressUpdate(PublishProgress... values) {
		if (values == null) {
			return;
		}
		Utilities.pushContext(context);
		try {
			for (PublishProgress progress : values) {
				if (progress != null) {
					PlaybackServer.progressChanged(0, progress.getID(), progress.getTitle(), progress.getValue(), progress.isBlocking());
				}
			}
		} finally {
			Utilities.popContext();
		}
	}
}