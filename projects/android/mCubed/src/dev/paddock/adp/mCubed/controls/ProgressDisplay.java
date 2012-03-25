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

public class ProgressDisplay extends LinearLayout implements View.OnClickListener, IProvideClientReceiver {
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
		initProgress(App.getInitStatus());
		
		// Register listeners
		setOnClickListener(this);
	}
	
	private void initProgress(InitStatus initStatus) {
		if (initStatus == InitStatus.Initializing) {
			showProgress("Initializing...", 0);
		} else if (initStatus == InitStatus.Initialized) {
			hideProgress();
		}
	}
	
	private void showProgress(String title, int progress) {
		progressBar.setProgress(progress);
		titleView.setText(title);
		setVisibility(View.VISIBLE);
	}
	
	private void hideProgress() {
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
				public void propertyInitStatusChanged(InitStatus initStatus) {
					initProgress(initStatus);
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