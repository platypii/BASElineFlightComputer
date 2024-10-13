package com.platypii.baseline.lasers.rangefinder;

class Crc16 {
    /**
     * Computes the CRC16 checksum for a given byte array.
     */
    public static short crc16(byte[] byteArray) {
        int crc = 0xffff;

        // Process bytes in pairs (LSB first)
        for (int i = 0; i < byteArray.length; i++) {
            int b = byteArray[i] & 0xff;
            crc ^= b;

            // Process each bit
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x0001) != 0) {
                    crc = (crc >> 1) ^ 0x8408;  // 0x8408 is reversed polynomial
                } else {
                    crc = crc >> 1;
                }
            }
        }

        // XOR with 0xfff
        crc ^= 0xffff;

        return (short) crc;
    }
}
