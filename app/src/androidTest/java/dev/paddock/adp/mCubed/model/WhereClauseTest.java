package dev.paddock.adp.mCubed.model;

import android.test.AndroidTestCase;
import dev.paddock.adp.mCubed.TestUtils;

public class WhereClauseTest extends AndroidTestCase {
	public void testCreate() {
		WhereClause a = WhereClause.create("a");
		assertEquals("a", a.getSelection());
	}
	
	public void testOr() {
		WhereClause a = WhereClause.create("a");
		WhereClause b = WhereClause.create("b");
		WhereClause or = WhereClause.or(a, b);
		assertEquals("(a OR b)", or.getSelection());
	}
	
	public void testAnd() {
		WhereClause a = WhereClause.create("a");
		WhereClause b = WhereClause.create("b");
		WhereClause and = WhereClause.and(a, b);
		assertEquals("(a AND b)", and.getSelection());
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
		assertEquals("((a OR b) AND (c OR d))", and.getSelection());
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
		assertEquals("((a AND b) OR (c AND d))", or.getSelection());
	}
	
	public void testComplexClause() {
		// Create threesome clause
		WhereClause a = WhereClause.create("a").or("b").and("c");
		assertEquals("((a OR b) AND c)", a.getSelection());
		
		// Create foursome clause
		WhereClause b = WhereClause.create("d").and("e").or("f").and("g");
		assertEquals("(((d AND e) OR f) AND g)", b.getSelection());
		
		// Or them together
		WhereClause orResult = a.or(b);
		assertEquals("(((a OR b) AND c) OR (((d AND e) OR f) AND g))", orResult.getSelection());
		
		// And them together
		WhereClause andResult = b.and(a);
		assertEquals("((((d AND e) OR f) AND g) AND ((a OR b) AND c))", andResult.getSelection());
	}
	
	public void testNoSelectionArgs() {
		// Create threesome clause
		WhereClause a = WhereClause.create("a").and("b").and("c");
		assertEquals("((a AND b) AND c)", a.getSelection());
		
		// Assert the selection args come out right
		TestUtils.assertSequenceEmpty(a.getSelectionArgs());
	}
	
	public void testSimpleSelectionArgs() {
		// Create threesome clause
		WhereClause a = WhereClause.create("a = ?", "aVal").or("b = ?", "bVal").and("c = ?", "cVal");
		assertEquals("((a = ? OR b = ?) AND c = ?)", a.getSelection());
		
		// Assert the selection args come out right
		TestUtils.assertSequenceEquals(new String[] { "aVal", "bVal", "cVal" }, a.getSelectionArgs());
	}
	
	public void testComplexSelectionArgs() {
		// Create threesome clause
		WhereClause a = WhereClause.create("a = ?", "aVal").or("b = ?", "bVal").and("c = ?", "cVal");
		assertEquals("((a = ? OR b = ?) AND c = ?)", a.getSelection());
		
		// Create foursome clause
		WhereClause b = WhereClause.create("d = ?", "dVal").and("e = ?", "eVal").or("f = ?", "fVal").and("g = ?", "gVal");
		assertEquals("(((d = ? AND e = ?) OR f = ?) AND g = ?)", b.getSelection());
		
		// And them together
		WhereClause andResult = a.and(b);
		assertEquals("(((a = ? OR b = ?) AND c = ?) AND (((d = ? AND e = ?) OR f = ?) AND g = ?))", andResult.getSelection());
		
		// Assert the selection args come out right
		TestUtils.assertSequenceEquals(new String[] { "aVal", "bVal", "cVal", "dVal", "eVal", "fVal", "gVal" }, andResult.getSelectionArgs());
	}
}