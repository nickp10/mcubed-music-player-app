package dev.paddock.adp.mCubed.model;

import java.util.ArrayList;

import junit.framework.TestCase;

import dev.paddock.adp.mCubed.model.BindingList;
import dev.paddock.adp.mCubed.model.BindingList.BindingListObserver;
import dev.paddock.adp.mCubed.model.Holder;

public class BindingListTest extends TestCase {
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testAddItem() {
		ArrayList<Integer> startItems = new ArrayList<Integer>();
		BindingList<Integer> list = new BindingList<Integer>(startItems);
		
		list.add(3);
		list.add(6);
		list.add(0, 2);
		
		assertEquals(2, (int)list.get(0));
		assertEquals(3, (int)list.get(1));
		assertEquals(6, (int)list.get(2));
		assertEquals(3, list.size());
	}
	
	public void testAddItemObserver() {
		// Setup
		ArrayList<Integer> startItems = new ArrayList<Integer>();
		BindingList<Integer> list = new BindingList<Integer>(startItems);
		final Holder<Integer> locHolder = new Holder<Integer>(-1);
		final Holder<Integer> valHolder = new Holder<Integer>(-1);
		list.addObserver(new BindingListObserver<Integer>() {
			@Override
			public void itemAdded(int location, Integer item) {
				locHolder.setValue(location);
				valHolder.setValue(item);
			}

			@Override
			public void itemRemoved(int location, Integer item) {
				fail("Shouldn't be called");
			}

			@Override
			public void itemsCleared() {
				fail("Shouldn't be called");
			}
		});
		
		// Assert setup
		assertEquals(-1, (int)locHolder.getValue());
		assertEquals(-1, (int)valHolder.getValue());
		
		// Add an item and assert
		list.add(3);
		assertEquals(0, (int)locHolder.getValue());
		assertEquals(3, (int)valHolder.getValue());
		
		// Add an item and assert
		list.add(6);
		assertEquals(1, (int)locHolder.getValue());
		assertEquals(6, (int)valHolder.getValue());
		
		// Add an item and assert
		list.add(0, 2);
		assertEquals(0, (int)locHolder.getValue());
		assertEquals(2, (int)valHolder.getValue());
	}
	
	public void testRemoveItem() {
		ArrayList<Integer> startItems = new ArrayList<Integer>();
		startItems.add(3);
		startItems.add(6);
		startItems.add(0, 2);
		BindingList<Integer> list = new BindingList<Integer>(startItems);
		
		assertEquals(3, list.size());
		
		list.remove(2);
		
		assertEquals(2, (int)list.get(0));
		assertEquals(3, (int)list.get(1));
		assertEquals(2, list.size());
	}
	
	public void testRemoveItemObserver() {
		// Setup
		ArrayList<Integer> startItems = new ArrayList<Integer>();
		startItems.add(3);
		startItems.add(6);
		startItems.add(0, 2);
		BindingList<Integer> list = new BindingList<Integer>(startItems);
		final Holder<Integer> locHolder = new Holder<Integer>(-1);
		final Holder<Integer> valHolder = new Holder<Integer>(-1);
		list.addObserver(new BindingListObserver<Integer>() {
			@Override
			public void itemAdded(int location, Integer item) {
				fail("Shouldn't be called");
			}

			@Override
			public void itemRemoved(int location, Integer item) {
				locHolder.setValue(location);
				valHolder.setValue(item);
			}

			@Override
			public void itemsCleared() {
				fail("Shouldn't be called");
			}
		});
		
		// Assert setup
		assertEquals(-1, (int)locHolder.getValue());
		assertEquals(-1, (int)valHolder.getValue());
		
		// Remove item and assert
		list.remove(2);
		assertEquals(2, (int)locHolder.getValue());
		assertEquals(6, (int)valHolder.getValue());
		
		// Remove item and assert
		list.remove((Integer)3);
		assertEquals(1, (int)locHolder.getValue());
		assertEquals(3, (int)valHolder.getValue());
	}
	
	public void testRemoveAllOccurrencesWithObserver() {
		// Setup
		ArrayList<Integer> startItems = new ArrayList<Integer>();
		startItems.add(3);
		startItems.add(6);
		startItems.add(2);
		startItems.add(6);
		BindingList<Integer> list = new BindingList<Integer>(startItems);
		final ArrayList<Integer> locHolder = new ArrayList<Integer>();
		final ArrayList<Integer> valHolder = new ArrayList<Integer>();
		list.addObserver(new BindingListObserver<Integer>() {
			@Override
			public void itemAdded(int location, Integer item) {
				fail("Shouldn't be called");
			}

			@Override
			public void itemRemoved(int location, Integer item) {
				locHolder.add(location);
				valHolder.add(item);
			}

			@Override
			public void itemsCleared() {
				fail("Shouldn't be called");
			}
		});
		
		// Assert setup
		assertEquals(0, locHolder.size());
		assertEquals(0, valHolder.size());
		assertEquals(4, list.size());
		assertEquals(6, (int)list.get(1));
		assertEquals(6, (int)list.get(3));
		
		// Remove item and assert
		list.remove(6, true);
		assertEquals(1, (int)locHolder.get(0));
		assertEquals(2, (int)locHolder.get(1));
		assertEquals(6, (int)valHolder.get(0));
		assertEquals(6, (int)valHolder.get(1));
		assertEquals(2, list.size());
		assertEquals(2, (int)list.get(1));
	}
	
	public void testStartItems() {
		ArrayList<Integer> startItems = new ArrayList<Integer>();
		startItems.add(4);
		startItems.add(8);
		
		BindingList<Integer> list = new BindingList<Integer>(startItems);
		
		assertEquals(2, list.size());
		assertEquals(4, (int)list.get(0));
		assertEquals(8, (int)list.get(1));
	}
	
	public void testClearItems() {
		ArrayList<Integer> startItems = new ArrayList<Integer>();
		startItems.add(3);
		startItems.add(6);
		startItems.add(0, 2);
		BindingList<Integer> list = new BindingList<Integer>(startItems);
		
		assertEquals(3, list.size());
		
		list.clear();
		
		assertEquals(0, list.size());
	}
	
	public void testClearItemsObserver() {
		// Setup
		ArrayList<Integer> startItems = new ArrayList<Integer>();
		startItems.add(3);
		startItems.add(6);
		startItems.add(0, 2);
		BindingList<Integer> list = new BindingList<Integer>(startItems);
		final Holder<Boolean> executed = new Holder<Boolean>(false);
		list.addObserver(new BindingListObserver<Integer>() {
			@Override
			public void itemAdded(int location, Integer item) {
				fail("Shouldn't be called");
			}

			@Override
			public void itemRemoved(int location, Integer item) {
				fail("Shouldn't be called");
			}

			@Override
			public void itemsCleared() {
				executed.setValue(true);
			}
		});
		
		assertEquals(3, list.size());
		assertFalse(executed.getValue());
		
		list.clear();
		
		assertEquals(0, list.size());
		assertTrue(executed.getValue());
	}
	
	public void testRemoveObserver() {
		// Setup
		ArrayList<Integer> startItems = new ArrayList<Integer>();
		startItems.add(3);
		startItems.add(6);
		BindingList<Integer> list = new BindingList<Integer>(startItems);
		final Holder<Boolean> executed = new Holder<Boolean>(false);
		BindingListObserver<Integer> observer = new BindingListObserver<Integer>() {
			@Override
			public void itemAdded(int location, Integer item) {
				executed.setValue(true);
			}

			@Override
			public void itemRemoved(int location, Integer item) {
				fail("Shouldn't be called");
			}

			@Override
			public void itemsCleared() {
				fail("Shouldn't be called");
			}
		};
		list.addObserver(observer);
		
		assertFalse(executed.getValue());
		
		list.add(9);
		
		assertTrue(executed.getValue());
		executed.setValue(false);
		assertFalse(executed.getValue());
		
		list.removeObserver(observer);
		
		list.add(12);
		assertFalse(executed.getValue());
	}
	
	public void testSetItem() {
		ArrayList<Integer> startItems = new ArrayList<Integer>();
		startItems.add(3);
		startItems.add(6);
		startItems.add(9);
		BindingList<Integer> list = new BindingList<Integer>(startItems);
		
		assertEquals(3, list.size());
		assertEquals(9, (int)list.get(2));
		assertTrue(list.contains(9));
		
		list.set(2, 12);
		
		assertEquals(3, list.size());
		assertEquals(12, (int)list.get(2));
		assertFalse(list.contains(9));
	}
	
	public void testSetItemObserver() {
		// Setup
		ArrayList<Integer> startItems = new ArrayList<Integer>();
		startItems.add(3);
		startItems.add(6);
		startItems.add(9);
		BindingList<Integer> list = new BindingList<Integer>(startItems);
		final Holder<Integer> addLocHolder = new Holder<Integer>(-1);
		final Holder<Integer> addValHolder = new Holder<Integer>(-1);
		final Holder<Integer> remLocHolder = new Holder<Integer>(-1);
		final Holder<Integer> remValHolder = new Holder<Integer>(-1);
		list.addObserver(new BindingListObserver<Integer>() {
			@Override
			public void itemAdded(int location, Integer item) {
				addLocHolder.setValue(location);
				addValHolder.setValue(item);
			}

			@Override
			public void itemRemoved(int location, Integer item) {
				remLocHolder.setValue(location);
				remValHolder.setValue(item);
			}

			@Override
			public void itemsCleared() {
				fail("Shouldn't be called");
			}
		});
		
		assertEquals(-1, (int)addLocHolder.getValue());
		assertEquals(-1, (int)addValHolder.getValue());
		assertEquals(-1, (int)remLocHolder.getValue());
		assertEquals(-1, (int)remValHolder.getValue());
		assertEquals(3, list.size());
		assertEquals(9, (int)list.get(2));
		assertTrue(list.contains(9));
		
		list.set(2, 12);
		
		assertEquals(2, (int)addLocHolder.getValue());
		assertEquals(12, (int)addValHolder.getValue());
		assertEquals(2, (int)remLocHolder.getValue());
		assertEquals(9, (int)remValHolder.getValue());
		assertEquals(3, list.size());
		assertEquals(12, (int)list.get(2));
		assertFalse(list.contains(9));
	}
}