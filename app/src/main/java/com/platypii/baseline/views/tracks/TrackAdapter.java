package com.platypii.baseline.views.tracks;

import com.platypii.baseline.Intents;
import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.cloud.CloudData;
import com.platypii.baseline.tracks.TrackFile;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

import static com.platypii.baseline.views.tracks.TrackListItem.*;

/**
 * Track adapter renders a list of tracks, both local and remote
 */
public class TrackAdapter extends BaseAdapter {

    @NonNull
    private final LayoutInflater inflater;
    private List<TrackListItem> items;

    @NonNull
    private String filter = "";

    TrackAdapter(@NonNull Context context) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        populateItems();
    }

    private void populateItems() {
        final List<TrackListItem> updated = new ArrayList<>();
        // Add local tracks
        final List<TrackFile> localTracks = Services.trackStore.getLocalTracks();
        if (!localTracks.isEmpty()) {
            updated.add(new ListHeader("Not synced"));
            for (TrackFile track : localTracks) {
                updated.add(new ListTrackFile(track));
            }
        }
        // Add cloud tracks
        final List<CloudData> cloudTracks = Services.cloud.listing.cache.list();
        if (cloudTracks != null && !cloudTracks.isEmpty()) {
            updated.add(new ListHeader("Synced"));
            for (CloudData track : cloudTracks) {
                if (filterMatch(track)) {
                    updated.add(new ListTrackData(track));
                }
            }
        }
        items = updated;
    }

    /**
     * Return true if the track matches the search filter string
     * TODO: Search track.stats.plan.name
     */
    private boolean filterMatch(@NonNull CloudData track) {
        // Make a lower case super string of all properties we want to search
        final StringBuilder sb = new StringBuilder();
        if (track.place != null) {
            sb.append(track.place.name);
            sb.append(' ');
            sb.append(track.place.region);
            sb.append(' ');
            sb.append(track.place.country);
            sb.append(' ');
            sb.append(track.place.objectType);
            if (track.place.wingsuitable) {
                sb.append(" wingsuit");
            }
            if ("DZ".equals(track.place.objectType)) {
                sb.append(" skydive");
            }
            if (track.place.isBASE()) {
                sb.append(" BASE");
            }
        }
        final String superString = sb.toString().toLowerCase();
        // Break into tokens
        for (String token : filter.toLowerCase().split(" ")) {
            if (!superString.contains(token)) {
                return false;
            }
        }
        return true;
    }

    void setFilter(@NonNull String filter) {
        this.filter = filter;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final TrackListItem item = getItem(position);
        final int itemType = item.getType();

        if (convertView == null) {
            switch (itemType) {
                case TYPE_HEADER:
                    convertView = inflater.inflate(R.layout.track_list_header, parent, false);
                    break;
                case TYPE_TRACK_LOCAL:
                case TYPE_TRACK_REMOTE:
                    convertView = inflater.inflate(R.layout.track_list_item, parent, false);
                    break;
            }
        }

        // Update views
        switch (itemType) {
            case TYPE_HEADER:
                final ListHeader header = (ListHeader) item;
                final TextView headerNameView = convertView.findViewById(R.id.list_header_name);
                headerNameView.setText(header.name);
                break;
            case TYPE_TRACK_LOCAL:
                final TrackFile trackFile = ((ListTrackFile) item).track;
                final TextView itemNameView = convertView.findViewById(R.id.list_item_name);
                final TextView itemSizeView = convertView.findViewById(R.id.list_item_subtitle);
                final ProgressBar itemSpinner = convertView.findViewById(R.id.list_spinner);
                itemNameView.setText(trackFile.getName());
                itemSizeView.setText(trackFile.getSize());

                // Update based on logging and sync state
                if (Services.trackStore.isUploading(trackFile)) {
                    // Show upload progress
                    final int progress = Services.trackStore.getUploadProgress(trackFile);
                    final int filesize = (int) trackFile.file.length();
                    itemSpinner.setProgress(progress);
                    itemSpinner.setMax(filesize);
                    itemSpinner.setVisibility(View.VISIBLE);
                } else {
                    itemSpinner.setVisibility(View.GONE);
                }
                break;
            case TYPE_TRACK_REMOTE:
                final CloudData trackData = ((ListTrackData) item).track;
                final TextView itemNameView2 = convertView.findViewById(R.id.list_item_name);
                final TextView itemSizeView2 = convertView.findViewById(R.id.list_item_subtitle);
                final ProgressBar itemSpinner2 = convertView.findViewById(R.id.list_spinner);
                itemNameView2.setText(trackData.date_string);
                itemSizeView2.setText(trackData.location());
                itemSpinner2.setVisibility(View.GONE);
                break;
        }

        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        populateItems();
        super.notifyDataSetChanged();
    }

    @Override
    public TrackListItem getItem(int position) {
        return items.get(position);
    }

    void clickItem(int position, @NonNull Context context) {
        final TrackListItem item = items.get(position);
        switch (item.getType()) {
            case TYPE_TRACK_LOCAL:
                final TrackFile trackFile = ((ListTrackFile) item).track;
                Intents.openTrackLocal(context, trackFile);
                break;
            case TYPE_TRACK_REMOTE:
                final CloudData trackData = ((ListTrackData) item).track;
                Intents.openTrackRemote(context, trackData);
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
        final TrackListItem item = getItem(position);
        return item.getType();
    }

}
