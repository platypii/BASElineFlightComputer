package com.platypii.baseline.cloud;

import androidx.annotation.NonNull;

public class UploadFailedException extends Exception {
    public UploadFailedException(@NonNull String error) {
        super("upload failed: " + error);
    }
}
