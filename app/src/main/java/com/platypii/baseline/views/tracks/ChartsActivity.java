package com.platypii.baseline.views.tracks;

import com.platypii.baseline.R;
import com.platypii.baseline.events.ChartFocusEvent;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.tracks.TrackFileData;
import com.platypii.baseline.tracks.TrackStats;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.views.charts.FlightProfile;
import com.platypii.baseline.views.charts.PolarPlot;
import com.platypii.baseline.views.charts.TimeChart;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.io.File;
import java.lang.ref.WeakReference;
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

    private TrackStats stats;

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
        updateChartFocus(null);

        // Load track from extras
        final File trackFile = getTrackFile();
        if (trackFile != null) {
            Log.i(TAG, "Loading track data");
            // Load async
            new LoadTask(trackFile, this).execute();
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

    private static class LoadTask extends AsyncTask<Void,Void,Void> {
        private final File trackFile;
        private final WeakReference<ChartsActivity> activityRef;

        private LoadTask(File trackFile, ChartsActivity activity) {
            this.trackFile = trackFile;
            this.activityRef = new WeakReference<>(activity);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            final ChartsActivity activity = activityRef.get();
            if (activity != null && !activity.isFinishing()) {
                final List<MLocation> trackData = TrackFileData.getTrackData(trackFile);
                activity.loadData(trackData);
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void v) {
            final ChartsActivity activity = activityRef.get();
            if (activity != null && !activity.isFinishing()) {
                activity.doneLoading();
            }
        }
    }

    /**
     * Load data into charts, called from LoadTask in background thread
     */
    private void loadData(List<MLocation> trackData) {
        stats = new TrackStats(trackData);
        timeChart.loadTrack(trackData);
        flightProfile.loadTrack(trackData);
        polarChart.loadTrack(trackData);
    }

    /**
     * Invalidate charts after data is loaded, called from LoadTask in UI thread
     */
    private void doneLoading() {
        updateChartFocus(null);
        timeChart.invalidate();
        flightProfile.invalidate();
        polarChart.invalidate();
        progress.setVisibility(View.GONE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onChartFocus(@NonNull ChartFocusEvent event) {
        updateChartFocus(event.location);
        // Notify child views
        timeChart.onFocus(event.location);
        flightProfile.onFocus(event.location);
        polarChart.onFocus(event.location);
    }

    /**
     * Update views for focus event
     */
    private void updateChartFocus(@Nullable MLocation focus) {
        if (focus != null) {
            // TODO: Date should have timezone
            timeLabel.setText(new Date(focus.millis).toString());
            altitudeLabel.setText(Convert.distance(focus.altitude_gps) + " MSL");
            horizontalSpeedLabel.setText(Convert.speed(focus.groundSpeed()));
            verticalSpeedLabel.setText(Convert.speed(focus.climb));
            speedLabel.setText(Convert.speed(focus.totalSpeed()));
            glideLabel.setText(Convert.glide(focus.groundSpeed(), focus.climb, 1, true));
        } else {
            if (stats != null) {
                if (stats.exit != null) {
                    timeLabel.setText(new Date(stats.exit.millis).toString());
                } else {
                    timeLabel.setText("");
                }
                if (!stats.altitude.isEmpty()) {
                    altitudeLabel.setText(Convert.distance(stats.altitude.max - stats.altitude.min));
                } else {
                    altitudeLabel.setText("");
                }
            } else {
                timeLabel.setText("");
                altitudeLabel.setText("");
            }
            horizontalSpeedLabel.setText("");
            verticalSpeedLabel.setText("");
            speedLabel.setText("");
            glideLabel.setText("");
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
