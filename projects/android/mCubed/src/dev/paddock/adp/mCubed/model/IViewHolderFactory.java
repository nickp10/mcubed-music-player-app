package dev.paddock.adp.mCubed.model;

public abstract class IViewHolderFactory<E> {
	public abstract IViewHolder<E> createViewHolder();
}
