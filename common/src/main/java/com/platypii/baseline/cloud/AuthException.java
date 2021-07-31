package com.platypii.baseline.cloud;

public class AuthException extends Exception {
    public AuthException(String message) {
        super("authorization error - auth: " + message);
    }

    AuthException(String message, Exception e) {
        super("authorization error - auth: " + message, e);
    }
}
