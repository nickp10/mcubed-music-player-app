package dev.paddock.adp.mCubed.listeners;

import android.content.ComponentName;
import android.content.Context;
import android.media.AudioManager;
import dev.paddock.adp.mCubed.receivers.AudioFocusReceiver;
import dev.paddock.adp.mCubed.receivers.MediaKeyReceiver;
import dev.paddock.adp.mCubed.receivers.RemoteControlReceiver;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class MediaAssociateListener implements IListener {
	@Override
	public void register() {
		Context context = Utilities.getContext();
		if (context != null) {
			AudioManager manager = App.getSystemService(AudioManager.class, context, Context.AUDIO_SERVICE);
			if (manager != null) {
				ComponentName component = new ComponentName(context, MediaKeyReceiver.class);
				AudioFocusReceiver.registerAudioFocus(manager);
				manager.registerMediaButtonEventReceiver(component);
				RemoteControlReceiver.registerRemoteControl(manager, component);
			}
		}
	}

	@Override
	public void unregister() {
		Context context = Utilities.getContext();
		if (context != null) {
			AudioManager manager = App.getSystemService(AudioManager.class, context, Context.AUDIO_SERVICE);
			if (manager != null) {
				ComponentName component = new ComponentName(context, MediaKeyReceiver.class);
				RemoteControlReceiver.unregisterRemoteControl(manager);
				manager.unregisterMediaButtonEventReceiver(component);
				AudioFocusReceiver.unregisterAudioFocus(manager);
			}
		}
	}
}
