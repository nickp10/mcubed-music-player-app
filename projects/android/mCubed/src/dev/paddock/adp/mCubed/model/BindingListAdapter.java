package dev.paddock.adp.mCubed.model;

import android.widget.BaseAdapter;
import dev.paddock.adp.mCubed.model.BindingList.BindingListObserver;

public abstract class BindingListAdapter<E> extends BaseAdapter implements BindingListObserver<E> {
	private final BindingList<E> list;
	private boolean isNotifyOnChange = true;
	
	public BindingListAdapter(BindingList<E> list) {
		if (list == null) {
			throw new IllegalArgumentException("The paramater 'list' may not be null.");
		}
		this.list = list;
		this.list.addObserver(this);
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
		notifyDataSetChanged();
	}

	@Override
	public void itemRemoved(int location, E item) {
		notifyDataSetChanged();
	}

	@Override
	public void itemsCleared() {
		notifyDataSetChanged();
	}
}