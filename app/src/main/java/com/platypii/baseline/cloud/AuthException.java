package com.platypii.baseline.cloud;

import java.io.IOException;

class AuthException extends IOException {
    AuthException(String auth) {
        super("authorization error - auth: " + auth);
    }
}
