package com.platypii.baseline.tracks;

import com.platypii.baseline.Intents;
import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.cloud.CloudData;
import com.platypii.baseline.cloud.UploadManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

class TrackAdapter extends BaseAdapter {

    // Item types
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_TRACK_FILE = 1;
    private static final int TYPE_TRACK_DATA = 2;

    private final List<TrackFile> tracks;

    private final LayoutInflater inflater;
    private List<ListItem> items;

    TrackAdapter(@NonNull Context context, @NonNull List<TrackFile> tracks) {
        this.tracks = tracks;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        items = populateItems(tracks);
    }

    @NonNull
    private static List<ListItem> populateItems(@NonNull List<TrackFile> trackFiles) {
        final List<ListItem> items = new ArrayList<>();
        // Add Unsynced tracks
        if(!trackFiles.isEmpty()) {
            items.add(new ListHeader("Not synced (local only)"));
            for (TrackFile trackFile : trackFiles) {
                items.add(new ListTrackFile(trackFile));
            }
        }
        // Add cloud tracks
        final List<CloudData> trackList = Services.cloud.listing.cache.list();
        if(trackList != null && !trackList.isEmpty()) {
            items.add(new ListHeader("Synced"));
            for(CloudData trackData : trackList) {
                items.add(new ListTrackData(trackData));
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
                case TYPE_TRACK_DATA:
                    convertView = inflater.inflate(R.layout.track_list_item, parent, false);
                    break;
            }
        }

        // Update views
        switch(itemType) {
            case TYPE_HEADER:
                final ListHeader header = (ListHeader) item;
                final TextView headerNameView = convertView.findViewById(R.id.list_header_name);
                headerNameView.setText(header.name);
                break;
            case TYPE_TRACK_FILE:
                final TrackFile trackFile = ((ListTrackFile) item).track;
                final TextView itemNameView = convertView.findViewById(R.id.list_item_name);
                final TextView itemSizeView = convertView.findViewById(R.id.list_item_subtitle);
                final ProgressBar itemSpinner = convertView.findViewById(R.id.list_spinner);
                itemNameView.setText(trackFile.toString());
                itemSizeView.setText(trackFile.getSize());
                final int uploadState = Services.cloud.uploads.getState(trackFile);
                if(uploadState == UploadManager.UPLOADING) {
                    itemSpinner.setVisibility(View.VISIBLE);
                } else {
                    itemSpinner.setVisibility(View.GONE);
                }
                break;
            case TYPE_TRACK_DATA:
                final CloudData trackData = ((ListTrackData) item).track;
                final TextView itemNameView2 = convertView.findViewById(R.id.list_item_name);
                final TextView itemSizeView2 = convertView.findViewById(R.id.list_item_subtitle);
                final ProgressBar itemSpinner2 = convertView.findViewById(R.id.list_spinner);
                itemNameView2.setText(trackData.date_string);
                itemSizeView2.setText(trackData.location);
                itemSpinner2.setVisibility(View.GONE);
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

    public void clickItem(int position, @NonNull Context context) {
        final ListItem item = items.get(position);
        final int itemType = item.getType();
        switch(itemType) {
            case TYPE_TRACK_FILE:
                final TrackFile trackFile = ((ListTrackFile) item).track;
                Intents.openTrackActivity(context, trackFile);
                break;
            case TYPE_TRACK_DATA:
                final CloudData trackData = ((ListTrackData) item).track;
                Intents.openTrackDataActivity(context, trackData);
                break;
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

    @Override
    public int getViewTypeCount() {
        return 3;
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

    private static class ListTrackData implements ListItem {
        final CloudData track;
        ListTrackData(CloudData track) {
            this.track = track;
        }
        public int getType() {
            return TYPE_TRACK_DATA;
        }
    }

}
