package com.platypii.baseline.cloud.tasks;

public class AuthRequiredException extends Exception {
    public AuthRequiredException() {
        super("authorization required");
    }
}
