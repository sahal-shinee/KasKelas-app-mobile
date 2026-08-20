package com.kaskelas.ui.siswa.tagihan;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.*;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.kaskelas.R;
import com.kaskelas.data.api.ApiClient;
import com.kaskelas.data.api.response.TransactionListResponse;
import com.kaskelas.data.model.Transaction;
import com.kaskelas.data.model.TreasurerDashboard;
import com.kaskelas.ui.bendahara.transaksi.TransaksiAdapter;
import com.kaskelas.utils.CurrencyUtils;
import com.kaskelas.viewmodel.DashboardViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import retrofit2.*;

public class LaporanKasFragment extends Fragment {

    private TextView tvSaldo, tvPemasukan, tvPengeluaran;
    private RecyclerView rv;
    private ShimmerFrameLayout shimmer;
    private LinearLayout emptyState;
    private BarChart barChart;
    private DashboardViewModel viewModel;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        return i.inflate(R.layout.fragment_laporan_kas, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tvSaldo       = view.findViewById(R.id.tv_saldo);
        tvPemasukan   = view.findViewById(R.id.tv_pemasukan);
        tvPengeluaran = view.findViewById(R.id.tv_pengeluaran);
        rv            = view.findViewById(R.id.rv_transaksi);
        shimmer       = view.findViewById(R.id.shimmer_layout);
        emptyState    = view.findViewById(R.id.empty_state);
        barChart      = view.findViewById(R.id.bar_chart);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        loadKas();
        loadTransaksi();
    }

    // ── Saldo + chart dari student dashboard ─────────────────────────────
    private void loadKas() {
        viewModel.getStudentDashboard().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess() && result.data != null && result.data.data != null) {
                var data = result.data.data;
                tvSaldo.setText(CurrencyUtils.formatRupiah(data.saldoKas));
                // Label pemasukan / pengeluaran di dalam card
                tvPemasukan.setText("↓  " + CurrencyUtils.formatRupiahShort(data.totalPemasukan));
                tvPengeluaran.setText("↑  " + CurrencyUtils.formatRupiahShort(data.totalPengeluaran));
            }
        });

        // Chart pakai treasurer dashboard (data chart sama, siswa boleh lihat)
        viewModel.getTreasurerDashboard().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess() && result.data != null) {
                setupChart(result.data.data.chartBulanan);
            }
        });
    }

    // ── Setup Bar Chart ───────────────────────────────────────────────────
    private void setupChart(List<TreasurerDashboard.ChartData> chartData) {
        if (chartData == null || chartData.isEmpty()) {
            barChart.setVisibility(View.GONE);
            return;
        }

        List<BarEntry> entriesMasuk  = new ArrayList<>();
        List<BarEntry> entriesKeluar = new ArrayList<>();
        List<String>   labels        = new ArrayList<>();

        for (int i = 0; i < chartData.size(); i++) {
            TreasurerDashboard.ChartData d = chartData.get(i);
            entriesMasuk.add(new BarEntry(i, (float) d.pemasukan));
            entriesKeluar.add(new BarEntry(i, (float) d.pengeluaran));
            // Tampilkan bulan saja: "2024-05" → "05"
            labels.add(d.bulan.length() >= 7 ? d.bulan.substring(5) : d.bulan);
        }

        BarDataSet setMasuk = new BarDataSet(entriesMasuk, "Pemasukan");
        setMasuk.setColor(requireContext().getColor(R.color.color_primary));
        setMasuk.setDrawValues(false);

        BarDataSet setKeluar = new BarDataSet(entriesKeluar, "Pengeluaran");
        setKeluar.setColor(requireContext().getColor(R.color.color_error));
        setKeluar.setDrawValues(false);

        BarData barData = new BarData(setMasuk, setKeluar);
        float groupSpace = 0.2f, barSpace = 0.05f, barWidth = 0.35f;
        barData.setBarWidth(barWidth);
        barData.groupBars(0f, groupSpace, barSpace);

        barChart.setData(barData);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setDrawGridLines(true);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(true);
        barChart.setTouchEnabled(false);
        barChart.animateY(600);
        barChart.invalidate();
    }

    // ── Load riwayat transaksi ────────────────────────────────────────────
    private void loadTransaksi() {
        shimmer.setVisibility(View.VISIBLE);
        shimmer.startShimmer();

        ApiClient.getService(requireContext()).getTransactions()
            .enqueue(new Callback<TransactionListResponse>() {
                @Override public void onResponse(Call<TransactionListResponse> c,
                                                 Response<TransactionListResponse> r) {
                    shimmer.stopShimmer();
                    shimmer.setVisibility(View.GONE);
                    if (r.isSuccessful() && r.body() != null) {
                        List<Transaction> list = r.body().data;
                        if (list == null || list.isEmpty()) {
                            emptyState.setVisibility(View.VISIBLE);
                            return;
                        }
                        rv.setVisibility(View.VISIBLE);
                        emptyState.setVisibility(View.GONE);
                        // Siswa view-only — onDelete = null → tombol hapus disembunyikan
                        rv.setAdapter(new TransaksiAdapter(list, null));
                    } else {
                        emptyState.setVisibility(View.VISIBLE);
                    }
                }
                @Override public void onFailure(Call<TransactionListResponse> c, Throwable t) {
                    shimmer.stopShimmer();
                    shimmer.setVisibility(View.GONE);
                    emptyState.setVisibility(View.VISIBLE);
                }
            });
    }
}
