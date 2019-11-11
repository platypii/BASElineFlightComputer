package com.platypii.baseline.util;

import android.os.Bundle;
import androidx.annotation.NonNull;

public class ABundle {
    @NonNull
    public static Bundle of(String key, String value) {
        final Bundle bundle = new Bundle();
        bundle.putString(key, value);
        return bundle;
    }
}
