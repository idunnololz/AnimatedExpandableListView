AnimatedExpandableListView
==========================

An extendable, flexible ExpandableListView widget that supports animations.

Here is a video of the example, showcasing the robustness of the widget:

[![Alt text for your video](http://img.youtube.com/vi/J7rcFRKvpyY/0.jpg)](http://www.youtube.com/watch?v=J7rcFRKvpyY)

This project is dedicated to <strong>J Withey</strong> for giving me the motivation to write and release this source.

This fork for performance tuning
==========================

There can be performance issue using this AnimatedExpandableListView when the child view getting more complex.

For example, when there are 5 children in a group, each child needs at least twice of getting the new child view, one for appending to the dummy view, and another for normal creation of the expanded list.We see this by adding method profiling and using traceview tool in Android SDK.

This perforamnce issue may not be obvious when the child view is simple, but we found it very severe when the layout is complex, and the measure spec for the child view needs set the upper boundary. Getting new child view is just too expense.

The root cause is that convertView is not given during animation, and the same child view needs to get initiated after animation. The LRU cache is used to hold the child to be reused. 

The tuning result can be seen from the trace graphs. Before this tuning, a group with 5 child needs 10 times of getting new view. With this view cache, 5 times are just enough to go.

See TRACEVIEW graphs below as a comparison:

Before:
![image](https://github.com/neokidd/AnimatedExpandableListView/blob/master/docs/example_activity_no_cache_view_10times_inflate.png)

After:
![image](https://github.com/neokidd/AnimatedExpandableListView/blob/master/docs/cache_view_5times_inflate.png)
