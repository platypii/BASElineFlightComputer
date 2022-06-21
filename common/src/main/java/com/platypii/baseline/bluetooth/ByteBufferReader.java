package com.platypii.baseline.bluetooth;

import java.nio.ByteBuffer;

public class ByteBufferReader {
    private final byte[] value;
    private final ByteBuffer buff;

    public ByteBufferReader(byte[] value) {
        this.value = value;
        this.buff = ByteBuffer.wrap(value);
    }

    public long getLong(int bytes) {
        if (bytes > 8) {
            throw new IllegalArgumentException("Can only read a max of 8 bytes");
        }
        long out = 0;
        for (int i = 0; i < bytes; i++) {
            // data is encoded in little-endian
            int b = (buff.get() & 0xff) << (8 * i);
            out |= b;
        }

        return out;
    }

    public int getUnsignedShortAsInt() {
        return (int) getLong(2);
    }

    public short getShort() {
        return (short) getLong(2);
    }

    public long getUnsignedIntAsLong() {
        return getLong(4);
    }

    public int getInt() {
        return (int) getLong(4);
    }

    public short getByte() {
        return (byte) getLong(1);
    }

}
