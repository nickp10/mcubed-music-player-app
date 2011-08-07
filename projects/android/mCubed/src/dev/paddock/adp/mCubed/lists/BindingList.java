package dev.paddock.adp.mCubed.lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class BindingList<E> implements List<E> {
	private List<E> items;
	private final List<BindingListObserver<E>> observers = new ArrayList<BindingListObserver<E>>();
	
	public static interface BindingListObserver<E> {
		void itemAdded(BindingList<E> list, int location, E item);
		void itemRemoved(BindingList<E> list, int location, E item);
		void itemsCleared(BindingList<E> list);
	}
	
	public BindingList() {
		this(new ArrayList<E>());
	}
	
	public BindingList(List<E> startItems) {
		if (startItems == null) {
			throw new IllegalArgumentException("startItems");
		}
		items = startItems;
	}
	
	public void addObserver(BindingListObserver<E> observer) {
		observers.add(observer);
	}
	
	public void removeObserver(BindingListObserver<E> observer) {
		observers.remove(observer);
	}
	
	private void notifyItemAdded(int location, E item) {
		for (BindingListObserver<E> observer : observers) {
			observer.itemAdded(this, location, item);
		}
	}
	
	private void notifyItemRemoved(int location, E item) {
		for (BindingListObserver<E> observer : observers) {
			observer.itemRemoved(this, location, item);
		}
	}
	
	private void notifyItemsCleared() {
		for (BindingListObserver<E> observer : observers) {
			observer.itemsCleared(this);
		}
	}

	@Override
	public void add(int location, E object) {
		items.add(location, object);
		notifyItemAdded(location, object);
	}

	@Override
	public boolean add(E object) {
		add(size(), object);
		return true;
	}

	@Override
	public boolean addAll(int location, Collection<? extends E> collection) {
		for (E object : collection) {
			add(location++, object);
		}
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends E> collection) {
		return addAll(size(), collection);
	}

	@Override
	public void clear() {
		items.clear();
		notifyItemsCleared();
	}

	@Override
	public boolean contains(Object object) {
		return items.contains(object);
	}

	@Override
	public boolean containsAll(Collection<?> collection) {
		return items.containsAll(collection);
	}

	@Override
	public E get(int location) {
		return items.get(location);
	}

	@Override
	public int indexOf(Object object) {
		return items.indexOf(object);
	}

	@Override
	public boolean isEmpty() {
		return items.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return items.iterator();
	}

	@Override
	public int lastIndexOf(Object object) {
		return items.lastIndexOf(object);
	}

	@Override
	public ListIterator<E> listIterator() {
		return items.listIterator();
	}

	@Override
	public ListIterator<E> listIterator(int location) {
		return items.listIterator(location);
	}

	@Override
	public E remove(int location) {
		E object = items.remove(location);
		notifyItemRemoved(location, object);
		return object;
	}

	@Override
	public boolean remove(Object object) {
		int location = indexOf(object);
        if (location >= 0) {
            remove(location);
            return true;
        }
        return false;
	}
	
	public boolean remove(Object object, boolean removeAllOccurrences) {
		boolean modified = false;
		if (removeAllOccurrences) {
			while (remove(object)) {
				modified = true;
			}
		} else {
			modified = remove(object);
		}
		return modified;
	}

	@Override
	public boolean removeAll(Collection<?> collection) {
		boolean modified = false;
		for (Object object : collection) {
			if (remove(object)) {
				modified = true;
			}
		}
		return modified;
	}

	@Override
	public boolean retainAll(Collection<?> collection) {
		Collection<Object> removeAll = new ArrayList<Object>(size());
		for (Object object : this) {
			if (!collection.contains(object)) {
				removeAll.add(object);
			}
		}
		return removeAll(removeAll);
	}

	@Override
	public E set(int location, E object) {
		E oldItem = items.set(location, object);
		notifyItemRemoved(location, oldItem);
		notifyItemAdded(location, object);
		return oldItem;
	}

	@Override
	public int size() {
		return items.size();
	}

	@Override
	public List<E> subList(int start, int end) {
		return items.subList(start, end);
	}

	@Override
	public Object[] toArray() {
		return items.toArray();
	}

	@Override
	public <T> T[] toArray(T[] array) {
		return items.toArray(array);
	}
}