package dev.paddock.adp.mCubed.lists;

import java.util.Arrays;
import java.util.Comparator;

import dev.paddock.adp.mCubed.TestUtils;

import junit.framework.Assert;
import junit.framework.TestCase;

public class SortedListTest extends TestCase {
	private static final Comparator<String> comparer = new Comparator<String>() {
		@Override
		public int compare(String str1, String str2) {
			if (str1 == null) {
				return str2 == null ? 0 : -1;
			} else if (str2 == null) {
				return 1;
			} else {
				return str1.compareToIgnoreCase(str2);
			}
		}
	};
	
	public void testAddItemsIteratorIsSorted() {
		SortedList<String> sortedList = new SortedList<String>(comparer);
		sortedList.add("Hank");
		sortedList.add("Helga");
		sortedList.add("Flutie");
		sortedList.add("Adam");
		sortedList.add("Manny");
		sortedList.add("Xavier");
		sortedList.add("Joe");
		sortedList.add("Billy");
		
		TestUtils.assertSequenceEquals(new String[] {
			"Adam", "Billy", "Flutie", "Hank", "Helga", "Joe", "Manny", "Xavier"
		}, sortedList);
	}
	
	public void testRemoveItemsIteratorIsSorted() {
		SortedList<String> sortedList = new SortedList<String>(comparer);
		sortedList.add("Helga");
		sortedList.add("Lauren");
		sortedList.add("Hank");
		sortedList.add("Sandra");
		sortedList.add("Flutie");
		sortedList.add("Adam");
		sortedList.add("Manny");
		sortedList.add("Xavier");
		sortedList.add("Joe");
		sortedList.add("Billy");
		sortedList.add("Raphael");
		
		sortedList.remove("Sandra");
		sortedList.remove("Helga");
		sortedList.remove("Raphael");
		
		TestUtils.assertSequenceEquals(new String[] {
			"Adam", "Billy", "Flutie", "Hank", "Joe", "Lauren", "Manny", "Xavier"
		}, sortedList);
	}
	
	public void testRemoveAllItems() {
		SortedList<String> sortedList = new SortedList<String>(comparer);
		sortedList.add("Lauren");
		sortedList.add("Hank");
		sortedList.add("Helga");
		sortedList.add("Sandra");
		sortedList.add("Flutie");
		sortedList.add("Adam");
		sortedList.add("Manny");
		sortedList.add("Xavier");
		sortedList.add("Joe");
		sortedList.add("Billy");
		sortedList.add("Raphael");
		
		sortedList.removeAll(Arrays.asList("Adam", "Billy", "Flutie", "Hank", "Joe", "Lauren", "Manny", "Xavier", "Sandra", "Helga", "Raphael"));
		
		TestUtils.assertSequenceEmpty(sortedList);
	}
	
	public void testClearItems() {
		SortedList<String> sortedList = new SortedList<String>(comparer);
		sortedList.add("Lauren");
		sortedList.add("Hank");
		sortedList.add("Helga");
		sortedList.add("Sandra");
		sortedList.add("Flutie");
		sortedList.add("Adam");
		sortedList.add("Manny");
		sortedList.add("Xavier");
		sortedList.add("Joe");
		sortedList.add("Billy");
		sortedList.add("Raphael");
		
		sortedList.clear();
		
		TestUtils.assertSequenceEmpty(sortedList);
	}
	
	public void testToArrayFillsExistingArray() {
		SortedList<String> sortedList = new SortedList<String>(comparer);
		sortedList.addAll(Arrays.asList("Billy", "Xavier", "Hank", "Adam", "Helga", "Joe", "Flutie", "Manny"));
		
		String[] items = new String[8];
		String[] array = sortedList.toArray(items);
		
		Assert.assertEquals(items, array);
		TestUtils.assertSequenceEquals(new String[] {
			"Adam", "Billy", "Flutie", "Hank", "Helga", "Joe", "Manny", "Xavier"
		}, array);
	}
	
	public void testToArrayFillsExistingArrayAndClearsOutExcess() {
		SortedList<String> sortedList = new SortedList<String>(comparer);
		sortedList.addAll(Arrays.asList("Billy", "Xavier", "Hank", "Adam", "Helga", "Joe", "Flutie", "Manny"));
		
		String[] items = new String[10];
		items[8] = "Random";
		items[9] = "Gone";
		String[] array = sortedList.toArray(items);
		
		Assert.assertEquals(items, array);
		TestUtils.assertSequenceEquals(new String[] {
			"Adam", "Billy", "Flutie", "Hank", "Helga", "Joe", "Manny", "Xavier", null, null
		}, array);
	}
	
	public void testToArrayCreatesNewArray() {
		SortedList<String> sortedList = new SortedList<String>(comparer);
		sortedList.addAll(Arrays.asList("Billy", "Xavier", "Hank", "Adam", "Helga", "Joe", "Flutie", "Manny"));
		
		String[] items = new String[0];
		String[] array = sortedList.toArray(items);
		
		Assert.assertNotSame(items, array);
		TestUtils.assertSequenceEquals(new String[] {
			"Adam", "Billy", "Flutie", "Hank", "Helga", "Joe", "Manny", "Xavier"
		}, array);
	}
}