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
	public boolean onOptionsItemSelected(MenuItem item) {
		return ActivityUtils.onOptionsItemSelected(this, item) ||
				super.onOptionsItemSelected(item);
	}
	
	@Override
	public List<Integer> getMenuOptions() {
		return Arrays.asList(Schema.MN_NOWPLAYING, Schema.MN_LIBRARY,
				Schema.MN_PLAYALL, Schema.MN_SETTINGS, Schema.MN_ABOUT,
				Schema.MN_FEEDBACK, Schema.MN_EXIT);
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
			new Entry<String, String>("How do I add music to mCubed?", "Refer to the documentation for your device or for your version of the Android operating system. mCubed will simply query your device for the music that is on it."),
			new Entry<String, String>("How do I add playlists to mCubed?", "Refer to the documentation for your device or for your version of the Android operating system. mCubed will simply query your device for the playlists that are on it."),
			new Entry<String, String>("What is the \"History\" on the \"Now Playing\" screen?", "The \"History\" is the list of songs that have been played. The song that appears first is the song that was played most recently. The song that appears last is the song that was played first."),
			new Entry<String, String>("What is the \"Queue\" on the \"Now Playing\" screen?", "The \"Queue\" is the list of songs that will be played next. The song that appears first is the song that will be played next. The \"Queue\" will always contain at least one song in it. Once the \"Queue\" is empty, mCubed will add another song to it. Once all the songs in the playlist have been played, the \"Queue\" will be empty which signifies the last song in the \"History\" will be played next (depending on the \"Repeat Status\" setting)."),
			new Entry<String, String>("What is the difference between \"Persisted Shuffle\" and \"Perpetual Shuffle\"?", "\"Persisted Shuffle\" is the smarter and better shuffle mechanism. It will randomly play the \"Now Playing\" playlist such that every song will be played exactly once before a song is played again.\n\n\"Perpetual Shuffle\" is the annoying shuffle mechanism that everyone loves. The next song that plays will be randomly chosen from the entire \"Now Playing\" playlist (including the current song). It is possible that the same song will play 100 times consecutively, granted the probability of that is extremely low. This mode will ignore the \"Repeat Status\" setting as it will perpetually play songs."),
			new Entry<String, String>("How do I create dynamic playlists?", "The \"Now Playing\" playlist is the only dynamic playlist. Additional dynamic playlists may not be created. To add or remove songs to this playlist, browse through the library of music. Touch and hold the artist, album, song, etc. that you want to add or remove and select the corresponding \"Add to Now Playing\" or \"Remove from Now Playing\" option."),
			new Entry<String, String>("How do dynamic playlists work?", "Say you have added Artist A and Album B to the \"Now Playing\" playlist. The \"Now Playing\" playlist will now contain all the songs from Artist A and all the songs from Album B. If you add or remove songs from your device that are for Artist A or Album B, then the \"Now Playing\" playlist will be updated to reflect the newly added or removed songs."),
			new Entry<String, String>("What are \"Logs\" and why do I want them recorded?", "Logs are a collection of usage information containing errors the application encountered. mCubed will only log the necessary usage information to help fix problems that were encountered. These logs are stored on your device and may be deleted at any time by turning off the \"Record Logs\" setting. These logs are sent along with any feedback you provide through the \"Feedback\" screen. If you do not plan on sending feedback, then these logs are completely useless and you may turn the setting off.")
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
