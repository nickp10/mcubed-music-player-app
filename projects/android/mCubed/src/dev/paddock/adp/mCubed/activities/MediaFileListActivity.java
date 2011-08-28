package dev.paddock.adp.mCubed.activities;

import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.controls.MediaFileView;
import dev.paddock.adp.mCubed.model.IMediaFileProvider;
import dev.paddock.adp.mCubed.receivers.ClientReceiver;
import dev.paddock.adp.mCubed.utilities.Utilities;
import android.app.Activity;
import android.os.Bundle;

public class MediaFileListActivity extends Activity implements IActivity {
	private MediaFileView mediaFileView;
	
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
	public int getLayoutID() {
		return R.layout.media_file_list_activity;
	}

	@Override
	public void findViews() {
		mediaFileView = (MediaFileView)findViewById(R.id.mfa_media_file_view);
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
	public ClientReceiver getClientReceiver() {
		return null;
	}
}