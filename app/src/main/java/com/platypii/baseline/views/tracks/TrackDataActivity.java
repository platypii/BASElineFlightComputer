package com.platypii.baseline.views.tracks;

import com.platypii.baseline.R;
import com.platypii.baseline.tracks.TrackData;
import com.platypii.baseline.views.BaseActivity;

import android.view.View;
import androidx.activity.OnBackPressedCallback;
import java9.util.concurrent.CompletableFuture;

/**
 * Common parent class of TrackLocalActivity and TrackRemoteActivity.
 * Represents a class that provides a future TrackData.
 */
public abstract class TrackDataActivity extends BaseActivity {
    public final CompletableFuture<TrackData> trackData = new CompletableFuture<>();

    protected void setupMenu() {
        findViewById(R.id.trackOptionsMenu).setOnClickListener(this::clickMenu);
        findViewById(R.id.trackMenu).setOnClickListener(this::clickMenuOverlay);

        // Handle back press for menu
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                final View menu = findViewById(R.id.trackMenu);
                if (menu.getVisibility() == View.VISIBLE) {
                    menu.setVisibility(View.GONE);
                } else {
                    finish();
                }
            }
        });
    }

    private void clickMenu(View view) {
        final View menu = findViewById(R.id.trackMenu);
        if (menu.getVisibility() == View.VISIBLE) {
            menu.setVisibility(View.GONE);
        } else {
            menu.setVisibility(View.VISIBLE);
        }
    }

    private void clickMenuOverlay(View view) {
        findViewById(R.id.trackMenu).setVisibility(View.GONE);
    }

}
