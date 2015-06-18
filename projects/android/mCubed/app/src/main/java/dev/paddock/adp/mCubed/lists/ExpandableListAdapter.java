package dev.paddock.adp.mCubed.lists;

import java.util.List;
import java.util.Map.Entry;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import dev.paddock.adp.mCubed.utilities.App;

public class ExpandableListAdapter<E> extends BaseExpandableListAdapter {
	private List<Entry<String, E>> items;
	private Context context;
	private int groupViewResource, childViewResource;
	
	public ExpandableListAdapter(Context context, int groupViewResource, int childViewResource, List<Entry<String, E>> items) {
		this.context = context;
		this.groupViewResource = groupViewResource;
		this.childViewResource = childViewResource;
		this.items = items;
	}
	
	public Context getContext() {
		return context;
	}
	
	public int getGroupViewResource() {
		return groupViewResource;
	}
	
	public int getChildViewResource() {
		return childViewResource;
	}
	
	@Override
	public int getGroupCount() {
		return items.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		E child = items.get(groupPosition).getValue();
		if (child instanceof List<?>) {
			return ((List<?>)child).size();
		}
		return 1;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return items.get(groupPosition).getKey();
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		E child = items.get(groupPosition).getValue();
		if (child instanceof List<?>) {
			return ((List<?>)child).get(childPosition);
		}
		return child;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}
	
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		return getView(convertView, parent, getGroupViewResource(), getGroup(groupPosition));
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		return getView(convertView, parent, getChildViewResource(), getChild(groupPosition, childPosition));
	}
	
	protected final View getView(View convertView, ViewGroup parent, int resource, Object item) {
		// Inflate the view first
		if (convertView == null) {
			LayoutInflater inflater = App.getSystemService(LayoutInflater.class, getContext(), Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(resource, parent, false);
		}
		
		// Update the view second
		if (convertView != null && convertView instanceof TextView) {
			TextView textView = (TextView)convertView;
	        if (item == null) {
	        	textView.setText("");
	        } else if (item instanceof CharSequence) {
	        	textView.setText((CharSequence)item);
	        } else {
	        	textView.setText(item.toString());
	        }
		}
		return convertView;
	}
}
