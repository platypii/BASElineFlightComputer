package com.platypii.baseline;

import android.os.Bundle;
import android.view.View;

public class TrackListActivity extends BaseActivity {

    private View signInPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jumps);
        signInPanel = findViewById(R.id.sign_in_panel);
    }

    private void updateViews() {
        if(isSignedIn()) {
            signInPanel.setVisibility(View.GONE);
        } else {
            signInPanel.setVisibility(View.VISIBLE);
        }
    }

}
