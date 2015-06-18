package dev.paddock.adp.mCubed.activities;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.controls.MediaFileDetailsView;
import dev.paddock.adp.mCubed.controls.MountDisplay;
import dev.paddock.adp.mCubed.controls.ProgressDisplay;
import dev.paddock.adp.mCubed.model.MediaFile;
import dev.paddock.adp.mCubed.receivers.IProvideClientReceiver;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class MediaFileDetailsActivity extends Activity implements IActivity {
	private MediaFileDetailsView mediaFileView;
	private MountDisplay mountDisplay;
	private ProgressDisplay progressDisplay;
	
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
	public boolean onOptionsItemSelected(MenuItem item) {
		return ActivityUtils.onOptionsItemSelected(this, item) ||
				super.onOptionsItemSelected(item);
	}

	@Override
	public List<Integer> getMenuOptions() {
		return ActivityUtils.getMenuOptions(Schema.MN_LIBRARY);
	}

	@Override
	public int getLayoutID() {
		return R.layout.media_file_details_activity;
	}

	@Override
	public void findViews() {
		mediaFileView = (MediaFileDetailsView)findViewById(R.id.mfda_media_file_details_view);
		mountDisplay = (MountDisplay)findViewById(R.id.mfda_mount_display);
		progressDisplay = (ProgressDisplay)findViewById(R.id.mfda_progress_display);
	}

	@Override
	public void setupViews() {
	}

	@Override
	public void updateViews() {
	}

	@Override
	public void registerListeners() {
	}
	
	@Override
	public void handleExtras(Bundle extras) {
		Object data = extras.getSerializable(Schema.I_PARAM_ACTIVITY_DATA);
		Long idObj = Utilities.cast(Long.class, data);
		if (idObj != null) {
			MediaFile file = MediaFile.get(idObj.longValue());
			if (file != null) {
				mediaFileView.setMediaFile(file);
			}
		}
	}

	@Override
	public List<IProvideClientReceiver> getClientReceivers() {
		return Arrays.<IProvideClientReceiver>asList(mountDisplay, progressDisplay);
	}

	public MediaFile getMediaFile() {
		return mediaFileView == null ? null : mediaFileView.getMediaFile();
	}
}