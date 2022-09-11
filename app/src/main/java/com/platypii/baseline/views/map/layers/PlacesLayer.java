package com.platypii.baseline.views.map.layers;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.maps.model.LatLng;
import com.platypii.baseline.Services;
import com.platypii.baseline.databinding.MapInfoWindowBinding;
import com.platypii.baseline.measurements.LatLngAlt;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.places.Place;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.views.map.MapState;
import com.platypii.baseline.views.map.PlaceIcons;

import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PlacesLayer extends MapLayer {
    private static final String TAG = "PlacesLayer";

    @Nullable
    private GoogleMap map;
    @NonNull
    private final LayoutInflater inflater;
    @Nullable
    private final Context context;
    @NonNull
    private final Map<Place, Marker> placeMarkers = new HashMap<>();

    public PlacesLayer(@NonNull LayoutInflater inflater, @Nullable Context context) {
        this.inflater = inflater;
        this.context = context;
    }

    @Override
    public void onAdd(@NonNull GoogleMap map) {
        this.map = map;
        map.setOnInfoWindowClickListener(marker -> {
            Log.i(TAG, "Place click " + marker.getTitle());
            if (context != null) {
                // Copy lat,lng to clipboard and show toast
                final ClipboardManager clippy = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                final LatLng position = marker.getPosition();
                final String ll = position.latitude + "," + position.longitude;
                clippy.setPrimaryClip(ClipData.newPlainText(null, ll));
                Toast.makeText(context, "Copied: " + ll, Toast.LENGTH_SHORT).show();
            }
        });
        map.setInfoWindowAdapter(new PlaceInfoWindow());
    }

    @Override
    public void update() {
        if (map != null) {
            final LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
            final List<Place> places = filterPlaces(bounds);
            // Loop to remove old markers
            final Iterator<Map.Entry<Place, Marker>> it = placeMarkers.entrySet().iterator();
            while (it.hasNext()) {
                final Map.Entry<Place, Marker> entry = it.next();
                final Place place = entry.getKey();
                if (!places.contains(place)) {
                    // Remove from GoogleMap and marker map
                    entry.getValue().remove();
                    it.remove();
                }
            }
            // Add new places
            for (Place place : places) {
                if (!placeMarkers.containsKey(place)) {
                    addMarker(place);
                }
            }
        }
    }

    /**
     * Return places that should be shown based on place options.
     */
    private List<Place> filterPlaces(LatLngBounds bounds) {
        final List<Place> places = Services.places.getPlacesByArea(bounds);
        // Loop to remove filtered place types
        final Iterator<Place> it = places.listIterator();
        while (it.hasNext()) {
            final Place place = it.next();
            if ("DZ".equals(place.objectType) && !MapState.showDropzones) {
                it.remove();
            } else if (place.isBASE() && !MapState.showExits) {
                it.remove();
            } else if ("PG".equals(place.objectType) && !MapState.showLaunches) {
                it.remove();
            }
        }
        return places;
    }

    private void addMarker(@NonNull Place place) {
        if (map != null) {
            final Marker placeMarker = map.addMarker(new MarkerOptions()
                    .position(place.latLng())
                    .visible(true)
                    .alpha(0.5f)
                    .anchor(0.5f, 1f)
                    .flat(true)
                    .icon(PlaceIcons.icon(place))
                    .title(place.shortName())
                    .snippet(snippet(place))
            );
            placeMarkers.put(place, placeMarker);
        }
    }

    @NonNull
    private String snippet(Place p) {
        final String region = p.region == null || p.region.isEmpty() ? p.country : p.region + ", " + p.country;
        final String ll = LatLngAlt.formatLatLng(p.lat, p.lng);
        final String alt = Double.isNaN(p.alt) ? "" : "\n" + Convert.distance(p.alt) + " MSL";
        final MLocation loc = Services.location.lastLoc;
        final String dist = loc == null ? "" : Convert.distance3(loc.distanceTo(p.latLng()));
        final String distString = dist.isEmpty() ? "" : "\n" + dist + " away";
        return region + "\n" + ll + alt + distString;
    }

    private class PlaceInfoWindow implements GoogleMap.InfoWindowAdapter {
        @Nullable
        @Override
        public View getInfoContents(@NonNull Marker marker) {
            final String title = marker.getTitle();
            if (title != null && !title.isEmpty()) {
                final MapInfoWindowBinding binding = MapInfoWindowBinding.inflate(inflater);
                binding.infoTitle.setText(title);
                binding.infoSnippet.setText(marker.getSnippet());
                return binding.getRoot();
            } else {
                return null;
            }
        }

        @Nullable
        @Override
        public View getInfoWindow(@NonNull Marker marker) {
            return null;
        }
    }

}
