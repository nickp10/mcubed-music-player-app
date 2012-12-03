package dev.paddock.adp.mCubed.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.model.InitStatus;
import dev.paddock.adp.mCubed.receivers.ClientReceiver;
import dev.paddock.adp.mCubed.receivers.IProvideClientReceiver;
import dev.paddock.adp.mCubed.services.ClientCallback;
import dev.paddock.adp.mCubed.services.IClientCallback;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class ProgressDisplay extends LinearLayout implements View.OnClickListener, IProvideClientReceiver {
	private static final int INDETERMINATE_VALUE = Integer.MIN_VALUE;
	private TextView titleView;
	private ProgressBar progressBar;
	private ClientReceiver clientReceiver;
	private IClientCallback clientCallback;
	
	public ProgressDisplay(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}
	
	private void initView(Context context) {
		// Inflate the layout
		LayoutInflater inflater = App.getSystemService(LayoutInflater.class, context, Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.progress_display, this, true);
		
		// Find the views
		titleView = (TextView)findViewById(R.id.pd_title);
		progressBar = (ProgressBar)findViewById(R.id.pd_progress);
		
		// Initialize the views
		initProgress(App.getInitStatus(), App.isMounted(), App.isScanRequired());
		
		// Register listeners
		setOnClickListener(this);
	}
	
	private void initProgress(InitStatus initStatus, boolean isMounted, boolean isScanRequired) {
		if (isScanRequired) {
			showProgress(Utilities.getResourceString(R.string.prog_scanning), INDETERMINATE_VALUE);
		} else if (initStatus == InitStatus.Initializing && isMounted) {
			showProgress(Utilities.getResourceString(R.string.prog_initializing), INDETERMINATE_VALUE);
		} else if (initStatus == InitStatus.Initialized || !isMounted) {
			hideProgress();
		}
	}
	
	private void showProgress(String title, int progress) {
		progressBar.setIndeterminate(progress == INDETERMINATE_VALUE);
		progressBar.setProgress(progress);
		titleView.setText(title);
		setVisibility(View.VISIBLE);
	}
	
	private void hideProgress() {
		progressBar.setIndeterminate(false);
		progressBar.setProgress(0);
		titleView.setText("");
		setVisibility(View.GONE);
	}
	
	@Override
	public ClientReceiver getClientReceiver() {
		if (clientReceiver == null) {
			clientReceiver = new ClientReceiver(getClientCallback(), false);
			clientReceiver.addAction(Schema.I_MCUBED_PROGRESS);
		}
		return clientReceiver;
	}
	
	private IClientCallback getClientCallback() {
		if (clientCallback == null) {
			clientCallback = new ClientCallback() {
				@Override
				public void propertyInitStatusChanged(InitStatus initStatus) {
					initProgress(initStatus, App.isMounted(), App.isScanRequired());
				}
				
				@Override
				public void propertyMountedChanged(boolean isMounted) {
					initProgress(App.getInitStatus(), isMounted, App.isScanRequired());
				}
				
				@Override
				public void propertyScanRequiredChanged(boolean isScanRequired) {
					initProgress(App.getInitStatus(), App.isMounted(), isScanRequired);
				}
				
				@Override
				public void progressChanged(int intentID, String progressID, String progressTitle, byte progressValue, boolean progressBlocking) {
					if (progressBlocking) {
						if (progressValue < 100) {
							showProgress(progressTitle, progressValue);
						} else {
							hideProgress();
						}
					}
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