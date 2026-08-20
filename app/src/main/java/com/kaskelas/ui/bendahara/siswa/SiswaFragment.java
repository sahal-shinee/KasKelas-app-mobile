package com.kaskelas.ui.bendahara.siswa;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.*;
import com.kaskelas.R;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.kaskelas.data.api.ApiClient;
import com.kaskelas.data.api.ApiService;
import com.kaskelas.data.api.response.BaseResponse;
import com.kaskelas.data.model.Student;
import com.kaskelas.viewmodel.StudentViewModel;

import java.util.*;
import retrofit2.*;

public class SiswaFragment extends Fragment {

    private StudentViewModel viewModel;
    private SiswaAdapter adapter;
    private RecyclerView rv;
    private ShimmerFrameLayout shimmer;
    private LinearLayout emptyState, errorState;
    private TextView tvError, tvTotalSiswa;
    private TextInputEditText etSearch;
    private ApiService api;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        return i.inflate(R.layout.fragment_siswa, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rv           = view.findViewById(R.id.rv_siswa);
        shimmer      = view.findViewById(R.id.shimmer_layout);
        emptyState   = view.findViewById(R.id.empty_state);
        errorState   = view.findViewById(R.id.error_state);
        tvError      = view.findViewById(R.id.tv_error);
        tvTotalSiswa = view.findViewById(R.id.tv_total_siswa);
        etSearch     = view.findViewById(R.id.et_search);

        // Setup Filter Status
        ChipGroup cgFilter = view.findViewById(R.id.cg_filter);
        cgFilter.setOnCheckedChangeListener((group, checkedId) -> {
            if (adapter == null) return;
            if (checkedId == R.id.chip_semua) adapter.setFilterStatus("SEMUA");
            else if (checkedId == R.id.chip_lunas) adapter.setFilterStatus("LUNAS");
            else if (checkedId == R.id.chip_menunggak) adapter.setFilterStatus("MENUNGGAK");
            else if (checkedId == R.id.chip_belum) adapter.setFilterStatus("BELUM");
        });

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        api = ApiClient.getService(requireContext());
        viewModel = new ViewModelProvider(this).get(StudentViewModel.class);

        // Search — filter adapter langsung
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) adapter.filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        view.findViewById(R.id.btn_tambah_siswa).setOnClickListener(v -> showAddDialog());
        view.findViewById(R.id.fab_add).setOnClickListener(v -> showAddDialog());
        view.findViewById(R.id.btn_retry).setOnClickListener(v -> loadData());

        loadData();
    }

    private void loadData() {
        shimmer.setVisibility(View.VISIBLE); shimmer.startShimmer();
        rv.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);
        errorState.setVisibility(View.GONE);

        viewModel.getStudents("SEMUA").observe(getViewLifecycleOwner(), result -> {
            if (result.isLoading()) return;

            shimmer.stopShimmer(); shimmer.setVisibility(View.GONE);

            if (result.isError()) {
                errorState.setVisibility(View.VISIBLE);
                tvError.setText(result.message);
                return;
            }

            List<Student> list = result.data != null ? result.data.data : null;
            tvTotalSiswa.setText(list != null ? String.valueOf(list.size()) : "0");

            if (list == null || list.isEmpty()) {
                emptyState.setVisibility(View.VISIBLE);
                return;
            }

            rv.setVisibility(View.VISIBLE);
            adapter = new SiswaAdapter(list, this::onAction);
            rv.setAdapter(adapter);

            // Re-apply current search/filter if list is reloaded
            if (etSearch.getText() != null) adapter.filter(etSearch.getText().toString());
        });
    }

    private void onAction(Student s, String action) {
        switch (action) {
            case "EDIT":   showEditDialog(s);   break;
            case "DELETE": confirmDelete(s);    break;
            case "DETAIL":
                StudentDetailFragment detail = StudentDetailFragment.newInstance(s.id, s.nama);
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, detail)
                        .addToBackStack(null)
                        .commit();
                break;
        }
    }

    private void showAddDialog() {
        View dv = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_siswa, null);
        TextInputEditText etNis = dv.findViewById(R.id.et_nis);
        TextInputEditText etNama = dv.findViewById(R.id.et_nama);
        TextInputEditText etPin = dv.findViewById(R.id.et_pin);
        TextInputEditText etAbsen = dv.findViewById(R.id.et_nomor_absen);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Tambah Siswa")
                .setView(dv)
                .setPositiveButton("Tambah", (d, w) -> {
                    String nis = text(etNis), nama = text(etNama), pin = text(etPin), absen = text(etAbsen);
                    if (nis.isEmpty() || nama.isEmpty() || pin.isEmpty() || absen.isEmpty()) {
                        Snackbar.make(requireView(), "Semua field wajib diisi", Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    Map<String, Object> body = new HashMap<>();
                    body.put("nis", nis); body.put("nama", nama); body.put("pin", pin);
                    body.put("nomor_absen", Integer.parseInt(absen));

                    api.addStudent(body).enqueue(new Callback<BaseResponse>() {
                        @Override public void onResponse(Call<BaseResponse> c, Response<BaseResponse> r) {
                            if (r.isSuccessful()) { loadData(); }
                        }
                        @Override public void onFailure(Call<BaseResponse> c, Throwable t) {}
                    });
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showEditDialog(Student s) {
        View dv = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_siswa, null);
        TextInputEditText etNis = dv.findViewById(R.id.et_nis);
        TextInputEditText etNama = dv.findViewById(R.id.et_nama);
        TextInputEditText etPin = dv.findViewById(R.id.et_pin);
        TextInputEditText etAbsen = dv.findViewById(R.id.et_nomor_absen);

        etNis.setText(s.nis); etNama.setText(s.nama); etAbsen.setText(String.valueOf(s.nomorAbsen));

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Edit Siswa")
                .setView(dv)
                .setPositiveButton("Update", (d, w) -> {
                    String nama = text(etNama), pin = text(etPin), absen = text(etAbsen);
                    Map<String, Object> body = new HashMap<>();
                    body.put("nama", nama); body.put("nomor_absen", Integer.parseInt(absen));
                    if (!pin.isEmpty()) body.put("pin", pin);

                    api.updateStudent(s.id, body).enqueue(new Callback<BaseResponse>() {
                        @Override public void onResponse(Call<BaseResponse> c, Response<BaseResponse> r) {
                            if (r.isSuccessful()) loadData();
                        }
                        @Override public void onFailure(Call<BaseResponse> c, Throwable t) {}
                    });
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void confirmDelete(Student s) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Hapus Siswa")
                .setMessage("Yakin hapus " + s.nama + "?")
                .setPositiveButton("Hapus", (d, w) -> api.deleteStudent(s.id).enqueue(new Callback<BaseResponse>() {
                    @Override public void onResponse(Call<BaseResponse> c, Response<BaseResponse> r) {
                        if (r.isSuccessful()) loadData();
                    }
                    @Override public void onFailure(Call<BaseResponse> c, Throwable t) {}
                }))
                .setNegativeButton("Batal", null)
                .show();
    }

    private String text(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
