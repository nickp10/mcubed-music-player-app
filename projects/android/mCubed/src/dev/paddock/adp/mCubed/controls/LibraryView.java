package dev.paddock.adp.mCubed.controls;

import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.lists.BindingListAdapter;
import dev.paddock.adp.mCubed.model.MediaGroup;
import dev.paddock.adp.mCubed.model.MediaGrouping;
import dev.paddock.adp.mCubed.utilities.App;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ListView;

public class LibraryView extends LinearLayout {
	private ListView listView;
	private MediaGroup mediaGroup;
	private BindingListAdapter<MediaGrouping> itemsAdapter;
	
	public LibraryView(Context context) {
		super(context);
		initView(context);
	}

	public LibraryView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
		initAttributes(attrs);
	}
	
	private void initView(Context context) {
		// Initialize the list for the list view
		itemsAdapter = new LibraryViewAdapter(context);
		
		// Inflate the layout
		LayoutInflater inflater = App.getSystemService(LayoutInflater.class, context, Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.library_view, this, true);
		
		// Find and initialize the list view
		listView = (ListView)findViewById(R.id.lv_listView);
		itemsAdapter.registerWithListView(listView);
	}
	
	private void initAttributes(AttributeSet attrs) {
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LibraryView);
		setMediaGroup(a.getString(R.styleable.LibraryView_media_group));
		a.recycle();
	}
	
	public MediaGroup getMediaGroup() {
		return mediaGroup;
	}

	public void setMediaGroup(MediaGroup mediaGroup) {
		if (this.mediaGroup != mediaGroup) {
			this.mediaGroup = mediaGroup;
			if (this.mediaGroup == null) {
				itemsAdapter.setList(null);
			} else {
				itemsAdapter.setList(this.mediaGroup.getGroupings());
			}
		}
	}
	
	public void setMediaGroup(String mediaGroup) {
		setMediaGroup(MediaGroup.valueOf(mediaGroup));
	}
}