package com.platypii.baseline.cloud.lasers;

import com.platypii.baseline.laser.LaserProfile;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface LaserApi {

    @GET("lasers.json")
    Call<List<LaserProfile>> getPublic();

    @GET("users/{userId}/lasers.json")
    Call<List<LaserProfile>> byUser(@Path("userId") String userId);

    @POST("lasers")
    Call<LaserProfile> post(@Body LaserProfile laserProfile);

    @DELETE("lasers/{laserId}")
    Call<Void> delete(@Path("laserId") String laserId);

}
