package com.platypii.baseline.data;

import java.io.File;

public class Jump {

    // Jump info
    public final File logFile;

    public Jump(File logFile) {
        this.logFile = logFile;
    }

    @Override
    public String toString() {
        return logFile.getName();
    }
}
