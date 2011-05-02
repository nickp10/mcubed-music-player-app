package dev.paddock.adp.mCubed.model;

public class Holder<T> {
	private T value;
	
	public Holder() { }
	
	public Holder(T value) {
		setValue(value);
	}
	
	public T getValue() {
		return value;
	}
	public void setValue(T value) {
		this.value = value;
	}
}