package com.platypii.baseline.events;

/**
 * Indicates that a bluetooth connection or disconnection event
 */
public class BluetoothEvent {

    public final int bluetoothState;

    public BluetoothEvent(int bluetoothState) {
        this.bluetoothState = bluetoothState;
    }

}
