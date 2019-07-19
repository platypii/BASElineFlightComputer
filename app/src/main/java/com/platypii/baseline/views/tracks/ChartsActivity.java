package com.platypii.baseline.views.tracks;

import com.platypii.baseline.R;
import com.platypii.baseline.events.ChartFocusEvent;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.views.BaseActivity;
import android.os.Bundle;
import android.widget.TextView;
import java.util.Date;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ChartsActivity extends BaseActivity {

    private TextView timeLabel;
    private TextView altitudeLabel;
    private TextView horizontalDistLabel;
    private TextView verticalDistLabel;
    private TextView horizontalSpeedLabel;
    private TextView verticalSpeedLabel;
    private TextView speedLabel;
    private TextView glideLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_charts);

        timeLabel = findViewById(R.id.timeLabel);
        altitudeLabel = findViewById(R.id.altitudeLabel);
        horizontalDistLabel = findViewById(R.id.hDistLabel);
        verticalDistLabel = findViewById(R.id.vDistLabel);
        horizontalSpeedLabel = findViewById(R.id.hSpeedLabel);
        verticalSpeedLabel = findViewById(R.id.vSpeedLabel);
        speedLabel = findViewById(R.id.speedLabel);
        glideLabel = findViewById(R.id.glideLabel);

        // Init chart stats
        onChartFocus(null);
    }

    /**
     * Update views for focus event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onChartFocus(ChartFocusEvent event) {
        if (event != null && event.location != null) {
            final MLocation loc = event.location;
            // TODO: Date should have timezone
            timeLabel.setText(new Date(loc.millis).toString());
            altitudeLabel.setText(Convert.distance(loc.altitude_gps) + " MSL");
//            horizontalDistLabel.setText(Convert.distance(loc.distanceTo(stats.exit))); // TODO
//            verticalDistLabel.setText(Convert.distance(loc.altitude_gps - stats.exit.altitude_gps)); // TODO
            horizontalSpeedLabel.setText(Convert.speed(loc.groundSpeed()));
            verticalSpeedLabel.setText(Convert.speed(loc.climb));
            speedLabel.setText(Convert.speed(loc.totalSpeed()));
            glideLabel.setText(Convert.glide(loc.groundSpeed(), loc.climb, 1, true));
        } else {
            timeLabel.setText("");
            altitudeLabel.setText("");
            horizontalDistLabel.setText("");
            verticalDistLabel.setText("");
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
