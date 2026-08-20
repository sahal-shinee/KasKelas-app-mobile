package com.kaskelas.data.api;

import android.content.Context;

import com.kaskelas.utils.Constants;
import com.kaskelas.utils.SessionManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit;

    public static Retrofit getInstance(Context context) {
        if (retrofit == null) {
            SessionManager session = new SessionManager(context);

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout   (Constants.READ_TIMEOUT,    TimeUnit.SECONDS)
                    .writeTimeout  (Constants.WRITE_TIMEOUT,   TimeUnit.SECONDS)
                    .addInterceptor(chain -> {
                        String token = session.getToken();
                        Request original = chain.request();
                        Request request  = token != null
                                ? original.newBuilder()
                                .header("Authorization", "Bearer " + token)
                                .header("Accept", "application/json")
                                .build()
                                : original;
                        return chain.proceed(request);
                    })
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    /** Reset instance (dipanggil saat logout) */
    public static void reset() { retrofit = null; }

    public static ApiService getService(Context context) {
        return getInstance(context).create(ApiService.class);
    }

    /**
     * Kembalikan OkHttpClient mentah (tanpa logging interceptor).
     * Dipakai saat perlu baca errorBody sebelum di-consume oleh logger.
     */
    public static okhttp3.OkHttpClient getRawClient(Context context) {
        SessionManager session = new SessionManager(context);
        return new okhttp3.OkHttpClient.Builder()
                .connectTimeout(Constants.CONNECT_TIMEOUT, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout   (Constants.READ_TIMEOUT,    java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout  (Constants.WRITE_TIMEOUT,   java.util.concurrent.TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    String token   = session.getToken();
                    okhttp3.Request req = chain.request().newBuilder()
                            .header("Authorization", "Bearer " + (token != null ? token : ""))
                            .header("Accept", "application/json")
                            .build();
                    return chain.proceed(req);
                })
                .build();
    }
}
