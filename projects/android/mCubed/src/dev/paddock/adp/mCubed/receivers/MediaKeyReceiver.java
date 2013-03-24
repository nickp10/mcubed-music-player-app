package dev.paddock.adp.mCubed.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import dev.paddock.adp.mCubed.compatability.KeyEventCompat;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class MediaKeyReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Utilities.pushContext(context);
		try {
			String action = intent.getAction();
			if (Intent.ACTION_MEDIA_BUTTON.equals(action) && !PhoneStateReceiver.isPhoneCallActive()) {
				KeyEvent keyEvent = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
				if (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_UP) {
					int key = keyEvent.getKeyCode();
					if (key == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || key == KeyEvent.KEYCODE_HEADSETHOOK) {
						if (App.getPlayer().isPlaying()) {
							App.getPlayer().pause();
						} else {
							App.getPlayer().play();
						}
					} else if (key == KeyEventCompat.KEYCODE_MEDIA_PAUSE) {
						App.getPlayer().pause();
					} else if (key == KeyEventCompat.KEYCODE_MEDIA_PLAY) {
						App.getPlayer().play();
					} else if (key == KeyEvent.KEYCODE_MEDIA_NEXT) {
						App.movePlaybackNext();
					} else if (key == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
						App.movePlaybackPrev();
					} else if (key == KeyEvent.KEYCODE_MEDIA_STOP) {
						App.getPlayer().stop();
					}
				}
			}
		} finally {
			Utilities.popContext();
		}
	}
}
