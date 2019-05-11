package com.platypii.baseline.audible;

import androidx.annotation.NonNull;

/**
 * Represents a single audible sample and phrase
 */
public class AudibleSample {

    final double value;
    final String phrase;

    public AudibleSample(double value, String phrase) {
        this.value = value;
        this.phrase = phrase;
    }

    @NonNull
    @Override
    public String toString() {
        return "AudibleSample(" + value + "," + phrase + ")";
    }

}
