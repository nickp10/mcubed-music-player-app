package dev.paddock.adp.mCubed.widgets;

import java.util.HashMap;
import java.util.Map;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.activities.ActivityUtils;
import dev.paddock.adp.mCubed.listeners.AudioFocusListener;
import dev.paddock.adp.mCubed.services.IClientCallback;
import dev.paddock.adp.mCubed.services.PlaybackClient;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.Utilities;

public abstract class PlaybackProvider extends AppWidgetProvider {
	private static final Map<Class<?>, IClientCallback> clientCallbacks = new HashMap<Class<?>, IClientCallback>();
	private static final Map<Class<?>, IRemoteViewsUpdater> widgetUpdaters = new HashMap<Class<?>, IRemoteViewsUpdater>();
	
	@Override
	public final void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Utilities.pushContext(context);
		try {
			super.onUpdate(context, appWidgetManager, appWidgetIds);
			invalidate(appWidgetManager);
			PlaybackClient.startService();
		} finally {
			Utilities.popContext();
		}
	}
	
	protected abstract int getLayoutID();
	
	protected final int[] getWidgetIDs() {
		return getWidgetIDs(AppWidgetManager.getInstance(Utilities.getContext()));
	}
	
	protected final int[] getWidgetIDs(AppWidgetManager appWidgetManager) {
		ComponentName componentName = new ComponentName(Utilities.getContext(), getClass());
		return appWidgetManager.getAppWidgetIds(componentName);
	}

	protected final void invalidate() {
		invalidate(null);
	}
	
	protected final void invalidate(int flags) {
		invalidate(null, flags);
	}
	
	protected final void invalidate(AppWidgetManager manager) {
		invalidate(manager, Schema.FLAG_ALL);
	}
	
	protected final void invalidate(AppWidgetManager manager, int flags) {
		// Get the widget manager
		if (manager == null) {
			manager = AppWidgetManager.getInstance(Utilities.getContext());
		}
		
		// Make sure we actually got a manager
		if (manager != null) {
			// Get the widget IDs needing updated
			int[] widgetIDs = getWidgetIDs(manager);
			if (widgetIDs != null && widgetIDs.length > 0) {
				// Grab the remote view
				RemoteViews views = new RemoteViews(Utilities.getContext().getPackageName(), getLayoutID());
				
				// Update the remote view
				IRemoteViewsUpdater updater = getUpdater();
				if (updater != null) {
					updater.updateView(views, flags);
				}
				
				// Update the widgets
				manager.updateAppWidget(widgetIDs, views);
			}
		}
	}
	
	@Override
	public final void onReceive(final Context context, final Intent intent) {
		Utilities.pushContext(context);
		try {
			super.onReceive(context, intent);
			String action = intent.getAction();
			
			// Call on deleted, if the widget was deleted
			if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
				int appWidgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
				if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
					onDeleted(context, new int[] { appWidgetId });
				}
			}
			
			// Handle mCubed intents
			else if (Schema.ismCubedIntent(intent)) {
				int[] widgetIDs = getWidgetIDs();
				if (widgetIDs != null && widgetIDs.length > 0) {
					PlaybackClient.handleIntent(intent, getClientCallback(), false);
				}
			}
			
			// Handle other intents
			else if (Schema.WI_PLAY_CLICK.equals(action)) {
				if (App.getPlayer().isPlaying()) {
					PlaybackClient.pause();
				} else {
					PlaybackClient.play();
				}

				// Re-associate the media player
				AudioFocusListener.getInstance().requestAudioFocus(context);
			} else if (Schema.WI_PREV_CLICK.equals(action)) {
				PlaybackClient.movePlaybackPrev();

				// Re-associate the media player
				AudioFocusListener.getInstance().requestAudioFocus(context);
			} else if (Schema.WI_NEXT_CLICK.equals(action)) {
				PlaybackClient.movePlaybackNext();

				// Re-associate the media player
				AudioFocusListener.getInstance().requestAudioFocus(context);
			} else if (Schema.WI_OPEN_APP_CLICK.equals(action)) {
				ActivityUtils.startMainActivity();
			}
		} finally {
			Utilities.popContext();
		}
	}
	
	protected final PendingIntent generateClickIntent(String action) {
		Context context = Utilities.getContext();
		Intent intent = new Intent(context, getClass());
		intent.setAction(action);
		return PendingIntent.getBroadcast(context, 0, intent, 0);
	}
	
	private final IRemoteViewsUpdater getUpdater() {
		if (!widgetUpdaters.containsKey(getClass())) {
			widgetUpdaters.put(getClass(), generateUpdater());
		}
		return widgetUpdaters.get(getClass());
	}
	
	protected abstract IRemoteViewsUpdater generateUpdater();
	
	private final IClientCallback getClientCallback() {
		if (!clientCallbacks.containsKey(getClass())) {
			clientCallbacks.put(getClass(), generateClientCallback());
		}
		return clientCallbacks.get(getClass());
	}
	
	protected abstract IClientCallback generateClientCallback();
}