package com.platypii.baseline.cloud.tracks;

import com.platypii.baseline.cloud.CloudData;

import androidx.annotation.NonNull;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface TrackApi {

    @NonNull
    @GET("/v1/tracks")
    Call<List<CloudData>> list();

}
