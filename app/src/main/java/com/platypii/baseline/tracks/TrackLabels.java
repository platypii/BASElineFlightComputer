package com.platypii.baseline.tracks;

import com.platypii.baseline.jarvis.FlightMode;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.Exceptions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;

import static com.platypii.baseline.jarvis.FlightMode.MODE_CANOPY;
import static com.platypii.baseline.jarvis.FlightMode.MODE_FREEFALL;
import static com.platypii.baseline.jarvis.FlightMode.MODE_WINGSUIT;

class TrackLabels {

    int exit;
    int deploy;
    int land;

    private TrackLabels(int exit, int deploy, int land) {
        this.exit = exit;
        this.deploy = deploy;
        this.land = land;
    }

    @Nullable
    static TrackLabels from(@NonNull List<MLocation> points) {
        try {
            final TrackLabels labels = findExitLand(points);
            if (labels != null) {
                labels.deploy = findDeploy(points, labels.exit, labels.land);
            }
            return labels;
        } catch (Exception e) {
            Exceptions.report(e);
            return null;
        }
    }

    /**
     * Find exit and land index by minimizing "in-flight" error.
     * Return as a TrackLabels with deploy set to 0.
     */
    @Nullable
    private static TrackLabels findExitLand(@NonNull List<MLocation> points) {
        final int n = points.size();
        // Cumulative in-flight distribution
        final int[] cum = new int[n + 1];
        for (int i = 0; i < n; i++) {
            cum[i + 1] = cum[i] + (inFlight(FlightMode.getMode(points.get(i))) ? 1 : 0);
        }
        // Find median index
        int median = -1;
        for (int i = 0; i < n; i++) {
            if (cum[i] > cum[n] / 2) {
                median = i;
                break;
            }
        }
        if (median >= 0) {
            int exit = 0;
            for (int i = 0; i < median + 1; i++) {
                if (exit - 2 * cum[exit] < i - 2 * cum[i]) {
                    exit = i;
                }
            }
            int land = 0;
            for (int i = median; i < n; i++) {
                if (2 * cum[land] - land < 2 * cum[i] - i) {
                    land = i;
                }
            }
            return new TrackLabels(exit, 0, land);
        } else {
            return null;
        }
    }

    /**
     * Find deploy index by minimizing classification error
     */
    private static int findDeploy(@NonNull List<MLocation> points, int exit, int land) {
        final int n = points.size();
        // Cumulative in-flight distribution
        final int[] freefallCum = new int[n + 1];
        final int[] canopyCum = new int[n + 1];
        for (int i = exit; i < land; i++) {
            freefallCum[i + 1] = freefallCum[i] + (freefall(FlightMode.getMode(points.get(i))) ? 1 : 0);
            canopyCum[i + 1] = canopyCum[i] + (canopy(FlightMode.getMode(points.get(i))) ? 1 : 0);
        }
        int deploy = exit;
        for (int i = exit; i < land; i++) {
            if (deploy - freefallCum[deploy] - canopyCum[deploy] < i - freefallCum[i] - canopyCum[i]) {
                deploy = i;
            }
        }
        return deploy;
    }

    private static boolean inFlight(int mode) {
        return mode == MODE_FREEFALL || mode == MODE_WINGSUIT || mode == MODE_CANOPY;
    }

    private static boolean freefall(int mode) {
        return mode == MODE_FREEFALL || mode == MODE_WINGSUIT;
    }

    private static boolean canopy(int mode) {
        return mode == MODE_CANOPY;
    }

}
