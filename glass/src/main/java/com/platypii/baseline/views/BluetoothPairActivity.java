package com.platypii.baseline.views;

import com.platypii.baseline.bluetooth.BluetoothPairCardAdapter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import com.google.android.glass.widget.CardScrollView;

public class BluetoothPairActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private CardScrollView cardScroller;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        cardScroller = new CardScrollView(this);
        cardScroller.setAdapter(new BluetoothPairCardAdapter(this));
        setContentView(cardScroller);
        cardScroller.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO: Go to bluetooth activity
        finish();
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
