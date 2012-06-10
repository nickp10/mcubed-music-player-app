package dev.paddock.adp.mCubed.controls;

import java.util.List;

import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.lists.BindingList;
import dev.paddock.adp.mCubed.lists.BindingListAdapter;
import dev.paddock.adp.mCubed.model.IMediaFileProvider;
import dev.paddock.adp.mCubed.model.MediaFile;
import dev.paddock.adp.mCubed.utilities.App;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ListView;

public class MediaFileView extends LinearLayout {
	private ListView listView;
	private IMediaFileProvider mediaFileProvider;
	private BindingListAdapter<MediaFile> itemsAdapter;
	
	public MediaFileView(Context context) {
		super(context);
		initView(context);
	}
	
	public MediaFileView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}
	
	private void initView(Context context) {
		// Initialize the list for the list view
		itemsAdapter = new MediaFileViewAdapter(context);
		
		// Inflate the layout
		LayoutInflater inflater = App.getSystemService(LayoutInflater.class, context, Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.media_file_view, this, true);
		
		// Find and initialize the list view
		listView = (ListView)findViewById(R.id.mfv_listView);
		itemsAdapter.registerWithListView(listView);
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
			List<MediaFile> files = mediaFileProvider.getMediaFiles();
			itemsAdapter.setList(BindingList.fromList(files));
		}
	}
}