package com.example.animatedexpandablelistview;

import java.util.ArrayList;
import java.util.List;

import com.idunnololz.widgets.AnimatedExpandableListView;
import com.idunnololz.widgets.AnimatedExpandableListView.AnimatedExpandableListAdapter;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.TextView;

public class MainActivity extends Activity {
	private AnimatedExpandableListView listView;
	private ExampleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        List<GroupItem> items = new ArrayList<GroupItem>();
        
        for(int i = 0; i < 100; i++) {
        	GroupItem item = new GroupItem();
        	
        	item.title = "Group " + i;
        	
        	for(int j = 0; j < 100; j++) {
        		ChildItem child = new ChildItem();
        		child.title = "Awesome item " + j;
        		child.hint = "Too awesome";
        		
        		item.items.add(child);
        	}
        	
        	items.add(item);
        }
        
        adapter = new ExampleAdapter(this);
        adapter.setData(items);
        
        listView = (AnimatedExpandableListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        
        listView.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
				if (listView.isGroupExpanded(groupPosition)) {
					listView.collapseGroupWithAnimation(groupPosition);
				} else {
					listView.expandGroupWithAnimation(groupPosition);
				}
				return true;
			}
        	
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    private static class GroupItem {
    	String title;
    	List<ChildItem> items = new ArrayList<ChildItem>();
    }
    
    private static class ChildItem {
    	String title;
    	String hint;
    }
    
    private static class ChildHolder {
    	TextView title;
    	TextView hint;
    }
    
    private static class GroupHolder {
    	TextView title;
    }
    
    private class ExampleAdapter extends AnimatedExpandableListAdapter {
    	private LayoutInflater inflater;
    	
    	private List<GroupItem> items;
    	
    	public ExampleAdapter(Context context) {
    		 inflater = LayoutInflater.from(context);
    	}

		public void setData(List<GroupItem> items) {
			this.items = items;
		}

		@Override
		public ChildItem getChild(int groupPosition, int childPosition) {
			return items.get(groupPosition).items.get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public View getRealChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			ChildHolder holder;
			ChildItem item = getChild(groupPosition, childPosition);
			if (convertView == null) {
				holder = new ChildHolder();
				convertView = inflater.inflate(R.layout.list_item, parent, false);
				holder.title = (TextView) convertView.findViewById(R.id.textTitle);
				holder.hint = (TextView) convertView.findViewById(R.id.textHint);
				convertView.setTag(holder);
			} else {
				holder = (ChildHolder) convertView.getTag();
			}
			
			holder.title.setText(item.title);
			holder.hint.setText(item.hint);
			
			return convertView;
		}

		@Override
		public int getRealChildrenCount(int groupPosition) {
			return items.get(groupPosition).items.size();
		}

		@Override
		public GroupItem getGroup(int groupPosition) {
			return items.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return items.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			GroupHolder holder;
			GroupItem item = getGroup(groupPosition);
			if (convertView == null) {
				holder = new GroupHolder();
				convertView = inflater.inflate(R.layout.group_item, parent, false);
				holder.title = (TextView) convertView.findViewById(R.id.textTitle);
				convertView.setTag(holder);
			} else {
				holder = (GroupHolder) convertView.getTag();
			}
			
			holder.title.setText(item.title);
			
			return convertView;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public boolean isChildSelectable(int arg0, int arg1) {
			return true;
		}
    	
    }
    
}
