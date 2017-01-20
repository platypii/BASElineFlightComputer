package com.platypii.baseline;

import android.content.Context;
import android.util.AttributeSet;
import android.view.WindowInsets;
import android.widget.FrameLayout;

/**
 * A layout hack to handle screens with a chin.
 * This frame layout will add padding equal to the size of the chin.
 */
public class ChinLayout extends FrameLayout {

    public ChinLayout(Context context) {
        super(context);
    }

    public ChinLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChinLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        int chin = insets.getSystemWindowInsetBottom();
        setPadding(0, 0, 0, chin);
        return insets;
    }
}
