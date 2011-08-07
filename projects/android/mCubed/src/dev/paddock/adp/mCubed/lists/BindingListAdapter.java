package dev.paddock.adp.mCubed.lists;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import dev.paddock.adp.mCubed.lists.BindingList.BindingListObserver;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class BindingListAdapter<E> extends BaseAdapter implements BindingListObserver<E>, SectionIndexer {
	private static final String DEFAULT_KEY = "";
	private static final int VIEW_TYPE_GROUP_HEADER = 0;
	private static final int VIEW_TYPE_ITEM = 1;
	private final TreeMap<String, List<E>> map = new TreeMap<String, List<E>>();
	private final List<BindingListView> listViews = new LinkedList<BindingListView>();
	private ListView currentListView;
	private BindingList<E> list;
	private boolean isNotifyOnChange = true;
	private int headerDropDownViewResource, headerViewResource, itemDropDownViewResource, itemViewResource;
	private LayoutInflater inflater;
	private IViewHolderFactory<String> headerViewHolderFactory;
	private IViewHolderFactory<E> itemViewHolderFactory;
	private Grouper<E> grouper;
	private Comparator<E> sorter;
	
	private static class BindingListView {
		private ListView listView;
		private DataSetObserver observer;
		private boolean flagWidth;
	}
	
	private static class BindingListHeader {
		private String header;
		private BindingListHeader(String header) {
			this.header = header;
		}
	}
	
	public BindingListAdapter() {
		this(new BindingList<E>());
	}
	
	public BindingListAdapter(List<E> list) {
		this(new BindingList<E>(list));
	}
	
	public BindingListAdapter(BindingList<E> list) {
		this(Utilities.getContext(), list);
	}
	
	public BindingListAdapter(Context context) {
		this(context, new BindingList<E>());
	}
	
	public BindingListAdapter(Context context, List<E> list) {
		this(context, new BindingList<E>(list));
	}
	
	public BindingListAdapter(Context context, BindingList<E> list) {
		setHeaderDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		setItemDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		setHeaderViewResource(android.R.layout.simple_list_item_1);
		setItemViewResource(android.R.layout.simple_spinner_item);
		setInflater(App.getSystemService(LayoutInflater.class, context, Context.LAYOUT_INFLATER_SERVICE));
		onBeforeInitialize();
		setList(list);
		onAfterInitialize();
	}
	
	protected void onBeforeInitialize() { }
	protected void onAfterInitialize() { }
	
	public final BindingList<E> getList() {
		return list;
	}
	
	public final void setList(BindingList<E> list) {
		if (list == null) {
			throw new IllegalArgumentException("The paramater 'list' may not be null.");
		}
		if (this.list != list) {
			// Unregister from the old list
			if (this.list != null) {
				this.list.removeObserver(this);
			}
			
			// Update the value
			this.list = list;
			
			// Register to the new list and refresh
			if (this.list != null) {
				this.list.addObserver(this);
			}
			refreshView();
		}
	}
	
	/**
	 * Returns the number of items that will be displayed. A separator counts as 
	 * an item. For example, 1 separator with 2 items would report a count of 3.
	 */
	@Override
	public final int getCount() {
		int count = 0;
		if (getGrouper() == null) {
			if (map.containsKey(DEFAULT_KEY)) {
				count = map.get(DEFAULT_KEY).size();
			}
		} else {
			for (String key : map.keySet()) {
				List<E> list = map.get(key);
				count += list.size() + 1;
			}
		}
		return count;
	}

	/**
	 * Returns the item at the given position. For example, 1 separator with 2 items
	 * would have the separator at position 0 and the 2 items at 1 and 2, respectively.
	 */
	@Override
	public final Object getItem(int position) {
		if (getGrouper() == null) {
			if (map.containsKey(DEFAULT_KEY)) {
				return map.get(DEFAULT_KEY).get(position);
			}
		} else {
			for (String key : map.keySet()) {
				List<E> list = map.get(key);
				int size = list.size() + 1;
				if (position == 0) {
					return new BindingListHeader(key);
				}
				if (position < size) {
					return list.get(position - 1);
				}
				position -= size;
			}
		}
		return null;
	}
	
	@Override
	public final long getItemId(int position) {
		return position;
	}
	
	/**
	 * Returns the number of unique types of items there. For example,
	 * if an item is either a separator or an item, then this reports 2.  
	 */
	@Override
	public final int getViewTypeCount() {
		return 2;
	}
	
	/**
	 * Returns a unique zero-based number for the item type at the given position.
	 * For example, for a view with separators and items, the separator could report
	 * 0 whereas the items would then have to report 1.
	 */
	@Override
	public final int getItemViewType(int position) {
		if (getGrouper() == null) {
			if (map.containsKey(DEFAULT_KEY)) {
				return VIEW_TYPE_ITEM;
			}
		} else {
			for (String key : map.keySet()) {
				List<E> list = map.get(key);
				int size = list.size() + 1;
				if (position == 0) {
					return VIEW_TYPE_GROUP_HEADER;
				}
				if (position < size) {
					return VIEW_TYPE_ITEM;
				}
				position -= size;
			}
		}
		return Adapter.IGNORE_ITEM_VIEW_TYPE;
	}
	
	public final void registerWithListView(ListView view) {
		currentListView = view;
		view.setAdapter(this);
		view.setFastScrollEnabled(true);
		currentListView = null;
	}
	
	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		super.registerDataSetObserver(observer);
		if (currentListView != null) {
			BindingListView view = new BindingListView();
			view.listView = currentListView;
			view.observer = observer;
			listViews.add(view);
		}
	}
	
	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		super.unregisterDataSetObserver(observer);
		Iterator<BindingListView> it = listViews.iterator();
		while (it.hasNext()) {
			BindingListView view = it.next();
			if (view.observer == observer) {
				it.remove();
			}
		}
	}
	
	public final boolean isNotifyOnChange() {
		return isNotifyOnChange;
	}
	
	public final void setNotifyOnChange(boolean isNotifyOnChange) {
		this.isNotifyOnChange = isNotifyOnChange;
	}
	
	@Override
	public void notifyDataSetChanged() {
		if (isNotifyOnChange()) {
			super.notifyDataSetChanged();
			updateSections();
		}
	}
	
	@Override
	public void notifyDataSetInvalidated() {
		super.notifyDataSetInvalidated();
		updateSections();
	}
	
	private void updateSections() {
		for (BindingListView view : listViews) {
			// Hack to re-query the sections for the list view
			view.listView.setFastScrollEnabled(false);
			view.listView.setFastScrollEnabled(true);
			
			// Hack to fix the position of the section overlay
			// http://stackoverflow.com/questions/2912082/section-indexer-overlay-is-not-updating-as-the-adapters-data-changes
			int width = view.listView.getWidth();
			if (width > 0) {
				ViewGroup.LayoutParams params = view.listView.getLayoutParams();
				params.width = (width + (view.flagWidth ? -1 : 1));
				view.listView.requestLayout();
				view.flagWidth = !view.flagWidth;
			}
		}
	}
	
	@Override
	public void itemAdded(BindingList<E> list, int location, E item) {
		addItem(item);
		notifyDataSetChanged();
	}

	@Override
	public void itemRemoved(BindingList<E> list, int location, E item) {
		if (removeItem(item)) {
			notifyDataSetChanged();
		}
	}

	@Override
	public void itemsCleared(BindingList<E> list) {
		map.clear();
		notifyDataSetChanged();
	}
	
	private void addItem(E item) {
		// Determine the item's key
		String key = DEFAULT_KEY;
		Grouper<E> grouper = getGrouper();
		if (grouper != null) {
			key = grouper.getGroup(item);
		}
		
		// Add the item
		List<E> list = null;
		Comparator<E> sorter = getSorter();
		if (map.containsKey(key)) {
			list = map.get(key);
		} else if (sorter == null) {
			list = new ArrayList<E>();
			map.put(key, list);
		} else {
			list = new SortedList<E>(getSorter());
			map.put(key, list);
		}
		list.add(item);
	}
	
	private boolean removeItem(E item) {
		boolean itemRemoved = false;
		for (Iterator<String> it = map.keySet().iterator(); it.hasNext(); ) {
			String key = it.next();
			
			// Remove all occurrences of the instance
			List<E> value = map.get(key);
			while (value.remove(item)) {
				itemRemoved = true;
			}
			
			// Remove the group if no items are left
			if (value.isEmpty()) {
				it.remove();
				itemRemoved = true;
			}
		}
		return itemRemoved;
	}
	
	public Comparator<E> getSorter() {
		return sorter;
	}
	
	public void setSorter(Comparator<E> sorter) {
		if (this.sorter != sorter) {
			this.sorter = sorter;
			refreshView();
		}
	}
	
	public Grouper<E> getGrouper() {
		return grouper;
	}
	
	public void setGrouper(Grouper<E> grouper) {
		if (this.grouper != grouper) {
			this.grouper = grouper;
			refreshView();
		}
	}
	
	public void refreshView() {
		map.clear();
		List<E> list = getList();
		if (list != null) {
			for (E item : list) {
				addItem(item);
			}
		}
		notifyDataSetChanged();
	}
	
	public LayoutInflater getInflater() {
		return inflater;
	}
	
	private void setInflater(LayoutInflater inflater) {
		this.inflater = inflater;
	}
	
	public IViewHolderFactory<String> getHeaderViewHolderFactory() {
		return headerViewHolderFactory;
	}
	
	public void setHeaderViewHolderFactory(IViewHolderFactory<String> headerViewHolderFactory) {
		this.headerViewHolderFactory = headerViewHolderFactory;
	}
	
	public IViewHolderFactory<E> getItemViewHolderFactory() {
		return itemViewHolderFactory;
	}
	
	public void setItemViewHolderFactory(IViewHolderFactory<E> itemViewHolderFactory) {
		this.itemViewHolderFactory = itemViewHolderFactory;
	}
	
	public int getHeaderViewResource() {
		return headerViewResource;
	}
	
	public void setHeaderViewResource(int headerViewResource) {
		this.headerViewResource = headerViewResource;
		notifyDataSetInvalidated();
	}
	
	public int getHeaderDropDownViewResource() {
		return headerDropDownViewResource;
	}
	
	public void setHeaderDropDownViewResource(int headerDropDownViewResource) {
		this.headerDropDownViewResource = headerDropDownViewResource;
		notifyDataSetInvalidated();
	}
	
	public int getItemViewResource() {
		return itemViewResource;
	}
	
	public void setItemViewResource(int itemViewResource) {
		this.itemViewResource = itemViewResource;
		notifyDataSetInvalidated();
	}
	
	public int getItemDropDownViewResource() {
		return itemDropDownViewResource;
	}
	
	public void setItemDropDownViewResource(int itemDropDownViewResource) {
		this.itemDropDownViewResource = itemDropDownViewResource;
		notifyDataSetInvalidated();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final View getDropDownView(int position, View convertView, ViewGroup parent) {
		Object item = getItem(position);
		if (item != null && item instanceof BindingListHeader) {
			String header = ((BindingListHeader)item).header;
			int resource = getHeaderDropDownViewResource();
			IViewHolderFactory<String> factory = getHeaderViewHolderFactory();
			if (factory == null) {
				return getHeaderDropDownView(position, header, convertView, parent, resource);
			} else {
				return getHeaderDropDownView(position, header, convertView, parent, resource, factory);
			}
		} else {
			int resource = getItemDropDownViewResource();
			IViewHolderFactory<E> factory = getItemViewHolderFactory();
			if (factory == null) {
				return getItemDropDownView(position, (E)item, convertView, parent, resource);
			} else {
				return getItemDropDownView(position, (E)item, convertView, parent, resource, factory);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final View getView(int position, View convertView, ViewGroup parent) {
		Object item = getItem(position);
		if (item != null && item instanceof BindingListHeader) {
			String header = ((BindingListHeader)item).header;
			int resource = getHeaderViewResource();
			IViewHolderFactory<String> factory = getHeaderViewHolderFactory();
			if (factory == null) {
				return getHeaderView(position, header, convertView, parent, resource);
			} else {
				return getHeaderView(position, header, convertView, parent, resource, factory);
			}
		} else {
			int resource = getItemViewResource();
			IViewHolderFactory<E> factory = getItemViewHolderFactory();
			if (factory == null) {
				return getItemView(position, (E)item, convertView, parent, resource);
			} else {
				return getItemView(position, (E)item, convertView, parent, resource, factory);
			}
		}
	}
	
	/**
	 * Called when generating a drop-down view for a header when a view holder factory has not been specified.
	 * @param position
	 * @param header
	 * @param convertView
	 * @param parent
	 * @param resource
	 * @return
	 */
	protected View getHeaderDropDownView(int position, String header, View convertView, ViewGroup parent, int resource) {
		return getHeaderView(position, null, convertView, parent, resource);
	}
	
	/**
	 * Called when generating a drop-down view for a header when a view holder factory has been specified.
	 * @param position
	 * @param header
	 * @param convertView
	 * @param parent
	 * @param resource
	 * @param factory
	 * @return
	 */
	protected View getHeaderDropDownView(int position, String header, View convertView, ViewGroup parent, int resource, IViewHolderFactory<String> factory) {
		return getHeaderView(position, null, convertView, parent, resource, factory);
	}
	
	/**
	 * Called when generating a drop-down view for an item when a view holder factory has not been specified.
	 * @param position
	 * @param item
	 * @param convertView
	 * @param parent
	 * @param resource
	 * @return
	 */
	protected View getItemDropDownView(int position, E item, View convertView, ViewGroup parent, int resource) {
		return getItemView(position, item, convertView, parent, resource);
	}
	
	/**
	 * Called when generating a drop-down view for an item when a view holder factory has been specified.
	 * @param position
	 * @param item
	 * @param convertView
	 * @param parent
	 * @param resource
	 * @param factory
	 * @return
	 */
	protected View getItemDropDownView(int position, E item, View convertView, ViewGroup parent, int resource, IViewHolderFactory<E> factory) {
		return getItemView(position, item, convertView, parent, resource, factory);
	}
	
	/**
	 * Called when generating a regular view for a header when a view holder factory has not been specified.
	 * @param position
	 * @param header
	 * @param convertView
	 * @param parent
	 * @param resource
	 * @return
	 */
	protected View getHeaderView(int position, String header, View convertView, ViewGroup parent, int resource) {
		return createView(position, header, convertView, parent, resource);
	}
	
	/**
	 * Called when generating a regular view for a header when a view holder factory has been specified.
	 * @param position
	 * @param header
	 * @param convertView
	 * @param parent
	 * @param resource
	 * @param factory
	 * @return
	 */
	protected View getHeaderView(int position, String header, View convertView, ViewGroup parent, int resource, IViewHolderFactory<String> factory) {
		return createView(position, header, convertView, parent, resource, factory);
	}
	
	/**
	 * Called when generating a regular view for an item when a view holder factory has not been specified.
	 * @param position
	 * @param item
	 * @param convertView
	 * @param parent
	 * @param resource
	 * @return
	 */
	protected View getItemView(int position, E item, View convertView, ViewGroup parent, int resource) {
		return createView(position, item, convertView, parent, resource);
	}
	
	/**
	 * Called when generating a regular view for an item when a view holder factory has been specified.
	 * @param position
	 * @param item
	 * @param convertView
	 * @param parent
	 * @param resource
	 * @param factory
	 * @return
	 */
	protected View getItemView(int position, E item, View convertView, ViewGroup parent, int resource, IViewHolderFactory<E> factory) {
		return createView(position, item, convertView, parent, resource, factory);
	}
	
	protected final View createView(int position, Object item, View convertView, ViewGroup parent, int resource) {
		// Inflate the view
        View view = null;
        if (convertView == null) {
            view = getInflater().inflate(resource, parent, false);
        } else {
            view = convertView;
        }

        // Update the text view
        TextView text = (TextView)view;
        if (item == null) {
        	text.setText("");
        } else if (item instanceof CharSequence) {
            text.setText((CharSequence)item);
        } else {
            text.setText(item.toString());
        }

        return view;
	}
	
	@SuppressWarnings("unchecked")
	protected final <F> View createView(int position, F item, View convertView, ViewGroup parent, int resource, IViewHolderFactory<F> factory) {
		// Create the view and find its child views
		IViewHolder<F> viewHolder = null;
		if (convertView == null) {
			convertView = getInflater().inflate(resource, null);
			viewHolder = factory.createViewHolder();
			viewHolder.findViews(convertView);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (IViewHolder<F>)convertView.getTag();
		}
		
		// Update the information in the view
		viewHolder.updateViews(item);
		return convertView;
	}
	
	/**
	 * Override this method to determine the sections for the given list
	 * of items. This method will only be called if there isn't a group
	 * by on the adapter.
	 * @param items The items to determine the sections for.
	 * @return The available sections from the items in the list.
	 */
	public Object[] getSections(List<E> items) {
		return new Object[0];
	}
	
	/**
	 * Override this method to determine the index at which the given
	 * section begins in the given list of items. This method will only
	 * be called if there isn't a group by on the adapter.
	 * @param items The items to get the index location of the section.
	 * @param section The section to get the index location for.
	 * @return The index location of where the given section begins.
	 */
	public int getPositionForSection(List<E> items, int section) {
		return 0;
	}
	
	/**
	 * Override this method to determine the section in which the
	 * given index location belongs in the given list ofitems. This
	 * method will only be called if there isn't a group by on the
	 * adapter.
	 * @param items The items to get the section of the index location.
	 * @param position The index location to get the section for.
	 * @return The section of where the given index location belongs.
	 */
	public int getSectionForPosition(List<E> items, int position) {
		return 0;
	}

	@Override
	public final Object[] getSections() {
		if (getGrouper() == null) {
			if (map.containsKey(DEFAULT_KEY)) {
				return getSections(map.get(DEFAULT_KEY));
			}
		} else {
			return map.keySet().toArray();
		}
		return new Object[0];
	}

	@Override
	public final int getPositionForSection(int section) {
		if (getGrouper() == null) {
			if (map.containsKey(DEFAULT_KEY)) {
				return getPositionForSection(map.get(DEFAULT_KEY), section);
			}
		} else {
			int position = 0;
			for (String key : map.keySet()) {
				if (section == 0) {
					return position;
				}
				List<E> list = map.get(key);
				position += list.size() + 1;
				section--;
			}
		}
		return 0;
	}

	@Override
	public final int getSectionForPosition(int position) {
		if (getGrouper() == null) {
			if (map.containsKey(DEFAULT_KEY)) {
				return getSectionForPosition(map.get(DEFAULT_KEY), position);
			}
		} else {
			int section = 0;
			for (String key : map.keySet()) {
				List<E> list = map.get(key);
				position -= (list.size() + 1);
				if (position <= 0) {
					return section;
				}
				section++;
			}
		}
		return 0;
	}
}