package com.platypii.baseline.views;

import com.platypii.baseline.R;
import com.platypii.baseline.tracks.TrackFile;
import com.platypii.baseline.tracks.TrackFiles;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.views.charts.TimeChart;
import com.platypii.baseline.views.tracks.TrackLocalActivity;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import java.io.File;

public class TimeChartActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_chart);

        // Load track from extras
        final TrackFile trackFile = getTrackFile();
        if (trackFile != null) {
            final TimeChart timeChart = findViewById(R.id.timeChart);
            timeChart.loadTrack(trackFile);
        } else {
            Exceptions.report(new IllegalStateException("Failed to load track file from extras"));
            // Finish activity
            finish();
        }
    }

    /**
     * Gets the track file from activity extras
     */
    @Nullable
    private TrackFile getTrackFile() {
        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            final String extraTrackFile = extras.getString(TrackLocalActivity.EXTRA_TRACK_FILE);
            if (extraTrackFile != null) {
                final File trackDir = TrackFiles.getTrackDirectory(getApplicationContext());
                return new TrackFile(new File(trackDir, extraTrackFile));
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
