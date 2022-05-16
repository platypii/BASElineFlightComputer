package com.platypii.baseline.location;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.places.Place;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Profile adapter renders a list of track and laser profiles
 */
public class PlaceAdapter extends BaseAdapter {

    @NonNull
    private final LayoutInflater inflater;
    private final List<Place> items = new ArrayList<>();

    // Search filter
    @NonNull
    private String filter = "";

    public PlaceAdapter(@NonNull Context context) {
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        populateItems();
    }

    private void populateItems() {
        items.clear();
        // Fast filter for empty search string
        if (filter.isEmpty()) {
            return;
        }
        final List<Place> places = Services.places.getPlaces();
        // Add places
        if (places != null && !places.isEmpty()) {
            // Sort by country, by name
            Collections.sort(places, (o1, o2) -> {
                // TODO: Handle nulls
                final String str1 = o1.country + " " + o1.name;
                final String str2 = o2.country + " " + o2.name;
                return str1.compareTo(str2);
            });
            // Add public lasers
            for (Place place : places) {
                if (PlaceSearch.matchPlace(place, filter)) {
                    items.add(place);
                }
            }
        }
    }

    public void setFilter(@NonNull String filter) {
        this.filter = filter;
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        populateItems();
        super.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.track_list_item, parent, false);
        }

        final Place place = getItem(position);
        // Update name
        final TextView nameView = convertView.findViewById(R.id.list_item_name);
        nameView.setText(place.name);
        // Update subtitle
        final TextView subtitle = convertView.findViewById(R.id.list_item_subtitle);
        subtitle.setText(place.country);

        // Update place icon
        if (place.isBASE()) {
            subtitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.type_base, 0, 0, 0);
        } else if (place.isSkydive()) {
            subtitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.type_sky, 0, 0, 0);
        } else if ("PG".equals(place.objectType)) {
            subtitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.type_pg, 0, 0, 0);
        } else {
            subtitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Place getItem(int position) {
        if (items.size() <= position) {
            return null;
        } else {
            return items.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

}
