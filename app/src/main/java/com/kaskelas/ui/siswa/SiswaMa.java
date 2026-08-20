package com.kaskelas.ui.siswa;

import android.app.AlarmManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.kaskelas.R;
import com.kaskelas.service.NotificationPollingWorker;
import com.kaskelas.ui.auth.LoginActivity;
import com.kaskelas.ui.siswa.dashboard.SiswaDashboardFragment;
import com.kaskelas.ui.siswa.tagihan.LaporanKasFragment;
import com.kaskelas.ui.siswa.pembayaran.RiwayatPembayaranFragment;
import com.kaskelas.ui.siswa.notifikasi.SiswaNotifikasiFragment;
import com.kaskelas.viewmodel.AuthViewModel;

public class SiswaMa extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private AuthViewModel authViewModel;
    private boolean sudahMintaIzinAlarm = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_siswa_main);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        bottomNav     = findViewById(R.id.bottom_nav);

        loadFragment(new SiswaDashboardFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();
            if      (id == R.id.nav_dashboard)  fragment = new SiswaDashboardFragment();
            else if (id == R.id.nav_kas)        fragment = new LaporanKasFragment();
            else if (id == R.id.nav_pembayaran) fragment = new RiwayatPembayaranFragment();
            else if (id == R.id.nav_notifikasi) fragment = new SiswaNotifikasiFragment();

            if (fragment != null) { loadFragment(fragment); return true; }
            return false;
        });

        // Android 12+: minta izin exact alarm (sekali saat pertama buka)
        mintaIzinExactAlarm();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Langsung cek notifikasi baru saat app dibuka / kembali ke foreground
        NotificationPollingWorker.periksaSekarang(this);
    }

    /**
     * Android 12+ memerlukan izin khusus agar alarm tepat waktu bisa jalan.
     * Tampilkan dialog sekali untuk meminta izin ini.
     */
    private void mintaIzinExactAlarm() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return; // Android 11 ke bawah tidak perlu
        if (sudahMintaIzinAlarm) return;
        sudahMintaIzinAlarm = true;

        AlarmManager am = (AlarmManager) getSystemService(AlarmManager.class);
        if (am != null && !am.canScheduleExactAlarms()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Izinkan Notifikasi Tepat Waktu")
                    .setMessage("Agar notifikasi pembayaran muncul segera, " +
                                "izinkan KasKelas mengatur alarm di pengaturan.")
                    .setPositiveButton("Izinkan", (d, w) -> {
                        Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        startActivity(intent);
                    })
                    .setNegativeButton("Nanti", null)
                    .show();
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit();
    }

    public void logout() {
        authViewModel.logout();
        startActivity(new Intent(this, LoginActivity.class)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}
