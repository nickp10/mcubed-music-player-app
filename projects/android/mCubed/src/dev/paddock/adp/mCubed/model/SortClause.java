package dev.paddock.adp.mCubed.model;

public class SortClause {
	public static enum Direction {
		ASC, DESC
	}
	
	private Direction direction;
	private String field;
	private SortClause thenBy;
	
	private SortClause(String field) {
		this(field, null);
	}
	
	private SortClause(String field, Direction direction) {
		this.field = field;
		this.direction = direction == null ? Direction.ASC : direction;
	}
	
	public String toSQL() {
		StringBuilder builder = new StringBuilder();
		toSQL(builder);
		return builder.toString();
	}
	
	private void toSQL(StringBuilder builder) {
		builder.append(field);
		builder.append(" ");
		builder.append(direction);
		if (thenBy != null) {
			builder.append(", ");
			thenBy.toSQL(builder);
		}
	}
	
	public static SortClause create(String field) {
		return new SortClause(field);
	}
	
	public static SortClause create(String field, Direction direction) {
		return new SortClause(field, direction);
	}
	
	public SortClause thenBy(SortClause clause) {
		this.thenBy = clause;
		return thenBy;
	}
	
	public SortClause thenBy(String field) {
		return thenBy(create(field));
	}
	
	public SortClause thenBy(String field, Direction direction) {
		return thenBy(create(field, direction));
	}
}