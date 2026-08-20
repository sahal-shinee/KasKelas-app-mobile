package com.kaskelas.ui.bendahara.tagihan;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.kaskelas.R;
import com.kaskelas.data.model.PaymentSummary;
import com.kaskelas.utils.CurrencyUtils;
import java.util.List;
import java.util.function.Consumer;

public class PaymentSummaryAdapter extends RecyclerView.Adapter<PaymentSummaryAdapter.VH> {

    private final List<PaymentSummary> list;
    /** Callback: klik tombol "Kelola" → buka KonfirmasiPerTagihanFragment */
    private final Consumer<PaymentSummary> onKelola;

    public PaymentSummaryAdapter(List<PaymentSummary> list,
                                 Consumer<PaymentSummary> onKelola) {
        this.list     = list;
        this.onKelola = onKelola;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_payment_summary, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        PaymentSummary s = list.get(pos);
        h.tvKategori.setText(s.kategori);
        h.tvPeriode.setText(s.periodeLabel + "  ·  " + CurrencyUtils.formatRupiah(s.nominal));
        h.tvLunas.setText("✓ Lunas: " + s.lunas);
        h.tvMenunggak.setText("⚠ Menunggak: " + s.menunggak);
        h.tvBelum.setText("○ Belum: " + s.belum);
        h.tvTotal.setText("Total: " + s.totalSiswa + " siswa");

        // Highlight jika masih ada yang menunggak
        int colorNormal = h.itemView.getContext().getColor(R.color.text_secondary);
        int colorError  = h.itemView.getContext().getColor(R.color.color_error);
        h.tvMenunggak.setTextColor(s.menunggak > 0 ? colorError : colorNormal);

        h.btnCatat.setText("Kelola Pembayaran");
        h.btnCatat.setOnClickListener(v -> onKelola.accept(s));
        h.itemView.setOnClickListener(v -> onKelola.accept(s));
    }

    @Override public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvKategori, tvPeriode, tvLunas, tvMenunggak, tvBelum, tvTotal;
        MaterialButton btnCatat;

        VH(@NonNull View v) {
            super(v);
            tvKategori  = v.findViewById(R.id.tv_kategori);
            tvPeriode   = v.findViewById(R.id.tv_periode);
            tvLunas     = v.findViewById(R.id.tv_lunas);
            tvMenunggak = v.findViewById(R.id.tv_menunggak);
            tvBelum     = v.findViewById(R.id.tv_belum);
            tvTotal     = v.findViewById(R.id.tv_total);
            btnCatat    = v.findViewById(R.id.btn_catat);
        }
    }
}
