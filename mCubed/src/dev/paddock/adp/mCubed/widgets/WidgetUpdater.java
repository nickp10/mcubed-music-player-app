package dev.paddock.adp.mCubed.widgets;

import java.util.HashMap;
import java.util.Map;

import android.app.PendingIntent;
import android.net.Uri;
import android.widget.RemoteViews;

public class WidgetUpdater implements IRemoteViewsUpdater {
	private final Map<Integer, CharSequence> textViews = new HashMap<Integer, CharSequence>();
	private final Map<Integer, ClickIntent> clickIntents = new HashMap<Integer, ClickIntent>();
	private final Map<Integer, ProgressBarUpdate> progressBarUpdates = new HashMap<Integer, ProgressBarUpdate>();
	private final Map<Integer, Integer> imageResources = new HashMap<Integer, Integer>();
	private final Map<Integer, Uri> imageUris = new HashMap<Integer, Uri>();
	private final Map<Integer, Integer> viewVisibilities = new HashMap<Integer, Integer>();
	
	@Override
	public void updateView(RemoteViews views) {
		// Update the text view text
		for (Map.Entry<Integer, CharSequence> textViewEntry : textViews.entrySet()) {
			views.setTextViewText(textViewEntry.getKey(), textViewEntry.getValue());
		}
		
		// Update the click handlers
		for (Map.Entry<Integer, ClickIntent> clickIntentEntry : clickIntents.entrySet()) {
			ClickIntent clickIntent = clickIntentEntry.getValue();
			PendingIntent pendingIntent = PendingIntent.getBroadcast(clickIntent.getContext(), 0, clickIntent, 0);
			views.setOnClickPendingIntent(clickIntentEntry.getKey(), pendingIntent);
		}
		
		// Update the progress views
		for (Map.Entry<Integer, ProgressBarUpdate> progressBarUpdate : progressBarUpdates.entrySet()) {
			ProgressBarUpdate update = progressBarUpdate.getValue();
			views.setProgressBar(progressBarUpdate.getKey(), update.getMax(), update.getProgress(), update.isIndeterminate());
		}
		
		// Update the visibilities
		for (Map.Entry<Integer, Integer> viewVisibility : viewVisibilities.entrySet()) {
			views.setViewVisibility(viewVisibility.getKey(), viewVisibility.getValue());
		}
		
		// Update the image view resources
		for (Map.Entry<Integer, Integer> imageResource : imageResources.entrySet()) {
			views.setImageViewResource(imageResource.getKey(), imageResource.getValue());
		}
		
		// Update the image view URI's
		for (Map.Entry<Integer, Uri> imageUri : imageUris.entrySet()) {
			views.setImageViewUri(imageUri.getKey(), imageUri.getValue());
		}
	}
	
	public void setTextViewText(int id, CharSequence text) {
		textViews.put(id, text);
	}
	
	public void setOnClickIntent(int id, ClickIntent intent) {
		clickIntents.put(id, intent);
	}
	
	public void setViewVisibility(int id, int visibility) {
		viewVisibilities.put(id, visibility);
	}
	
	public void setImageViewResource(int id, int resource) {
		imageResources.put(id, resource);
	}
	
	public void setImageViewUri(int id, Uri uri) {
		imageUris.put(id, uri);
	}
	
	public void setProgressBar(int id, int max, int progress, boolean isIndeterminate) {
		ProgressBarUpdate update = new ProgressBarUpdate(max, progress, isIndeterminate);
		progressBarUpdates.put(id, update);
	}
}