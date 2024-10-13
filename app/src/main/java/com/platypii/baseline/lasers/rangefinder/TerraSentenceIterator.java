package com.platypii.baseline.lasers.rangefinder;

import android.util.Log;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// Sentences start with 7e and end with 7e
// Sometimes 1 sentence takes 2 messages
// Sometimes 2 sentences come in 1 message
class TerraSentenceIterator implements Iterator<byte[]> {
    private static final String TAG = "TerraSentenceIterator";

    @NonNull
    private final List<Byte> byteBuffer = new ArrayList<>();

    // Sentences ready to read
    @NonNull
    private final List<byte[]> sentences = new ArrayList<>();

    private int state = 0;

    void addBytes(@NonNull byte[] bytes) {
        for (byte b : bytes) {
            addByte(b);
        }
    }

    private void addByte(byte b) {
        byteBuffer.add(b);
        if (state == 0) {
            if (b == 0x7e) state = 1;
            else Log.e(TAG, "missing preamble");
        } else if (state == 1) {
            if (b == 0x7e) {
                addSentence();
                state = 0;
            }
        }
    }

    private void addSentence() {
        byte[] sent = new byte[byteBuffer.size() - 2];
        for (int i = 0; i < byteBuffer.size() - 2; i++) {
            sent[i] = byteBuffer.get(i + 1);
        }
        sentences.add(sent);
        byteBuffer.clear();
    }

    @Override
    public boolean hasNext() {
        return !sentences.isEmpty();
    }

    @Override
    public byte[] next() {
        return sentences.remove(0);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
