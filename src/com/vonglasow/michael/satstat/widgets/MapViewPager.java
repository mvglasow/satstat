package com.vonglasow.michael.satstat.widgets;

import org.mapsforge.map.android.view.MapView;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

public class MapViewPager extends ViewPager {

	public MapViewPager(Context context) {
		super(context);
	}

	public MapViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
    /**
     * Determines if a child view can be scrolled.
     * 
     * If the child view is scrollable, then true will be returned, indicating
     * that scroll operation should not be picked up by the MapViewPager but
     * passed on to the child view in which it took place.
     * 
     * This method will return true if the child view is a {@link MapView} and
     * retain default behavior in all other cases.
     */
	@Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        if (v instanceof MapView) {
            return true;
        }
        return super.canScroll(v, checkV, dx, x, y);
    }

}
