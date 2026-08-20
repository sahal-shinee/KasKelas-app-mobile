package com.kaskelas.ui.siswa.dashboard;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.kaskelas.R;
import com.kaskelas.data.model.Payment;
import com.kaskelas.utils.CurrencyUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TagihanAktifAdapter extends RecyclerView.Adapter<TagihanAktifAdapter.VH> {
    private final List<Payment> list;
    public TagihanAktifAdapter(List<Payment> list) { this.list = list; }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tagihan_aktif, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Payment p = list.get(pos);
        h.tvKategori.setText(p.kategori);
        h.tvPeriode.setText(p.periodeLabel);
        h.tvNominal.setText(CurrencyUtils.formatRupiah(p.nominal));
        h.tvJatuhTempo.setText("Jatuh tempo: " + p.tanggalJatuhTempo);

        // LOGIKA OTOMATIS: Cek apakah sudah lewat jatuh tempo
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        boolean isOverdue = p.tanggalJatuhTempo != null && !p.tanggalJatuhTempo.isEmpty() && today.compareTo(p.tanggalJatuhTempo) > 0;

        if ("MENUNGGAK".equals(p.status) || isOverdue) {
            h.chipStatus.setText("Menunggak");
            h.chipStatus.setChipBackgroundColorResource(R.color.color_error_light);
            h.chipStatus.setTextColor(h.itemView.getContext().getColor(R.color.color_error));
        } else {
            h.chipStatus.setText("Belum Jatuh Tempo");
            h.chipStatus.setChipBackgroundColorResource(R.color.color_warning_light);
            h.chipStatus.setTextColor(h.itemView.getContext().getColor(R.color.color_warning));
        }
    }

    @Override public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvKategori, tvPeriode, tvNominal, tvJatuhTempo;
        Chip chipStatus;
        VH(@NonNull View v) {
            super(v);
            tvKategori  = v.findViewById(R.id.tv_kategori);
            tvPeriode   = v.findViewById(R.id.tv_periode);
            tvNominal   = v.findViewById(R.id.tv_nominal);
            tvJatuhTempo= v.findViewById(R.id.tv_jatuh_tempo);
            chipStatus  = v.findViewById(R.id.chip_status);
        }
    }
}
