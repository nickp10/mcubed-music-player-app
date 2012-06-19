package dev.paddock.adp.mCubed.controls;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.lists.BindingList;
import dev.paddock.adp.mCubed.lists.BindingListAdapter;
import dev.paddock.adp.mCubed.lists.IGrouper;
import dev.paddock.adp.mCubed.model.IMediaFileProvider;
import dev.paddock.adp.mCubed.model.MediaFile;
import dev.paddock.adp.mCubed.model.MediaFileValue;
import dev.paddock.adp.mCubed.model.MediaGroup;
import dev.paddock.adp.mCubed.model.MediaGrouping;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class MediaFileListView extends LinearLayout {
	private ListView listView;
	private Spinner groupSpinner;
	private IMediaFileProvider mediaFileProvider;
	private BindingListAdapter<MediaFileValue> groupAdapter;
	private BindingListAdapter<MediaFile> itemsAdapter;
	
	/**
	 * Listens for a new group-by item to be selected.
	 */
	private final OnItemSelectedListener groupBySelectedListener = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> adapter, View view, int position, long id) {
			MediaFileValue value = (MediaFileValue)adapter.getItemAtPosition(position);
			itemsAdapter.setGrouper(value);
		}

		@Override
		public void onNothingSelected(AdapterView<?> adapter) { }
	};
	
	public MediaFileListView(Context context) {
		super(context);
		initView(context);
	}
	
	public MediaFileListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}
	
	private void initView(Context context) {
		// Initialize the list for the list view
		itemsAdapter = new MediaFileListViewAdapter(context);
		
		// Inflate the layout
		LayoutInflater inflater = App.getSystemService(LayoutInflater.class, context, Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.media_file_list_view, this, true);
		
		// Find and initialize the list view
		listView = (ListView)findViewById(R.id.mflv_list_view);
		itemsAdapter.registerWithListView(listView);
		
		// Find and initialize the group by spinner
		groupSpinner = (Spinner)findViewById(R.id.mflv_group_spinner);
		groupAdapter = new BindingListAdapter<MediaFileValue>(getContext(), Arrays.asList(MediaFileValue.values()));
		groupAdapter.registerWithSpinnerAdapterView(groupSpinner);
		groupSpinner.setOnItemSelectedListener(groupBySelectedListener);
	}
	
	public IMediaFileProvider getMediaFileProvider() {
		return mediaFileProvider;
	}
	
	public void setMediaFileProvider(IMediaFileProvider mediaFileProvider) {
		if (this.mediaFileProvider != mediaFileProvider) {
			this.mediaFileProvider = mediaFileProvider;
			refreshMediaFileProvider();
		}
	}
	
	public void refreshMediaFileProvider() {
		if (mediaFileProvider == null) {
			itemsAdapter.setList(null);
		} else {
			refreshGrouper();
			List<MediaFile> files = mediaFileProvider.getMediaFiles();
			itemsAdapter.setList(BindingList.fromList(files));
		}
	}
	
	public void setGrouper(IGrouper<MediaFile> grouper) {
		groupSpinner.setSelection(groupAdapter.getList().indexOf(grouper), true);
	}
	
	public void refreshGrouper() {
		MediaGrouping grouping = Utilities.cast(MediaGrouping.class, mediaFileProvider);
		if (grouping != null && grouping.getGroup() == MediaGroup.Artist) {
			setGrouper(MediaFileValue.Album);
		} else {
			setGrouper(MediaFileValue.Artist);
		}
	}
}