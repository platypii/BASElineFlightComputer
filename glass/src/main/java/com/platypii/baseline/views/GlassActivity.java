package com.platypii.baseline.views;

import com.platypii.baseline.R;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollView;
import java.util.ArrayList;
import java.util.List;

public class GlassActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private CardScrollView cardScroller;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        cardScroller = new CardScrollView(this);
        cardScroller.setAdapter(new CardAdapter(createCards(this)));
        cardScroller.setOnItemClickListener(this);

        setContentView(cardScroller);
    }

    private List<Card> createCards(Context context) {
        ArrayList<Card> cards = new ArrayList<Card>();

        // Add cards that demonstrate TEXT layouts.
        cards.add(new Card(context)
                .setText(R.string.action_polar));
        cards.add(new Card(context)
                .setText(R.string.action_bluetooth));

        return cards;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Card card = (Card) cardScroller.getItemAtPosition(position);
        if(card.getText().equals("Polar")) {
            startActivity(new Intent(this, PolarActivity.class));
        } else if(card.getText().equals("Bluetooth")) {
            startActivity(new Intent(this, BluetoothActivity.class));
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
