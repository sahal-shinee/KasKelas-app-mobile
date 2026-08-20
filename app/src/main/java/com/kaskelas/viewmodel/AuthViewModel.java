package com.kaskelas.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.kaskelas.data.api.response.LoginResponse;
import com.kaskelas.data.repository.AuthRepository;
import com.kaskelas.data.repository.Result;

public class AuthViewModel extends AndroidViewModel {
    private final AuthRepository repo;

    public AuthViewModel(@NonNull Application app) {
        super(app);
        repo = new AuthRepository(app.getApplicationContext());
    }

    public MutableLiveData<Result<LoginResponse>> login(String nis, String pin) {
        return repo.login(nis, pin);
    }

    public void logout() {
        repo.logout(getApplication().getApplicationContext());
    }
}
