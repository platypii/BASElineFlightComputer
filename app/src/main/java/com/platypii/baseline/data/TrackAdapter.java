package com.platypii.baseline.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.platypii.baseline.R;

import java.util.ArrayList;
import java.util.List;

// TODO: notifydatasetchanged probably broken

public class TrackAdapter extends BaseAdapter {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private final List<Jump> tracks;

    private final LayoutInflater inflater;
    private List<ListItem> items;

    public TrackAdapter(Context context, List<Jump> tracks) {
        this.tracks = tracks;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        items = populateItems(tracks);
    }

    private static List<ListItem> populateItems(List<Jump> tracks) {
        // Count synced and not synced
        int num_synced = 0;
        int num_unsynced = 0;
        for(Jump jump : tracks) {
            if(jump.getCloudData() == null) {
                num_unsynced++;
            } else {
                num_synced++;
            }
        }
        final List<ListItem> items = new ArrayList<>();
        // Add Unsynced tracks
        if(num_unsynced > 0) {
            items.add(new TrackHeader("Not synced (local only)"));
            for (Jump jump : tracks) {
                if (jump.getCloudData() == null) {
                    items.add(new TrackItem(jump));
                }
            }
        }
        // Add synced tracks
        if(num_synced > 0) {
            items.add(new TrackHeader("Synced"));
            for (Jump jump : tracks) {
                if (jump.getCloudData() != null) {
                    items.add(new TrackItem(jump));
                }
            }
        }
        return items;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final ListItem item = getItem(position);
        final int itemType = getItemViewType(position);

        if(convertView == null) {
            switch(itemType) {
                case TYPE_HEADER:
                    convertView = inflater.inflate(R.layout.track_list_header, parent, false);
                    break;
                case TYPE_ITEM:
                    convertView = inflater.inflate(R.layout.track_list_item, parent, false);
                    break;
            }
        }

        // Update views
        switch(itemType) {
            case TYPE_HEADER:
                final TrackHeader header = (TrackHeader) item;
                final TextView headerNameView = (TextView) convertView.findViewById(R.id.list_header_name);
                headerNameView.setText(header.name);
                break;
            case TYPE_ITEM:
                final Jump track = ((TrackItem) item).track;
                final TextView itemNameView = (TextView) convertView.findViewById(R.id.list_item_name);
                final TextView itemSizeView = (TextView) convertView.findViewById(R.id.list_item_filesize);
                itemNameView.setText(track.toString());
                itemSizeView.setText(track.getSize());
                break;
        }

        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        items = populateItems(tracks);
        super.notifyDataSetChanged();
    }

    @Override
    public ListItem getItem(int position) {
        return items.get(position);
    }

    public Jump getTrack(int position) {
        final ListItem item = items.get(position);
        if(item instanceof TrackItem) {
            return ((TrackItem) item).track;
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    // Set the number of list item types to 2 (tracks and headers)
    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        final ListItem item = getItem(position);
        if(item instanceof TrackHeader) {
            return TYPE_HEADER;
        } else {
            return TYPE_ITEM;
        }
    }

    // Create two types of list item so we can have headers
    interface ListItem {}

    private static class TrackHeader implements ListItem {
        public final String name;
        TrackHeader(String name) {
            this.name = name;
        }
    }

    private static class TrackItem implements ListItem {
        public final Jump track;
        TrackItem(Jump track) {
            this.track = track;
        }
    }

}
