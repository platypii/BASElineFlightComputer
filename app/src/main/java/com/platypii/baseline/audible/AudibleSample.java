package com.platypii.baseline.audible;

/**
 * Represents a single audible sample and phrase
 */
class AudibleSample {

    double value;
    String phrase;

    AudibleSample(double value, String phrase) {
        this.value = value;
        this.phrase = phrase;
    }

    @Override
    public String toString() {
        return "AudibleSample(" + value + "," + phrase + ")";
    }

}
