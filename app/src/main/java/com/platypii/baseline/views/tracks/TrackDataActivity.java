package com.platypii.baseline.views.tracks;

import com.platypii.baseline.tracks.TrackData;
import com.platypii.baseline.views.BaseActivity;

import java9.util.concurrent.CompletableFuture;

/**
 * Common parent class of TrackLocalActivity and TrackRemoteActivity.
 * Repesents a class that provides a future TrackData.
 */
public abstract class TrackDataActivity extends BaseActivity {
    public final CompletableFuture<TrackData> trackData = new CompletableFuture<>();
}
