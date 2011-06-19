package dev.paddock.adp.mCubed.model;

import android.test.AndroidTestCase;

public class WhereClauseTest extends AndroidTestCase {
	public void testCreate() {
		WhereClause a = WhereClause.create("a");
		assertEquals("a", a.toSQL());
	}
	
	public void testOr() {
		WhereClause a = WhereClause.create("a");
		WhereClause b = WhereClause.create("b");
		WhereClause or = WhereClause.or(a, b);
		assertEquals("(a OR b)", or.toSQL());
	}
	
	public void testAnd() {
		WhereClause a = WhereClause.create("a");
		WhereClause b = WhereClause.create("b");
		WhereClause and = WhereClause.and(a, b);
		assertEquals("(a AND b)", and.toSQL());
	}
	
	public void testOrsAnded() {
		// Create first or
		WhereClause a = WhereClause.create("a");
		WhereClause b = WhereClause.create("b");
		WhereClause orA = WhereClause.or(a, b);
		
		// Create second or
		WhereClause c = WhereClause.create("c");
		WhereClause d = WhereClause.create("d");
		WhereClause orB = WhereClause.or(c, d);
		
		// And the ors
		WhereClause and = WhereClause.and(orA, orB);
		assertEquals("((a OR b) AND (c OR d))", and.toSQL());
	}
	
	public void testAndsOred() {
		// Create first and
		WhereClause a = WhereClause.create("a");
		WhereClause b = WhereClause.create("b");
		WhereClause andA = WhereClause.and(a, b);
		
		// Create second and
		WhereClause c = WhereClause.create("c");
		WhereClause d = WhereClause.create("d");
		WhereClause andB = WhereClause.and(c, d);
		
		// Or the ands
		WhereClause or = WhereClause.or(andA, andB);
		assertEquals("((a AND b) OR (c AND d))", or.toSQL());
	}
	
	public void testComplexClause() {
		// Create threesome clause
		WhereClause a = WhereClause.create("a").or("b").and("c");
		assertEquals("((a OR b) AND c)", a.toSQL());
		
		// Create foursome clause
		WhereClause b = WhereClause.create("d").and("e").or("f").and("g");
		assertEquals("(((d AND e) OR f) AND g)", b.toSQL());
		
		// Or them together
		WhereClause orResult = a.or(b);
		assertEquals("(((a OR b) AND c) OR (((d AND e) OR f) AND g))", orResult.toSQL());
		
		// And them together
		WhereClause andResult = b.and(a);
		assertEquals("((((d AND e) OR f) AND g) AND ((a OR b) AND c))", andResult.toSQL());
	}
}