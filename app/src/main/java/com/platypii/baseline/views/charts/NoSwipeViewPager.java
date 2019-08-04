package com.platypii.baseline.views.charts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class NoSwipeViewPager extends ViewPager {
    public NoSwipeViewPager(@NonNull Context context) {
        super(context);
    }

    public NoSwipeViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // Never allow swiping to switch between pages
        return false;
    }
}
