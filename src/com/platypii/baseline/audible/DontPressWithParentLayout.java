package com.platypii.baseline.audible;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Special class to to allow the parent to be pressed without being pressed
 * itself. This way an event can be enabled/disabled without changing
 * the background of the indicator.
 */
public class DontPressWithParentLayout extends LinearLayout {
    public DontPressWithParentLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    public void setPressed(boolean pressed) {
        // If the parent is pressed, do not set to pressed.
        if(pressed && ((View) getParent()).isPressed()) {
            return;
        }
        super.setPressed(pressed);
    }
}
