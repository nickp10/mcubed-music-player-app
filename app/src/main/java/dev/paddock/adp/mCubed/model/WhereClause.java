package dev.paddock.adp.mCubed.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class WhereClause {
	public static enum Operator {
		OR, AND
	}
	
	private static class AggregateWhereClause extends WhereClause {
		private Operator operator;
		private WhereClause leftClause, rightClause;
		
		public AggregateWhereClause(WhereClause leftClause, Operator operator, WhereClause rightClause) {
			this.leftClause = leftClause;
			this.rightClause = rightClause;
			this.operator = operator;
		}
		
		@Override
		protected void getSelection(StringBuilder builder) {
			builder.append("(");
			leftClause.getSelection(builder);
			builder.append(" ");
			builder.append(operator);
			builder.append(" ");
			rightClause.getSelection(builder);
			builder.append(")");
		}
		
		@Override
		protected void getSelectionArgs(List<String> args) {
			leftClause.getSelectionArgs(args);
			rightClause.getSelectionArgs(args);
		}
	}
	
	private static class ConditionWhereClause extends WhereClause {
		private String condition;
		private String[] selectionArgs;
		
		public ConditionWhereClause(String condition, String... selectionArgs) {
			this.condition = condition;
			this.selectionArgs = selectionArgs;
		}
		
		@Override
		protected void getSelection(StringBuilder builder) {
			builder.append(condition);
		}
		
		@Override
		protected void getSelectionArgs(List<String> args) {
			if (selectionArgs != null) {
				args.addAll(Arrays.asList(selectionArgs));
			}
		}
	}
	
	public final String getSelection() {
		StringBuilder builder = new StringBuilder();
		getSelection(builder);
		return builder.toString();
	}
	
	public final String[] getSelectionArgs() {
		List<String> args = new ArrayList<String>();
		getSelectionArgs(args);
		return args.toArray(new String[0]);
	}
	
	protected abstract void getSelection(StringBuilder builder);
	protected abstract void getSelectionArgs(List<String> args);
	
	public static WhereClause create(String condition, String... selectionArgs) {
		return new ConditionWhereClause(condition, selectionArgs);
	}
	
	public static WhereClause or(WhereClause leftClause, WhereClause rightClause) {
		return new AggregateWhereClause(leftClause, Operator.OR, rightClause);
	}
	
	public static WhereClause and(WhereClause leftClause, WhereClause rightClause) {
		return new AggregateWhereClause(leftClause, Operator.AND, rightClause);
	}
	
	public WhereClause or(String condition, String... selectionArgs) {
		return or(create(condition, selectionArgs));
	}
	
	public WhereClause or(WhereClause clause) {
		return or(this, clause);
	}
	
	public WhereClause and(String condition, String... selectionArgs) {
		return and(create(condition, selectionArgs));
	}
	
	public WhereClause and(WhereClause clause) {
		return and(this, clause);
	}
}