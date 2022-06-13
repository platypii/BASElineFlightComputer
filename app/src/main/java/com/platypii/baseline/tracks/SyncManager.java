package com.platypii.baseline.tracks;

import com.platypii.baseline.BaseService;
import com.platypii.baseline.Services;
import com.platypii.baseline.cloud.AuthState;
import com.platypii.baseline.cloud.tasks.TaskType;
import com.platypii.baseline.events.LoggingEvent;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Manages track uploads.
 * This includes queueing finished tracks, and retrying in the future.
 */
class SyncManager implements BaseService {
    private static final String TAG = "UploadManager";

    @Override
    public void start(@NonNull Context context) {
        // Listen for track logging stops
        EventBus.getDefault().register(this);
        // Check for queued tracks to upload
        uploadAll();
    }

    /**
     * Clear exiting track uploads, and re-add all local track files to task queue
     */
    void uploadAll() {
        Services.tasks.removeType(TaskType.trackUpload);
        // Can't upload if you're not signed in
        if (AuthState.getUser() != null) {
            for (TrackFile track : Services.tracks.local.getLocalTracks()) {
                Services.tasks.add(new UploadTrackTask(track));
            }
        }
    }

    @Subscribe
    public void onLoggingStop(@NonNull LoggingEvent.LoggingStop event) {
        if (AuthState.getUser() != null) {
            Log.i(TAG, "Auto syncing track " + event.trackFile);
            Services.tasks.add(new UploadTrackTask(event.trackFile));
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
        Services.tasks.removeType(TaskType.trackUpload);
    }

    @Override
    public void stop() {
        EventBus.getDefault().unregister(this);
    }

}
