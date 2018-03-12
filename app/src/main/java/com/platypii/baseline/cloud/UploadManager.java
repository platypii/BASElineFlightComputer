package com.platypii.baseline.cloud;

import com.platypii.baseline.views.BaseActivity;
import com.platypii.baseline.Services;
import com.platypii.baseline.events.AuthEvent;
import com.platypii.baseline.events.LoggingEvent;
import com.platypii.baseline.events.SyncEvent;
import com.platypii.baseline.tracks.TrackFile;
import com.platypii.baseline.util.Exceptions;
import android.content.Context;
import android.support.annotation.NonNull;
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

    private Context context;

    public void start(Context context) {
        this.context = context;
        // Listen for track completion
        EventBus.getDefault().register(this);
        // Check for queued tracks to upload
        uploadAll();
    }

    private void upload(@NonNull TrackFile trackFile) {
        // Mark track as queued for upload
        Services.trackStore.setUploading(trackFile);
        // Start upload thread
        new Thread(new UploadTask(context, trackFile)).start();
    }

    private void uploadAll() {
        if (BaseActivity.currentAuthState == AuthEvent.SIGNED_IN) {
            for (TrackFile trackFile : Services.trackStore.getLocalTracks()) {
                Log.i(TAG, "Auto syncing track " + trackFile);
                upload(trackFile);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onLoggingEvent(@NonNull LoggingEvent event) {
        if (BaseActivity.currentAuthState == AuthEvent.SIGNED_IN && !event.started) {
            Log.i(TAG, "Auto syncing track " + event.trackFile);
            Exceptions.log("Logging stopped, autosyncing track " + event.trackFile);
            upload(event.trackFile);
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUploadSuccess(@NonNull SyncEvent.UploadSuccess event) {
        Services.trackStore.setUploadSuccess(event.trackFile, event.cloudData);
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUploadFailure(@NonNull SyncEvent.UploadFailure event) {
        Services.trackStore.setNotUploaded(event.trackFile);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onSignIn(@NonNull AuthEvent event) {
        if (event == AuthEvent.SIGNED_IN) {
            Log.i(TAG, "User signed in, uploading all tracks");
            uploadAll();
        }
    }

    public void stop() {
        EventBus.getDefault().unregister(this);
    }

}
