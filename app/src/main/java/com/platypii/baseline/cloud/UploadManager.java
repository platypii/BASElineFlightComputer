package com.platypii.baseline.cloud;

import com.platypii.baseline.BaseActivity;
import com.platypii.baseline.events.AuthEvent;
import com.platypii.baseline.events.LoggingEvent;
import com.platypii.baseline.tracks.TrackFile;
import com.platypii.baseline.tracks.TrackFiles;
import com.platypii.baseline.util.Callback;
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

    public void upload(TrackFile trackFile, Callback<CloudData> cb) {
        new UploadTask(context, trackFile, cb).execute();
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
            // TODO: Mark track as queued for upload
            upload(event.trackFile, null);
        }
    }

    public void stop() {
        EventBus.getDefault().unregister(this);
    }

}
