package com.platypii.baseline;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class WearActivity extends Activity {
    private static final String TAG = "WearActivity";

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });
    }

    public void clickRecord(View v) {
        Log.i(TAG, "Clicked record");
    }

    public void clickAudible(View v) {
        Log.i(TAG, "Clicked audible");
    }

    public void clickAltimeter(View v) {
        Log.i(TAG, "Clicked altimeter");
    }

}
