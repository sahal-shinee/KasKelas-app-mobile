package com.kaskelas.ui.siswa.notifikasi;

import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.kaskelas.R;
import com.kaskelas.data.api.ApiClient;
import com.kaskelas.data.api.ApiService;
import com.kaskelas.data.api.response.BaseResponse;
import com.kaskelas.data.api.response.NotificationListResponse;
import com.kaskelas.data.model.Notification;
import com.kaskelas.ui.bendahara.notifikasi.NotifikasiAdapter;

import java.util.List;

import retrofit2.*;

public class SiswaNotifikasiFragment extends Fragment {

    private RecyclerView rv;
    private ShimmerFrameLayout shimmer;
    private LinearLayout emptyState, errorState;
    private TextView tvError;
    private View scrollView;
    private LinearLayout tvCaughtUpContainer;
    private com.google.android.material.button.MaterialButton btnHapusSemua;

    private ApiService api;
    private NotifikasiAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifikasi, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rv = view.findViewById(R.id.rv_notifikasi);
        shimmer = view.findViewById(R.id.shimmer_layout);
        emptyState = view.findViewById(R.id.empty_state);
        errorState = view.findViewById(R.id.error_state);
        tvError = view.findViewById(R.id.tv_error);
        scrollView = view.findViewById(R.id.scroll_view);
        tvCaughtUpContainer = view.findViewById(R.id.tv_caught_up_container);
        btnHapusSemua = view.findViewById(R.id.btn_hapus_semua);

        // ❌ Sembunyikan FAB kirim (khusus bendahara)
        view.findViewById(R.id.fab_kirim).setVisibility(View.GONE);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setNestedScrollingEnabled(false);

        api = ApiClient.getService(requireContext());

        view.findViewById(R.id.btn_retry).setOnClickListener(v -> loadData());
        btnHapusSemua.setOnClickListener(v -> confirmHapusSemua());

        loadData();
    }

    // ================= LOAD DATA =================

    private void loadData() {
        showShimmer();

        api.getMyNotifications().enqueue(new Callback<NotificationListResponse>() {
            @Override
            public void onResponse(@NonNull Call<NotificationListResponse> call,
                                   @NonNull Response<NotificationListResponse> response) {

                hideShimmer();

                if (response.isSuccessful() && response.body() != null) {
                    List<Notification> list = response.body().data;

                    if (list == null || list.isEmpty()) {
                        showEmpty();
                    } else {
                        showList(list);
                        markAllAsRead(list);
                    }
                } else {
                    showError("Gagal memuat (" + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<NotificationListResponse> call,
                                  @NonNull Throwable t) {
                hideShimmer();
                showError(t.getMessage());
            }
        });
    }

    // ================= UI =================

    private void showShimmer() {
        shimmer.setVisibility(View.VISIBLE);
        shimmer.startShimmer();

        scrollView.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);
        errorState.setVisibility(View.GONE);
        btnHapusSemua.setVisibility(View.GONE);
    }

    private void hideShimmer() {
        shimmer.stopShimmer();
        shimmer.setVisibility(View.GONE);
    }

    private void showList(List<Notification> list) {
        adapter = new NotifikasiAdapter(list);

        // ✅ AKTIFKAN DELETE
        adapter.setOnDeleteListener(this::confirmHapusItem);

        rv.setAdapter(adapter);

        scrollView.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);
        errorState.setVisibility(View.GONE);
        tvCaughtUpContainer.setVisibility(View.GONE);
        btnHapusSemua.setVisibility(View.VISIBLE);
    }

    private void showEmpty() {
        scrollView.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
        errorState.setVisibility(View.GONE);
        btnHapusSemua.setVisibility(View.GONE);
    }

    private void showError(String msg) {
        scrollView.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);
        errorState.setVisibility(View.VISIBLE);
        tvError.setText(msg);
        btnHapusSemua.setVisibility(View.GONE);
    }

    private void checkIfEmpty() {
        if (adapter == null || adapter.getItemCount() == 0) {
            tvCaughtUpContainer.setVisibility(View.VISIBLE);
            btnHapusSemua.setVisibility(View.GONE);
        }
    }

    // ================= DELETE ITEM =================

    private void confirmHapusItem(Notification notif, int position) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Hapus Notifikasi")
                .setMessage("Yakin ingin menghapus?")
                .setPositiveButton("Hapus", (d, w) -> deleteItem(notif, position))
                .setNegativeButton("Batal", null)
                .show();
    }

    private void deleteItem(Notification notif, int position) {
        adapter.removeAt(position);
        checkIfEmpty();

        api.deleteNotification(notif.id).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                if (!response.isSuccessful()) {
                    Snackbar.make(requireView(), "Gagal menghapus", Snackbar.LENGTH_SHORT).show();
                    loadData();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                Snackbar.make(requireView(), "Error: " + t.getMessage(), Snackbar.LENGTH_SHORT).show();
                loadData();
            }
        });

        Snackbar.make(requireView(), "Notifikasi dihapus", Snackbar.LENGTH_SHORT).show();
    }

    // ================= DELETE ALL =================

    private void confirmHapusSemua() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Hapus Semua")
                .setMessage("Hapus semua notifikasi?")
                .setPositiveButton("Hapus", (d, w) -> deleteAllItems())
                .setNegativeButton("Batal", null)
                .show();
    }

    private void deleteAllItems() {
        if (adapter == null) return;

        adapter.clearAll();
        checkIfEmpty();

        api.deleteAllNotifications().enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                if (!response.isSuccessful()) {
                    Snackbar.make(requireView(), "Gagal hapus semua", Snackbar.LENGTH_SHORT).show();
                    loadData();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                Snackbar.make(requireView(), "Error: " + t.getMessage(), Snackbar.LENGTH_SHORT).show();
                loadData();
            }
        });

        Snackbar.make(requireView(), "Semua notifikasi dihapus", Snackbar.LENGTH_SHORT).show();
    }

    // ================= MARK READ =================

    private void markAllAsRead(List<Notification> list) {
        for (Notification n : list) {
            if (n.isRead == 0) {
                api.markNotificationRead(n.id).enqueue(new Callback<BaseResponse>() {
                    @Override public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {}
                    @Override public void onFailure(Call<BaseResponse> call, Throwable t) {}
                });
            }
        }
    }
}