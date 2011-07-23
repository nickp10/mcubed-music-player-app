package dev.paddock.adp.mCubed.model;

import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import dev.paddock.adp.mCubed.model.BindingList.BindingListObserver;
import dev.paddock.adp.mCubed.utilities.App;
import dev.paddock.adp.mCubed.utilities.Utilities;

public class BindingListAdapter<E> extends BaseAdapter implements BindingListObserver<E> {
	private final BindingList<E> list;
	private boolean isNotifyOnChange = true;
	private int dropDownViewResource, viewResource;
	private LayoutInflater inflater;
	private IViewHolderFactory<E> factory;
	private Comparator<E> comparator;
	
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
		if (list == null) {
			throw new IllegalArgumentException("The paramater 'list' may not be null.");
		}
		this.list = list;
		this.list.addObserver(this);
		dropDownViewResource = android.R.layout.simple_spinner_dropdown_item;
		viewResource = android.R.layout.simple_spinner_item;
		inflater = App.getSystemService(LayoutInflater.class, context, Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public BindingList<E> getList() {
		return list;
	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public boolean isNotifyOnChange() {
		return isNotifyOnChange;
	}
	
	public void setNotifyOnChange(boolean isNotifyOnChange) {
		this.isNotifyOnChange = isNotifyOnChange;
	}
	
	@Override
	public void notifyDataSetChanged() {
		if (isNotifyOnChange()) {
			super.notifyDataSetChanged();
		}
	}

	@Override
	public void itemAdded(int location, E item) {
		if (!doSort()) {
			notifyDataSetChanged();
		}
	}

	@Override
	public void itemRemoved(int location, E item) {
		notifyDataSetChanged();
	}

	@Override
	public void itemsCleared() {
		notifyDataSetChanged();
	}
	
	@Override
	public void itemsSorted() {
		notifyDataSetChanged();
	}
	
	private boolean doSort() {
		if (comparator != null) {
			list.sort(comparator);
			return true;
		}
		return false;
	}
	
	public Comparator<E> getSorter() {
		return comparator;
	}
	
	public void setSorter(Comparator<E> comparator) {
		if (this.comparator != comparator) {
			this.comparator = comparator;
			doSort();
		}
	}
	
	public LayoutInflater getInflater() {
		return inflater;
	}
	
	public IViewHolderFactory<E> getViewHolderFactory() {
		return factory;
	}
	
	public void setViewHolderFactory(IViewHolderFactory<E> factory) {
		this.factory = factory;
	}
	
	public int getViewResource() {
		return viewResource;
	}
	
	public void setViewResource(int viewResource) {
		this.viewResource = viewResource;
		notifyDataSetInvalidated();
	}
	
	public int getDropDownViewResource() {
		return dropDownViewResource;
	}
	
	public void setDropDownViewResource(int dropDownViewResource) {
		this.dropDownViewResource = dropDownViewResource;
		notifyDataSetInvalidated();
	}
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		if (factory == null) {
			return createViewFromResource(position, convertView, parent, dropDownViewResource);
		} else {
			return createViewFromResource(position, convertView, parent, dropDownViewResource, factory);
		}
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (factory == null) {
			return createViewFromResource(position, convertView, parent, viewResource);
		} else {
			return createViewFromResource(position, convertView, parent, viewResource, factory);
		}
	}
	
	private View createViewFromResource(int position, View convertView, ViewGroup parent, int resource) {
		// Inflate the view
        View view = null;
        if (convertView == null) {
            view = inflater.inflate(resource, parent, false);
        } else {
            view = convertView;
        }

        // Create the text view
        TextView text = (TextView) view;
        Object item = getItem(position);
        if (item instanceof CharSequence) {
            text.setText((CharSequence)item);
        } else {
            text.setText(item.toString());
        }

        return view;
    }
	
	@SuppressWarnings("unchecked")
	private View createViewFromResource(int position, View convertView, ViewGroup parent, int resource, IViewHolderFactory<E> factory) {
		// Create the view and find its child views
		IViewHolder<E> viewHolder = null;
		if (convertView == null) {
			convertView = getInflater().inflate(resource, null);
			viewHolder = factory.createViewHolder();
			viewHolder.findViews(convertView);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (IViewHolder<E>)convertView.getTag();
		}
		
		// Update the information in the view
		E item = (E)getItem(position);
		viewHolder.updateViews(item);
		return convertView;
	}
}