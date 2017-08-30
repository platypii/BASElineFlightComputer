package com.platypii.baseline.location;

import com.platypii.baseline.altimeter.MyAltimeter;
import com.platypii.baseline.bluetooth.BluetoothService;
import com.platypii.baseline.util.Numbers;

import android.content.Context;
import android.support.annotation.NonNull;

class LocationProviderBluetooth extends LocationProviderNMEA {

    private final BluetoothService bluetooth;

    @Override
    protected String providerName() {
        return "LocationServiceBluetooth";
    }

    LocationProviderBluetooth(MyAltimeter alti, BluetoothService bluetooth) {
        super(alti);
        this.bluetooth = bluetooth;
    }

    /** Listen for GPPWR command */
    @Override
    protected void handleNmea(long timestamp, String nmea) throws NMEAException {
        // Parse NMEA command
        final String split[] = NMEA.splitNmea(nmea);
        if(split[0].equals("$GPPWR")) {
            bluetooth.powerLevel = NMEA.parsePowerLevel(split);
            bluetooth.charging = Numbers.parseInt(split[5], 0) == 1;
        }
        super.handleNmea(timestamp, nmea);
    }

    /**
     * Start location updates
     * @param context The Application context
     */
    @Override
    public synchronized void start(@NonNull Context context) throws SecurityException {
        // Start NMEA updates
        bluetooth.addNmeaListener(this);
    }

    @Override
    public void stop() {
        super.stop();
        bluetooth.removeNmeaListener(this);
    }
}
