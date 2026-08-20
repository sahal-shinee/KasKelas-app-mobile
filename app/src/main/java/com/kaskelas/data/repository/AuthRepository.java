package com.kaskelas.data.repository;

import android.content.Context;
import androidx.lifecycle.MutableLiveData;

import com.kaskelas.data.api.ApiClient;
import com.kaskelas.data.api.ApiService;
import com.kaskelas.data.api.response.LoginResponse;
import com.kaskelas.service.MyFirebaseMessagingService;
import com.kaskelas.service.NotificationPollingWorker;
import com.kaskelas.utils.SessionManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {
    private final ApiService api;
    private final SessionManager session;
    private final Context context;

    public AuthRepository(Context context) {
        this.context = context.getApplicationContext();
        api     = ApiClient.getService(context);
        session = new SessionManager(context);
    }

    public MutableLiveData<Result<LoginResponse>> login(String nis, String pin) {
        MutableLiveData<Result<LoginResponse>> liveData = new MutableLiveData<>();
        liveData.setValue(Result.loading());

        Map<String, String> body = new HashMap<>();
        body.put("nis", nis);
        body.put("pin", pin);

        api.login(body).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    LoginResponse res  = response.body();
                    LoginResponse.User u = res.data.user;
                    session.saveSession(res.data.token, u.role, u.id, u.nis, u.nama);

                    // Kirim FCM token ke server setelah login berhasil
                    MyFirebaseMessagingService.kirimTokenKeServer(AuthRepository.this.context);

                    // Mulai polling notifikasi sistem
                    NotificationPollingWorker.jadwalkan(AuthRepository.this.context);

                    liveData.setValue(Result.success(res));
                } else {
                    liveData.setValue(Result.error("NIS atau PIN salah"));
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                liveData.setValue(Result.error("Gagal terhubung ke server: " + t.getMessage()));
            }
        });
        return liveData;
    }

    public void logout(Context context) {
        api.logout().enqueue(new Callback<>() {
            @Override public void onResponse(Call call, Response response) {}
            @Override public void onFailure(Call call, Throwable t) {}
        });
        NotificationPollingWorker.batalkan(context);
        session.clearSession();
        ApiClient.reset();
    }
}
