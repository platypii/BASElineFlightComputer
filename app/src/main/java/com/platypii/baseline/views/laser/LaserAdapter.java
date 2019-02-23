package com.platypii.baseline.views.laser;

import com.platypii.baseline.R;
import com.platypii.baseline.cloud.AuthState;
import com.platypii.baseline.laser.LaserProfile;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

import static com.platypii.baseline.views.laser.LaserListItem.TYPE_HEADER;
import static com.platypii.baseline.views.laser.LaserListItem.TYPE_LASER;

/**
 * Profile adapter renders a list of track and laser profiles
 */
class LaserAdapter extends BaseAdapter {

    private final LayoutInflater inflater;
    private final List<LaserListItem> items = new ArrayList<>();

    LaserAdapter(@NonNull Context context) {
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setLayers(@NonNull List<LaserProfile> lasers) {
        final String userId = AuthState.getUser();
        items.clear();
        if (userId != null) {
            items.add(new LaserListItem.ListHeader("My Profiles"));
            // Add my lasers
            for (LaserProfile laser : lasers) {
                if (userId.equals(laser.user_id)) {
                    items.add(new LaserListItem.ListLaser(laser));
                }
            }
        }
        // Add public lasers
        items.add(new LaserListItem.ListHeader("Public Profiles"));
        for (LaserProfile laser : lasers) {
            if (laser.user_id == null || !laser.user_id.equals(userId)) {
                items.add(new LaserListItem.ListLaser(laser));
            }
        }
        notifyDataSetChanged();
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
                // final TextView subView = convertView.findViewById(R.id.list_item_subtitle);
                // TODO: subView.setText(laser.place);
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
        return items.get(position);
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
        return getItem(position).getType();
    }

}
