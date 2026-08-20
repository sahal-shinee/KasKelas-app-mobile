package com.kaskelas.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.kaskelas.data.api.response.TreasurerDashboardResponse;
import com.kaskelas.data.api.response.StudentDashboardResponse;
import com.kaskelas.data.repository.DashboardRepository;
import com.kaskelas.data.repository.Result;

public class DashboardViewModel extends AndroidViewModel {
    private final DashboardRepository repo;

    public DashboardViewModel(@NonNull Application app) {
        super(app);
        repo = new DashboardRepository(app.getApplicationContext());
    }

    public MutableLiveData<Result<TreasurerDashboardResponse>> getTreasurerDashboard() {
        return repo.getTreasurerDashboard();
    }

    public MutableLiveData<Result<StudentDashboardResponse>> getStudentDashboard() {
        return repo.getStudentDashboard();
    }
}
