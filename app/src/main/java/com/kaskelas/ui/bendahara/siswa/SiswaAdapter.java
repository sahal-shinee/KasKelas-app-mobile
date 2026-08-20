package com.kaskelas.ui.bendahara.siswa;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.kaskelas.R;
import com.kaskelas.data.model.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class SiswaAdapter extends RecyclerView.Adapter<SiswaAdapter.VH> {

    private final List<Student> fullList;
    private List<Student> displayList;
    private final BiConsumer<Student, String> onAction;
    private String currentFilter = "SEMUA";

    public SiswaAdapter(List<Student> list, BiConsumer<Student, String> onAction) {
        this.fullList    = new ArrayList<>(list);
        this.displayList = new ArrayList<>(list);
        this.onAction    = onAction;
    }

    public void filter(String query) {
        applyFilter(query, currentFilter);
    }

    public void setFilterStatus(String status) {
        this.currentFilter = status;
        applyFilter(null, status);
    }

    private void applyFilter(String query, String status) {
        displayList = new ArrayList<>();
        String q = (query != null) ? query.toLowerCase().trim() : "";
        
        for (Student s : fullList) {
            boolean matchesSearch = q.isEmpty() || (s.nama != null && s.nama.toLowerCase().contains(q)) || (s.nis != null && s.nis.contains(q));
            
            // LOGIKA STRICT (SAMA DENGAN DISPLAY)
            // 1. Menunggak jika ada nominal tunggakan > 0 atau jumlah tunggakan > 0
            boolean isMenunggak = (s.totalTunggakan > 0 || s.totalTunggakanNominal > 0.01);
            
            // 2. Belum Bayar jika tidak menunggak tapi masih ada tagihan belum lunas
            boolean isBelumBayar = !isMenunggak && (s.jumlahBelumBayar > 0 || s.tagihanLunas < s.totalTagihan);
            
            // 3. Lunas hanya jika benar-benar tidak ada tunggakan dan tidak ada yang belum bayar
            boolean isLunas = !isMenunggak && !isBelumBayar;

            boolean matchesStatus = status.equals("SEMUA") ||
                    (status.equals("LUNAS") && isLunas) ||
                    (status.equals("MENUNGGAK") && isMenunggak) ||
                    (status.equals("BELUM") && isBelumBayar);

            if (matchesSearch && matchesStatus) {
                displayList.add(s);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_siswa, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Student s = displayList.get(pos);
        Context ctx = h.itemView.getContext();

        // --- Inisial Nama ---
        String initial = "?";
        if (s.nama != null && !s.nama.trim().isEmpty()) {
            String name = s.nama.trim();
            String[] parts = name.split("\\s+");
            if (parts.length >= 2) {
                initial = String.valueOf(parts[0].charAt(0)).toUpperCase()
                        + String.valueOf(parts[1].charAt(0)).toUpperCase();
            } else {
                initial = String.valueOf(parts[0].charAt(0)).toUpperCase();
            }
        }
        h.tvInitial.setText(initial);
        h.tvNama.setText(s.nama);
        h.tvNisAbsen.setText("NIS: " + s.nis + " | Absen: " + s.nomorAbsen);

        // --- LOGIKA STATUS KETAT (STRICT) ---
        // 1. Menunggak: Prioritas Utama.
        boolean isMenunggak = (s.totalTunggakan > 0 || s.totalTunggakanNominal > 0.01);
        
        // 2. Belum Bayar: Jika tidak menunggak, tapi masih ada tagihan belum lunas
        boolean isBelumBayar = !isMenunggak && (s.jumlahBelumBayar > 0 || s.tagihanLunas < s.totalTagihan);

        int bgColor, textColor;
        String statusText;

        if (isMenunggak) {
            statusText = "Menunggak";
            bgColor = ContextCompat.getColor(ctx, R.color.color_error_light);
            textColor = ContextCompat.getColor(ctx, R.color.color_error);
        } else if (isBelumBayar) {
            statusText = "Belum Bayar";
            bgColor = ContextCompat.getColor(ctx, R.color.color_warning_light);
            textColor = ContextCompat.getColor(ctx, R.color.color_warning);
        } else {
            statusText = "Lunas";
            bgColor = ContextCompat.getColor(ctx, R.color.color_success_light);
            textColor = ContextCompat.getColor(ctx, R.color.color_success);
        }

        h.tvStatusLabel.setText(statusText);
        h.tvStatusLabel.setTextColor(textColor);
        h.cvStatus.setCardBackgroundColor(bgColor);

        h.btnEdit.setOnClickListener(v -> onAction.accept(s, "EDIT"));
        h.btnMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add("Detail Pembayaran")
                    .setOnMenuItemClickListener(item -> { onAction.accept(s, "DETAIL"); return true; });
            popup.getMenu().add("Hapus")
                    .setOnMenuItemClickListener(item -> { onAction.accept(s, "DELETE"); return true; });
            popup.show();
        });

        h.itemView.setOnClickListener(v -> onAction.accept(s, "DETAIL"));
    }

    @Override public int getItemCount() { return displayList.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvNama, tvNisAbsen, tvInitial, tvStatusLabel;
        MaterialCardView cvStatus;
        ImageButton btnEdit, btnMenu;

        VH(@NonNull View v) {
            super(v);
            tvNama         = v.findViewById(R.id.tv_nama);
            tvNisAbsen     = v.findViewById(R.id.tv_nis_absen);
            tvInitial      = v.findViewById(R.id.tv_initial);
            cvStatus       = v.findViewById(R.id.cv_status);
            tvStatusLabel  = v.findViewById(R.id.tv_status_label);
            btnEdit        = v.findViewById(R.id.btn_edit);
            btnMenu        = v.findViewById(R.id.btn_menu);
        }
    }
}
