package com.kaskelas.ui.siswa.pembayaran;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.*;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.kaskelas.R;
import com.kaskelas.data.api.ApiClient;
import com.kaskelas.data.api.response.PaymentListResponse;
import com.kaskelas.data.model.Payment;
import com.kaskelas.ui.bendahara.siswa.PaymentHistoryAdapter;
import com.kaskelas.utils.CurrencyUtils;
import com.kaskelas.viewmodel.DashboardViewModel;

import java.util.List;
import retrofit2.*;

public class RiwayatPembayaranFragment extends Fragment {

    private RecyclerView rv;
    private ShimmerFrameLayout shimmer;
    private LinearLayout emptyState, errorState;
    private TextView tvError, tvTotalTunggakan, tvJumlahTunggakan;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        return i.inflate(R.layout.fragment_riwayat_pembayaran, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rv                = view.findViewById(R.id.rv_pembayaran);
        shimmer           = view.findViewById(R.id.shimmer_layout);
        emptyState        = view.findViewById(R.id.empty_state);
        errorState        = view.findViewById(R.id.error_state);
        tvError           = view.findViewById(R.id.tv_error);
        tvTotalTunggakan  = view.findViewById(R.id.tv_total_tunggakan);
        tvJumlahTunggakan = view.findViewById(R.id.tv_jumlah_tunggakan);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        view.findViewById(R.id.btn_retry).setOnClickListener(v -> loadData());

        // Load stat tunggakan dari student dashboard
        loadStatCard();
        loadData();
    }

    // ── Stat card: total tunggakan dari dashboard ─────────────────────────
    private void loadStatCard() {
        DashboardViewModel vm = new ViewModelProvider(this).get(DashboardViewModel.class);
        vm.getStudentDashboard().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess() && result.data != null && result.data.data != null) {
                var data = result.data.data;
                tvTotalTunggakan.setText(CurrencyUtils.formatRupiah(data.totalTunggakan));
                tvJumlahTunggakan.setText(data.jumlahTunggakan + " Tagihan Menunggu");
            }
        });
    }

    // ── Load daftar riwayat pembayaran ────────────────────────────────────
    private void loadData() {
        shimmer.setVisibility(View.VISIBLE); shimmer.startShimmer();
        rv.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);
        errorState.setVisibility(View.GONE);

        ApiClient.getService(requireContext()).getMyPayments(null)
            .enqueue(new Callback<PaymentListResponse>() {
                @Override public void onResponse(Call<PaymentListResponse> c,
                                                 Response<PaymentListResponse> r) {
                    shimmer.stopShimmer(); shimmer.setVisibility(View.GONE);
                    if (r.isSuccessful() && r.body() != null) {
                        List<Payment> list = r.body().data;
                        if (list == null || list.isEmpty()) {
                            emptyState.setVisibility(View.VISIBLE);
                            return;
                        }
                        rv.setVisibility(View.VISIBLE);
                        rv.setAdapter(new PaymentHistoryAdapter(list));
                    } else {
                        errorState.setVisibility(View.VISIBLE);
                        tvError.setText("Gagal memuat riwayat");
                    }
                }
                @Override public void onFailure(Call<PaymentListResponse> c, Throwable t) {
                    shimmer.stopShimmer(); shimmer.setVisibility(View.GONE);
                    errorState.setVisibility(View.VISIBLE);
                    tvError.setText(t.getMessage());
                }
            });
    }
}
