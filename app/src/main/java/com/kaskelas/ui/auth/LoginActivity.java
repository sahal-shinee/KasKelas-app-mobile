package com.kaskelas.ui.auth;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.kaskelas.R;
import com.kaskelas.data.repository.Result;
import com.kaskelas.ui.bendahara.BendaharaMa;
import com.kaskelas.ui.siswa.SiswaMa;
import com.kaskelas.utils.Constants;
import com.kaskelas.utils.SessionManager;
import com.kaskelas.viewmodel.AuthViewModel;

public class LoginActivity extends AppCompatActivity {

    private AuthViewModel    viewModel;
    private TextInputEditText etNis, etPin;
    private MaterialButton    btnLogin;
    private LinearProgressIndicator progress;

    // Launcher untuk meminta izin POST_NOTIFICATIONS (Android 13+)
    private final ActivityResultLauncher<String> requestNotifPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                // Tidak perlu tindakan khusus — notifikasi akan tetap dicoba tampilkan
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Minta izin notifikasi Android 13+ (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        // Jika sudah login, langsung redirect
        SessionManager session = new SessionManager(this);
        if (session.isLoggedIn()) {
            redirectByRole(session.getRole());
            return;
        }

        setContentView(R.layout.activity_login);

        etNis    = findViewById(R.id.et_nis);
        etPin    = findViewById(R.id.et_pin);
        btnLogin = findViewById(R.id.btn_login);
        progress = findViewById(R.id.progress);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String nis = etNis.getText() != null ? etNis.getText().toString().trim() : "";
        String pin = etPin.getText() != null ? etPin.getText().toString().trim() : "";

        if (nis.isEmpty()) { etNis.setError("NIS wajib diisi"); return; }
        if (pin.isEmpty()) { etPin.setError("PIN wajib diisi"); return; }

        viewModel.login(nis, pin).observe(this, result -> {
            if (result.isLoading()) {
                progress.setVisibility(View.VISIBLE);
                btnLogin.setEnabled(false);
            } else {
                progress.setVisibility(View.GONE);
                btnLogin.setEnabled(true);

                if (result.isSuccess()) {
                    SessionManager sm = new SessionManager(this);
                    redirectByRole(sm.getRole());
                } else {
                    Snackbar.make(btnLogin, result.message, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void redirectByRole(String role) {
        Intent intent = Constants.ROLE_BENDAHARA.equals(role)
            ? new Intent(this, BendaharaMa.class)
            : new Intent(this, SiswaMa.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
