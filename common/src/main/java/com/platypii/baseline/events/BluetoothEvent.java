package com.platypii.baseline.events;

import androidx.annotation.NonNull;

import static com.platypii.baseline.bluetooth.BluetoothState.BT_STATES;

/**
 * Indicates that a bluetooth connection or disconnection event
 */
public class BluetoothEvent {
    public final int state;

    public BluetoothEvent(int state) {
        this.state = state;
    }

    @NonNull
    @Override
    public String toString() {
        return "BluetoothEvent(" + BT_STATES[state] + ")";
    }
}
