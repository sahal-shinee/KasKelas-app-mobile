package com.kaskelas.ui.bendahara.tagihan;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.kaskelas.R;
import com.kaskelas.data.model.BillCategory;
import com.kaskelas.utils.CurrencyUtils;
import java.util.List;
import java.util.function.BiConsumer;

public class BillCategoryAdapter extends RecyclerView.Adapter<BillCategoryAdapter.VH> {
    private final List<BillCategory> list;
    private final BiConsumer<BillCategory, String> onAction;

    public BillCategoryAdapter(List<BillCategory> list, BiConsumer<BillCategory, String> onAction) {
        this.list     = list;
        this.onAction = onAction;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bill_category, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        BillCategory c = list.get(pos);
        h.tvNama.setText(c.nama);
        h.tvNominal.setText(CurrencyUtils.formatRupiah(c.nominal));
        
        // Tampilkan info periode dan periode terakhir jika ada
        String periodeInfo = c.tipe + " · " + c.periode;
        if (c.latestPeriode != null && !c.latestPeriode.isEmpty()) {
            periodeInfo += " (Terakhir: " + c.latestPeriode + ")";
        }
        h.tvPeriode.setText(periodeInfo);
        
        h.tvJatuhTempo.setText("Jatuh Tempo: " + c.tanggalJatuhTempo);

        h.chipTipe.setText(c.tipe);
        if ("RUTIN".equals(c.tipe)) {
            h.chipTipe.setChipBackgroundColorResource(R.color.color_primary_light);
            h.chipTipe.setTextColor(h.itemView.getContext().getColor(R.color.color_primary));
        } else {
            h.chipTipe.setChipBackgroundColorResource(R.color.color_warning_light);
            h.chipTipe.setTextColor(h.itemView.getContext().getColor(R.color.color_warning));
        }

        // Click listener pada seluruh item card
        h.itemView.setOnClickListener(v -> {
            if (c.latestBillId > 0) {
                onAction.accept(c, "PEMBAYARAN");
            } else {
                onAction.accept(c, "VIEW_PAYMENTS");
            }
        });

        // Fix recycling bug by setting alpha for both states
        if (c.isActive == 0) {
            h.itemView.setAlpha(0.5f);
        } else {
            h.itemView.setAlpha(1.0f);
        }

        h.btnGenerate.setOnClickListener(v -> onAction.accept(c, "GENERATE"));
        h.btnLihat.setOnClickListener(v -> onAction.accept(c, "VIEW_PAYMENTS"));
        h.btnMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            if (c.isActive == 1) {
                popup.getMenu().add("Nonaktifkan").setOnMenuItemClickListener(item -> { 
                    onAction.accept(c, "DELETE"); 
                    return true; 
                });
            } else {
                popup.getMenu().add("Hapus Permanen").setOnMenuItemClickListener(item -> { 
                    onAction.accept(c, "FORCE_DELETE"); 
                    return true; 
                });
                popup.getMenu().add("Aktifkan Kembali").setOnMenuItemClickListener(item -> { 
                    onAction.accept(c, "ACTIVATE"); 
                    return true; 
                });
            }
            popup.show();
        });
    }

    @Override public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvNama, tvNominal, tvPeriode, tvJatuhTempo;
        Chip chipTipe;
        Button btnGenerate, btnLihat;
        ImageButton btnMenu;
        VH(@NonNull View v) {
            super(v);
            tvNama      = v.findViewById(R.id.tv_nama);
            tvNominal   = v.findViewById(R.id.tv_nominal);
            tvPeriode   = v.findViewById(R.id.tv_periode);
            tvJatuhTempo= v.findViewById(R.id.tv_jatuh_tempo);
            chipTipe    = v.findViewById(R.id.chip_tipe);
            btnGenerate = v.findViewById(R.id.btn_generate);
            btnLihat    = v.findViewById(R.id.btn_lihat_pembayaran);
            btnMenu     = v.findViewById(R.id.btn_menu);
        }
    }
}
