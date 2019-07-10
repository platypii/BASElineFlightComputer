package com.platypii.baseline.util;

import android.os.Bundle;

public class ABundle {
    public static Bundle of(String key, String value) {
        final Bundle bundle = new Bundle();
        bundle.putString(key, value);
        return bundle;
    }
}
