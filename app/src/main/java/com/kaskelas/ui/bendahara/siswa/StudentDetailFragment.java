package com.kaskelas.ui.bendahara.siswa;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.kaskelas.R;
import com.kaskelas.data.api.ApiClient;
import com.kaskelas.data.api.ApiService;
import com.kaskelas.data.api.response.BaseResponse;
import com.kaskelas.data.api.response.StudentPaymentGroupResponse;
import com.kaskelas.data.model.StudentPaymentGroup;
import com.kaskelas.utils.DatePickerHelper;

import java.text.SimpleDateFormat;
import java.util.*;
import retrofit2.*;

public class StudentDetailFragment extends Fragment {

    private static final String ARG_ID   = "student_id";
    private static final String ARG_NAMA = "student_nama";

    public static StudentDetailFragment newInstance(int id, String nama) {
        Bundle args = new Bundle();
        args.putInt(ARG_ID, id);
        args.putString(ARG_NAMA, nama);
        StudentDetailFragment f = new StudentDetailFragment();
        f.setArguments(args);
        return f;
    }

    private int    studentId;
    private String studentNama;
    private ApiService api;
    private RecyclerView rv;
    private ShimmerFrameLayout shimmer;
    private LinearLayout emptyState;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        return i.inflate(R.layout.fragment_student_detail, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        studentId   = getArguments() != null ? getArguments().getInt(ARG_ID)       : -1;
        studentNama = getArguments() != null ? getArguments().getString(ARG_NAMA)  : "";

        rv          = view.findViewById(R.id.rv_payments);
        shimmer     = view.findViewById(R.id.shimmer_layout);
        emptyState  = view.findViewById(R.id.empty_state);

        ((TextView) view.findViewById(R.id.tv_title)).setText(studentNama);
        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        api = ApiClient.getService(requireContext());
        loadData();
    }

    private void loadData() {
        shimmer.startShimmer();
        shimmer.setVisibility(View.VISIBLE);
        rv.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);

        api.getStudentPayments(studentId).enqueue(new Callback<StudentPaymentGroupResponse>() {
            @Override public void onResponse(Call<StudentPaymentGroupResponse> c,
                                             Response<StudentPaymentGroupResponse> r) {
                shimmer.stopShimmer(); shimmer.setVisibility(View.GONE);
                if (r.isSuccessful() && r.body() != null) {
                    List<StudentPaymentGroup> list = r.body().data;
                    if (list == null || list.isEmpty()) {
                        emptyState.setVisibility(View.VISIBLE);
                        return;
                    }
                    rv.setVisibility(View.VISIBLE);
                    rv.setAdapter(new StudentPaymentGroupAdapter(list,
                            item -> showTandaiLunasDialog(item)));
                } else {
                    emptyState.setVisibility(View.VISIBLE);
                    Toast.makeText(requireContext(), "Gagal memuat data", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<StudentPaymentGroupResponse> c, Throwable t) {
                shimmer.stopShimmer(); shimmer.setVisibility(View.GONE);
                emptyState.setVisibility(View.VISIBLE);
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Dialog konfirmasi + date picker untuk tandai lunas satu periode */
    private void showTandaiLunasDialog(StudentPaymentGroup.PeriodeItem item) {
        TextInputEditText etTanggal = new TextInputEditText(requireContext());
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        etTanggal.setText(today);
        DatePickerHelper.attach(requireContext(), etTanggal, null);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Tandai Lunas")
                .setMessage("Periode: " + item.periodeLabel + "\nUrutan ke-" + item.urutan +
                        " dalam kategori ini")
                .setView(etTanggal)
                .setPositiveButton("Konfirmasi Lunas", (d, w) -> {
                    String tgl = etTanggal.getText() != null
                            ? etTanggal.getText().toString() : today;
                    ubahStatusLunas(item.paymentId, tgl, item.periodeLabel);
                })
                .setNegativeButton("Batal", null)
                .show();

        // Buka date picker langsung
        etTanggal.performClick();
    }

    private void ubahStatusLunas(int paymentId, String tanggal, String periodeLabel) {
        Map<String, Object> body = new HashMap<>();
        body.put("status",       "LUNAS");
        body.put("tanggal_bayar", tanggal);

        // Gunakan OkHttp langsung agar bisa baca errorBody tanpa di-consume logger
        com.kaskelas.data.api.ApiClient.getRawClient(requireContext())
                .newCall(buildUpdateRequest(paymentId, body))
                .enqueue(new okhttp3.Callback() {
                    @Override public void onFailure(okhttp3.Call call, java.io.IOException e) {
                        requireActivity().runOnUiThread(() ->
                                Snackbar.make(requireView(), "Error: " + e.getMessage(),
                                        Snackbar.LENGTH_SHORT).show());
                    }

                    @Override public void onResponse(okhttp3.Call call, okhttp3.Response response)
                            throws java.io.IOException {
                        // Baca body SEKALI di sini sebelum apapun lainnya
                        String rawBody = response.body() != null ? response.body().string() : "";

                        requireActivity().runOnUiThread(() -> {
                            if (response.isSuccessful()) {
                                Snackbar.make(requireView(),
                                        "✅ " + periodeLabel + " ditandai LUNAS — kas diperbarui",
                                        Snackbar.LENGTH_LONG).show();
                                loadData();
                            } else {
                                // Parse error JSON yang sudah dibaca
                                String msg   = "Gagal mengubah status";
                                String belum = "";
                                int jumlah   = 0;
                                try {
                                    com.google.gson.JsonObject json =
                                            com.google.gson.JsonParser.parseString(rawBody).getAsJsonObject();
                                    if (json.has("message"))       msg    = json.get("message").getAsString();
                                    if (json.has("periode_belum")) belum  = json.get("periode_belum").getAsString();
                                    if (json.has("jumlah_belum"))  jumlah = json.get("jumlah_belum").getAsInt();
                                } catch (Exception ignored) {}
                                showValidasiError(msg, belum, jumlah);
                            }
                        });
                    }
                });
    }

    /** Build OkHttp Request untuk PATCH payments/{id}/status */
    private okhttp3.Request buildUpdateRequest(int paymentId, Map<String, Object> body) {
        String baseUrl = com.kaskelas.utils.Constants.BASE_URL;
        String url     = baseUrl + "payments/" + paymentId + "/status";
        String token   = new com.kaskelas.utils.SessionManager(requireContext()).getToken();

        String json = new com.google.gson.Gson().toJson(body);
        okhttp3.RequestBody reqBody = okhttp3.RequestBody.create(
                json, okhttp3.MediaType.parse("application/json"));

        return new okhttp3.Request.Builder()
                .url(url)
                .patch(reqBody)
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .build();
    }

    /**
     * Dialog khusus saat validasi urutan gagal.
     * Tampilkan pesan jelas: periode mana saja yang harus dibayar dulu.
     */
    private void showValidasiError(String message, String periodeBelum, int jumlah) {
        // Buat pesan yang jelas dan terstruktur
        StringBuilder sb = new StringBuilder();
        sb.append(message);

        if (jumlah > 0 && periodeBelum != null && !periodeBelum.isEmpty()) {
            sb.append("\n\n");
            sb.append("📋 Periode yang harus dilunasi terlebih dahulu:");

            // Pecah per koma dan tampilkan sebagai bullet list
            String[] periodeList = periodeBelum.split(",\\s*");
            for (int i = 0; i < periodeList.length; i++) {
                sb.append("\n  ").append(i + 1).append(". ").append(periodeList[i].trim());
            }

            sb.append("\n\nPembayaran harus dilakukan secara berurutan dari periode paling lama.");
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("⚠️ Pembayaran Tidak Berurutan")
                .setMessage(sb.toString())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Mengerti", null)
                .setCancelable(true)
                .show();
    }
}
