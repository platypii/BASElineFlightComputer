package com.platypii.baseline.views.tracks;

import com.platypii.baseline.R;
import com.platypii.baseline.events.ChartFocusEvent;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.tracks.TrackFileData;
import com.platypii.baseline.util.Convert;
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
import android.widget.TextView;
import io.fabric.sdk.android.services.concurrency.AsyncTask;
import java.io.File;
import java.util.Date;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ChartsActivity extends Activity {
    private static final String TAG = "Charts";

    private ProgressBar progress;

    private TextView timeLabel;
    private TextView altitudeLabel;
    private TextView horizontalSpeedLabel;
    private TextView verticalSpeedLabel;
    private TextView speedLabel;
    private TextView glideLabel;

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

        timeLabel = findViewById(R.id.timeLabel);
        altitudeLabel = findViewById(R.id.altitudeLabel);
        horizontalSpeedLabel = findViewById(R.id.hSpeedLabel);
        verticalSpeedLabel = findViewById(R.id.vSpeedLabel);
        speedLabel = findViewById(R.id.speedLabel);
        glideLabel = findViewById(R.id.glideLabel);

        timeChart = findViewById(R.id.timeChart);
        flightProfile = findViewById(R.id.flightProfile);
        polarChart = findViewById(R.id.polarChart);

        // Init chart stats
        onChartFocus(null);

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onChartFocus(@Nullable ChartFocusEvent event) {
        updateChartStats(event);
        // Notify child views
        timeChart.onFocus(event);
        flightProfile.onFocus(event);
        polarChart.onFocus(event);
    }

    private void updateChartStats(@Nullable ChartFocusEvent event) {
        if (event != null && event.location != null) {
            final MLocation location = event.location;
            Log.i(TAG, "Focus " + location);
            // TODO: Date should have timezone
            timeLabel.setText("time: " + new Date(location.millis).toString());
            altitudeLabel.setText("alt: " + Convert.distance(location.altitude_gps));
            horizontalSpeedLabel.setText("h-speed: " + Convert.speed(location.groundSpeed()));
            verticalSpeedLabel.setText("v-speed: " + Convert.speed(location.climb));
            speedLabel.setText("speed: " + Convert.speed(location.totalSpeed()));
            glideLabel.setText("glide: " + Convert.glide(location.groundSpeed(), location.climb, 1, true));
        } else {
            Log.i(TAG, "Clear focus");
            timeLabel.setText("time: ");
            altitudeLabel.setText("alt: ");
            horizontalSpeedLabel.setText("h-speed: ");
            verticalSpeedLabel.setText("v-speed: ");
            speedLabel.setText("speed: ");
            glideLabel.setText("glide: ");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

}
