package dev.paddock.adp.mCubed.activities;

import java.util.Arrays;
import java.util.List;

import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.model.MediaFile;
import dev.paddock.adp.mCubed.receivers.ClientReceiver;
import dev.paddock.adp.mCubed.utilities.Utilities;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
				Schema.MN_SETTINGS, Schema.MN_HELP, Schema.MN_EXIT,
				Schema.MN_ABOUT, Schema.MN_FEEDBACK);
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