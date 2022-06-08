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

    private View menu;
    private View buttons;

    protected void setupMenu() {
        menu = findViewById(R.id.trackMenu);
        menu.setOnClickListener(this::clickMenuOverlay);
        findViewById(R.id.trackOptionsMenu).setOnClickListener(this::clickMenu);

        buttons = menu.findViewById(R.id.buttons);

        // Handle back press for menu
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (menu.getVisibility() == View.VISIBLE) {
                    buttons.animate()
                            .translationY(-menu.getHeight())
                            .withEndAction(() -> menu.setVisibility(View.INVISIBLE));
                } else {
                    finish();
                }
            }
        });
    }

    private void clickMenu(View view) {
        if (menu.getVisibility() == View.VISIBLE) {
            buttons.animate()
                    .translationY(-buttons.getHeight())
                    .withEndAction(() -> menu.setVisibility(View.INVISIBLE));
        } else {
            buttons.setTranslationY(-buttons.getHeight());
            menu.setVisibility(View.VISIBLE);
            buttons.animate().translationY(0);
        }
    }

    private void clickMenuOverlay(View view) {
        menu.setVisibility(View.GONE);
    }

}
