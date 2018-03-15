package dev.paddock.adp.mCubed.utilities;

public class Delegate {
	public static interface Action<E> {
		void act(E object);
	}
	
	public static interface Func<E, F> {
		F act(E object);
	}
}