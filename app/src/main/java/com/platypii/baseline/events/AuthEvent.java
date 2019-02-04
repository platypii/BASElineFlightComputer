package com.platypii.baseline.events;

import android.support.annotation.NonNull;

/**
 * Indicates that sign in or sign out status has changed.
 *
 * The constructor is private, so only available class instances are the three static ones.
 * That means it is safe to use == equality.
 */
public class AuthEvent {

    public static final AuthEvent SIGNED_OUT = new AuthEvent("SignedOut");
    public static final AuthEvent SIGNING_IN = new AuthEvent("SigningIn");
    public static final AuthEvent SIGNED_IN = new AuthEvent("SignedIn");

    public final String state;
    private AuthEvent(String state) {
        this.state = state;
    }

    public static AuthEvent fromString(String state) {
        if (SIGNED_OUT.state.equals(state)) {
            return SIGNED_OUT;
        } else if (SIGNING_IN.state.equals(state)) {
            return SIGNING_IN;
        } else if (SIGNED_IN.state.equals(state)) {
            return SIGNED_IN;
        } else {
            return null;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "AuthEvent(" + state + ")";
    }
}
