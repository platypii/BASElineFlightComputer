package com.platypii.baseline.cloud;

import java.io.IOException;

public class AuthException extends IOException {
    public AuthException(String auth) {
        super("authorization error - auth: " + auth);
    }
}
