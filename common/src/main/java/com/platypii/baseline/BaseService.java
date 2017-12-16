package com.platypii.baseline;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * A baseline service that can be started and stopped
 * When an instance is created, it should be in the "stopped" state
 */
public interface BaseService {

    void start(@NonNull Context context);

    void stop();

}
