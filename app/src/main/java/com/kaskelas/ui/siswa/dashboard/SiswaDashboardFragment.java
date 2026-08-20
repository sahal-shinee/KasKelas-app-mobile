package com.kaskelas.ui.siswa.dashboard;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.*;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.card.MaterialCardView;
import com.kaskelas.R;
import com.kaskelas.data.model.StudentDashboard;
import com.kaskelas.ui.siswa.SiswaMa;
import com.kaskelas.utils.CurrencyUtils;
import com.kaskelas.utils.SessionManager;
import com.kaskelas.viewmodel.DashboardViewModel;

public class SiswaDashboardFragment extends Fragment {

    private DashboardViewModel viewModel;
    private TextView tvNama, tvTotalTunggakan;
    private MaterialCardView cardTunggakan, cardLunas;
    private RecyclerView rvTagihan;
    private ShimmerFrameLayout shimmer;
    private MaterialCardView emptyTagihan, errorState;
    private TextView tvError;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        return i.inflate(R.layout.fragment_dashboard_siswa, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tvNama           = view.findViewById(R.id.tv_nama);
        tvTotalTunggakan = view.findViewById(R.id.tv_total_tunggakan);
        cardTunggakan    = view.findViewById(R.id.card_tunggakan);
        cardLunas        = view.findViewById(R.id.card_lunas);
        rvTagihan        = view.findViewById(R.id.rv_tagihan);
        shimmer          = view.findViewById(R.id.shimmer_layout);
        emptyTagihan     = view.findViewById(R.id.empty_tagihan);
        errorState       = view.findViewById(R.id.error_state);
        tvError          = view.findViewById(R.id.tv_error);

        rvTagihan.setLayoutManager(new LinearLayoutManager(requireContext()));

        SessionManager session = new SessionManager(requireContext());
        tvNama.setText(session.getNama());

        view.findViewById(R.id.btn_logout).setOnClickListener(v ->
            ((SiswaMa) requireActivity()).logout());

        view.findViewById(R.id.btn_retry).setOnClickListener(v -> loadData());

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        loadData();
    }

    private void loadData() {
        // Sembunyikan semua state dulu
        cardTunggakan.setVisibility(View.GONE);
        cardLunas.setVisibility(View.GONE);
        errorState.setVisibility(View.GONE);
        emptyTagihan.setVisibility(View.GONE);
        rvTagihan.setVisibility(View.GONE);

        viewModel.getStudentDashboard().observe(getViewLifecycleOwner(), result -> {
            if (result.isLoading()) {
                shimmer.setVisibility(View.VISIBLE);
                shimmer.startShimmer();
                return;
            }

            shimmer.stopShimmer();
            shimmer.setVisibility(View.GONE);

            if (result.isError()) {
                errorState.setVisibility(View.VISIBLE);
                tvError.setText(result.message);
                return;
            }

            // SUCCESS
            StudentDashboard data = result.data.data;

            // Tampilkan status tunggakan
            if (data.jumlahTunggakan > 0) {
                cardTunggakan.setVisibility(View.VISIBLE);
                cardLunas.setVisibility(View.GONE);
                tvTotalTunggakan.setText(CurrencyUtils.formatRupiah(data.totalTunggakan));
            } else {
                cardTunggakan.setVisibility(View.GONE);
                cardLunas.setVisibility(View.VISIBLE);
            }

            // Tampilkan tagihan aktif (MENUNGGAK + BELUM_JATUH_TEMPO)
            if (data.tagihanAktif == null || data.tagihanAktif.isEmpty()) {
                emptyTagihan.setVisibility(View.VISIBLE);
                rvTagihan.setVisibility(View.GONE);
            } else {
                emptyTagihan.setVisibility(View.GONE);
                rvTagihan.setVisibility(View.VISIBLE);
                rvTagihan.setAdapter(new TagihanAktifAdapter(data.tagihanAktif));
            }
        });
    }
}
