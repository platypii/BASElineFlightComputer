package com.platypii.baseline.views.tracks;

import com.platypii.baseline.R;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.tracks.TrackFileData;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.views.charts.FlightProfile;
import com.platypii.baseline.views.charts.PolarPlot;
import com.platypii.baseline.views.charts.TimeChart;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import java.io.File;
import java.util.List;

public class ChartsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_charts);

        // TODO: Load async
        // Load track from extras
        final File trackFile = getTrackFile();
        if (trackFile != null) {
            final List<MLocation> trackData = TrackFileData.getTrackData(trackFile);
            final TimeChart timeChart = findViewById(R.id.timeChart);
            final FlightProfile flightProfile = findViewById(R.id.flightProfile);
            final PolarPlot polarChart = findViewById(R.id.polarChart);
            timeChart.loadTrack(trackData);
            flightProfile.loadTrack(trackData);
            polarChart.loadTrack(trackData);
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
