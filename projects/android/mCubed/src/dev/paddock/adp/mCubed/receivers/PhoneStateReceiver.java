package dev.paddock.adp.mCubed.receivers;

import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.model.MediaPlayerState;
import dev.paddock.adp.mCubed.model.NotificationArgs;
import dev.paddock.adp.mCubed.services.PlaybackServer;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.PropertyManager;
import dev.paddock.adp.mCubed.utilities.Utilities;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;

public class PhoneStateReceiver extends BroadcastReceiver implements IReceiver {
	private static final PhoneStateReceiver instance = new PhoneStateReceiver();
	private MediaPlayerState savedState;
	private int phoneCallState;
	
	/** 
	 * Prevent external instances of a PhoneStateReceiver
	 */
	private PhoneStateReceiver() {
		Context context = Utilities.getContext();
		TelephonyManager manager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		setPhoneCallState(manager.getCallState());
	}
	
	public static PhoneStateReceiver getInstance() {
		return instance;
	}
	
	public boolean isPhoneCallActive() {
		return getPhoneCallState() != TelephonyManager.CALL_STATE_IDLE;
	}
	
	public int getPhoneCallState() {
		return phoneCallState;
	}
	
	private void setPhoneCallState(int phoneCallState) {
		if (this.phoneCallState != phoneCallState) {
			boolean isPhoneCallActiveOld = isPhoneCallActive();
			this.phoneCallState = phoneCallState;
			boolean isPhoneCallActiveNew = isPhoneCallActive();
			if (isPhoneCallActiveOld != isPhoneCallActiveNew) {
				NotificationArgs args = new NotificationArgs(this, "PhoneCallActive", isPhoneCallActiveOld, isPhoneCallActiveNew);
				PropertyManager.notifyPropertyChanging(this, "PhoneCallActive", args);
				updatePlayer();
				PropertyManager.notifyPropertyChanged(this, "PhoneCallActive", args);
				PlaybackServer.propertyChanged(0, Schema.PROP_PHONE_CALL_ACTIVE, isPhoneCallActiveNew);
			}
		}
	}
	
	private void updatePlayer() {
		if (isPhoneCallActive()) {
			savedState = App.getPlayer().getMediaPlayerStateWithLocks(false, true, true);
		} else {
			App.getPlayer().setMediaPlayerState(savedState);
		}
	}
	
	@Override
	public IntentFilter getIntentFilter() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
		return intentFilter;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Utilities.pushContext(context);
		try {
			String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
			if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
				setPhoneCallState(TelephonyManager.CALL_STATE_IDLE);
			} else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)) {
				setPhoneCallState(TelephonyManager.CALL_STATE_OFFHOOK);
			} else if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
				setPhoneCallState(TelephonyManager.CALL_STATE_RINGING);
			}
		} finally {
			Utilities.popContext();
		}
	}
}