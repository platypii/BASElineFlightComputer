package com.platypii.baseline.views.map;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.google.android.gms.maps.MapFragment;
import java.util.ArrayList;
import java.util.List;

/**
 * A hack to detect map touches
 */
public class TouchableMapFragment extends MapFragment {
    private View originalContentView;

    private final List<View.OnTouchListener> listeners = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        originalContentView = super.onCreateView(inflater, parent, savedInstanceState);

        final TouchableWrapper touchView = new TouchableWrapper(getActivity());
        touchView.addView(originalContentView);

        return touchView;
    }

    @Override
    public View getView() {
        return originalContentView;
    }

    public void setOnTouchListener(View.OnTouchListener listener) {
        listeners.add(listener);
    }
    public void removeOnTouchListeners() {
        listeners.clear();
    }

    private class TouchableWrapper extends FrameLayout {
        public TouchableWrapper(@NonNull Context context) {
            super(context);
        }
        public TouchableWrapper(@NonNull Context context, AttributeSet attrs) {
            super(context, attrs);
        }
        public TouchableWrapper(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            for(OnTouchListener listener : listeners) {
                listener.onTouch(originalContentView, ev);
            }

            return super.dispatchTouchEvent(ev);
        }
    }
}
