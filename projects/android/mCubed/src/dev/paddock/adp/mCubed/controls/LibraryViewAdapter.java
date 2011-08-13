package dev.paddock.adp.mCubed.controls;

import java.util.Comparator;
import java.util.List;

import android.content.Context;
import dev.paddock.adp.mCubed.R;
import dev.paddock.adp.mCubed.lists.BindingList;
import dev.paddock.adp.mCubed.lists.BindingListAdapter;
import dev.paddock.adp.mCubed.lists.IViewItem;
import dev.paddock.adp.mCubed.lists.IViewItemFactory;
import dev.paddock.adp.mCubed.model.MediaGrouping;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class LibraryViewAdapter extends BindingListAdapter<MediaGrouping> {
	private Object[] sections;
	
	public LibraryViewAdapter(Context context, BindingList<MediaGrouping> items) {
		super(context, items);
	}
	
	@Override
	protected void onBeforeInitialize() {
		setItemViewResource(R.layout.library_view_item);
		setItemViewItemFactory(new IViewItemFactory<MediaGrouping>() {
			@Override
			public IViewItem<MediaGrouping> createViewItem() {
				return new LibraryViewItem();
			}
		});
		setSorter(new Comparator<MediaGrouping>() {
			@Override
			public int compare(MediaGrouping leftGrouping, MediaGrouping rightGrouping) {
				if (leftGrouping == null) {
					return rightGrouping == null ? 0 : -1;
				} else if (rightGrouping == null) {
					return 1;
				} else {
					return leftGrouping.getName().compareToIgnoreCase(rightGrouping.getName());
				}
			}
		});
	}

	@Override
	public Object[] getSections(List<MediaGrouping> items) {
		if (sections == null) {
			sections = new Object[27];
			sections[0] = "#";
			for (int i = 'A'; i <= 'Z'; i++) {
				sections[(i - 'A') + 1] = Character.toString((char)i);
			}
		}
		return sections;
	}

	@Override
	public int getPositionForSection(List<MediaGrouping> items, int section) {
		if (section == 0) {
			return 0;
		}
		String letter = (String)getSections()[section];
		int position = 0;
		for (MediaGrouping grouping : items) {
			String name = grouping.getName();
			if (letter.compareToIgnoreCase(name) <= 0) {
				break;
			}
			position++;
		}
		return position;
	}

	@Override
	public int getSectionForPosition(List<MediaGrouping> items, int position) {
		MediaGrouping grouping = items.get(position);
		if (grouping != null) {
			String name = grouping.getName();
			if (!Utilities.isNullOrEmpty(name)) {
				char character = Character.toUpperCase(name.charAt(0));
				if (character >= 'A') {
					if (character > 'Z') {
						character = 'Z';
					}
					return (character - 'A') + 1;
				}
			}
		}
		return 0;
	}
}