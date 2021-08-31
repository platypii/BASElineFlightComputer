package com.platypii.baseline.cloud;

import androidx.annotation.NonNull;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {

    @NonNull
    @POST("auth/web/token")
    Call<Void> exchangeToken(@Body RequestBody body);

}
