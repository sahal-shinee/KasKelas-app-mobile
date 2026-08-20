package com.kaskelas.ui.bendahara.tagihan;

import android.content.Context;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.kaskelas.R;
import com.kaskelas.data.api.ApiClient;
import com.kaskelas.data.api.ApiService;
import com.kaskelas.data.api.response.BaseResponse;
import com.kaskelas.data.api.response.BillPaymentStatusResponse;
import com.kaskelas.data.model.BillPaymentStatus;
import com.kaskelas.utils.CurrencyUtils;
import com.kaskelas.utils.DatePickerHelper;

import java.text.SimpleDateFormat;
import java.util.*;
import retrofit2.*;

/**
 * Layar: daftar semua siswa + status bayar untuk SATU tagihan.
 * Bendahara bisa tap siswa → ubah status LUNAS / MENUNGGAK.
 */
public class KonfirmasiPerTagihanFragment extends Fragment {

    private static final String ARG_BILL_ID      = "bill_id";
    private static final String ARG_BILL_LABEL   = "bill_label";
    private static final String ARG_KATEGORI     = "kategori";
    private static final String ARG_NOMINAL      = "nominal";
    private static final String ARG_DUE_DATE     = "due_date";

    private int    billId;
    private String billLabel, kategori, dueDate;
    private double nominal;

    private ApiService api;
    private RecyclerView rv;
    private ShimmerFrameLayout shimmer;
    private LinearLayout emptyState, errorState;
    private TextView tvError, tvJudul, tvSummary;
    // 4 TextView untuk stat card
    private TextView tvTotalDana, tvTotalTunggakan, tvLunasCount, tvMenunggakCount;

    public static KonfirmasiPerTagihanFragment newInstance(
            int billId, String billLabel, String kategori, double nominal, String dueDate) {
        Bundle args = new Bundle();
        args.putInt(ARG_BILL_ID, billId);
        args.putString(ARG_BILL_LABEL, billLabel);
        args.putString(ARG_KATEGORI, kategori);
        args.putDouble(ARG_NOMINAL, nominal);
        args.putString(ARG_DUE_DATE, dueDate);
        KonfirmasiPerTagihanFragment f = new KonfirmasiPerTagihanFragment();
        f.setArguments(args);
        return f;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        return i.inflate(R.layout.fragment_konfirmasi_per_tagihan, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        billId    = getArguments() != null ? getArguments().getInt(ARG_BILL_ID)       : -1;
        billLabel = getArguments() != null ? getArguments().getString(ARG_BILL_LABEL) : "";
        kategori  = getArguments() != null ? getArguments().getString(ARG_KATEGORI)   : "";
        nominal   = getArguments() != null ? getArguments().getDouble(ARG_NOMINAL)    : 0;
        dueDate   = getArguments() != null ? getArguments().getString(ARG_DUE_DATE)   : "";

        rv         = view.findViewById(R.id.rv_siswa_status);
        shimmer    = view.findViewById(R.id.shimmer_layout);
        emptyState = view.findViewById(R.id.empty_state);
        errorState = view.findViewById(R.id.error_state);
        tvError    = view.findViewById(R.id.tv_error);
        tvJudul    = view.findViewById(R.id.tv_judul_tagihan);
        tvSummary  = view.findViewById(R.id.tv_summary);
        
        tvTotalDana      = view.findViewById(R.id.tv_total_dana);
        tvTotalTunggakan = view.findViewById(R.id.tv_total_tunggakan);
        tvLunasCount     = view.findViewById(R.id.tv_lunas_count);
        tvMenunggakCount = view.findViewById(R.id.tv_menunggak_count);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        api = ApiClient.getService(requireContext());

        tvJudul.setText(kategori + " — " + billLabel);

        view.findViewById(R.id.btn_back).setOnClickListener(v ->
            requireActivity().getSupportFragmentManager().popBackStack());

        view.findViewById(R.id.btn_tandai_semua_lunas).setOnClickListener(v ->
            konfirmasiTandaiSemuaLunas());

        view.findViewById(R.id.btn_retry).setOnClickListener(v -> loadData());

        loadData();
    }

    private void loadData() {
        shimmer.setVisibility(View.VISIBLE); shimmer.startShimmer();
        rv.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);
        errorState.setVisibility(View.GONE);

        api.getPaymentsByBill(billId).enqueue(new Callback<BillPaymentStatusResponse>() {
            @Override public void onResponse(Call<BillPaymentStatusResponse> c,
                                             Response<BillPaymentStatusResponse> r) {
                shimmer.stopShimmer(); shimmer.setVisibility(View.GONE);
                if (r.isSuccessful() && r.body() != null) {
                    List<BillPaymentStatus> list = r.body().data;
                    if (list == null || list.isEmpty()) {
                        emptyState.setVisibility(View.VISIBLE);
                        return;
                    }
                    updateStatCard(list);
                    updateSummary(list);
                    rv.setVisibility(View.VISIBLE);
                    rv.setAdapter(new SiswaStatusAdapter(list, nominal, dueDate, item
                            -> showUbahStatusDialog(item)));
                } else {
                    errorState.setVisibility(View.VISIBLE);
                    tvError.setText("Gagal memuat data siswa");
                }
            }
            @Override public void onFailure(Call<BillPaymentStatusResponse> c, Throwable t) {
                shimmer.stopShimmer(); shimmer.setVisibility(View.GONE);
                errorState.setVisibility(View.VISIBLE);
                tvError.setText(t.getMessage());
            }
        });
    }

    private void updateStatCard(List<BillPaymentStatus> list) {
        int lunas = 0, tunggak = 0;
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        boolean isOverdue = dueDate != null && !dueDate.isEmpty() && today.compareTo(dueDate) > 0;

        for (BillPaymentStatus s : list) {
            if ("LUNAS".equals(s.status)) {
                lunas++;
            } else if ("MENUNGGAK".equals(s.status) || isOverdue) {
                tunggak++;
            }
        }
        double terkumpul  = lunas   * nominal;
        double tertunggak = tunggak * nominal;

        tvTotalDana.setText(CurrencyUtils.formatRupiahShort(terkumpul));
        tvTotalTunggakan.setText(CurrencyUtils.formatRupiahShort(tertunggak));
        tvLunasCount.setText("● " + lunas + " Lunas");
        tvMenunggakCount.setText("● " + tunggak + " Menunggak");
        tvSummary.setText(list.size() + " Total");
    }

    private void updateSummary(List<BillPaymentStatus> list) {
        int lunas = 0, tunggak = 0, belum = 0;
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        boolean isOverdue = dueDate != null && !dueDate.isEmpty() && today.compareTo(dueDate) > 0;

        for (BillPaymentStatus s : list) {
            if ("LUNAS".equals(s.status)) {
                lunas++;
            } else if ("MENUNGGAK".equals(s.status) || isOverdue) {
                tunggak++;
            } else {
                belum++;
            }
        }
        tvSummary.setText(String.format(Locale.getDefault(),
            "Lunas: %d  |  Menunggak: %d  |  Belum: %d  |  Nominal: %s",
            lunas, tunggak, belum, CurrencyUtils.formatRupiahShort(nominal)));
    }

    private void showUbahStatusDialog(BillPaymentStatus item) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(item.siswaNama);
        
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        boolean isOverdue = dueDate != null && !dueDate.isEmpty() && today.compareTo(dueDate) > 0;
        String displayStatus = item.status;
        if (!"LUNAS".equals(item.status) && isOverdue) displayStatus = "MENUNGGAK";
        
        builder.setMessage("Status saat ini: " + displayStatus.replace('_', ' '));

        if (!"LUNAS".equals(item.status)) {
            builder.setPositiveButton("Tandai Lunas", (d, w) -> {
                showTanggalBayarPicker(item.paymentId, item.siswaNama, "LUNAS");
            });
            // Hapus manual toggle menunggak karena sudah otomatis
        } else {
            builder.setPositiveButton("Batalkan Pelunasan", (d, w) -> {
                ubahStatus(item.paymentId, "BELUM_JATUH_TEMPO", null, item.siswaNama);
            });
        }

        builder.setNegativeButton("Tutup", null);
        builder.show();
    }

    private void showTanggalBayarPicker(int paymentId, String siswaNama, String newStatus) {
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding, padding, 0);

        Context contextThemeWrapper = new ContextThemeWrapper(requireContext(), 
                com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
        TextInputLayout layout = new TextInputLayout(contextThemeWrapper);
        layout.setHint("Pilih Tanggal Bayar");

        TextInputEditText etTanggal = new TextInputEditText(layout.getContext());
        etTanggal.setFocusable(false);
        etTanggal.setClickable(true);

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        etTanggal.setText(today);

        layout.addView(etTanggal, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        container.addView(layout);

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Konfirmasi Lunas")
            .setMessage("Siswa: " + siswaNama + "\nPastikan tanggal sudah sesuai:")
            .setView(container)
            .setPositiveButton("KONFIRMASI LUNAS", (d, w) -> {
                String tgl = etTanggal.getText() != null ? etTanggal.getText().toString() : today;
                ubahStatus(paymentId, newStatus, tgl, siswaNama);
            })
            .setNegativeButton("BATAL", null)
            .show();

        DatePickerHelper.attach(requireContext(), etTanggal, date -> etTanggal.setText(date));
    }

    private void ubahStatus(int paymentId, String newStatus, String tanggal, String siswaNama) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", newStatus);
        if (tanggal != null) body.put("tanggal_bayar", tanggal);

        api.updatePaymentStatus(paymentId, body).enqueue(new Callback<BaseResponse>() {
            @Override public void onResponse(Call<BaseResponse> c, Response<BaseResponse> r) {
                if (r.isSuccessful()) {
                    String msg = "LUNAS".equals(newStatus)
                        ? "✅ " + siswaNama + " ditandai LUNAS"
                        : "Status " + siswaNama + " diperbarui";
                    Snackbar.make(requireView(), msg, Snackbar.LENGTH_LONG).show();
                    loadData();
                } else {
                    // Parse error body dari server untuk mendapatkan pesan detail
                    BaseResponse errBody = null;
                    try {
                        if (r.errorBody() != null) {
                            errBody = new Gson().fromJson(r.errorBody().string(), BaseResponse.class);
                        }
                    } catch (Exception ignored) {}

                    if (errBody != null && errBody.blocked) {
                        // Tampilkan dialog khusus jika terblokir karena urutan pembayaran
                        showBlockedDialog(siswaNama, errBody.periodeBelum, errBody.jumlahBelum);
                    } else {
                        String pesanError = (errBody != null && errBody.message != null)
                                ? errBody.message
                                : "Gagal mengubah status";
                        Snackbar.make(requireView(), pesanError, Snackbar.LENGTH_LONG).show();
                    }
                }
            }
            @Override public void onFailure(Call<BaseResponse> c, Throwable t) {
                Snackbar.make(requireView(), "Error: " + t.getMessage(), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    /** Dialog peringatan saat siswa masih punya tunggakan periode sebelumnya */
    private void showBlockedDialog(String siswaNama, String periodeBelum, int jumlahBelum) {
        if (!isAdded()) return;

        String periodeText = (periodeBelum != null && !periodeBelum.isEmpty())
                ? periodeBelum
                : "-";

        String pesan = "Tidak bisa menandai lunas!\n\n"
                + siswaNama + " masih memiliki "
                + jumlahBelum + " tagihan yang belum dibayar pada periode sebelumnya:\n\n"
                + "📋 " + periodeText + "\n\n"
                + "Harap lunasi tagihan periode tersebut terlebih dahulu.";

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("⚠️ Tagihan Belum Lunas")
                .setMessage(pesan)
                .setPositiveButton("Mengerti", null)
                .setCancelable(true)
                .show();
    }

    private void konfirmasiTandaiSemuaLunas() {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Tandai Semua Lunas")
            .setMessage("Semua siswa yang belum lunas akan ditandai LUNAS " +
                        "pada hari ini. Otomatis mencatat kas. Lanjutkan?")
            .setPositiveButton("Tandai Semua", (d, w) -> {
                String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                if (rv.getAdapter() instanceof SiswaStatusAdapter adapter) {
                    List<BillPaymentStatus> nonLunas = adapter.getNonLunasList();
                    if (nonLunas.isEmpty()) {
                        Snackbar.make(requireView(), "Semua siswa sudah lunas", Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    prosesSemuaLunas(nonLunas, today, 0);
                }
            })
            .setNegativeButton("Batal", null)
            .show();
    }

    private void prosesSemuaLunas(List<BillPaymentStatus> list, String tanggal, int index) {
        if (index >= list.size()) {
            Snackbar.make(requireView(), "✅ " + list.size() + " siswa ditandai LUNAS", Snackbar.LENGTH_LONG).show();
            loadData();
            return;
        }

        BillPaymentStatus item = list.get(index);
        Map<String, Object> body = new HashMap<>();
        body.put("status", "LUNAS");
        body.put("tanggal_bayar", tanggal);

        api.updatePaymentStatus(item.paymentId, body).enqueue(new Callback<BaseResponse>() {
            @Override public void onResponse(Call<BaseResponse> c, Response<BaseResponse> r) {
                prosesSemuaLunas(list, tanggal, index + 1);
            }
            @Override public void onFailure(Call<BaseResponse> c, Throwable t) {
                prosesSemuaLunas(list, tanggal, index + 1);
            }
        });
    }
}
