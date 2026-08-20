package com.kaskelas.ui.bendahara.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.snackbar.Snackbar;
import com.kaskelas.R;
import com.kaskelas.data.model.TreasurerDashboard;
import com.kaskelas.ui.bendahara.BendaharaMa;
import com.kaskelas.utils.CurrencyUtils;
import com.kaskelas.utils.SessionManager;
import com.kaskelas.viewmodel.DashboardViewModel;

import java.util.ArrayList;
import java.util.List;

public class BendaharaDashboardFragment extends Fragment {

    private DashboardViewModel viewModel;
    private TextView tvNama, tvSaldo, tvPemasukan, tvPengeluaran, tvError;
    private RecyclerView rvTransactions;
    private ShimmerFrameLayout shimmer;
    private LinearLayout emptyState, errorState;
    private BarChart barChart;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard_bendahara, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tvNama        = view.findViewById(R.id.tv_nama);
        tvSaldo       = view.findViewById(R.id.tv_saldo);
        tvPemasukan   = view.findViewById(R.id.tv_pemasukan);
        tvPengeluaran = view.findViewById(R.id.tv_pengeluaran);
        tvError       = view.findViewById(R.id.tv_error);
        rvTransactions= view.findViewById(R.id.rv_transactions);
        shimmer       = view.findViewById(R.id.shimmer_layout);
        emptyState    = view.findViewById(R.id.empty_state);
        errorState    = view.findViewById(R.id.error_state);
        barChart      = view.findViewById(R.id.bar_chart);

        SessionManager session = new SessionManager(requireContext());
        tvNama.setText(session.getNama());

        view.findViewById(R.id.btn_logout).setOnClickListener(v ->
            ((BendaharaMa) requireActivity()).logout());

        view.findViewById(R.id.btn_retry).setOnClickListener(v -> loadData());

        rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        loadData();
    }

    private void loadData() {
        viewModel.getTreasurerDashboard().observe(getViewLifecycleOwner(), result -> {
            if (result.isLoading()) {
                shimmer.setVisibility(View.VISIBLE);
                shimmer.startShimmer();
                rvTransactions.setVisibility(View.GONE);
                emptyState.setVisibility(View.GONE);
                errorState.setVisibility(View.GONE);
            } else if (result.isSuccess()) {
                shimmer.stopShimmer();
                shimmer.setVisibility(View.GONE);
                errorState.setVisibility(View.GONE);

                TreasurerDashboard data = result.data.data;
                tvSaldo.setText(CurrencyUtils.formatRupiah(data.saldo));
                tvPemasukan.setText(CurrencyUtils.formatRupiahShort(data.totalPemasukan));
                tvPengeluaran.setText(CurrencyUtils.formatRupiahShort(data.totalPengeluaran));

                setupChart(data);

                if (data.transaksiTerbaru == null || data.transaksiTerbaru.isEmpty()) {
                    emptyState.setVisibility(View.VISIBLE);
                    rvTransactions.setVisibility(View.GONE);
                } else {
                    emptyState.setVisibility(View.GONE);
                    rvTransactions.setVisibility(View.VISIBLE);
                    rvTransactions.setAdapter(new RecentTransactionAdapter(data.transaksiTerbaru));
                }
            } else {
                shimmer.stopShimmer();
                shimmer.setVisibility(View.GONE);
                errorState.setVisibility(View.VISIBLE);
                tvError.setText(result.message);
            }
        });
    }

    private void setupChart(TreasurerDashboard data) {
        if (data.chartBulanan == null || data.chartBulanan.isEmpty()) return;

        List<BarEntry> entriesPemasukan   = new ArrayList<>();
        List<BarEntry> entriesPengeluaran = new ArrayList<>();
        List<String>   labels             = new ArrayList<>();

        for (int i = 0; i < data.chartBulanan.size(); i++) {
            TreasurerDashboard.ChartData d = data.chartBulanan.get(i);
            entriesPemasukan.add(new BarEntry(i, (float) d.pemasukan));
            entriesPengeluaran.add(new BarEntry(i, (float) d.pengeluaran));
            labels.add(d.bulan.substring(5)); // tampilkan bulan saja
        }

        BarDataSet setMasuk = new BarDataSet(entriesPemasukan, "Pemasukan");
        setMasuk.setColor(0xFF1A237E);
        BarDataSet setKeluar = new BarDataSet(entriesPengeluaran, "Pengeluaran");
        setKeluar.setColor(0xFFC62828);

        BarData barData = new BarData(setMasuk, setKeluar);
        barData.setBarWidth(0.35f);
        barData.groupBars(0, 0.2f, 0.05f);

        barChart.setData(barData);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setGranularity(1f);
        barChart.getDescription().setEnabled(false);
        barChart.animateY(800);
        barChart.invalidate();
    }
}
