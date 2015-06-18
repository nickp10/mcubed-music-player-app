package dev.paddock.adp.mCubed.lists;

public class BindingListAdapterTag {
	private final int resource;
	private IViewItem<?> viewItem;
	
	public BindingListAdapterTag(int resource) {
		this.resource = resource;
	}
	
	public int getResource() {
		return resource;
	}
	
	public IViewItem<?> getViewItem() {
		return viewItem;
	}
	
	public void setViewItem(IViewItem<?> viewItem) {
		this.viewItem = viewItem;
	}
}
