package com.platypii.baseline.cloud;

import com.platypii.baseline.BuildConfig;

import android.content.Context;
import androidx.annotation.NonNull;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit;

    public static Retrofit getRetrofit(@NonNull Context context) {
        if (retrofit == null) {
            // Interceptor to add auth header
            final Interceptor authInterceptor = chain -> {
                Request request = chain.request();
                final Headers.Builder headerBuilder = request.headers().newBuilder();
                headerBuilder.add("User-Agent", "BASEline Android App/" + BuildConfig.VERSION_NAME);
                // Get auth token
                if (AuthState.getUser() != null) {
                    try {
                        final String authToken = AuthToken.getAuthToken(context);
                        headerBuilder.add("Authorization", authToken);
                    } catch (AuthException ignored) {}
                }
                final Headers headers = headerBuilder.build();
                request = request.newBuilder().headers(headers).build();
                return chain.proceed(request);
            };
            final OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .build();
            retrofit = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(BaselineCloud.baselineServer)
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}
