package com.platypii.baseline.lasers.cloud;

import com.platypii.baseline.lasers.LaserProfile;

import androidx.annotation.NonNull;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface LaserApi {

    @NonNull
    @GET("lasers.json")
    Call<List<LaserProfile>> getPublic();

    @NonNull
    @GET("lasers.json")
    Call<List<LaserProfile>> byUser(@Query("userid") String userId);

    @NonNull
    @POST("lasers")
    Call<LaserProfile> post(@Body LaserProfile laserProfile);

    @NonNull
    @DELETE("lasers/{laserId}")
    Call<Void> delete(@Path("laserId") String laserId);

}
