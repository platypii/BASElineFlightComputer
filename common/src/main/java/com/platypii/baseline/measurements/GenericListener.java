package com.platypii.baseline.measurements;

import com.platypii.baseline.measurements.MSensor;

public interface GenericListener<T extends MSensor> {
    void onSensorChanged(T r);
}
