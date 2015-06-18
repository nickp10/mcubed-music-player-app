package dev.paddock.adp.mCubed;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.fail;

import java.util.Arrays;
import java.util.List;

public class TestUtils {
	public static <T> void assertSequenceEmpty(T[] actual) {
		if (actual == null) {
			assertNull(actual);
		} else {
			assertSequenceEmpty(Arrays.asList(actual));
		}
	}
	
	public static <T> void assertSequenceEmpty(List<T> actual) {
		if (actual == null) {
			assertNull(actual);
		} else {
			assertEquals(0, actual.size());
			for (T item : actual) {
				fail("Item found when no items were expected: " + item);
			}
		}
	}
	
	public static <T> void assertSequenceEquals(T[] expected, T[] actual) {
		if (expected == null || actual == null) {
			assertNull(expected);
			assertNull(actual);
		} else {
			assertSequenceEquals(expected, Arrays.asList(actual));
		}
	}
	
	public static <T> void assertSequenceEquals(T[] expected, List<T> actual) {
		if (expected == null || actual == null) {
			assertNull(expected);
			assertNull(actual);
		} else {
			assertEquals(expected.length, actual.size());
			for (int i = 0; i < expected.length; i++) {
				assertEquals(expected[i], actual.get(i));
			}
		}
	}
}