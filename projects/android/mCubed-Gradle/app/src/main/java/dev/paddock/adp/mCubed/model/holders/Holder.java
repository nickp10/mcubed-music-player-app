package dev.paddock.adp.mCubed.model.holders;

public class Holder<T> {
	public T value;
	
	public Holder() { }
	
	public Holder(T value) {
		this.value = value;
	}
}