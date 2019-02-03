package com.platypii.baseline.views.laser;

import com.platypii.baseline.R;
import com.platypii.baseline.views.charts.layers.ProfileLayer;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.List;

/**
 * Profile adapter renders a list of track and laser profiles
 */
class ProfileAdapter extends BaseAdapter {

    private final LaserActivity laserActivity;
    private final LayoutInflater inflater;
    private final List<ProfileLayer> layers;

    private String filter = "";

    ProfileAdapter(@NonNull LaserActivity laserActivity, List<ProfileLayer> layers) {
        this.laserActivity = laserActivity;
        this.inflater = (LayoutInflater) laserActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.layers = layers;
    }

    void setFilter(@NonNull String filter) {
        this.filter = filter;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ProfileLayer layer = getItem(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.laser_item, parent, false);
        }

        // Update name
        final TextView nameView = convertView.findViewById(R.id.list_item_name);
        nameView.setText(layer.name);
        // Update color
        convertView.findViewById(R.id.list_item_color).setBackgroundColor(layer.color);
        // Bind delete button
        convertView.findViewById(R.id.list_item_delete).setOnClickListener(v -> {
            // Remove from layers
            laserActivity.removeLayer(layer);
        });

        return convertView;
    }

    @Override
    public int getCount() {
        return layers.size();
    }

    @Override
    public ProfileLayer getItem(int position) {
        return layers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    void clickItem(int position, @NonNull Context context) {
        final ProfileLayer layer = layers.get(position);
        // TODO: Enable / disable
    }

}
