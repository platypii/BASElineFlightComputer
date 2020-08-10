package com.platypii.baseline.views.tracks;

import com.platypii.baseline.Intents;
import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.tracks.TrackFile;
import com.platypii.baseline.tracks.TrackMetadata;
import com.platypii.baseline.tracks.TrackSearch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.platypii.baseline.views.tracks.TrackListItem.ListHeader;
import static com.platypii.baseline.views.tracks.TrackListItem.ListTrackData;
import static com.platypii.baseline.views.tracks.TrackListItem.ListTrackFile;
import static com.platypii.baseline.views.tracks.TrackListItem.TYPE_HEADER;
import static com.platypii.baseline.views.tracks.TrackListItem.TYPE_TRACK_LOCAL;
import static com.platypii.baseline.views.tracks.TrackListItem.TYPE_TRACK_REMOTE;

/**
 * Track adapter renders a list of tracks, both local and remote
 */
public class TrackAdapter extends BaseAdapter {

    @NonNull
    private final Context context;
    @NonNull
    private final LayoutInflater inflater;
    private List<TrackListItem> items;

    @NonNull
    private String filter = "";

    TrackAdapter(@NonNull Context context) {
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        populateItems();
    }

    private void populateItems() {
        final List<TrackListItem> updated = new ArrayList<>();
        // Add local tracks
        final List<TrackFile> localTracks = Services.tracks.store.getLocalTracks();
        if (!localTracks.isEmpty()) {
            updated.add(new ListHeader("Not synced"));
            for (TrackFile track : localTracks) {
                updated.add(new ListTrackFile(track));
            }
        }
        // Add cloud tracks
        final List<TrackMetadata> cloudTracks = Services.tracks.cache.list();
        if (cloudTracks != null && !cloudTracks.isEmpty()) {
            updated.add(new ListHeader("Synced"));
            for (TrackMetadata track : cloudTracks) {
                if (TrackSearch.matchTrack(track, filter)) {
                    updated.add(new ListTrackData(track));
                }
            }
        }
        items = updated;
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
                final View itemCheck = convertView.findViewById(R.id.list_check);
                itemNameView.setText(trackFile.getName());
                itemSizeView.setText(trackFile.getSize());

                // Update based on logging and sync state
                if (Services.tracks.store.isUploading(trackFile)) {
                    // Show upload progress
                    final int progress = Services.tracks.store.getUploadProgress(trackFile);
                    final int filesize = (int) trackFile.file.length();
                    itemSpinner.setProgress(progress);
                    itemSpinner.setMax(filesize);
                    itemSpinner.setVisibility(View.VISIBLE);
                } else {
                    itemSpinner.setVisibility(View.GONE);
                }
                itemCheck.setVisibility(View.GONE);
                break;
            case TYPE_TRACK_REMOTE:
                final TrackMetadata trackData = ((ListTrackData) item).track;
                final TextView itemNameView2 = convertView.findViewById(R.id.list_item_name);
                final TextView itemSizeView2 = convertView.findViewById(R.id.list_item_subtitle);
                final ProgressBar itemSpinner2 = convertView.findViewById(R.id.list_spinner);
                final View itemCheck2 = convertView.findViewById(R.id.list_check);
                itemNameView2.setText(trackData.date_string);
                itemSizeView2.setText(trackData.subtitle());
                itemSpinner2.setVisibility(View.GONE);
                if (trackData.abbrvFile(context).exists() || trackData.localFile(context).exists()) {
                    itemCheck2.setVisibility(View.VISIBLE);
                } else {
                    itemCheck2.setVisibility(View.GONE);
                }
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
                final TrackMetadata trackData = ((ListTrackData) item).track;
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
        return getItem(position).getType();
    }

}
