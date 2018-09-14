package com.platypii.baseline.views;

import com.platypii.baseline.R;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollView;
import java.util.ArrayList;
import java.util.List;

public class GlassActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "Glass";

    private CardScrollView cardScroller;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        cardScroller = new CardScrollView(this);
        cardScroller.setAdapter(new CardAdapter(createCards(this)));
        cardScroller.setOnItemClickListener(this);

        setContentView(cardScroller);
    }

    @NonNull
    private List<CardBuilder> createCards(Context context) {
        final ArrayList<CardBuilder> cards = new ArrayList<>();

        // Add cards that demonstrate TEXT layouts.
        cards.add(new CardBuilder(context, CardBuilder.Layout.COLUMNS)
                .setIcon(R.drawable.polar)
                .setText(R.string.action_polar));
        cards.add(new CardBuilder(context, CardBuilder.Layout.COLUMNS)
                .setIcon(R.drawable.nav)
                .setText(R.string.action_nav));
        cards.add(new CardBuilder(context, CardBuilder.Layout.COLUMNS)
                .setIcon(R.drawable.bluetooth)
                .setText(R.string.action_bluetooth));
        cards.add(new CardBuilder(context, CardBuilder.Layout.COLUMNS)
                .setIcon(R.drawable.bluetooth)
                .setText(R.string.action_bluetooth_pair));

        return cards;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            startActivity(new Intent(this, PolarActivity.class));
        } else if (position == 1) {
            startActivity(new Intent(this, NavActivity.class));
        } else if (position == 2) {
            startActivity(new Intent(this, BluetoothActivity.class));
        } else if (position == 3) {
            startActivity(new Intent(this, BluetoothPairActivity.class));
        } else {
            Log.e(TAG, "Invalid click position = " + position);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cardScroller.activate();
    }

    @Override
    protected void onPause() {
        cardScroller.deactivate();
        super.onPause();
    }

}
