package com.platypii.baseline;

import android.app.Application;
import org.greenrobot.eventbus.EventBus;

public class BaselineApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Configure EventBus
        EventBus.builder().logNoSubscriberMessages(false).installDefaultEventBus();

    }
}
