package com.platypii.baseline.lasers.rangefinder;

import java.util.UUID;

interface RangefinderProtocol {

    void onServicesDiscovered();

    void processBytes(byte[] value);

    UUID getCharacteristic();

}
