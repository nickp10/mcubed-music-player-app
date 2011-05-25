package dev.paddock.adp.mCubed.widgets;

import java.util.HashMap;
import java.util.Map;

import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.model.InitStatus;
import dev.paddock.adp.mCubed.model.MediaStatus;
import dev.paddock.adp.mCubed.services.ClientCallback;
import dev.paddock.adp.mCubed.services.IClientCallback;
import dev.paddock.adp.mCubed.services.PlaybackClient;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.Utilities;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public abstract class PlaybackProvider extends AppWidgetProvider {
	private static final Map<Class<?>, IClientCallback> clientCallbacks = new HashMap<Class<?>, IClientCallback>();
	
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
	
	protected abstract IRemoteViewsUpdater onUpdate(WidgetUpdater updater);
	protected abstract int getLayoutID();
	
	protected final int[] getWidgetIDs() {
		return getWidgetIDs(AppWidgetManager.getInstance(Utilities.getContext()));
	}
	
	protected final int[] getWidgetIDs(AppWidgetManager appWidgetManager) {
		ComponentName componentName = new ComponentName(Utilities.getContext(), getClass());
		return appWidgetManager.getAppWidgetIds(componentName);
	}
	
	protected final void updateWidget(IRemoteViewsUpdater... updaters) {
		updateWidget(AppWidgetManager.getInstance(Utilities.getContext()), updaters);
	}
	
	protected final void updateWidget(AppWidgetManager appWidgetManager, IRemoteViewsUpdater... updaters) {
		// Get the widget IDs needing updated
		int[] widgetIDs = getWidgetIDs(appWidgetManager);
		if (widgetIDs != null && widgetIDs.length > 0) {
			// Grab the remote view
			RemoteViews views = new RemoteViews(Utilities.getContext().getPackageName(), getLayoutID());
			
			// Register the mouse click intent
			for (IRemoteViewsUpdater updater : updaters) {
				if (updater != null) {
					updater.updateView(views);
				}
			}
			
			// Update the widgets
			appWidgetManager.updateAppWidget(widgetIDs, views);
		}
	}
	
	protected final void invalidate() {
		invalidate(null);
	}
	
	protected final void invalidate(AppWidgetManager manager) {
		if (manager == null) {
			manager = AppWidgetManager.getInstance(Utilities.getContext());
		}
		WidgetUpdater updater = new WidgetUpdater();
		updateWidget(manager, onUpdate(updater), updater);
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
				if (App.getPlayer().getStatus() == MediaStatus.Play) {
					PlaybackClient.pause();
				} else {
					PlaybackClient.play();
				}
			} else if (Schema.WI_PREV_CLICK.equals(action)) {
				PlaybackClient.movePlaybackPrev();
			} else if (Schema.WI_NEXT_CLICK.equals(action)) {
				PlaybackClient.movePlaybackNext();
			} else if (Schema.WI_OPEN_APP_CLICK.equals(action)) {
				Utilities.launchMainActivity();
			}
		} finally {
			Utilities.popContext();
		}
	}
	
	private final IClientCallback getClientCallback() {
		if (!clientCallbacks.containsKey(getClass())) {
			clientCallbacks.put(getClass(), generateClientCallback());
		}
		return clientCallbacks.get(getClass());
	}
	
	protected IClientCallback generateClientCallback() {
		return new ClientCallback() {
			@Override
			public void propertyInitStatusChanged(InitStatus initStatus) {
				invalidate();
			}

			@Override
			public void propertyPlaybackStatusChanged(MediaStatus playbackStatus) {
				invalidate();
			}
		};
	}
}