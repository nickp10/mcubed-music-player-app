package dev.paddock.adp.mCubed.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;
import dev.paddock.adp.mCubed.model.NotificationArgs;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.PropertyManager;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class PhoneStateReceiver extends BroadcastReceiver implements IReceiver {
	private static int phoneCallState;
	
	static {
		Utilities.pushContext(App.getAppContext());
		try {
			TelephonyManager manager = App.getSystemService(TelephonyManager.class, Context.TELEPHONY_SERVICE);
			if (manager != null) {
				setPhoneCallState(manager.getCallState());
			}
		} finally {
			Utilities.popContext();
		}
	}
	
	public static boolean isPhoneCallActive() {
		return isPhoneCallActive(getPhoneCallState());
	}
	
	public static int getPhoneCallState() {
		return phoneCallState;
	}
	
	private static void setPhoneCallState(int phoneCallState) {
		if (PhoneStateReceiver.phoneCallState != phoneCallState) {
			boolean isPhoneCallActiveOld = isPhoneCallActive(PhoneStateReceiver.phoneCallState);
			boolean isPhoneCallActiveNew = isPhoneCallActive(phoneCallState);
			if (isPhoneCallActiveOld != isPhoneCallActiveNew) {
				NotificationArgs args = new NotificationArgs(PhoneStateReceiver.class, "PhoneCallActive", isPhoneCallActiveOld, isPhoneCallActiveNew);
				PropertyManager.notifyPropertyChanging(args);
				PhoneStateReceiver.phoneCallState = phoneCallState;
				PropertyManager.notifyPropertyChanged(args);
			} else {
				PhoneStateReceiver.phoneCallState = phoneCallState;
			}
		}
	}
	
	private static boolean isPhoneCallActive(int phoneCallState) {
		return phoneCallState != TelephonyManager.CALL_STATE_IDLE;
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