package com.kaskelas.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.kaskelas.data.api.ApiClient;
import com.kaskelas.data.api.ApiService;
import com.kaskelas.data.api.response.StudentListResponse;
import com.kaskelas.data.repository.Result;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentViewModel extends AndroidViewModel {
    private final ApiService api;

    public StudentViewModel(@NonNull Application app) {
        super(app);
        api = ApiClient.getService(app.getApplicationContext());
    }

    public MutableLiveData<Result<StudentListResponse>> getStudents(String filter) {
        MutableLiveData<Result<StudentListResponse>> ld = new MutableLiveData<>();
        ld.setValue(Result.loading());
        api.getStudents(filter).enqueue(new Callback<StudentListResponse>() {
            @Override public void onResponse(Call<StudentListResponse> c, Response<StudentListResponse> r) {
                if (r.isSuccessful() && r.body() != null) ld.setValue(Result.success(r.body()));
                else ld.setValue(Result.error("Gagal memuat data siswa"));
            }
            @Override public void onFailure(Call<StudentListResponse> c, Throwable t) {
                ld.setValue(Result.error(t.getMessage()));
            }
        });
        return ld;
    }
}
