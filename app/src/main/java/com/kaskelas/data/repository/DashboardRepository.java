package com.kaskelas.data.repository;

import android.content.Context;
import androidx.lifecycle.MutableLiveData;

import com.kaskelas.data.api.ApiClient;
import com.kaskelas.data.api.ApiService;
import com.kaskelas.data.api.response.TreasurerDashboardResponse;
import com.kaskelas.data.api.response.StudentDashboardResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardRepository {
    private final ApiService api;

    public DashboardRepository(Context context) {
        api = ApiClient.getService(context);
    }

    public MutableLiveData<Result<TreasurerDashboardResponse>> getTreasurerDashboard() {
        MutableLiveData<Result<TreasurerDashboardResponse>> ld = new MutableLiveData<>();
        ld.setValue(Result.loading());
        api.getTreasurerDashboard().enqueue(new Callback<TreasurerDashboardResponse>() {
            @Override public void onResponse(Call<TreasurerDashboardResponse> c, Response<TreasurerDashboardResponse> r) {
                if (r.isSuccessful() && r.body() != null) ld.setValue(Result.success(r.body()));
                else ld.setValue(Result.error("Gagal memuat dashboard"));
            }
            @Override public void onFailure(Call<TreasurerDashboardResponse> c, Throwable t) {
                ld.setValue(Result.error(t.getMessage()));
            }
        });
        return ld;
    }

    public MutableLiveData<Result<StudentDashboardResponse>> getStudentDashboard() {
        MutableLiveData<Result<StudentDashboardResponse>> ld = new MutableLiveData<>();
        ld.setValue(Result.loading());
        api.getStudentDashboard().enqueue(new Callback<StudentDashboardResponse>() {
            @Override public void onResponse(Call<StudentDashboardResponse> c, Response<StudentDashboardResponse> r) {
                if (r.isSuccessful() && r.body() != null) ld.setValue(Result.success(r.body()));
                else ld.setValue(Result.error("Gagal memuat dashboard"));
            }
            @Override public void onFailure(Call<StudentDashboardResponse> c, Throwable t) {
                ld.setValue(Result.error(t.getMessage()));
            }
        });
        return ld;
    }
}
