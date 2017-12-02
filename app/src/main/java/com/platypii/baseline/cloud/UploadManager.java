package com.platypii.baseline.cloud;

import com.platypii.baseline.BaseActivity;
import com.platypii.baseline.events.AuthEvent;
import com.platypii.baseline.events.LoggingEvent;
import com.platypii.baseline.events.SyncEvent;
import com.platypii.baseline.tracks.TrackFile;
import com.platypii.baseline.tracks.TrackFiles;
import com.platypii.baseline.util.Exceptions;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Manages track uploads.
 * This includes queueing finished tracks, and retrying in the future.
 */
public class UploadManager {
    private static final String TAG = "UploadManager";

    // Upload state for each track file
    private static final int NOT_UPLOADED = 0;
    public static final int UPLOADING = 1;
    public static final int UPLOADED = 2;
    private final Map<TrackFile,Integer> trackFileState = new HashMap<>();
    private final Map<TrackFile,CloudData> completedUploads = new HashMap<>();

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
        trackFileState.put(trackFile, UPLOADING);
        // Start upload thread
        new Thread(new UploadTask(context, trackFile)).start();
    }

    private void uploadAll() {
        if(BaseActivity.currentAuthState == AuthEvent.SIGNED_IN) {
            for(TrackFile trackFile : TrackFiles.getTracks(context)) {
                Log.i(TAG, "Auto syncing track " + trackFile);
                upload(trackFile);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onLoggingEvent(@NonNull LoggingEvent event) {
        if(BaseActivity.currentAuthState == AuthEvent.SIGNED_IN && !event.started) {
            Log.i(TAG, "Auto syncing track " + event.trackFile);
            Exceptions.log("Logging stopped, autosyncing track " + event.trackFile);
            upload(event.trackFile);
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUploadSuccess(@NonNull SyncEvent.UploadSuccess event) {
        trackFileState.put(event.trackFile, UPLOADED);
        completedUploads.put(event.trackFile, event.cloudData);
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUploadFailure(@NonNull SyncEvent.UploadFailure event) {
        trackFileState.put(event.trackFile, NOT_UPLOADED);
    }

    public int getState(@NonNull TrackFile trackFile) {
        if(trackFileState.containsKey(trackFile)) {
            return trackFileState.get(trackFile);
        } else {
            return NOT_UPLOADED;
        }
    }
    public CloudData getCompleted(TrackFile trackFile) {
        return completedUploads.get(trackFile);
    }

    public void stop() {
        EventBus.getDefault().unregister(this);
    }

}
