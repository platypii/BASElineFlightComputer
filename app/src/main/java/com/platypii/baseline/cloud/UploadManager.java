package com.platypii.baseline.cloud;

import com.platypii.baseline.BaseService;
import com.platypii.baseline.Services;
import com.platypii.baseline.cloud.tasks.TaskTypes;
import com.platypii.baseline.cloud.tracks.TrackUploadTask;
import com.platypii.baseline.events.LoggingEvent;
import com.platypii.baseline.tracks.TrackFile;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Manages track uploads.
 * This includes queueing finished tracks, and retrying in the future.
 */
class UploadManager implements BaseService {
    private static final String TAG = "UploadManager";

    @Override
    public void start(@NonNull Context context) {
        // Listen for track completion
        EventBus.getDefault().register(this);
        // Check for queued tracks to upload
        uploadAll();
    }

    /**
     * Clear exiting track uploads, and re-add all local track files to task queue
     */
    private void uploadAll() {
        Services.tasks.removeType(TaskTypes.trackUpload);
        // Can't upload if you're not signed in
        if (AuthState.getUser() != null) {
            for (TrackFile track : Services.trackStore.getLocalTracks()) {
                Services.tasks.add(new TrackUploadTask(track));
            }
        }
    }

    @Subscribe
    public void onLoggingEvent(@NonNull LoggingEvent.LoggingStop event) {
        if (AuthState.getUser() != null) {
            Log.i(TAG, "Auto syncing track " + event.trackFile);
            Services.tasks.add(new TrackUploadTask(event.trackFile));
        }
    }

    @Subscribe
    public void onSignIn(@NonNull AuthState.SignedIn event) {
        Log.d(TAG, "User signed in, uploading queued tracks");
        uploadAll();
    }

    @Subscribe
    public void onSignOut(@NonNull AuthState.SignedOut event) {
        // Cancel pending upload tasks
        Services.tasks.removeType(TaskTypes.trackUpload);
    }

    @Override
    public void stop() {
        EventBus.getDefault().unregister(this);
    }

}
