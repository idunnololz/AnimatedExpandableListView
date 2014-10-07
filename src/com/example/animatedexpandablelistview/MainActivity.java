package com.example.animatedexpandablelistview;

import java.lang.Long;
import java.lang.Override;
import java.util.ArrayList;
import java.util.List;

import com.idunnololz.widgets.AnimatedExpandableListView;
import com.idunnololz.widgets.AnimatedExpandableListView.AnimatedExpandableListAdapter;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.os.Debug;
import android.os.SystemClock;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.TextView;

/**
 * This is an example usage of the AnimatedExpandableListView class.
 * 
 * It is an activity that holds a listview which is populated with 100 groups
 * where each group has from 1 to 100 children (so the first group will have one
 * child, the second will have two children and so on...).
 */
public class MainActivity extends Activity {
    private AnimatedExpandableListView listView;
    private ExampleAdapter adapter;

    private static final String TAG = "idun";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        List<GroupItem> items = new ArrayList<GroupItem>();
        
        // Populate our list with groups and it's children
        for(int i = 1; i < 100; i++) {
            GroupItem item = new GroupItem();
            
            item.title = "Group " + i;
            
            for(int j = 0; j < i; j++) {
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
        
        // In order to show animations, we need to use a custom click handler
        // for our ExpandableListView.
        listView.setOnGroupClickListener(new OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                // We call collapseGroupWithAnimation(int) and
                // expandGroupWithAnimation(int) to animate group 
                // expansion/collapse.
                if (listView.isGroupExpanded(groupPosition)) {
                    listView.collapseGroupWithAnimation(groupPosition);
                } else {
                    String traceName = "method_trace_idun_" + SystemClock.uptimeMillis();
//                    Debug.startMethodTracing(traceName);
                    listView.expandGroupWithAnimation(groupPosition);
                }
                return true;
            }
            
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
//        Debug.stopMethodTracing();
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
        int type;
    }
    
    private static class GroupHolder {
        TextView title;
    }
    
    /**
     * Adapter for our list of {@link GroupItem}s.
     */
    private class ExampleAdapter extends AnimatedExpandableListAdapter {
        private LayoutInflater inflater;
        
        private List<GroupItem> items;

        private LruCache<Long, View> vcache;
        
        public ExampleAdapter(Context context) {
            inflater = LayoutInflater.from(context);
            vcache = new LruCache<Long, View>(20);
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
                // when convertView is not given, we try using view from cache.
                // Note: check the view type to ensure it's really resuable.
                Long key = ((long)groupPosition << 32) + childPosition;
                View view = vcache.get(key);
                if (view != null && view.getTag() != null) {
                    holder = (ChildHolder) view.getTag();
                    if (holder.type == getRealChildType(groupPosition, childPosition) ) {
                        convertView = view;
                        Log.d(TAG, "getRealChildView: " + groupPosition + " " + childPosition + " " + "Got Cache!");
                    }
                } else {
                    holder = new ChildHolder();
                    convertView = inflater.inflate(R.layout.list_item, parent, false);
                    holder.title = (TextView) convertView.findViewById(R.id.textTitle);
                    holder.hint = (TextView) convertView.findViewById(R.id.textHint);
                    convertView.setTag(holder);
                    vcache.put(key, convertView);
                    Log.d(TAG, "getRealChildView: " + groupPosition + " " + childPosition + " " + "No Cache!");
                }
            } else {
                Log.d(TAG, "getRealChildView: " + groupPosition + " " + childPosition + " " + convertView);
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
