package com.platypii.baseline;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.CameraXConfig;
import org.greenrobot.eventbus.EventBus;

public class BaseApplication extends Application implements CameraXConfig.Provider {

    @Override
    public void onCreate() {
        super.onCreate();

        // Configure EventBus
        EventBus.builder().logNoSubscriberMessages(false).installDefaultEventBus();

    }

    @NonNull
    @Override
    public CameraXConfig getCameraXConfig() {
        return Camera2Config.defaultConfig();
    }
}
