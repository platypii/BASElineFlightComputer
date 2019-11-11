package com.platypii.baseline.cloud;

public class AuthException extends Exception {
    public AuthException(String auth) {
        super("authorization error - auth: " + auth);
    }

    AuthException(String auth, Exception e) {
        super("authorization error - auth: " + auth, e);
    }
}
