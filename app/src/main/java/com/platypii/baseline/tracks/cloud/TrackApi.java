package com.platypii.baseline.tracks.cloud;

import com.platypii.baseline.tracks.TrackMetadata;

import androidx.annotation.NonNull;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface TrackApi {

    @NonNull
    @GET("/v1/tracks")
    Call<List<TrackMetadata>> list();

}
