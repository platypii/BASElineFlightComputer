package com.platypii.baseline.tracks;

import com.platypii.baseline.jarvis.FlightMode;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.Exceptions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;

import static com.platypii.baseline.jarvis.FlightMode.MODE_CANOPY;
import static com.platypii.baseline.jarvis.FlightMode.MODE_FREEFALL;
import static com.platypii.baseline.jarvis.FlightMode.MODE_GROUND;
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
            cum[i + 1] = cum[i] + inFlight(FlightMode.getMode(points.get(i)));
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
            for (int i = 0; i <= median; i++) {
                if (exit - 2 * cum[exit] < i - 2 * cum[i]) {
                    exit = i;
                }
            }
            int land = median;
            for (int i = median; i <= n; i++) {
                if (2 * cum[land] - land < 2 * cum[i] - i) {
                    land = i;
                }
            }
            land--;
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
        // Cumulative in-freefall distribution
        final int[] freefallCum = new int[n + 1];
        for (int i = exit; i <= land; i++) {
            freefallCum[i + 1] = freefallCum[i] + inFreefall(FlightMode.getMode(points.get(i)));
        }
        int deploy = exit + 1;
        for (int i = exit + 1; i <= land; i++) {
            // #correct = c(i) + (land - i - total + c(i)) ~= 2 c(i) - i
            if (2 * freefallCum[deploy] - deploy < 2 * freefallCum[i] - i) {
                deploy = i;
            }
        }
        return deploy - 1;
    }

    private static int inFlight(int mode) {
        return mode == MODE_FREEFALL || mode == MODE_WINGSUIT || mode == MODE_CANOPY ? 1 : 0;
    }

    private static int inFreefall(int mode) {
        return mode == MODE_GROUND || mode == MODE_CANOPY ? 0 : 1;
    }

}
