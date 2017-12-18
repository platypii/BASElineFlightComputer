package com.platypii.baseline.views;

import android.view.View;
import android.view.ViewGroup;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import java.util.List;

/**
 * Adapter class that handles list of cards.
 */
class CardAdapter extends CardScrollAdapter {

    private final List<CardBuilder> cards;

    CardAdapter(List<CardBuilder> cards) {
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
        final CardBuilder card = (CardBuilder) getItem(position);
        return card.getView(convertView, parent);
    }

    @Override
    public int getPosition(Object item) {
        final CardBuilder card = (CardBuilder) item;
        return cards.indexOf(card);
    }
}