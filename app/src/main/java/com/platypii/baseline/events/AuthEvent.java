package com.platypii.baseline.events;

/**
 * Indicates that sign in or sign out status has changed
 */
public class AuthEvent {

    public static final AuthEvent SIGNED_OUT = new AuthEvent("SignedOut");
    public static final AuthEvent SIGNING_IN = new AuthEvent("SigningIn");
    public static final AuthEvent SIGNED_IN = new AuthEvent("SignedIn");

    private String state;
    private AuthEvent(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return state;
    }
}
