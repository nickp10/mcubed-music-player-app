package dev.paddock.adp.mCubed.activities;

import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.model.MediaFile;
import dev.paddock.adp.mCubed.receivers.ClientReceiver;
import dev.paddock.adp.mCubed.utilities.Utilities;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MediaFileDetailsActivity extends Activity implements IActivity {
	private TextView artistTextView;
	private MediaFile mediaFile;
	
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
		return R.layout.media_file_details_activity;
	}

	@Override
	public void findViews() {
		artistTextView = (TextView)findViewById(R.id.mfd_artist_textView);
	}

	@Override
	public void setupViews() {
	}

	@Override
	public void updateViews() {
		if (mediaFile == null) {
			artistTextView.setText("Unknown");
		} else {
			artistTextView.setText(mediaFile.getArtist());
		}
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
				mediaFile = file;
			}
		}
	}

	@Override
	public ClientReceiver getClientReceiver() {
		return null;
	}
}