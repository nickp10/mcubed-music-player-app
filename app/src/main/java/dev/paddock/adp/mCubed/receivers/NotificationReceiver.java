package dev.paddock.adp.mCubed.receivers;

import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.listeners.AudioFocusListener;
import dev.paddock.adp.mCubed.services.PlaybackClient;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.Log;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class NotificationReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Utilities.pushContext(context);
		try {
			String action = intent.getAction();
			Log.i(String.format(Locale.US, "Handle notification intent [Action=%s]", action));
			if (Schema.NOTIF_PLAY_CLICK.equals(action)) {
				if (App.getPlayer().isPlaying()) {
					PlaybackClient.pause();
				} else {
					PlaybackClient.play();
				}

				// Re-associate the media player
				AudioFocusListener.getInstance().requestAudioFocus(context);
			} else if (Schema.NOTIF_PREV_CLICK.equals(action)) {
				PlaybackClient.movePlaybackPrev();

				// Re-associate the media player
				AudioFocusListener.getInstance().requestAudioFocus(context);
			} else if (Schema.NOTIF_NEXT_CLICK.equals(action)) {
				PlaybackClient.movePlaybackNext();

				// Re-associate the media player
				AudioFocusListener.getInstance().requestAudioFocus(context);
			}
		} finally {
			Utilities.popContext();
		}
	}
}
