package com.kaskelas.ui.bendahara.siswa;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.kaskelas.R;
import com.kaskelas.data.model.Bill;
import com.kaskelas.data.model.Payment;
import com.kaskelas.utils.CurrencyUtils;

import java.util.List;

public class PaymentHistoryAdapter extends RecyclerView.Adapter<PaymentHistoryAdapter.VH> {
    private final List<Payment> list;

    public PaymentHistoryAdapter(List<Payment> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment_history, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Payment p = list.get(pos);
        Bill b = p.bill;

        // Ambil kategori & periode (prioritas Payment, lalu Bill)
        String kategori = p.kategori != null ? p.kategori : (b != null ? b.kategori : "-");
        String periode  = p.periodeLabel != null ? p.periodeLabel : (b != null ? b.periodeLabel : "-");

        // Ambil nominal (cari di payment, jika 0 cari di bill)
        double nominalVal = p.nominal;
        if (nominalVal <= 0 && b != null) {
            nominalVal = b.nominal;
        }

        h.tvKategori.setText(kategori);
        h.tvPeriode.setText(periode);
        h.tvNominal.setText(CurrencyUtils.formatRupiah(nominalVal));

        if (p.status == null) return;

        // Warna utama
        int colorGreen  = h.itemView.getContext().getColor(R.color.color_success);
        int colorRed    = h.itemView.getContext().getColor(R.color.color_error);
        int colorOrange = h.itemView.getContext().getColor(R.color.color_warning);

        // Background icon
        int colorGreenBg  = h.itemView.getContext().getColor(R.color.color_success_light);
        int colorRedBg    = h.itemView.getContext().getColor(R.color.color_error_light);
        int colorOrangeBg = h.itemView.getContext().getColor(R.color.color_warning_light);

        switch (p.status.toUpperCase()) {
            case "LUNAS":
                h.tvStatus.setText("Lunas");
                h.tvStatus.setTextColor(colorGreen);
                h.cardIcon.setCardBackgroundColor(colorGreenBg);
                h.ivIcon.setColorFilter(colorGreen);
                break;

            case "MENUNGGAK":
                h.tvStatus.setText("Menunggak");
                h.tvStatus.setTextColor(colorRed);
                h.cardIcon.setCardBackgroundColor(colorRedBg);
                h.ivIcon.setColorFilter(colorRed);
                break;

            default:
                h.tvStatus.setText("Belum Jatuh Tempo");
                h.tvStatus.setTextColor(colorOrange);
                h.cardIcon.setCardBackgroundColor(colorOrangeBg);
                h.ivIcon.setColorFilter(colorOrange);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvKategori, tvPeriode, tvNominal, tvStatus;
        MaterialCardView cardIcon;
        ImageView ivIcon;

        VH(@NonNull View v) {
            super(v);
            tvKategori = v.findViewById(R.id.tv_kategori);
            tvPeriode  = v.findViewById(R.id.tv_periode);
            tvNominal  = v.findViewById(R.id.tv_nominal);
            tvStatus   = v.findViewById(R.id.tv_status);
            cardIcon   = v.findViewById(R.id.card_icon);
            ivIcon     = v.findViewById(R.id.iv_icon);
        }
    }
}