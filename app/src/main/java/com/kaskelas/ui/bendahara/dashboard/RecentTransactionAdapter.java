package com.kaskelas.ui.bendahara.dashboard;

import android.view.*;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.kaskelas.R;
import com.kaskelas.data.model.Transaction;
import com.kaskelas.utils.CurrencyUtils;
import java.util.List;

public class RecentTransactionAdapter extends RecyclerView.Adapter<RecentTransactionAdapter.VH> {
    private final List<Transaction> list;

    public RecentTransactionAdapter(List<Transaction> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaksi, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Transaction t = list.get(pos);

        boolean isPemasukan = "PEMASUKAN".equals(t.tipe);

        // Header tanggal disembunyikan
        h.tvTanggalHeader.setVisibility(View.GONE);

        h.tvNama.setText(t.namaItem);

        String sub = t.dicatatOlehNama != null
                ? "Oleh: " + t.dicatatOlehNama
                : t.tanggal;
        h.tvKeterangan.setText(sub);

        h.tvJumlah.setText((isPemasukan ? "+" : "-") +
                CurrencyUtils.formatRupiahShort(t.jumlah));

        h.tvJumlah.setTextColor(h.itemView.getContext().getColor(
                isPemasukan ? R.color.color_success : R.color.color_error));

        h.tvTipe.setText(isPemasukan ? "Pemasukan" : "Pengeluaran");
        h.tvTipe.setTextColor(h.itemView.getContext().getColor(
                isPemasukan ? R.color.color_success : R.color.color_error));

        // Warna & icon dinamis
        int colorGreen = h.itemView.getContext().getColor(R.color.color_success);
        int colorRed   = h.itemView.getContext().getColor(R.color.color_error);
        int bgColor    = isPemasukan ? colorGreen : colorRed;

        h.cardIcon.setCardBackgroundColor(bgColor);
        h.ivIcon.setImageResource(isPemasukan ? R.drawable.ic_add : R.drawable.ic_delete);
        h.ivIcon.setColorFilter(isPemasukan ? colorGreen : colorRed);

        // Sembunyikan tombol menu di dashboard
        if (h.btnMenu != null) h.btnMenu.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTanggalHeader, tvNama, tvKeterangan, tvJumlah, tvTipe;
        MaterialCardView cardIcon;
        ImageView ivIcon;
        View btnMenu;

        VH(@NonNull View v) {
            super(v);
            tvTanggalHeader = v.findViewById(R.id.tv_tanggal_header);
            tvNama          = v.findViewById(R.id.tv_nama);
            // Menggunakan tv_tanggal karena tv_keterangan tidak ada di XML
            tvKeterangan    = v.findViewById(R.id.tv_tanggal);
            tvJumlah        = v.findViewById(R.id.tv_jumlah);
            tvTipe          = v.findViewById(R.id.tv_tipe);
            cardIcon        = v.findViewById(R.id.card_icon);
            ivIcon          = v.findViewById(R.id.iv_icon);
            btnMenu         = v.findViewById(R.id.btn_menu);
        }
    }
}
