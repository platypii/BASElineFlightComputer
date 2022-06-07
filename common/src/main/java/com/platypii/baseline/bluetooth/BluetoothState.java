package com.platypii.baseline.bluetooth;

public class BluetoothState {

    // Bluetooth finite state machine
    public static final int BT_STOPPED = 0;
    public static final int BT_STARTING = 1;
    public static final int BT_CONNECTING = 2;
    public static final int BT_CONNECTED = 3;
    public static final int BT_STOPPING = 4;
    public static final int BT_SCANNING = 5;

    public static boolean started(int state) {
        return state == BT_STARTING || state == BT_CONNECTING || state == BT_CONNECTED;
    }

    public static final String[] BT_STATES = {"BT_STOPPED", "BT_STARTING", "BT_CONNECTING", "BT_CONNECTED", "BT_STOPPING", "BT_SCANNING"};

}
