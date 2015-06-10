package dev.paddock.adp.mCubed.receivers;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.services.IClientCallback;
import dev.paddock.adp.mCubed.services.PlaybackClient;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class ClientReceiver extends BroadcastReceiver implements IReceiver {
	private IClientCallback callback;
	private boolean isAsynchronous;
	private final List<String> intentActions = new ArrayList<String>();

	public ClientReceiver(IClientCallback callback, boolean isAsynchronous) {
		this.callback = callback;
		this.isAsynchronous = isAsynchronous;
		intentActions.add(Schema.I_MCUBED);
	}

	public void addAction(String action) {
		intentActions.add(action);
	}

	public void removeAction(String action) {
		intentActions.remove(action);
	}

	@Override
	public IntentFilter getIntentFilter() {
		IntentFilter intentFilter = new IntentFilter();
		for (String action : intentActions) {
			intentFilter.addAction(action);
		}
		return intentFilter;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Utilities.pushContext(context);
		try {
			PlaybackClient.handleIntent(intent, callback, isAsynchronous);
		} finally {
			Utilities.popContext();
		}
	}
}