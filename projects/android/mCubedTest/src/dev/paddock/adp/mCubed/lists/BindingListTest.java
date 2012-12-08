package dev.paddock.adp.mCubed.lists;

import java.util.ArrayList;

import junit.framework.TestCase;
import dev.paddock.adp.mCubed.lists.BindingList.BindingListObserver;
import dev.paddock.adp.mCubed.model.holders.HolderBoolean;
import dev.paddock.adp.mCubed.model.holders.HolderInt;

public class BindingListTest extends TestCase {
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
		final HolderInt locHolder = new HolderInt(-1);
		final HolderInt valHolder = new HolderInt(-1);
		list.addObserver(new BindingListObserver<Integer>() {
			@Override
			public void itemAdded(BindingList<Integer> list, int location, Integer item) {
				locHolder.value = location;
				valHolder.value = item;
			}

			@Override
			public void itemRemoved(BindingList<Integer> list, int location, Integer item) {
				fail("Shouldn't be called");
			}

			@Override
			public void itemsCleared(BindingList<Integer> list) {
				fail("Shouldn't be called");
			}
			
			@Override
			public void transactionCompleted(BindingList<Integer> list, boolean hasChanges) {
				fail("Shouldn't be called");
			}
		});
		
		// Assert setup
		assertEquals(-1, locHolder.value);
		assertEquals(-1, valHolder.value);
		
		// Add an item and assert
		list.add(3);
		assertEquals(0, locHolder.value);
		assertEquals(3, valHolder.value);
		
		// Add an item and assert
		list.add(6);
		assertEquals(1, locHolder.value);
		assertEquals(6, valHolder.value);
		
		// Add an item and assert
		list.add(0, 2);
		assertEquals(0, locHolder.value);
		assertEquals(2, valHolder.value);
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
		final HolderInt locHolder = new HolderInt(-1);
		final HolderInt valHolder = new HolderInt(-1);
		list.addObserver(new BindingListObserver<Integer>() {
			@Override
			public void itemAdded(BindingList<Integer> list, int location, Integer item) {
				fail("Shouldn't be called");
			}

			@Override
			public void itemRemoved(BindingList<Integer> list, int location, Integer item) {
				locHolder.value = location;
				valHolder.value = item;
			}

			@Override
			public void itemsCleared(BindingList<Integer> list) {
				fail("Shouldn't be called");
			}
			
			@Override
			public void transactionCompleted(BindingList<Integer> list, boolean hasChanges) {
				fail("Shouldn't be called");
			}
		});
		
		// Assert setup
		assertEquals(-1, locHolder.value);
		assertEquals(-1, valHolder.value);
		
		// Remove item and assert
		list.remove(2);
		assertEquals(2, locHolder.value);
		assertEquals(6, valHolder.value);
		
		// Remove item and assert
		list.remove((Integer)3);
		assertEquals(1, locHolder.value);
		assertEquals(3, valHolder.value);
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
			public void itemAdded(BindingList<Integer> list, int location, Integer item) {
				fail("Shouldn't be called");
			}

			@Override
			public void itemRemoved(BindingList<Integer> list, int location, Integer item) {
				locHolder.add(location);
				valHolder.add(item);
			}

			@Override
			public void itemsCleared(BindingList<Integer> list) {
				fail("Shouldn't be called");
			}
			
			@Override
			public void transactionCompleted(BindingList<Integer> list, boolean hasChanges) {
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
		final HolderBoolean executed = new HolderBoolean(false);
		list.addObserver(new BindingListObserver<Integer>() {
			@Override
			public void itemAdded(BindingList<Integer> list, int location, Integer item) {
				fail("Shouldn't be called");
			}

			@Override
			public void itemRemoved(BindingList<Integer> list, int location, Integer item) {
				fail("Shouldn't be called");
			}

			@Override
			public void itemsCleared(BindingList<Integer> list) {
				executed.value = true;
			}
			
			@Override
			public void transactionCompleted(BindingList<Integer> list, boolean hasChanges) {
				fail("Shouldn't be called");
			}
		});
		
		assertEquals(3, list.size());
		assertFalse(executed.value);
		
		list.clear();
		
		assertEquals(0, list.size());
		assertTrue(executed.value);
	}
	
	public void testRemoveObserver() {
		// Setup
		ArrayList<Integer> startItems = new ArrayList<Integer>();
		startItems.add(3);
		startItems.add(6);
		BindingList<Integer> list = new BindingList<Integer>(startItems);
		final HolderBoolean executed = new HolderBoolean(false);
		BindingListObserver<Integer> observer = new BindingListObserver<Integer>() {
			@Override
			public void itemAdded(BindingList<Integer> list, int location, Integer item) {
				executed.value = true;
			}

			@Override
			public void itemRemoved(BindingList<Integer> list, int location, Integer item) {
				fail("Shouldn't be called");
			}

			@Override
			public void itemsCleared(BindingList<Integer> list) {
				fail("Shouldn't be called");
			}
			
			@Override
			public void transactionCompleted(BindingList<Integer> list, boolean hasChanges) {
				fail("Shouldn't be called");
			}
		};
		list.addObserver(observer);
		
		assertFalse(executed.value);
		
		list.add(9);
		
		assertTrue(executed.value);
		executed.value = false;
		assertFalse(executed.value);
		
		list.removeObserver(observer);
		
		list.add(12);
		assertFalse(executed.value);
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
		final HolderInt addLocHolder = new HolderInt(-1);
		final HolderInt addValHolder = new HolderInt(-1);
		final HolderInt remLocHolder = new HolderInt(-1);
		final HolderInt remValHolder = new HolderInt(-1);
		list.addObserver(new BindingListObserver<Integer>() {
			@Override
			public void itemAdded(BindingList<Integer> list, int location, Integer item) {
				addLocHolder.value = location;
				addValHolder.value = item;
			}

			@Override
			public void itemRemoved(BindingList<Integer> list, int location, Integer item) {
				remLocHolder.value = location;
				remValHolder.value = item;
			}

			@Override
			public void itemsCleared(BindingList<Integer> list) {
				fail("Shouldn't be called");
			}
			
			@Override
			public void transactionCompleted(BindingList<Integer> list, boolean hasChanges) {
				fail("Shouldn't be called");
			}
		});
		
		assertEquals(-1, addLocHolder.value);
		assertEquals(-1, addValHolder.value);
		assertEquals(-1, remLocHolder.value);
		assertEquals(-1, remValHolder.value);
		assertEquals(3, list.size());
		assertEquals(9, (int)list.get(2));
		assertTrue(list.contains(9));
		
		list.set(2, 12);
		
		assertEquals(2, addLocHolder.value);
		assertEquals(12, addValHolder.value);
		assertEquals(2, remLocHolder.value);
		assertEquals(9, remValHolder.value);
		assertEquals(3, list.size());
		assertEquals(12, (int)list.get(2));
		assertFalse(list.contains(9));
	}
}