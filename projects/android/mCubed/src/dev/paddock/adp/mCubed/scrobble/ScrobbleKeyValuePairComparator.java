package dev.paddock.adp.mCubed.scrobble;

import java.util.Comparator;

public class ScrobbleKeyValuePairComparator implements Comparator<ScrobbleKeyValuePair> {

	@Override
	public int compare(ScrobbleKeyValuePair pair1, ScrobbleKeyValuePair pair2) {
		if (pair1 == null) {
			if (pair2 == null) {
				return 0;
			}
			return -1;
		} else if (pair2 == null) {
			return 1;
		} else {
			String key1 = pair1.getKey();
			String key2 = pair2.getKey();
			return key1.compareToIgnoreCase(key2);
		}
	}
}
