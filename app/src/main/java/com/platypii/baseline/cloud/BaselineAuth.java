package com.platypii.baseline.cloud;

import com.platypii.baseline.util.BaseCallback;

import androidx.annotation.NonNull;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BaselineAuth {

    /**
     * Exchange a google token for a baseline auth token.
     */
    public static void exchangeToken(@NonNull String googleToken, BaseCallback<String> cb) {
        final AuthApi authApi = RetrofitClient.getRetrofit().create(AuthApi.class);
        final RequestBody body = RequestBody.create(MediaType.get("text/plain"), googleToken);
        authApi.exchangeToken(body).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                final String cookie = response.headers().get("set-cookie");
                if (cookie != null) {
                    final String cookieValue = cookie.replace("; SameSite=Strict; Path=/; Secure; HTTPOnly", "");
                    cb.onSuccess(cookieValue);
                } else {
                    cb.onFailure(new AuthException("Failed to exchange token"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable ex) {
                cb.onFailure(ex);
            }
        });
    }
}
