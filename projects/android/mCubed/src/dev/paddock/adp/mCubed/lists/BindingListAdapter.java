package dev.paddock.adp.mCubed.lists;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Handler;
import android.os.Looper;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.SectionIndexer;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import dev.paddock.adp.mCubed.lists.BindingList.BindingListObserver;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class BindingListAdapter<E> extends BaseAdapter implements
		BindingListObserver<E>,
		SectionIndexer,
		AdapterView.OnItemClickListener,
		AdapterView.OnItemLongClickListener,
		View.OnCreateContextMenuListener,
		MenuItem.OnMenuItemClickListener {
	private static final Object DATA_SET_CHANGED_LOCK = new Object();
	private static final Object DATA_SET_INVALIDATED_LOCK = new Object();
	private static final String DEFAULT_KEY = "";
	private static final int VIEW_TYPE_GROUP_HEADER = 0;
	private static final int VIEW_TYPE_ITEM = 1;
	private final TreeMap<String, List<E>> map = new TreeMap<String, List<E>>();
	private final List<BindingListView> listViews = new LinkedList<BindingListView>();
	private AbsListView currentListView;
	private BindingList<E> list;
	private boolean isNotifyOnChange = true, isDataSetChangedPosted, isDataSetInvalidatedPosted;
	private int headerDropDownViewResource, headerViewResource, itemDropDownViewResource, itemViewResource;
	private Handler handler;
	private LayoutInflater inflater;
	private IViewItemFactory<String> headerViewItemFactory;
	private IViewItemFactory<E> itemViewItemFactory;
	private IGrouper<E> grouper;
	private Comparator<E> sorter;
	
	private static class BindingListView {
		private AbsListView listView;
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
		this(Utilities.getContext());
	}
	
	public BindingListAdapter(List<E> list) {
		this(Utilities.getContext(), list);
	}
	
	public BindingListAdapter(BindingList<E> list) {
		this(Utilities.getContext(), list);
	}
	
	public BindingListAdapter(Context context) {
		this(context, (BindingList<E>)null);
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
	
	public final void registerWithListView(AbsListView view) {
		currentListView = view;
		registerWithListAdapterView(view);
		view.setFastScrollEnabled(true);
		currentListView = null;
	}
	
	public final void registerWithListAdapterView(AdapterView<ListAdapter> view) {
		view.setAdapter(this);
		view.setOnItemClickListener(this);
		view.setOnItemLongClickListener(this);
		view.setOnCreateContextMenuListener(this);
	}
	
	public final void registerWithSpinnerAdapterView(AdapterView<SpinnerAdapter> view) {
		view.setAdapter(this);
		view.setOnCreateContextMenuListener(this);
	}
	
	@Override
	public final void registerDataSetObserver(DataSetObserver observer) {
		super.registerDataSetObserver(observer);
		if (currentListView != null) {
			BindingListView view = new BindingListView();
			view.listView = currentListView;
			view.observer = observer;
			listViews.add(view);
		}
	}
	
	@Override
	public final void unregisterDataSetObserver(DataSetObserver observer) {
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
			postDataSetChanged();
		}
	}
	
	@Override
	public void notifyDataSetInvalidated() {
		postDataSetInvalidated();
	}
	
	private final void doNotifyDataSetChanged() {
		synchronized (DATA_SET_CHANGED_LOCK) {
			super.notifyDataSetChanged();
			updateSections();
			isDataSetChangedPosted = false;
		}
	}
	
	private final void doNotifyDataSetInvalidated() {
		synchronized (DATA_SET_INVALIDATED_LOCK) {
			super.notifyDataSetInvalidated();
			updateSections();
			isDataSetInvalidatedPosted = false;
		}
	}
	
	private final void postDataSetChanged() {
		synchronized(DATA_SET_CHANGED_LOCK) {
			if (!isDataSetChangedPosted) {
				getHandler().post(new Runnable() {
					@Override
					public void run() {
						doNotifyDataSetChanged();
					}
				});
				isDataSetChangedPosted = true;
			}
		}
	}
	
	private final void postDataSetInvalidated() {
		synchronized(DATA_SET_INVALIDATED_LOCK) {
			if (!isDataSetInvalidatedPosted) {
				getHandler().post(new Runnable() {
					@Override
					public void run() {
						doNotifyDataSetInvalidated();
					}
				});
				isDataSetInvalidatedPosted = true;
			}
		}
	}
	
	private final void updateSections() {
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
		addItem(location, item);
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
	
	private final void addItem(E item) {
		addItem(-1, item);
	}
	
	private final void addItem(int index, E item) {
		// Determine the item's key
		String key = DEFAULT_KEY;
		IGrouper<E> grouper = getGrouper();
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
			list = new SortedList<E>(sorter);
			map.put(key, list);
		}
		if (index < 0 || index > list.size()) {
			list.add(item);
		} else {
			list.add(index, item);
		}
	}
	
	private final boolean removeItem(E item) {
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
	
	public final Comparator<E> getSorter() {
		return sorter;
	}
	
	public final void setSorter(Comparator<E> sorter) {
		if (this.sorter != sorter) {
			this.sorter = sorter;
			refreshView();
		}
	}
	
	public final IGrouper<E> getGrouper() {
		return grouper;
	}
	
	public final void setGrouper(IGrouper<E> grouper) {
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
	
	public final Handler getHandler() {
		if (handler == null) {
			handler = new Handler(Looper.getMainLooper());
		}
		return handler;
	}
	
	public final LayoutInflater getInflater() {
		return inflater;
	}
	
	private final void setInflater(LayoutInflater inflater) {
		this.inflater = inflater;
	}
	
	public final IViewItemFactory<String> getHeaderViewItemFactory() {
		return headerViewItemFactory;
	}
	
	public final void setHeaderViewItemFactory(IViewItemFactory<String> headerViewItemFactory) {
		this.headerViewItemFactory = headerViewItemFactory;
	}
	
	public final IViewItemFactory<E> getItemViewItemFactory() {
		return itemViewItemFactory;
	}
	
	public final void setItemViewItemFactory(IViewItemFactory<E> itemViewItemFactory) {
		this.itemViewItemFactory = itemViewItemFactory;
	}
	
	public final int getHeaderViewResource() {
		return headerViewResource;
	}
	
	public final void setHeaderViewResource(int headerViewResource) {
		this.headerViewResource = headerViewResource;
		notifyDataSetInvalidated();
	}
	
	public final int getHeaderDropDownViewResource() {
		return headerDropDownViewResource;
	}
	
	public final void setHeaderDropDownViewResource(int headerDropDownViewResource) {
		this.headerDropDownViewResource = headerDropDownViewResource;
		notifyDataSetInvalidated();
	}
	
	public final int getItemViewResource() {
		return itemViewResource;
	}
	
	public final void setItemViewResource(int itemViewResource) {
		this.itemViewResource = itemViewResource;
		notifyDataSetInvalidated();
	}
	
	public final int getItemDropDownViewResource() {
		return itemDropDownViewResource;
	}
	
	public final void setItemDropDownViewResource(int itemDropDownViewResource) {
		this.itemDropDownViewResource = itemDropDownViewResource;
		notifyDataSetInvalidated();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final View getDropDownView(int position, View convertView, ViewGroup parent) {
		Utilities.pushContext(parent.getContext());
		try {
			Object item = getItem(position);
			if (item != null && item instanceof BindingListHeader) {
				int resource = getHeaderDropDownViewResource();
				IViewItemFactory<String> factory = getHeaderViewItemFactory();
				String header = ((BindingListHeader)item).header;
				return getHeaderDropDownView(position, header, convertView, parent, resource, factory);
			} else {
				int resource = getItemDropDownViewResource();
				IViewItemFactory<E> factory = getItemViewItemFactory();
				return getItemDropDownView(position, (E)item, convertView, parent, resource, factory);
			}
		} finally {
			Utilities.popContext();
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final View getView(int position, View convertView, ViewGroup parent) {
		Utilities.pushContext(parent.getContext());
		try {
			Object item = getItem(position);
			if (item != null && item instanceof BindingListHeader) {
				int resource = getHeaderViewResource();
				IViewItemFactory<String> factory = getHeaderViewItemFactory();
				String header = ((BindingListHeader)item).header;
				return getHeaderView(position, header, convertView, parent, resource, factory);
			} else {
				int resource = getItemViewResource();
				IViewItemFactory<E> factory = getItemViewItemFactory();
				return getItemView(position, (E)item, convertView, parent, resource, factory);
			}
		} finally {
			Utilities.popContext();
		}
	}
	
	/**
	 * Reuses the given view unless it's null, in which case it will inflate
	 * the view by using the given resource and parent to do so.
	 * @param convertView
	 * @param parent
	 * @param resource
	 * @return
	 */
	protected final View inflateView(View convertView, ViewGroup parent, int resource) {
		if (convertView == null) {
			convertView = getInflater().inflate(resource, parent, false);
		}
		return convertView;
	}
	
	/**
	 * Finds the view item associated with the given view or it will
	 * create a view item and assign it to the view if one couldn't be found.
	 * @param <T>
	 * @param convertView
	 * @param factory
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected final <T> IViewItem<T> getViewItem(View convertView, IViewItemFactory<T> factory) {
		IViewItem<T> viewItem = null;
		if (convertView != null) {
			Object tag = convertView.getTag();
			if (tag != null && tag instanceof IViewItem<?>) {
				viewItem = (IViewItem<T>)tag;
			}
		}
		if (viewItem == null && factory != null) {
			viewItem = factory.createViewItem();
			if (convertView != null) {
				viewItem.findViews(convertView);
				convertView.setTag(viewItem);
			}
		}
		return viewItem;
	}
	
	/**
	 * Gets a generic version of the view of the items either using the view item
	 * associated with the view or by assuming the view is a text view.
	 * @param <T>
	 * @param position
	 * @param item
	 * @param convertView
	 * @param parent
	 * @param resource
	 * @param factory
	 * @return
	 */
	protected final <T> View getView(int position, T item, View convertView, ViewGroup parent, int resource, IViewItemFactory<T> factory) {
		// Inflate the view and find its view item
		convertView = inflateView(convertView, parent, resource);
		IViewItem<T> viewItem = getViewItem(convertView, factory);
		
		// Update the information in the view
		if (viewItem == null) {
			if (convertView != null && convertView instanceof TextView) {
				TextView textView = (TextView)convertView;
		        if (item == null) {
		        	textView.setText("");
		        } else if (item instanceof CharSequence) {
		        	textView.setText((CharSequence)item);
		        } else {
		        	textView.setText(item.toString());
		        }
			}
		} else {
			viewItem.updateViews(item);
		}
		return convertView;
	}
	
	/**
	 * Called when generating a drop-down view for a header.
	 * @param position
	 * @param header
	 * @param convertView
	 * @param parent
	 * @param resource
	 * @param factory
	 * @return
	 */
	protected View getHeaderDropDownView(int position, String header, View convertView, ViewGroup parent, int resource, IViewItemFactory<String> factory) {
		return getHeaderView(position, header, convertView, parent, resource, factory);
	}
	
	/**
	 * Called when generating a drop-down view for an item.
	 * @param position
	 * @param item
	 * @param convertView
	 * @param parent
	 * @param resource
	 * @param factory
	 * @return
	 */
	protected View getItemDropDownView(int position, E item, View convertView, ViewGroup parent, int resource, IViewItemFactory<E> factory) {
		return getItemView(position, item, convertView, parent, resource, factory);
	}
	
	/**
	 * Called when generating a regular view for a header.
	 * @param position
	 * @param header
	 * @param convertView
	 * @param parent
	 * @param resource
	 * @param factory
	 * @return
	 */
	protected View getHeaderView(int position, String header, View convertView, ViewGroup parent, int resource, IViewItemFactory<String> factory) {
		return getView(position, header, convertView, parent, resource, factory);
	}
	
	/**
	 * Called when generating a regular view for an item.
	 * @param position
	 * @param item
	 * @param convertView
	 * @param parent
	 * @param resource
	 * @param factory
	 * @return
	 */
	protected View getItemView(int position, E item, View convertView, ViewGroup parent, int resource, IViewItemFactory<E> factory) {
		return getView(position, item, convertView, parent, resource, factory);
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

	@SuppressWarnings("unchecked")
	@Override
	public final boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		Utilities.pushContext(parent.getContext());
		try {
			boolean handled = false;
			Object item = getItem(position);
			if (item != null && item instanceof BindingListHeader) {
				IViewItemFactory<String> factory = getHeaderViewItemFactory();
				IViewItem<String> viewItem = getViewItem(view, factory);
				String header = ((BindingListHeader)item).header;
				handled = onHeaderLongClick(parent, view, position, id, header);
				if (viewItem != null) {
					handled |= viewItem.onViewLongClick(header);
				}
			} else {
				IViewItemFactory<E> factory = getItemViewItemFactory();
				IViewItem<E> viewItem = getViewItem(view, factory);
				handled = onItemLongClick(parent, view, position, id, (E)item);
				if (viewItem != null) {
					handled |= viewItem.onViewLongClick((E)item);
				}
			}
			return handled;
		} finally {
			Utilities.popContext();
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Utilities.pushContext(parent.getContext());
		try {
			Object item = getItem(position);
			if (item != null && item instanceof BindingListHeader) {
				IViewItemFactory<String> factory = getHeaderViewItemFactory();
				IViewItem<String> viewItem = getViewItem(view, factory);
				String header = ((BindingListHeader)item).header;
				onHeaderClick(parent, view, position, id, header);
				if (viewItem != null) {
					viewItem.onViewClick(header);
				}
			} else {
				IViewItemFactory<E> factory = getItemViewItemFactory();
				IViewItem<E> viewItem = getViewItem(view, factory);
				onItemClick(parent, view, position, id, (E)item);
				if (viewItem != null) {
					viewItem.onViewClick((E)item);
				}
			}
		} finally {
			Utilities.popContext();
		}
	}
	
	/**
	 * Called when a header has been long-clicked upon.
	 * @param parent
	 * @param view
	 * @param position
	 * @param id
	 * @param header
	 * @return True if the long-click has been handled, or false otherwise.
	 */
	protected boolean onHeaderLongClick(AdapterView<?> parent, View view, int position, long id, String header) {
		return false;
	}
	
	/**
	 * Called when a header has been clicked upon.
	 * @param parent
	 * @param view
	 * @param position
	 * @param id
	 * @param header
	 */
	protected void onHeaderClick(AdapterView<?> parent, View view, int position, long id, String header) { }
	
	/**
	 * Called when an item has been long-clicked upon.
	 * @param parent
	 * @param view
	 * @param position
	 * @param id
	 * @param item
	 * @return True if the long-click has been handled, or false otherwise.
	 */
	protected boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id, E item) {
		return false;
	}
	
	/**
	 * Called when an item has been clicked upon.
	 * @param parent
	 * @param view
	 * @param position
	 * @param id
	 * @param item
	 */
	protected void onItemClick(AdapterView<?> parent, View view, int position, long id, E item) { }

	@SuppressWarnings("unchecked")
	@Override
	public final void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		Utilities.pushContext(view.getContext());
		try {
			// Create the menu items
			AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
			Object item = getItem(info.position);
			if (item != null && item instanceof BindingListHeader) {
				IViewItemFactory<String> factory = getHeaderViewItemFactory();
				IViewItem<String> viewItem = getViewItem(info.targetView, factory);
				String header = ((BindingListHeader)item).header;
				if (viewItem != null) {
					viewItem.onCreateContextMenu(menu, header);
				}
			} else {
				IViewItemFactory<E> factory = getItemViewItemFactory();
				IViewItem<E> viewItem = getViewItem(info.targetView, factory);
				if (viewItem != null) {
					viewItem.onCreateContextMenu(menu, (E)item);
				}
			}
			
			// Register for the click event on each menu item
			for (int i = 0; i < menu.size(); i++) {
				MenuItem menuItem = menu.getItem(i);
				menuItem.setOnMenuItemClickListener(this);
			}
		} finally {
			Utilities.popContext();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean onMenuItemClick(MenuItem menuItem) {
		Utilities.pushContext(App.getAppContext());
		try {
			boolean handled = false;
			AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuItem.getMenuInfo();
			Object item = getItem(info.position);
			if (item != null && item instanceof BindingListHeader) {
				IViewItemFactory<String> factory = getHeaderViewItemFactory();
				IViewItem<String> viewItem = getViewItem(info.targetView, factory);
				String header = ((BindingListHeader)item).header;
				if (viewItem != null) {
					handled = viewItem.onContextItemClick(menuItem.getItemId(), header);
				}
			} else {
				IViewItemFactory<E> factory = getItemViewItemFactory();
				IViewItem<E> viewItem = getViewItem(info.targetView, factory);
				if (viewItem != null) {
					handled = viewItem.onContextItemClick(menuItem.getItemId(), (E)item);
				}
			}
			return handled;
		} finally {
			Utilities.popContext();
		}
	}
}