package com.platypii.baseline.views.tracks;

import com.platypii.baseline.R;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.views.charts.TimeChart;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import java.io.File;

public class ChartsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_charts);

        // TODO: Load async
        // Load track from extras
        final File trackFile = getTrackFile();
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
    private File getTrackFile() {
        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            final String extraTrackFile = extras.getString(TrackLocalActivity.EXTRA_TRACK_FILE);
            if (extraTrackFile != null) {
                return new File(extraTrackFile);
            }
        }
        return null;
    }
}
