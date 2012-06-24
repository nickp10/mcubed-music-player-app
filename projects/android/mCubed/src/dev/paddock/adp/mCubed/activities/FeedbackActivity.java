package dev.paddock.adp.mCubed.activities;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.controls.MountDisplay;
import dev.paddock.adp.mCubed.controls.ProgressDisplay;
import dev.paddock.adp.mCubed.model.Holder;
import dev.paddock.adp.mCubed.receivers.IProvideClientReceiver;
import dev.paddock.adp.mCubed.utilities.Log;
import dev.paddock.adp.mCubed.utilities.Utilities;
import dev.paddock.adp.mCubed.utilities.WebService;

public class FeedbackActivity extends Activity implements IActivity {
	private MountDisplay mountDisplay;
	private ProgressDisplay progressDisplay;
	private Button submitButton;
	
	/**
	 * Listener for handling when the feedback form should be submitted.
	 */
	private final OnClickListener submitFeedbackListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Utilities.dispatchToBackgroundThread(FeedbackActivity.this, new Runnable() {
				@Override
				public void run() {
					final Holder<Integer> resource = new Holder<Integer>();
					String result = WebService.submitFeedback("test@test.com", "Hello");
					
					// Determine whether or not the submission succeeded
					if (Boolean.parseBoolean(result)) {
						Log.clearLogFile();
						resource.setValue(R.string.feedback_submit_success);
					} else {
						resource.setValue(R.string.feedback_submit_failure);
					}
					
					// Show the user the result
					Utilities.dispatchToUIThread(Utilities.getContext(), new Runnable() {
						@Override
						public void run() {
							Toast.makeText(Utilities.getContext(), Utilities.getResourceString(resource.getValue()), Toast.LENGTH_LONG).show();
						}
					});
				}
			});
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityUtils.onCreate(this, savedInstanceState);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		ActivityUtils.onDestroy(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		ActivityUtils.onResume(this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu) &&
				ActivityUtils.onCreateOptionsMenu(this, menu);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu) &&
				ActivityUtils.onPrepareOptionsMenu(this, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return ActivityUtils.onOptionsItemSelected(this, item) ||
				super.onOptionsItemSelected(item);
	}
	
	@Override
	public List<Integer> getMenuOptions() {
		return Arrays.asList(Schema.MN_NOWPLAYING, Schema.MN_PLAYALL,
				Schema.MN_SETTINGS, Schema.MN_LIBRARY, Schema.MN_EXIT,
				Schema.MN_ABOUT, Schema.MN_HELP);
	}

	@Override
	public int getLayoutID() {
		return R.layout.feedback_activity;
	}

	@Override
	public void findViews() {
		mountDisplay = (MountDisplay)findViewById(R.id.fa_mount_display);
		progressDisplay = (ProgressDisplay)findViewById(R.id.fa_progress_display);
		submitButton = (Button)findViewById(R.id.fa_submit_button);
	}

	@Override
	public void setupViews() {
	}

	@Override
	public void updateViews() {
	}

	@Override
	public void registerListeners() {
		submitButton.setOnClickListener(submitFeedbackListener);
	}
	
	@Override
	public void handleExtras(Bundle extras) {
	}
	
	@Override
	public List<IProvideClientReceiver> getClientReceivers() {
		return Arrays.<IProvideClientReceiver>asList(mountDisplay, progressDisplay);
	}
}