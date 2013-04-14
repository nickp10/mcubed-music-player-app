package dev.paddock.adp.mCubed.activities;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.controls.MediaFileListView;
import dev.paddock.adp.mCubed.controls.MountDisplay;
import dev.paddock.adp.mCubed.controls.ProgressDisplay;
import dev.paddock.adp.mCubed.model.IMediaFileProvider;
import dev.paddock.adp.mCubed.receivers.IProvideClientReceiver;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class MediaFileListActivity extends Activity implements IActivity {
	private MediaFileListView mediaFileView;
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
		return Arrays.asList(Schema.MN_NOWPLAYING, Schema.MN_PLAYALL,
				Schema.MN_SETTINGS, Schema.MN_HELP, Schema.MN_EXIT,
				Schema.MN_ABOUT, Schema.MN_FEEDBACK);
	}

	@Override
	public int getLayoutID() {
		return R.layout.media_file_list_activity;
	}

	@Override
	public void findViews() {
		mediaFileView = (MediaFileListView)findViewById(R.id.mfla_media_file_view);
		mountDisplay = (MountDisplay)findViewById(R.id.mfla_mount_display);
		progressDisplay = (ProgressDisplay)findViewById(R.id.mfla_progress_display);
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
		IMediaFileProvider mediaFileProvider = Utilities.cast(IMediaFileProvider.class, data);
		mediaFileView.setMediaFileProvider(mediaFileProvider);
	}

	@Override
	public List<IProvideClientReceiver> getClientReceivers() {
		return Arrays.<IProvideClientReceiver>asList(mountDisplay, progressDisplay);
	}
}