package com.platypii.baseline.laser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// Sentences start with ae-a7, and end with bc-b7
// Sometimes 1 sentence takes 2 messages
// Sometimes 2 sentences come in 1 message
// Wait for bc-b7:
public class RfSentenceIterator implements Iterator<byte[]> {

    private List<Byte> byteBuffer = new ArrayList<>();

    // Sentences ready to read
    private List<byte[]> sentences = new ArrayList<>();

    private int state = 0;

    void addBytes(byte[] bytes) {
        for (byte b : bytes) {
            addByte(b);
        }
    }

    private void addByte(byte b) {
        byteBuffer.add(b);
        if (state == 0) {
            if (b == -82) state = 1;
            else throw new IllegalStateException("invalid preamble 1");
        } else if (state == 1) {
            if (b == -89) state = 2;
            else throw new IllegalStateException("invalid preamble 2");
        } else if (state == 2) {
            if (b == -68) state = 3;
        } else if (state == 3) {
            if (b == -73) state = 4;
            else state = 2;
        }
        if (state == 4) {
            addSentence();
            state = 0;
        }
    }

    private void addSentence() {
        byte[] sent = new byte[byteBuffer.size() - 4];
        for (int i = 0; i < byteBuffer.size() - 4; i++) {
            sent[i] = byteBuffer.get(i + 2);
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
