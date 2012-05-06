package dev.paddock.adp.mCubed.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.receivers.ClientReceiver;
import dev.paddock.adp.mCubed.receivers.IProvideClientReceiver;
import dev.paddock.adp.mCubed.services.ClientCallback;
import dev.paddock.adp.mCubed.services.IClientCallback;
import dev.paddock.adp.mCubed.utilities.App;

public class MountDisplay extends LinearLayout implements View.OnClickListener, IProvideClientReceiver {
	private ClientReceiver clientReceiver;
	private IClientCallback clientCallback;
	
	public MountDisplay(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}
	
	private void initView(Context context) {
		// Inflate the layout
		LayoutInflater inflater = App.getSystemService(LayoutInflater.class, context, Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.mount_display, this, true);
		
		// Initialize the display
		initMountDisplay();
		
		// Register listeners
		setOnClickListener(this);
	}
	
	private void initMountDisplay() {
		initMountDisplay(App.isMounted());
	}
	
	private void initMountDisplay(boolean isMounted) {
		if (isMounted) {
			hideMountDisplay();
		} else {
			showMountDisplay();
		}
	}
	
	private void hideMountDisplay() {
		setVisibility(View.GONE);
	}
	
	private void showMountDisplay() {
		setVisibility(View.VISIBLE);
	}

	@Override
	public ClientReceiver getClientReceiver() {
		if (clientReceiver == null) {
			clientReceiver = new ClientReceiver(getClientCallback(), false);
		}
		return clientReceiver;
	}
	
	private IClientCallback getClientCallback() {
		if (clientCallback == null) {
			clientCallback = new ClientCallback() {
				@Override
				public void propertyMountChanged(boolean isMounted) {
					initMountDisplay(isMounted);
				}
			};
		}
		return clientCallback;
	}
	
	@Override
	public void onClick(View v) {
		// Do nothing to prevent click
	}
}