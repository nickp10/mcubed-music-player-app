package dev.paddock.adp.mCubed.model;

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
		protected void toSQL(StringBuilder builder) {
			builder.append("(");
			leftClause.toSQL(builder);
			builder.append(" ");
			builder.append(operator);
			builder.append(" ");
			rightClause.toSQL(builder);
			builder.append(")");
		}
	}
	
	private static class ConditionWhereClause extends WhereClause {
		private String condition;
		
		public ConditionWhereClause(String condition) {
			this.condition = condition;
		}
		
		@Override
		protected void toSQL(StringBuilder builder) {
			builder.append(condition);
		}
	}
	
	public final String toSQL() {
		StringBuilder builder = new StringBuilder();
		toSQL(builder);
		return builder.toString();
	}
	
	protected abstract void toSQL(StringBuilder builder);
	
	public static WhereClause create(String condition) {
		return new ConditionWhereClause(condition);
	}
	
	public static WhereClause or(String leftCondition, String rightCondition) {
		return or(create(leftCondition), create(rightCondition));
	}
	
	public static WhereClause or(WhereClause leftClause, WhereClause rightClause) {
		return new AggregateWhereClause(leftClause, Operator.OR, rightClause);
	}
	
	public static WhereClause and(String leftCondition, String rightCondition) {
		return and(create(leftCondition), create(rightCondition));
	}
	
	public static WhereClause and(WhereClause leftClause, WhereClause rightClause) {
		return new AggregateWhereClause(leftClause, Operator.AND, rightClause);
	}
	
	public WhereClause or(String condition) {
		return or(create(condition));
	}
	
	public WhereClause or(WhereClause clause) {
		return or(this, clause);
	}
	
	public WhereClause and(String condition) {
		return and(create(condition));
	}
	
	public WhereClause and(WhereClause clause) {
		return and(this, clause);
	}
}