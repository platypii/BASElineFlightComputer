package com.platypii.baseline.cloud;

import com.platypii.baseline.events.SyncEvent;

public class UploadFailedException extends Exception {
    UploadFailedException(SyncEvent.UploadFailure failure) {
        super("upload failed: " + failure.error);
    }
}
