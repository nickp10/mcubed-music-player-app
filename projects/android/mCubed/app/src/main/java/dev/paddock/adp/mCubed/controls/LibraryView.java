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
import dev.paddock.adp.mCubed.model.MediaGroup;
import dev.paddock.adp.mCubed.model.MediaGrouping;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class LibraryView extends Fragment {
	private ListView listView;
	private MediaGroup mediaGroup;
	private BindingListAdapter<MediaGrouping> itemsAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.library_view, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Utilities.pushContext(view.getContext());
		try {
			// Initialize the list for the list view
			itemsAdapter = new LibraryViewAdapter();
	
			// Find and initialize the list view
			listView = (ListView)view.findViewById(R.id.lv_listView);
			itemsAdapter.registerWithListView(listView);
			
			// Setup the media group for the view
			setMediaGroup((MediaGroup)getArguments().getSerializable(Schema.BUNDLE_MEDIA_GROUP));
		} finally {
			Utilities.popContext();
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		
		// Clean-up the view since it has been destroyed
		setMediaGroup(null);
	}

	public MediaGroup getMediaGroup() {
		return mediaGroup;
	}

	private void setMediaGroup(MediaGroup mediaGroup) {
		if (this.mediaGroup != mediaGroup) {
			this.mediaGroup = mediaGroup;
			if (this.mediaGroup == null) {
				itemsAdapter.setList(null);
			} else {
				itemsAdapter.setList(this.mediaGroup.getGroupings());
			}
		}
	}
}