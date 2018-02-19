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
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import io.fabric.sdk.android.services.concurrency.AsyncTask;
import java.io.File;
import java.util.List;

public class ChartsActivity extends Activity {
    private static final String TAG = "Charts";

    private ProgressBar progress;
    private TimeChart timeChart;
    private FlightProfile flightProfile;
    private PolarPlot polarChart;

    private File trackFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_charts);

        // Find views
        progress = findViewById(R.id.chartProgress);
        timeChart = findViewById(R.id.timeChart);
        flightProfile = findViewById(R.id.flightProfile);
        polarChart = findViewById(R.id.polarChart);

        // Load track from extras
        trackFile = getTrackFile();
        if (trackFile != null) {
            // Load async
            loadTrackDataAsync();
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

    private void loadTrackDataAsync() {
        Log.i(TAG, "Loading track data");
        new AsyncTask<Void,Void,Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                final List<MLocation> trackData = TrackFileData.getTrackData(trackFile);
                timeChart.loadTrack(trackData);
                flightProfile.loadTrack(trackData);
                polarChart.loadTrack(trackData);
                return null;
            }
            @Override
            protected void onPostExecute(Void v) {
                timeChart.invalidate();
                flightProfile.invalidate();
                polarChart.invalidate();
                progress.setVisibility(View.GONE);
            }
        }.execute();
    }
}
