package dev.paddock.adp.mCubed.controls;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.Schema;
import dev.paddock.adp.mCubed.lists.BindingListAdapter;
import dev.paddock.adp.mCubed.model.MediaFile;
import dev.paddock.adp.mCubed.model.Playlist;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.Comparators;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class PlaylistView extends Fragment {
	private BindingListAdapter<MediaFile> itemsAdapter;
	private ListView listView;
	private Playlist playlist;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.playlist_view, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Utilities.pushContext(view.getContext());
		try {
			// Initialize the list for the list view
			itemsAdapter = new PlaylistViewAdapter();
	
			// Find and initialize the list view
			listView = (ListView)view.findViewById(R.id.pv_listView);
			itemsAdapter.registerWithListView(listView);
	
			// Setup the list for the view
			boolean isHistory = getArguments().getBoolean(Schema.BUNDLE_BOOLEAN_IS_HISTORY);
			setPlaylist(App.getNowPlaying(), isHistory);
		} finally {
			Utilities.popContext();
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		
		// Clean-up the view since it has been destroyed
		setPlaylist(null,  false);
	}

	public Playlist getPlaylist() {
		return playlist;
	}

	private void setPlaylist(Playlist playlist, boolean isHistory) {
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
