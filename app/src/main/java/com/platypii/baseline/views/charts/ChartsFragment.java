package com.platypii.baseline.views.charts;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import com.platypii.baseline.R;
import com.platypii.baseline.tracks.TrackData;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.views.tracks.TrackLocalActivity;
import java.io.File;
import java.lang.ref.WeakReference;
import java9.util.concurrent.CompletableFuture;

public class ChartsFragment extends Fragment {
    private static final String TAG = "ChartsFrag";

    public CompletableFuture<TrackData> trackData = new CompletableFuture<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.charts_frag, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final ChartPagerAdapter chartPagerAdapter = new ChartPagerAdapter(getChildFragmentManager());
        final ViewPager viewPager = view.findViewById(R.id.chartPager);
        viewPager.setAdapter(chartPagerAdapter);

        // Load track from extras
        final File trackFile = getTrackFile();
        if (trackFile != null) {
            Log.i(TAG, "Loading track data");
            // Load async
            new LoadTask(trackFile, this).execute();
        } else {
            Exceptions.report(new IllegalStateException("Failed to load track file from extras"));
            // Finish activity
            final Activity activity = getActivity();
            if (activity != null) {
                getActivity().finish();
            }
        }
    }

    /**
     * Gets the track file from activity extras
     */
    @Nullable
    private File getTrackFile() {
        final Activity activity = getActivity();
        if (activity != null) {
            final Bundle extras = activity.getIntent().getExtras();
            if (extras != null) {
                final String extraTrackFile = extras.getString(TrackLocalActivity.EXTRA_TRACK_FILE);
                if (extraTrackFile != null) {
                    return new File(extraTrackFile);
                }
            }
        }
        return null;
    }

    private static class LoadTask extends AsyncTask<Void,Void,Void> {
        @NonNull
        private final WeakReference<ChartsFragment> fragRef;
        private final File trackFile;

        private LoadTask(@NonNull File trackFile, ChartsFragment frag) {
            this.trackFile = trackFile;
            this.fragRef = new WeakReference<>(frag);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            final ChartsFragment frag = fragRef.get();
            if (frag != null) {
                frag.trackData.complete(new TrackData(trackFile));
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void v) {
        }
    }

}
