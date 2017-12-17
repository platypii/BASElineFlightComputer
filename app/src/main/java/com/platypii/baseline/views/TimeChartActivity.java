package com.platypii.baseline.views;

import com.platypii.baseline.tracks.TrackFile;
import com.platypii.baseline.tracks.TrackFiles;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.views.charts.TimeChart;
import com.platypii.baseline.views.tracks.TrackLocalActivity;
import android.app.Activity;
import android.os.Bundle;
import java.io.File;

public class TimeChartActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load track from extras
        final Bundle extras = getIntent().getExtras();
        if(extras != null && extras.getString(TrackLocalActivity.EXTRA_TRACK_FILE) != null) {
            final String extraTrackFile = extras.getString(TrackLocalActivity.EXTRA_TRACK_FILE);
            final File trackDir = TrackFiles.getTrackDirectory(getApplicationContext());
            final TrackFile trackFile = new TrackFile(new File(trackDir, extraTrackFile));

            final TimeChart timeChart = new TimeChart(this, trackFile);
            setContentView(timeChart);
        } else {
            Exceptions.report(new IllegalStateException("Failed to load track file from extras"));
            // Finish activity
            finish();
        }
    }
}
