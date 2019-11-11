package com.platypii.baseline.views.laser;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.views.charts.layers.ProfileLayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Profile adapter renders a list of track and laser profiles
 */
class ProfileAdapter extends BaseAdapter {

    @NonNull
    private final LayoutInflater inflater;
    private final List<ProfileLayer> layers = new ArrayList<>();

    ProfileAdapter(@NonNull Context context) {
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setLayers(@NonNull List<ProfileLayer> layers) {
        this.layers.clear();
        this.layers.addAll(layers);
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
        nameView.setText(layer.name());
        // Update color
        convertView.findViewById(R.id.list_item_color).setBackgroundColor(layer.color);
        // Bind delete button
        convertView.findViewById(R.id.list_item_delete).setOnClickListener(v -> {
            // Remove from layers
            Services.cloud.lasers.layers.remove(layer.id());
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
}
