package com.platypii.baseline.data;

import com.platypii.baseline.measurements.Measurement;

/** Used by Managers to notify of updated sensors */
interface MySensorListener {

    void onSensorChanged(Measurement measurement);

}
