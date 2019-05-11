package com.platypii.baseline.sensors;

import com.platypii.baseline.measurements.Measurement;
import androidx.annotation.NonNull;

/**
 * Used by Managers to notify of updated sensors
 */
public interface MySensorListener {

    void onSensorChanged(@NonNull Measurement measurement);

}
