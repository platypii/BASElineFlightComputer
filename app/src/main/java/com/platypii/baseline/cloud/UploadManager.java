package com.platypii.baseline.cloud;

import com.google.firebase.crash.FirebaseCrash;
import com.platypii.baseline.BaseActivity;
import com.platypii.baseline.events.AuthEvent;
import com.platypii.baseline.events.LoggingEvent;
import com.platypii.baseline.tracks.TrackFile;
import android.content.Context;
import android.util.Log;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Manages track uploads.
 * This includes queueing finished tracks, and retrying in the future.
 */
public class UploadManager {
    private static final String TAG = "UploadManager";

    private static final boolean autosyncEnabled = true; // TODO: Make configurable

    private Context context;

    public void upload(TrackFile trackFile) {
        FirebaseCrash.log("User upload track " + trackFile.getName());
        new Thread(new UploadTask(context, trackFile)).start();
    }

//    private void uploadAll() {
//        if(BaseActivity.currentState == AuthEvent.SIGNED_IN && autosyncEnabled) {
//            for(TrackFile trackFile : TrackFiles.getTracks(context)) {
//                Log.i(TAG, "Auto syncing track " + trackFile);
//                upload(trackFile, null);
//            }
//        }
//    }

    public void start(Context context) {
        this.context = context;
        // Listen for track completion
        EventBus.getDefault().register(this);
        // TODO: Check for queued tracks to upload
        // uploadAll();
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onLoggingEvent(LoggingEvent event) {
        if(BaseActivity.currentState == AuthEvent.SIGNED_IN && autosyncEnabled && !event.started) {
            Log.i(TAG, "Auto syncing track " + event.trackFile);
            FirebaseCrash.log("Logging stopped, autosyncing track " + event.trackFile.getName());
            // TODO: Mark track as queued for upload
            new Thread(new UploadTask(context, event.trackFile)).start();
        }
    }

    public void stop() {
        EventBus.getDefault().unregister(this);
    }

}
