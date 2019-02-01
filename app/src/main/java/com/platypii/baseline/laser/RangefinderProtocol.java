package com.platypii.baseline.laser;

import java.util.UUID;

interface RangefinderProtocol {

    void onServicesDiscovered();

    void processBytes(byte[] value);

    UUID getCharacteristic();

}
