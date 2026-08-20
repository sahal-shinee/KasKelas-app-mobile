package com.kaskelas.ui.bendahara.tagihan;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.kaskelas.R;
import com.kaskelas.data.api.ApiClient;
import com.kaskelas.data.api.ApiService;
import com.kaskelas.data.api.response.BaseResponse;
import com.kaskelas.data.api.response.BillCategoryListResponse;
import com.kaskelas.data.api.response.BillListResponse;
import com.kaskelas.data.model.Bill;
import com.kaskelas.data.model.BillCategory;
import com.kaskelas.utils.DatePickerHelper;

import java.util.*;
import retrofit2.*;

public class TagihanFragment extends Fragment {

    private RecyclerView rv;
    private ShimmerFrameLayout shimmer;
    private LinearLayout emptyState, errorState;
    private TextView tvError, tvTotalKategori;
    private ApiService api;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        return i.inflate(R.layout.fragment_tagihan, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rv         = view.findViewById(R.id.rv_tagihan);
        shimmer    = view.findViewById(R.id.shimmer_layout);
        emptyState = view.findViewById(R.id.empty_state);
        errorState = view.findViewById(R.id.error_state);
        tvError    = view.findViewById(R.id.tv_error);
        tvTotalKategori = view.findViewById(R.id.tv_total_kategori);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        api = ApiClient.getService(requireContext());

        // FIX: Mengubah showAddDialog() menjadi showAddCategoryDialog()
        view.findViewById(R.id.btn_tambah_inline).setOnClickListener(v -> showAddCategoryDialog());

        view.findViewById(R.id.btn_retry).setOnClickListener(v -> loadData());
        ((FloatingActionButton) view.findViewById(R.id.fab_add))
            .setOnClickListener(v -> showAddCategoryDialog());

        loadData();
    }

    private void loadData() {
        shimmer.setVisibility(View.VISIBLE); shimmer.startShimmer();
        rv.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);
        errorState.setVisibility(View.GONE);

        api.getBillCategories().enqueue(new Callback<BillCategoryListResponse>() {
            @Override public void onResponse(Call<BillCategoryListResponse> c,
                                             Response<BillCategoryListResponse> r) {
                shimmer.stopShimmer(); shimmer.setVisibility(View.GONE);
                if (r.isSuccessful() && r.body() != null) {
                    List<BillCategory> list = r.body().data;

                    int total = list != null ? list.size() : 0;
                    tvTotalKategori.setText(String.format(Locale.getDefault(), "%02d", total));

                    if (list == null || list.isEmpty()) {
                        emptyState.setVisibility(View.VISIBLE);
                        return;
                    }
                    rv.setVisibility(View.VISIBLE);
                    rv.setAdapter(new BillCategoryAdapter(list, (cat, action) -> {
                        switch (action) {
                            case "DELETE":
                                deleteCategory(cat.id);
                                break;
                            case "FORCE_DELETE":
                                confirmForceDelete(cat.id, cat.nama);
                                break;
                            case "ACTIVATE":
                                activateCategory(cat.id);
                                break;
                            case "GENERATE":
                                showGenerateDialog(cat);
                                break;
                            case "VIEW_PAYMENTS":
                                showPeriodePilihanDialog(cat);
                                break;
                            case "PEMBAYARAN": bukaDetailTagihan(cat); break;
                        }
                    }));
                } else {
                    errorState.setVisibility(View.VISIBLE);
                    tvError.setText("Gagal memuat data");
                }
            }
            @Override public void onFailure(Call<BillCategoryListResponse> c, Throwable t) {
                shimmer.stopShimmer(); shimmer.setVisibility(View.GONE);
                errorState.setVisibility(View.VISIBLE);
                tvError.setText(t.getMessage());
            }
        });
    }

    private void bukaDetailTagihan(BillCategory cat) {
        // FIX: Menambahkan argument ke-5 (tanggalJatuhTempo) untuk KonfirmasiPerTagihanFragment
        KonfirmasiPerTagihanFragment detail = KonfirmasiPerTagihanFragment.newInstance(
                cat.latestBillId, cat.latestPeriode, cat.nama, cat.nominal, cat.tanggalJatuhTempo);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, detail)
                .addToBackStack(null)
                .commit();
    }

    private void activateCategory(int id) {
        Map<String, Object> body = new HashMap<>();
        body.put("is_active", 1);
        api.updateBillCategory(id, body).enqueue(new Callback<BaseResponse>() {
            @Override public void onResponse(Call<BaseResponse> c, Response<BaseResponse> r) {
                if (r.isSuccessful()) {
                    loadData();
                    Snackbar.make(requireView(), "Kategori diaktifkan kembali", Snackbar.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<BaseResponse> c, Throwable t) {}
        });
    }

    private void confirmForceDelete(int id, String nama) {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus Permanen")
            .setMessage("Apakah Anda yakin ingin menghapus '" + nama + "' secara permanen?\n\nPERINGATAN: Semua riwayat pembayaran siswa di kategori ini akan ikut terhapus dan tidak bisa dikembalikan.")
            .setPositiveButton("Hapus", (d, w) -> {
                api.forceDeleteBillCategory(id).enqueue(new Callback<BaseResponse>() {
                    @Override public void onResponse(Call<BaseResponse> c, Response<BaseResponse> r) {
                        if (r.isSuccessful()) {
                            loadData();
                            Snackbar.make(requireView(), "Berhasil dihapus dari server", Snackbar.LENGTH_SHORT).show();
                        } else {
                            Snackbar.make(requireView(), "Gagal: Pastikan API PHP sudah mendukung force delete", Snackbar.LENGTH_LONG).show();
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

    private void deleteCategory(int id) {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Nonaktifkan Kategori")
            .setMessage("Kategori akan dinonaktifkan. Anda masih bisa melihat riwayatnya tetapi tidak bisa generate tagihan baru.")
            .setPositiveButton("Ya, Nonaktifkan", (d, w) ->
                api.deleteBillCategory(id).enqueue(new Callback<BaseResponse>() {
                    @Override public void onResponse(Call<BaseResponse> c, Response<BaseResponse> r) {
                        if (r.isSuccessful()) {
                            loadData();
                            Snackbar.make(requireView(), "Kategori berhasil dinonaktifkan", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onFailure(Call<BaseResponse> c, Throwable t) {}
                }))
            .setNegativeButton("Batal", null)
            .show();
    }

    private void showPeriodePilihanDialog(BillCategory cat) {
        api.getBills(cat.id, null).enqueue(new Callback<BillListResponse>() {
            @Override
            public void onResponse(Call<BillListResponse> c, Response<BillListResponse> r) {
                if (r.isSuccessful() && r.body() != null) {
                    List<Bill> bills = r.body().data;
                    if (bills == null || bills.isEmpty()) {
                        Snackbar.make(requireView(), "Belum ada tagihan yang di-generate", Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    String[] items = new String[bills.size()];
                    for (int i = 0; i < bills.size(); i++) {
                        items[i] = bills.get(i).periodeLabel + " (" + bills.get(i).tanggalJatuhTempo + ")";
                    }
                    new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Pilih Periode Pembayaran")
                        .setItems(items, (d, which) -> {
                            Bill selected = bills.get(which);
                            requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, KonfirmasiPerTagihanFragment.newInstance(
                                    selected.id, selected.periodeLabel, cat.nama, selected.nominal, selected.tanggalJatuhTempo))
                                .addToBackStack(null)
                                .commit();
                        })
                        .show();
                }
            }
            @Override public void onFailure(Call<BillListResponse> c, Throwable t) {}
        });
    }

    private void showAddCategoryDialog() {
        View dv = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_kategori, null);
        TextInputEditText etNama = dv.findViewById(R.id.et_nama);
        Spinner spinnerTipe = dv.findViewById(R.id.spinner_tipe);
        Spinner spinnerPeriode = dv.findViewById(R.id.spinner_periode);
        TextInputEditText etNominal = dv.findViewById(R.id.et_nominal);
        TextInputEditText etMulai = dv.findViewById(R.id.et_tanggal_mulai);
        TextInputEditText etJatuh = dv.findViewById(R.id.et_tanggal_jatuh);
        DatePickerHelper.attach(requireContext(), etMulai);
        DatePickerHelper.attach(requireContext(), etJatuh);
        setupSpinner(spinnerTipe, new String[]{"RUTIN", "INSIDENTAL"});
        setupSpinner(spinnerPeriode, new String[]{"MINGGUAN", "BULANAN", "SEKALI"});
        new MaterialAlertDialogBuilder(requireContext()).setTitle("Tambah Kategori").setView(dv).setPositiveButton("Simpan", (d, w) -> {
            Map<String, Object> body = new HashMap<>();
            body.put("nama", etNama.getText().toString());
            body.put("tipe", spinnerTipe.getSelectedItem().toString());
            body.put("periode", spinnerPeriode.getSelectedItem().toString());
            try {
                body.put("nominal", Double.parseDouble(etNominal.getText().toString()));
            } catch (Exception e) {
                body.put("nominal", 0);
            }
            body.put("tanggal_mulai", etMulai.getText().toString());
            body.put("tanggal_jatuh_tempo", etJatuh.getText().toString());
            api.addBillCategory(body).enqueue(new Callback<BaseResponse>() {
                @Override public void onResponse(Call<BaseResponse> c, Response<BaseResponse> r) {
                    if (r.isSuccessful()) { loadData(); }
                }
                @Override public void onFailure(Call<BaseResponse> c, Throwable t) {}
            });
        }).setNegativeButton("Batal", null).show();
    }

    private void showGenerateDialog(BillCategory cat) {
        View dv = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_generate_tagihan, null);
        TextInputEditText etPeriode = dv.findViewById(R.id.et_periode_label);
        TextInputEditText etJatuh = dv.findViewById(R.id.et_tanggal_jatuh);
        DatePickerHelper.attach(requireContext(), etJatuh);
        new MaterialAlertDialogBuilder(requireContext()).setTitle("Generate Tagihan").setView(dv).setPositiveButton("Generate", (d, w) -> {
            Map<String, Object> body = new HashMap<>();
            body.put("bill_category_id", cat.id);
            body.put("periode_label", etPeriode.getText().toString());
            body.put("tanggal_jatuh_tempo", etJatuh.getText().toString());
            api.generateBill(body).enqueue(new Callback<BaseResponse>() {
                @Override public void onResponse(Call<BaseResponse> c, Response<BaseResponse> r) {
                    if (r.isSuccessful()) {
                        loadData();
                        Snackbar.make(requireView(), "Tagihan berhasil dibuat", Snackbar.LENGTH_SHORT).show(); }
                }
                @Override public void onFailure(Call<BaseResponse> c, Throwable t) {}
            });
        }).setNegativeButton("Batal", null).show();
    }

    private void setupSpinner(Spinner spinner, String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
}
