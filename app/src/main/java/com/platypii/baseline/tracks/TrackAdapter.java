package com.platypii.baseline.tracks;

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

public class TrackAdapter extends BaseAdapter {
    // Item types
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_TRACK_FILE = 1;

    private final List<TrackFile> tracks;

    private final LayoutInflater inflater;
    private List<ListItem> items;

    public TrackAdapter(Context context, List<TrackFile> tracks) {
        this.tracks = tracks;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        items = populateItems(tracks);
    }

    private static List<ListItem> populateItems(List<TrackFile> tracks) {
        // Count synced and not synced
        int num_synced = 0;
        int num_unsynced = 0;
        for(TrackFile jump : tracks) {
            if(jump.getCloudData() == null) {
                num_unsynced++;
            } else {
                num_synced++;
            }
        }
        final List<ListItem> items = new ArrayList<>();
        // Add Unsynced tracks
        if(num_unsynced > 0) {
            items.add(new ListHeader("Not synced (local only)"));
            for (TrackFile trackFile : tracks) {
                if (trackFile.getCloudData() == null) {
                    items.add(new ListTrackFile(trackFile));
                }
            }
        }
        // Add synced tracks
        if(num_synced > 0) {
            items.add(new ListHeader("Synced"));
            for (TrackFile trackFile : tracks) {
                if (trackFile.getCloudData() != null) {
                    items.add(new ListTrackFile(trackFile));
                }
            }
        }
        return items;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final ListItem item = getItem(position);
        final int itemType = item.getType();

        if(convertView == null) {
            switch(itemType) {
                case TYPE_HEADER:
                    convertView = inflater.inflate(R.layout.track_list_header, parent, false);
                    break;
                case TYPE_TRACK_FILE:
                    convertView = inflater.inflate(R.layout.track_list_item, parent, false);
                    break;
            }
        }

        // Update views
        switch(itemType) {
            case TYPE_HEADER:
                final ListHeader header = (ListHeader) item;
                final TextView headerNameView = (TextView) convertView.findViewById(R.id.list_header_name);
                headerNameView.setText(header.name);
                break;
            case TYPE_TRACK_FILE:
                final TrackFile track = ((ListTrackFile) item).track;
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

    public TrackFile getTrack(int position) {
        final ListItem item = items.get(position);
        if(item instanceof ListTrackFile) {
            return ((ListTrackFile) item).track;
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
        return item.getType();
    }

    // Create list item types so we can have headers
    private interface ListItem {
        int getType();
    }

    private static class ListHeader implements ListItem {
        final String name;
        ListHeader(String name) {
            this.name = name;
        }
        public int getType() {
            return TYPE_HEADER;
        }
    }

    private static class ListTrackFile implements ListItem {
        final TrackFile track;
        ListTrackFile(TrackFile track) {
            this.track = track;
        }
        public int getType() {
            return TYPE_TRACK_FILE;
        }
    }

}
