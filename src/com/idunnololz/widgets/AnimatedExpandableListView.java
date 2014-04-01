package com.idunnololz.widgets;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.view.animation.Transformation;

public class AnimatedExpandableListView extends ExpandableListView {
    private static final int ANIMATION_DURATION = 300;
    
    private AnimatedExpandableListAdapter adapter;

    public AnimatedExpandableListView(Context context) {
        super(context);
    }
    
    public AnimatedExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public AnimatedExpandableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    public void setAdapter(ExpandableListAdapter adapter) {
        super.setAdapter(adapter);
        if(adapter instanceof AnimatedExpandableListAdapter) {
            this.adapter = (AnimatedExpandableListAdapter) adapter;
            this.adapter.setParent(this);
        } else {
            throw new ClassCastException(adapter.toString() + " must implement AnimatedExpandableListAdapter");
        }
    }
    
    public boolean expandGroupWithAnimation(int groupPos) {
        int childPos = getPackedPositionChild(getExpandableListPosition(getFirstVisiblePosition()));
        childPos = childPos == -1 ? 0 : childPos;
        adapter.startExpandAnimation(groupPos, childPos);
        return expandGroup(groupPos);
    }

    public boolean collapseGroupWithAnimation() {
        return true;
    }
    
    public boolean collapseGroupWithAnimation(int groupPos) {
        int childPos = getPackedPositionChild(getExpandableListPosition(getFirstVisiblePosition()));
        childPos = childPos == -1 ? 0 : childPos;
        adapter.startCollapseAnimation(groupPos, childPos);
        adapter.notifyDataSetChanged();
        return isGroupExpanded(groupPos);
    }
    
    private int getAnimationDuration() {
        return ANIMATION_DURATION;
    }
    
    private static class GroupInfo {
        boolean animating = false;
        boolean expanding = false;
        int firstChildPosition;
        int dummyHeight = 0;
    }
    
    public static abstract class AnimatedExpandableListAdapter extends BaseExpandableListAdapter {
        private SparseArray<GroupInfo> groupInfo = new SparseArray<GroupInfo>();
        private AnimatedExpandableListView parent;
        
        public void setParent(AnimatedExpandableListView parent) {
            this.parent = parent;
        }
        
        public int getRealChildType(int groupPosition, int childPosition) {
            return 0;
        }
        
        public int getRealChildTypeCount() {
            return 1;
        }
        
        public abstract View getRealChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent);
        public abstract int getRealChildrenCount(int groupPosition);
        
        public GroupInfo getGroupInfo(int groupPosition) {
            GroupInfo info = groupInfo.get(groupPosition);
            if (info == null) {
                info = new GroupInfo();
                groupInfo.put(groupPosition, info);
            }
            return info;
        }
        
        public void startExpandAnimation(int groupPosition, int firstChildPosition) {
            GroupInfo info = getGroupInfo(groupPosition);
            info.animating = true;
            info.firstChildPosition = firstChildPosition;
            info.expanding = true;
        }
        
        public void startCollapseAnimation(int groupPosition, int firstChildPosition) {
            GroupInfo info = getGroupInfo(groupPosition);
            info.animating = true;
            info.firstChildPosition = firstChildPosition;
            info.expanding = false;
        }
        
        public void stopAnimation(int groupPosition) {
            GroupInfo info = getGroupInfo(groupPosition);
            info.animating = false;
        }

        @Override
        public final int getChildType(int groupPosition, int childPosition) {
            GroupInfo info = getGroupInfo(groupPosition);
            if (info.animating) {
                return 0;
            } else {
                return getRealChildType(groupPosition, childPosition) + 1;
            }
        }
        
        @Override
        public final int getChildTypeCount() {
            return getRealChildTypeCount() + 1;
        }

        @Override
        public final View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, final ViewGroup parent) {
            GroupInfo info = getGroupInfo(groupPosition);
            if (info.animating) {
                if (convertView == null) {
                    convertView = new DummyView(parent.getContext());
                    convertView.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, 0));
                }
                
                if (childPosition < info.firstChildPosition) {
                    convertView.getLayoutParams().height = 0;
                    return convertView;
                }
                
                final ExpandableListView listView = (ExpandableListView) parent;
                
                DummyView dummyView = (DummyView) convertView;
                dummyView.clearViews();
                
                dummyView.setDivider(listView.getDivider(), parent.getMeasuredWidth(), listView.getDividerHeight());
                
                final int measureSpecW = MeasureSpec.makeMeasureSpec(parent.getWidth(), MeasureSpec.EXACTLY);
                final int measureSpecH = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

                int totalHeight = 0;
                int clipHeight = parent.getHeight();

                final int len = getRealChildrenCount(groupPosition);
                for (int i = info.firstChildPosition; i < len; i++) {
                    View childView = getRealChildView(groupPosition, i, (i == len - 1), null, parent);
                    childView.measure(measureSpecW, measureSpecH);
                    totalHeight += childView.getMeasuredHeight();

                    if (totalHeight < clipHeight) {
                        // we only need to draw enough views to fool the user...
                        dummyView.addFakeView(childView);
                    } else {
                        // if this group has too many views, we don't want to calculate the height
                        // of everything... just do a light approximation...
                        int averageHeight = totalHeight / (i + 1);
                        totalHeight += (len - i - 1) * averageHeight;
                        break;
                    }
                }
                
                if (info.expanding) {
                    ExpandAnimation ani = new ExpandAnimation(dummyView, 0, totalHeight, info);
                    ani.setDuration(this.parent.getAnimationDuration());
                    ani.setAnimationListener(new AnimationListener() {

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            stopAnimation(groupPosition);
                            notifyDataSetChanged();
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {}

                        @Override
                        public void onAnimationStart(Animation animation) {}
                        
                    });
                    dummyView.startAnimation(ani);
                } else {
                    ExpandAnimation ani = new ExpandAnimation(dummyView, info.dummyHeight, 0, info);
                    ani.setDuration(this.parent.getAnimationDuration());
                    ani.setAnimationListener(new AnimationListener() {

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            stopAnimation(groupPosition);
                            listView.collapseGroup(groupPosition);
                            notifyDataSetChanged();
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {}

                        @Override
                        public void onAnimationStart(Animation animation) {}
                        
                    });
                    dummyView.startAnimation(ani);
                }
                
                return convertView;
            } else {
                return getRealChildView(groupPosition, childPosition, isLastChild, convertView, parent);
            }
        }

        @Override
        public final int getChildrenCount(int groupPosition) {
            GroupInfo info = getGroupInfo(groupPosition);
            if (info.animating) {
                return info.firstChildPosition + 1;
            } else {
                return getRealChildrenCount(groupPosition);
            }
        }
        
    }

    private static class DummyView extends View {
        private List<View> views = new ArrayList<View>();
        private Drawable divider;
        private int dividerWidth;
        private int dividerHeight;

        public DummyView(Context context) {
            super(context);
        }
        
        public void setDivider(Drawable divider, int dividerWidth, int dividerHeight) {
            this.divider = divider;
            this.dividerWidth = dividerWidth;
            this.dividerHeight = dividerHeight;
            
            divider.setBounds(0, 0, dividerWidth, dividerHeight);
        }

        public void addFakeView(View childView) {
            childView.layout(0, 0, getWidth(), getHeight());
            views.add(childView);
        }
        
        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);
            final int len = views.size();
            for(int i = 0; i < len; i++) {
                View v = views.get(i);
                v.layout(left, top, right, bottom);
            }
        }
        
        public void clearViews() {
            views.clear();
        }
        
        @Override
        public void dispatchDraw(Canvas canvas) {
            canvas.save();
            
            divider.setBounds(0, 0, dividerWidth, dividerHeight);

            final int len = views.size();
            for(int i = 0; i < len; i++) {
                View v = views.get(i);
                v.draw(canvas);
                canvas.translate(0, v.getMeasuredHeight());
                divider.draw(canvas);
                canvas.translate(0, dividerHeight);
            }
            
            canvas.restore();
        }
    }
    
    private static class ExpandAnimation extends Animation { 
        private int baseHeight;
        private int delta;
        private View view;
        private GroupInfo groupInfo;
        
        private ExpandAnimation(View v, int startHeight, int endHeight, GroupInfo info) {
            baseHeight = startHeight;
            delta = endHeight - startHeight;
            view = v;
            groupInfo = info;
            
            view.getLayoutParams().height = startHeight;
            view.requestLayout();
        }
        
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            if (interpolatedTime < 1.0f) {
                int val = baseHeight + (int) (delta * interpolatedTime);
                view.getLayoutParams().height = val;
                groupInfo.dummyHeight = val;
                view.requestLayout();
            } else {
                int val = baseHeight + delta;
                view.getLayoutParams().height = val;
                groupInfo.dummyHeight = val;
                view.requestLayout();
            }
        }
    }
}
