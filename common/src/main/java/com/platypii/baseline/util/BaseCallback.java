package com.platypii.baseline.util;

import androidx.annotation.NonNull;

public interface BaseCallback<T> {
    void onSuccess(@NonNull T result);
    void onFailure(@NonNull Throwable ex);
}
