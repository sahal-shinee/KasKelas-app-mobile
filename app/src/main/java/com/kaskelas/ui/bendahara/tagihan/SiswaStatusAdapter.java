package com.kaskelas.ui.bendahara.tagihan;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.kaskelas.R;
import com.kaskelas.data.model.BillPaymentStatus;
import com.kaskelas.utils.CurrencyUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * RecyclerView adapter: daftar siswa + status bayar untuk satu tagihan.
 * Tap baris → callback untuk ubah status.
 */
public class SiswaStatusAdapter extends RecyclerView.Adapter<SiswaStatusAdapter.VH> {

    private final List<BillPaymentStatus> list;
    private final Consumer<BillPaymentStatus> onItemClick;
    private final double nominal;
    private final String dueDate;

    public SiswaStatusAdapter(List<BillPaymentStatus> list,
                              double nominal,
                              String dueDate,
                              Consumer<BillPaymentStatus> onItemClick) {
        this.list        = list;
        this.nominal     = nominal;
        this.dueDate     = dueDate;
        this.onItemClick = onItemClick;
    }

    /** Kembalikan semua item yang belum LUNAS, untuk fitur "Tandai Semua Lunas" */
    public List<BillPaymentStatus> getNonLunasList() {
        List<BillPaymentStatus> result = new ArrayList<>();
        for (BillPaymentStatus s : list) {
            if (!"LUNAS".equals(s.status)) result.add(s);
        }
        return result;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_siswa_status, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        BillPaymentStatus s = list.get(pos);

        h.tvNama.setText(s.siswaNama);
        h.tvNis.setText("NIS: " + s.nis + "  |  Absen: " + s.nomorAbsen);
        h.tvInitial.setText(String.valueOf(s.nomorAbsen));
        h.tvTagihan.setText(CurrencyUtils.formatRupiahShort(nominal));

        // LOGIKA OTOMATIS: Tentukan status efektif (Menunggak jika sudah lewat jatuh tempo)
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        boolean isOverdue = dueDate != null && !dueDate.isEmpty() && today.compareTo(dueDate) > 0;
        
        String effectiveStatus = s.status;
        if (!"LUNAS".equals(s.status) && isOverdue) {
            effectiveStatus = "MENUNGGAK";
        }

        // Tentukan warna berdasarkan status efektif
        int color;
        switch (effectiveStatus) {
            case "LUNAS":
                color = ContextCompat.getColor(h.itemView.getContext(), R.color.color_success);
                h.chipStatus.setText("✓ Lunas");
                h.chipStatus.setChipBackgroundColorResource(R.color.color_success_light);
                h.btnUbah.setText("Batalkan");
                h.btnUbah.setStrokeColorResource(R.color.color_error);
                break;
            case "MENUNGGAK":
                color = ContextCompat.getColor(h.itemView.getContext(), R.color.color_error);
                h.chipStatus.setText("⚠ Menunggak");
                h.chipStatus.setChipBackgroundColorResource(R.color.color_error_light);
                h.btnUbah.setText("Tandai Lunas");
                h.btnUbah.setStrokeColorResource(R.color.color_success);
                break;
            default: // BELUM_JATUH_TEMPO
                color = ContextCompat.getColor(h.itemView.getContext(), R.color.color_warning);
                h.chipStatus.setText("○ Belum Bayar");
                h.chipStatus.setChipBackgroundColorResource(R.color.color_warning_light);
                h.btnUbah.setText("Tandai Lunas");
                h.btnUbah.setStrokeColorResource(R.color.color_success);
                break;
        }

        // Terapkan warna ke chip dan dot status
        h.chipStatus.setTextColor(color);
        h.tvStatusDot.setTextColor(color);

        if (s.tanggalBayar != null && !s.tanggalBayar.isEmpty()) {
            h.tvTanggalBayar.setVisibility(View.VISIBLE);
            h.tvTanggalBayar.setText("Bayar: " + s.tanggalBayar);
        } else {
            h.tvTanggalBayar.setVisibility(View.GONE);
        }

        h.btnUbah.setOnClickListener(v -> onItemClick.accept(s));
        h.itemView.setOnClickListener(v -> onItemClick.accept(s));
    }

    @Override public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvNama, tvNis, tvInitial, tvTanggalBayar;
        TextView tvTagihan;
        TextView tvStatusDot;
        Chip chipStatus;
        MaterialButton btnUbah;

        VH(@NonNull View v) {
            super(v);
            tvNama        = v.findViewById(R.id.tv_nama);
            tvNis         = v.findViewById(R.id.tv_nis);
            tvInitial     = v.findViewById(R.id.tv_initial);
            tvTanggalBayar= v.findViewById(R.id.tv_tanggal_bayar);
            chipStatus    = v.findViewById(R.id.chip_status);
            btnUbah       = v.findViewById(R.id.btn_ubah_status);
            tvTagihan     = v.findViewById(R.id.tv_tagihan);
            tvStatusDot   = v.findViewById(R.id.tv_status_dot);
        }
    }
}
