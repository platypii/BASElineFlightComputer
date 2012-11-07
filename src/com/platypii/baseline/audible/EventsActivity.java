package com.platypii.baseline.audible;

import com.platypii.baseline.R;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;


// Activity to configure Audible
public class EventsActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.events);
        
        // Action bar
        getActionBar().hide();
    }
}



