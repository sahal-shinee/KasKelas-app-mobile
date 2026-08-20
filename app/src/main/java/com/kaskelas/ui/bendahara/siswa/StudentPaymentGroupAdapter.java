package com.kaskelas.ui.bendahara.siswa;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.kaskelas.R;
import com.kaskelas.data.model.StudentPaymentGroup;
import com.kaskelas.utils.CurrencyUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Adapter untuk detail pembayaran siswa.
 * Setiap item = satu kategori tagihan berisi list periode.
 */
public class StudentPaymentGroupAdapter
        extends RecyclerView.Adapter<StudentPaymentGroupAdapter.VH> {

    private final List<StudentPaymentGroup> list;
    /**
     * Callback saat tombol "Lunas" ditekan.
     * Parameter: payment_id dari periode yang ingin ditandai lunas.
     */
    private final Consumer<StudentPaymentGroup.PeriodeItem> onTandaiLunas;

    public StudentPaymentGroupAdapter(List<StudentPaymentGroup> list,
                                      Consumer<StudentPaymentGroup.PeriodeItem> onTandaiLunas) {
        this.list           = list;
        this.onTandaiLunas  = onTandaiLunas;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_payment_group, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        StudentPaymentGroup group = list.get(pos);
        Context ctx = h.itemView.getContext();

        h.tvKategoriNama.setText(group.kategoriNama);

        // LOGIKA OTOMATIS: Recalculate summary based on current date
        int lunasCount = 0;
        int menunggakCount = 0;
        int belumCount = 0;
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        for (StudentPaymentGroup.PeriodeItem item : group.periode) {
            boolean isOverdue = item.tanggalJatuhTempo != null && today.compareTo(item.tanggalJatuhTempo) > 0;
            if ("LUNAS".equals(item.status)) {
                lunasCount++;
            } else if (isOverdue) {
                menunggakCount++;
            } else {
                belumCount++;
            }
        }

        // Ringkasan: "3 Lunas  •  1 Menunggak"
        StringBuilder ringkasan = new StringBuilder(lunasCount + " Lunas");
        if (menunggakCount > 0) ringkasan.append("  •  ").append(menunggakCount).append(" Menunggak");
        if (belumCount > 0)     ringkasan.append("  •  ").append(belumCount).append(" Belum");
        
        h.tvRingkasan.setText(ringkasan.toString());
        h.tvRingkasan.setTextColor(menunggakCount > 0
            ? ctx.getColor(R.color.color_error)
            : ctx.getColor(R.color.color_success));

        // Build list periode secara programatik
        h.containerPeriode.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(ctx);

        for (StudentPaymentGroup.PeriodeItem item : group.periode) {
            View row = inflater.inflate(R.layout.item_periode_row, h.containerPeriode, false);
            bindPeriodeRow(ctx, row, item, group);
            h.containerPeriode.addView(row);
        }
    }

    private void bindPeriodeRow(Context ctx, View row,
                                StudentPaymentGroup.PeriodeItem item,
                                StudentPaymentGroup group) {
        MaterialCardView cardUrutan = row.findViewById(R.id.card_urutan);
        TextView tvUrutan           = row.findViewById(R.id.tv_urutan);
        TextView tvLabel            = row.findViewById(R.id.tv_periode_label);
        TextView tvJatuh            = row.findViewById(R.id.tv_jatuh_tempo);
        TextView tvStatus           = row.findViewById(R.id.tv_status);
        TextView tvNominal          = row.findViewById(R.id.tv_nominal);
        com.google.android.material.button.MaterialButton btnLunas =
            row.findViewById(R.id.btn_tandai_lunas);

        tvUrutan.setText(String.valueOf(item.urutan));
        tvLabel.setText(item.periodeLabel);
        tvJatuh.setText("Jatuh tempo: " + item.tanggalJatuhTempo);
        tvNominal.setText(CurrencyUtils.formatRupiah(item.nominal));

        int colorGreen  = ctx.getColor(R.color.color_success);
        int colorRed    = ctx.getColor(R.color.color_error);
        int colorOrange = ctx.getColor(R.color.color_warning);
        int colorGreenBg  = ctx.getColor(R.color.color_success_light);
        int colorRedBg    = ctx.getColor(R.color.color_error_light);
        int colorOrangeBg = ctx.getColor(R.color.color_warning_light);

        // LOGIKA OTOMATIS: Cek apakah sudah menunggak berdasarkan tanggal hari ini
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        boolean isOverdue = item.tanggalJatuhTempo != null && today.compareTo(item.tanggalJatuhTempo) > 0;

        String effectiveStatus = item.status;
        if (!"LUNAS".equals(item.status) && isOverdue) {
            effectiveStatus = "MENUNGGAK";
        }

        switch (effectiveStatus) {
            case "LUNAS":
                tvStatus.setText("✓ Lunas");
                tvStatus.setTextColor(colorGreen);
                cardUrutan.setCardBackgroundColor(colorGreenBg);
                tvUrutan.setTextColor(colorGreen);
                btnLunas.setVisibility(View.GONE);
                break;
            case "MENUNGGAK":
                tvStatus.setText("⚠ Menunggak");
                tvStatus.setTextColor(colorRed);
                cardUrutan.setCardBackgroundColor(colorRedBg);
                tvUrutan.setTextColor(colorRed);
                btnLunas.setVisibility(View.VISIBLE);
                btnLunas.setOnClickListener(v -> onTandaiLunas.accept(item));
                break;
            default: // BELUM_JATUH_TEMPO
                tvStatus.setText("○ Belum Bayar");
                tvStatus.setTextColor(colorOrange);
                cardUrutan.setCardBackgroundColor(colorOrangeBg);
                tvUrutan.setTextColor(colorOrange);
                // Biarkan tombol muncul agar bisa dibayar sebelum jatuh tempo
                btnLunas.setVisibility(View.VISIBLE);
                btnLunas.setOnClickListener(v -> onTandaiLunas.accept(item));
                break;
        }
    }

    @Override public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvKategoriNama, tvRingkasan;
        LinearLayout containerPeriode;

        VH(@NonNull View v) {
            super(v);
            tvKategoriNama  = v.findViewById(R.id.tv_kategori_nama);
            tvRingkasan     = v.findViewById(R.id.tv_ringkasan);
            containerPeriode= v.findViewById(R.id.container_periode);
        }
    }
}
