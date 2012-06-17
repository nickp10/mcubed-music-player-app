package dev.paddock.adp.mCubed.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ListView;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.lists.BindingListAdapter;
import dev.paddock.adp.mCubed.model.MediaFile;
import dev.paddock.adp.mCubed.model.Playlist;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.Comparators;

public class PlaylistView extends LinearLayout {
	private BindingListAdapter<MediaFile> itemsAdapter;
	private PlayerControls playerControls;
	private ListView listView;
	private Playlist playlist;
	
	public PlaylistView(Context context) {
		super(context);
		initView(context);
	}

	public PlaylistView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}
	
	private void initView(Context context) {
		// Initialize the list for the list view
		itemsAdapter = new PlaylistViewAdapter(context);
		
		// Inflate the layout
		LayoutInflater inflater = App.getSystemService(LayoutInflater.class, context, Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.playlist_view, this, true);
		
		// Find and initialize the list view
		listView = (ListView)findViewById(R.id.pv_listView);
		itemsAdapter.registerWithListView(listView);
		
		// Find additional views
		playerControls = (PlayerControls)findViewById(R.id.pv_player_controls);
	}
	
	public PlayerControls getPlayerControls() {
		return playerControls;
	}
	
	public Playlist getPlaylist() {
		return playlist;
	}
	
	public void setPlaylist(Playlist playlist, boolean isHistory) {
		if (this.playlist != playlist) {
			this.playlist = playlist;
			if (this.playlist == null) {
				itemsAdapter.setList(null);
			} else if (isHistory) {
				itemsAdapter.setSorter(new Comparators.ReverseComparator<MediaFile>());
				itemsAdapter.setList(this.playlist.getHistory());
			} else {
				itemsAdapter.setList(this.playlist.getQueue());
			}
		}
	}
}
