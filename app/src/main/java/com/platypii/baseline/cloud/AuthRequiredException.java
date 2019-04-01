package com.platypii.baseline.cloud;

public class AuthRequiredException extends Exception {
    public AuthRequiredException() {
        super("authorization required");
    }
}
