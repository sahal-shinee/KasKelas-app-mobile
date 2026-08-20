package com.kaskelas.ui.bendahara;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kaskelas.R;
import com.kaskelas.ui.auth.LoginActivity;
import com.kaskelas.ui.bendahara.dashboard.BendaharaDashboardFragment;
import com.kaskelas.ui.bendahara.siswa.SiswaFragment;
import com.kaskelas.ui.bendahara.tagihan.TagihanFragment;
import com.kaskelas.ui.bendahara.transaksi.TransaksiFragment;
import com.kaskelas.ui.bendahara.notifikasi.NotifikasiFragment;
import com.kaskelas.viewmodel.AuthViewModel;

public class BendaharaMa extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bendahara_main);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        bottomNav     = findViewById(R.id.bottom_nav);

        loadFragment(new BendaharaDashboardFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();
            if      (id == R.id.nav_dashboard)  fragment = new BendaharaDashboardFragment();
            else if (id == R.id.nav_siswa)      fragment = new SiswaFragment();
            else if (id == R.id.nav_tagihan)    fragment = new TagihanFragment();
            else if (id == R.id.nav_transaksi)  fragment = new TransaksiFragment();
            else if (id == R.id.nav_notifikasi) fragment = new NotifikasiFragment();

            if (fragment != null) { loadFragment(fragment); return true; }
            return false;
        });
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
