package dev.paddock.adp.mCubed.model;

import dev.paddock.adp.mCubed.model.SortClause.Direction;
import android.test.AndroidTestCase;

public class SortClauseTest extends AndroidTestCase {
	public void testCreateNoDirection() {
		SortClause clause = SortClause.create("field");
		assertEquals("field ASC", clause.toSQL());
	}
	
	public void testCreateASCDirection() {
		SortClause clause = SortClause.create("field", Direction.ASC);
		assertEquals("field ASC", clause.toSQL());
	}
	
	public void testCreateDESCDirection() {
		SortClause clause = SortClause.create("field", Direction.DESC);
		assertEquals("field DESC", clause.toSQL());
	}
	
	public void testThenByOne() {
		SortClause clause = SortClause.create("field");
		clause.thenBy("second");
		assertEquals("field ASC, second ASC", clause.toSQL());
	}
	
	public void testThenByTwo() {
		SortClause clause = SortClause.create("field", Direction.DESC);
		SortClause second = clause.thenBy("second", Direction.ASC);
		second.thenBy("third", Direction.DESC);
		assertEquals("field DESC, second ASC, third DESC", clause.toSQL());
	}
}