package com.kaskelas.ui.bendahara.transaksi;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.*;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.kaskelas.R;
import com.kaskelas.data.api.ApiClient;
import com.kaskelas.data.api.ApiService;
import com.kaskelas.data.api.response.BaseResponse;
import com.kaskelas.data.api.response.TransactionListResponse;
import com.kaskelas.data.model.Transaction;
import com.kaskelas.utils.CurrencyUtils;
import com.kaskelas.utils.DatePickerHelper;
import com.kaskelas.viewmodel.DashboardViewModel;

import java.text.SimpleDateFormat;
import java.util.*;
import retrofit2.*;

public class TransaksiFragment extends Fragment {

    private RecyclerView rv;
    private ShimmerFrameLayout shimmer;
    private LinearLayout emptyState, errorState;
    private TextView tvError;

    private TextInputEditText etSearch;
    private TextView tvSaldo, tvTotalPemasukan, tvTotalPengeluaran;
    private TransaksiAdapter adapter;
    private String filterTipe = null;

    private ApiService api;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        return i.inflate(R.layout.fragment_transaksi, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rv         = view.findViewById(R.id.rv_transaksi);
        shimmer    = view.findViewById(R.id.shimmer_layout);
        emptyState = view.findViewById(R.id.empty_state);
        errorState = view.findViewById(R.id.error_state);
        tvError    = view.findViewById(R.id.tv_error);

        etSearch           = view.findViewById(R.id.et_search);
        tvSaldo            = view.findViewById(R.id.tv_saldo);
        // PERBAIKAN: Inisialisasi TextView ringkasan
        tvTotalPemasukan   = view.findViewById(R.id.tv_total_pemasukan);
        tvTotalPengeluaran = view.findViewById(R.id.tv_total_pengeluaran);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        api = ApiClient.getService(requireContext());

        if (etSearch != null) {
            etSearch.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (adapter != null) adapter.filter(s.toString());
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }

        View btnP = view.findViewById(R.id.btn_pemasukan);
        if (btnP != null) {
            btnP.setOnClickListener(v -> {
                filterTipe = "PEMASUKAN".equals(filterTipe) ? null : "PEMASUKAN";
                if (adapter != null) adapter.filterByTipe(filterTipe);
                updateFilterButton(view);
            });
        }

        View btnK = view.findViewById(R.id.btn_pengeluaran);
        if (btnK != null) {
            btnK.setOnClickListener(v -> {
                filterTipe = "PENGELUARAN".equals(filterTipe) ? null : "PENGELUARAN";
                if (adapter != null) adapter.filterByTipe(filterTipe);
                updateFilterButton(view);
            });
        }

        view.findViewById(R.id.btn_retry).setOnClickListener(v -> loadData());
        
        FloatingActionButton fab = view.findViewById(R.id.fab_add);
        if (fab != null) fab.setOnClickListener(v -> showAddDialog());

        loadSaldo();
        loadData();
    }

    private void loadSaldo() {
        DashboardViewModel vm = new ViewModelProvider(this).get(DashboardViewModel.class);
        vm.getTreasurerDashboard().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess() && result.data != null) {
                if (tvSaldo != null) tvSaldo.setText(CurrencyUtils.formatRupiah(result.data.data.saldo));
            }
        });
    }

    private void loadData() {
        shimmer.setVisibility(View.VISIBLE); shimmer.startShimmer();
        rv.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);
        errorState.setVisibility(View.GONE);

        api.getTransactions().enqueue(new Callback<TransactionListResponse>() {
            @Override public void onResponse(Call<TransactionListResponse> c,
                                             Response<TransactionListResponse> r) {
                shimmer.stopShimmer(); shimmer.setVisibility(View.GONE);
                if (r.isSuccessful() && r.body() != null) {
                    List<Transaction> list = r.body().data;
                    
                    // Hitung total ringkasan
                    double totalMasuk = 0;
                    double totalKeluar = 0;
                    if (list != null) {
                        for (Transaction t : list) {
                            if ("PEMASUKAN".equalsIgnoreCase(t.tipe)) totalMasuk += t.jumlah;
                            else totalKeluar += t.jumlah;
                        }
                    }
                    if (tvTotalPemasukan != null) tvTotalPemasukan.setText(CurrencyUtils.formatRupiahShort(totalMasuk));
                    if (tvTotalPengeluaran != null) tvTotalPengeluaran.setText(CurrencyUtils.formatRupiahShort(totalKeluar));

                    if (list == null || list.isEmpty()) {
                        emptyState.setVisibility(View.VISIBLE);
                        return;
                    }
                    rv.setVisibility(View.VISIBLE);

                    adapter = new TransaksiAdapter(list, (t, action) -> {
                        switch (action) {
                            case "DETAIL":
                                showDetailDialog(t);
                                break;
                            case "DELETE_HISTORY":
                                confirmDelete(t.id, true, "Hapus Riwayat Pembayaran", "Yakin ingin menghapus catatan riwayat ini? Saldo kas Anda tidak akan berubah.");
                                break;
                            case "CANCEL_TRANSACTION":
                                String msg = "Yakin ingin membatalkan transaksi ini? Saldo akan dikembalikan.";
                                if ("Uang Kas".equalsIgnoreCase(t.kategoriNama)) {
                                    msg += "\n\nCatatan: Status pembayaran siswa akan kembali menjadi 'Menunggak'.";
                                }
                                confirmDelete(t.id, false, "Batalkan Transaksi", msg);
                                break;
                        }
                    });

                    rv.setAdapter(adapter);
                    if (filterTipe != null) adapter.filterByTipe(filterTipe);

                } else {
                    errorState.setVisibility(View.VISIBLE);
                    tvError.setText("Gagal memuat transaksi");
                }
            }
            @Override public void onFailure(Call<TransactionListResponse> c, Throwable t) {
                shimmer.stopShimmer(); shimmer.setVisibility(View.GONE);
                errorState.setVisibility(View.VISIBLE);
                tvError.setText(t.getMessage());
            }
        });
    }

    private void updateFilterButton(View root) {
        View btnP = root.findViewById(R.id.btn_pemasukan);
        View btnK = root.findViewById(R.id.btn_pengeluaran);
        if (btnP == null || btnK == null) return;

        if (filterTipe == null) {
            btnP.setAlpha(1.0f); btnK.setAlpha(1.0f);
        } else if ("PEMASUKAN".equals(filterTipe)) {
            btnP.setAlpha(1.0f); btnK.setAlpha(0.5f);
        } else {
            btnP.setAlpha(0.5f); btnK.setAlpha(1.0f);
        }
    }

    private void showDetailDialog(Transaction t) {
        StringBuilder sb = new StringBuilder();
        sb.append("Nama Item: ").append(t.namaItem).append("\n");
        sb.append("Tipe: ").append(t.tipe).append("\n");
        sb.append("Jumlah: ").append(CurrencyUtils.formatRupiah(t.jumlah)).append("\n");
        sb.append("Tanggal: ").append(t.tanggal).append("\n");
        sb.append("Kategori: ").append(t.kategoriNama != null ? t.kategoriNama : "-").append("\n");
        sb.append("Dicatat Oleh: ").append(t.dicatatOlehNama != null ? t.dicatatOlehNama : "-").append("\n");
        sb.append("Keterangan: ").append(t.keterangan != null && !t.keterangan.isEmpty() ? t.keterangan : "-");

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Detail Transaksi")
                .setMessage(sb.toString())
                .setPositiveButton("Tutup", null)
                .show();
    }

    private void showAddDialog() {
        View dv = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_transaksi, null);

        TextInputEditText etNama       = dv.findViewById(R.id.et_nama_item);
        TextInputEditText etJumlah     = dv.findViewById(R.id.et_jumlah);
        TextInputEditText etKeterangan = dv.findViewById(R.id.et_keterangan);
        TextInputEditText etTanggal    = dv.findViewById(R.id.et_tanggal);
        Spinner spinnerTipe            = dv.findViewById(R.id.spinner_tipe);

        ArrayAdapter<String> tipeAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, new String[]{"PEMASUKAN", "PENGELUARAN"});
        tipeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipe.setAdapter(tipeAdapter);

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        etTanggal.setText(today);
        DatePickerHelper.attach(requireContext(), etTanggal);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Catat Transaksi")
                .setView(dv)
                .setPositiveButton("Simpan", (d, w) -> {

                    String nama    = t(etNama);
                    String jumlah  = t(etJumlah);
                    String tanggal = t(etTanggal);

                    if (nama.isEmpty() || jumlah.isEmpty()) {
                        Snackbar.make(requireView(), "Nama dan jumlah wajib diisi", Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> body = new HashMap<>();
                    body.put("nama_item",  nama);
                    body.put("jumlah",     Double.parseDouble(jumlah));
                    body.put("tipe",       spinnerTipe.getSelectedItem().toString());
                    body.put("tanggal",    tanggal);
                    body.put("keterangan", t(etKeterangan));

                    api.addTransaction(body).enqueue(new Callback<BaseResponse>() {
                        @Override public void onResponse(Call<BaseResponse> c, Response<BaseResponse> r) {
                            if (r.isSuccessful()) {
                                loadData();
                                loadSaldo();
                                Snackbar.make(requireView(), "Transaksi dicatat", Snackbar.LENGTH_SHORT).show();
                            } else {
                                Snackbar.make(requireView(), "Gagal menyimpan", Snackbar.LENGTH_SHORT).show();
                            }
                        }
                        @Override public void onFailure(Call<BaseResponse> c, Throwable t) {
                            Snackbar.make(requireView(), "Error: " + t.getMessage(), Snackbar.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void confirmDelete(int id, boolean keepBalance, String title, String message) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Ya, Lanjutkan", (d, w) ->
                        api.deleteTransaction(id, keepBalance).enqueue(new Callback<BaseResponse>() {
                            @Override public void onResponse(Call<BaseResponse> c, Response<BaseResponse> r) {
                                if (r.isSuccessful()) {
                                    loadSaldo();
                                    loadData();
                                    Snackbar.make(requireView(), "Berhasil dilakukan", Snackbar.LENGTH_SHORT).show();
                                } else {
                                    Snackbar.make(requireView(), "Gagal memproses", Snackbar.LENGTH_SHORT).show();
                                }
                            }
                            @Override public void onFailure(Call<BaseResponse> c, Throwable t) {
                                Snackbar.make(requireView(), "Error: " + t.getMessage(), Snackbar.LENGTH_SHORT).show();
                            }
                        }))
                .setNegativeButton("Kembali", null)
                .show();
    }

    private String t(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
