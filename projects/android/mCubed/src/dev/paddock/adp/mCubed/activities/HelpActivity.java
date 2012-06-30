package dev.paddock.adp.mCubed.activities;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.controls.MountDisplay;
import dev.paddock.adp.mCubed.controls.ProgressDisplay;
import dev.paddock.adp.mCubed.lists.ExpandableListAdapter;
import dev.paddock.adp.mCubed.model.Entry;
import dev.paddock.adp.mCubed.receivers.IProvideClientReceiver;

public class HelpActivity extends Activity implements IActivity {
	private MountDisplay mountDisplay;
	private ProgressDisplay progressDisplay;
	private ExpandableListView contentView;
	
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
				Schema.MN_ABOUT, Schema.MN_FEEDBACK);
	}

	@Override
	public int getLayoutID() {
		return R.layout.help_activity;
	}

	@Override
	public void findViews() {
		mountDisplay = (MountDisplay)findViewById(R.id.ha_mount_display);
		progressDisplay = (ProgressDisplay)findViewById(R.id.ha_progress_display);
		contentView = (ExpandableListView)findViewById(R.id.ha_content_view);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setupViews() {
		contentView.setAdapter(new ExpandableListAdapter<String>(this, R.layout.help_content_group_view, R.layout.help_content_child_view, Arrays.<java.util.Map.Entry<String, String>>asList(
			new Entry<String, String>("Test", "Hi"),
			new Entry<String, String>("Hello", "This will be a very long entry that is going to wrap across multiple lines when all is said and done. How will the Droid handle this lengthy help message? Even more lines are needed to see this wrap even more."),
			new Entry<String, String>("Goodbye", "This will be a very long entry that is going to wrap across multiple lines when all is said and done. How will the Droid handle this lengthy help message? Even more lines are needed to see this wrap even more."),
			new Entry<String, String>("Test", "Hi"),
			new Entry<String, String>("Hello", "This will be a very long entry that is going to wrap across multiple lines when all is said and done. How will the Droid handle this lengthy help message? Even more lines are needed to see this wrap even more."),
			new Entry<String, String>("Goodbye", "This will be a very long entry that is going to wrap across multiple lines when all is said and done. How will the Droid handle this lengthy help message? Even more lines are needed to see this wrap even more."),
			new Entry<String, String>("Test", "Hi"),
			new Entry<String, String>("Hello", "This will be a very long entry that is going to wrap across multiple lines when all is said and done. How will the Droid handle this lengthy help message? Even more lines are needed to see this wrap even more."),
			new Entry<String, String>("Goodbye", "This will be a very long entry that is going to wrap across multiple lines when all is said and done. How will the Droid handle this lengthy help message? Even more lines are needed to see this wrap even more."),
			new Entry<String, String>("Test", "Hi"),
			new Entry<String, String>("Hello", "This will be a very long entry that is going to wrap across multiple lines when all is said and done. How will the Droid handle this lengthy help message? Even more lines are needed to see this wrap even more."),
			new Entry<String, String>("Goodbye", "This will be a very long entry that is going to wrap across multiple lines when all is said and done. How will the Droid handle this lengthy help message? Even more lines are needed to see this wrap even more."),
			new Entry<String, String>("Test", "Hi"),
			new Entry<String, String>("Hello", "This will be a very long entry that is going to wrap across multiple lines when all is said and done. How will the Droid handle this lengthy help message? Even more lines are needed to see this wrap even more."),
			new Entry<String, String>("Goodbye", "This will be a very long entry that is going to wrap across multiple lines when all is said and done. How will the Droid handle this lengthy help message? Even more lines are needed to see this wrap even more."),
			new Entry<String, String>("Test", "Hi"),
			new Entry<String, String>("Hello", "This will be a very long entry that is going to wrap across multiple lines when all is said and done. How will the Droid handle this lengthy help message? Even more lines are needed to see this wrap even more."),
			new Entry<String, String>("Goodbye", "This will be a very long entry that is going to wrap across multiple lines when all is said and done. How will the Droid handle this lengthy help message? Even more lines are needed to see this wrap even more.")
		)));
	}

	@Override
	public void updateViews() {
	}

	@Override
	public void registerListeners() {
	}
	
	@Override
	public void handleExtras(Bundle extras) {
	}

	@Override
	public List<IProvideClientReceiver> getClientReceivers() {
		return Arrays.<IProvideClientReceiver>asList(mountDisplay, progressDisplay);
	}
}
