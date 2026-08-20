package com.kaskelas.ui.bendahara.tagihan;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.kaskelas.R;
import com.kaskelas.data.api.ApiClient;
import com.kaskelas.data.api.ApiService;
import com.kaskelas.data.api.response.PaymentSummaryResponse;
import com.kaskelas.data.model.PaymentSummary;

import java.util.List;
import retrofit2.*;

/**
 * Halaman: DAFTAR SEMUA TAGIHAN + ringkasan lunas/menunggak.
 * Tap satu tagihan → buka KonfirmasiPerTagihanFragment (detail per siswa).
 */
public class ConfirmasiPembayaranFragment extends Fragment {

    private RecyclerView rv;
    private ShimmerFrameLayout shimmer;
    private LinearLayout emptyState, errorState;
    private TextView tvError;
    private ApiService api;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        return i.inflate(R.layout.fragment_konfirmasi_pembayaran, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rv         = view.findViewById(R.id.rv_summary);
        shimmer    = view.findViewById(R.id.shimmer_layout);
        emptyState = view.findViewById(R.id.empty_state);
        errorState = view.findViewById(R.id.error_state);
        tvError    = view.findViewById(R.id.tv_error);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        api = ApiClient.getService(requireContext());

        view.findViewById(R.id.btn_retry).setOnClickListener(v -> loadData());
        loadData();
    }

    private void loadData() {
        shimmer.setVisibility(View.VISIBLE); shimmer.startShimmer();
        rv.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);
        errorState.setVisibility(View.GONE);

        api.getPaymentSummary().enqueue(new Callback<PaymentSummaryResponse>() {
            @Override public void onResponse(Call<PaymentSummaryResponse> c,
                                             Response<PaymentSummaryResponse> r) {
                shimmer.stopShimmer(); shimmer.setVisibility(View.GONE);
                if (r.isSuccessful() && r.body() != null) {
                    List<PaymentSummary> list = r.body().data;
                    if (list == null || list.isEmpty()) {
                        emptyState.setVisibility(View.VISIBLE);
                        return;
                    }
                    rv.setVisibility(View.VISIBLE);
                    rv.setAdapter(new PaymentSummaryAdapter(list, summary -> bukaDetail(summary)));
                } else {
                    errorState.setVisibility(View.VISIBLE);
                    tvError.setText("Gagal memuat data pembayaran");
                }
            }
            @Override public void onFailure(Call<PaymentSummaryResponse> c, Throwable t) {
                shimmer.stopShimmer(); shimmer.setVisibility(View.GONE);
                errorState.setVisibility(View.VISIBLE);
                tvError.setText(t.getMessage());
            }
        });
    }

    /** Buka detail per tagihan → daftar siswa + tombol ubah status */
    private void bukaDetail(PaymentSummary summary) {
        KonfirmasiPerTagihanFragment detail = KonfirmasiPerTagihanFragment.newInstance(
            summary.billId,
            summary.periodeLabel,
            summary.kategori,
            summary.nominal,
            summary.tanggalJatuhTempo // Pass due date for automatic overdue detection
        );
        requireActivity().getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_container, detail)
            .addToBackStack(null)
            .commit();
    }
}
