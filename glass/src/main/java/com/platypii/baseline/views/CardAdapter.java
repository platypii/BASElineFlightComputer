package com.platypii.baseline.views;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollAdapter;
import java.util.List;

/**
 * Adapter class that handles list of cards.
 */
class CardAdapter extends CardScrollAdapter {

    private final List<Card> cards;

    CardAdapter(List<Card> cards) {
        this.cards = cards;
    }

    @Override
    public int getCount() {
        return cards.size();
    }

    @Override
    public Object getItem(int position) {
        return cards.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Card card = (Card) getItem(position);
        return card.getView(convertView, parent);
    }

    @Override
    public int getPosition(Object item) {
        for (int i = 0; i < cards.size(); i++) {
            if (getItem(i).equals(item)) {
                return i;
            }
        }
        return AdapterView.INVALID_POSITION;
    }
}