package com.platypii.baseline.views.laser;

import android.util.Log;
import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.cloud.AuthState;
import com.platypii.baseline.lasers.LaserProfile;
import com.platypii.baseline.lasers.LaserSearch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.platypii.baseline.views.laser.LaserListItem.TYPE_HEADER;
import static com.platypii.baseline.views.laser.LaserListItem.TYPE_LASER;

/**
 * Profile adapter renders a list of track and laser profiles
 */
class LaserAdapter extends BaseAdapter {
    private static final String TAG = "LaserAdapter";

    @NonNull
    private final LayoutInflater inflater;
    private final List<LaserListItem> items = new ArrayList<>();

    // Search filter
    @NonNull
    private String filter;

    LaserAdapter(@NonNull Context context, @NonNull String filter) {
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.filter = filter;
        populateItems();
    }

    private void populateItems() {
        final long startTime = System.currentTimeMillis();
        final String userId = AuthState.getUser();
        items.clear();
        // Add unsynced lasers
        int sectionCount = 0;
        final List<LaserProfile> unsynced = Services.lasers.unsynced.list();
        if (unsynced != null && !unsynced.isEmpty()) {
            for (LaserProfile laser : unsynced) {
                if (LaserSearch.matchLaser(laser, filter)) {
                    if (sectionCount++ == 0) {
                        items.add(new LaserListItem.ListHeader("Not synced"));
                    }
                    items.add(new LaserListItem.ListLaser(laser));
                }
            }
        }
        final List<LaserProfile> lasers = Services.lasers.cache.list();
        if (lasers != null && !lasers.isEmpty()) {
            // Sort by country, by name
            Collections.sort(lasers, (o1, o2) -> {
                final String str1 = (o1.place == null ? "" : o1.place.country) + " " + o1.name;
                final String str2 = (o2.place == null ? "" : o2.place.country) + " " + o2.name;
                return str1.compareTo(str2);
            });

            // Add my lasers
            if (userId != null) {
                sectionCount = 0;
                for (LaserProfile laser : lasers) {
                    if (userId.equals(laser.user_id) && LaserSearch.matchLaser(laser, filter)) {
                        if (sectionCount++ == 0) {
                            items.add(new LaserListItem.ListHeader("My Profiles"));
                        }
                        items.add(new LaserListItem.ListLaser(laser));
                    }
                }
            }

            // Add public lasers
            sectionCount = 0;
            for (LaserProfile laser : lasers) {
                if ((laser.user_id == null || !laser.user_id.equals(userId)) && laser.isPublic && LaserSearch.matchLaser(laser, filter)) {
                    if (sectionCount++ == 0) {
                        items.add(new LaserListItem.ListHeader("Public Profiles"));
                    }
                    items.add(new LaserListItem.ListLaser(laser));
                }
            }

            // Add all remaining lasers
            sectionCount = 0;
            for (LaserProfile laser : lasers) {
                if ((laser.user_id == null || !laser.user_id.equals(userId)) && !laser.isPublic && LaserSearch.matchLaser(laser, filter)) {
                    if (sectionCount++ == 0) {
                        items.add(new LaserListItem.ListHeader("All Profiles"));
                    }
                    items.add(new LaserListItem.ListLaser(laser));
                }
            }
        }
        final long dt = System.currentTimeMillis() - startTime;
        Log.d(TAG, "Populate laser adapter: \"" + filter + "\" (" + dt + " ms)");
    }

    void setFilter(@NonNull String filter) {
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
        final LaserListItem item = getItem(position);
        final int itemType = item.getType();

        if (convertView == null) {
            switch (itemType) {
                case TYPE_HEADER:
                    convertView = inflater.inflate(R.layout.track_list_header, parent, false);
                    break;
                case TYPE_LASER:
                    convertView = inflater.inflate(R.layout.track_list_item, parent, false);
                    break;
            }
        }

        switch (itemType) {
            case TYPE_HEADER:
                final LaserListItem.ListHeader header = (LaserListItem.ListHeader) item;
                // Update header
                final TextView headerNameView = convertView.findViewById(R.id.list_header_name);
                headerNameView.setText(header.name);
                break;
            case TYPE_LASER:
                final LaserProfile laser = ((LaserListItem.ListLaser) item).laser;
                // Update name
                final TextView nameView = convertView.findViewById(R.id.list_item_name);
                nameView.setText(laser.name);
                // Update subtitle
                final TextView subView = convertView.findViewById(R.id.list_item_subtitle);
                subView.setText(subtitle(laser));
                break;
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public LaserListItem getItem(int position) {
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

    @Override
    public int getItemViewType(int position) {
        if (items.size() <= position) {
            return TYPE_LASER;
        } else {
            return items.get(position).getType();
        }
    }

    private String subtitle(LaserProfile laser) {
        final String points = laser.points.size() == 1 ? "1 point" : laser.points.size() + " points";
        if (laser.place != null) {
            if (laser.place.region.isEmpty()) {
                return laser.place.country + " (" + points + ")";
            } else {
                return laser.place.region + ", " + laser.place.country + " (" + points + ")";
            }
        } else {
            return points;
        }
    }

}
