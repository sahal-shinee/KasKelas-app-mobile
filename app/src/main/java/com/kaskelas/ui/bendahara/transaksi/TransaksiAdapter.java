package com.kaskelas.ui.bendahara.transaksi;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.kaskelas.R;
import com.kaskelas.data.model.Transaction;
import com.kaskelas.utils.CurrencyUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiConsumer;

public class TransaksiAdapter extends RecyclerView.Adapter<TransaksiAdapter.VH> {

    private List<Transaction> fullList;
    private List<Transaction> displayList;
    private final BiConsumer<Transaction, String> onAction;

    private final SimpleDateFormat sdfInput  = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat sdfHeader = new SimpleDateFormat("EEEE • dd MMM", Locale.getDefault());

    public TransaksiAdapter(List<Transaction> list, BiConsumer<Transaction, String> onAction) {
        this.fullList    = new ArrayList<>(list);
        this.displayList = new ArrayList<>(list);
        this.onAction    = onAction;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaksi, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Transaction t = displayList.get(pos);

        h.tvNama.setText(t.namaItem);
        if (h.tvTanggal != null) h.tvTanggal.setText(t.tanggal);

        boolean isPemasukan = "PEMASUKAN".equals(t.tipe);

        // Header tanggal
        boolean showHeader = pos == 0 || !displayList.get(pos - 1).tanggal.equals(t.tanggal);

        if (showHeader && h.tvTanggalHeader != null) {
            h.tvTanggalHeader.setVisibility(View.VISIBLE);
            h.tvTanggalHeader.setText(formatTanggalHeader(t.tanggal));
        } else if (h.tvTanggalHeader != null) {
            h.tvTanggalHeader.setVisibility(View.GONE);
        }

        h.tvJumlah.setText((isPemasukan ? "+" : "-") + CurrencyUtils.formatRupiahShort(t.jumlah));

        int color = h.itemView.getContext().getColor(isPemasukan ? R.color.color_success : R.color.color_error);
        h.tvJumlah.setTextColor(color);
        h.tvTipe.setText(isPemasukan ? "↑ Pemasukan" : "↓ Pengeluaran");
        h.tvTipe.setTextColor(color);

        if (h.cardIcon != null) {
            h.cardIcon.setCardBackgroundColor(isPemasukan ? 
                    h.itemView.getContext().getColor(R.color.color_success_light) : 
                    h.itemView.getContext().getColor(R.color.color_error_light));
        }
        
        if (h.ivIcon != null) {
            h.ivIcon.setImageResource(isPemasukan ? R.drawable.ic_add : R.drawable.ic_delete);
            h.ivIcon.setColorFilter(color);
        }

        // PERBAIKAN: Sembunyikan menu jika onAction null (mencegah crash NPE)
        if (onAction == null) {
            h.btnMenu.setVisibility(View.GONE);
        } else {
            h.btnMenu.setVisibility(View.VISIBLE);
            h.btnMenu.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.getMenu().add("Detail Transaksi");
                popup.getMenu().add("Hapus Riwayat");
                popup.getMenu().add("Batalkan Transaksi");

                popup.setOnMenuItemClickListener(item -> {
                    String title = item.getTitle().toString();
                    if (onAction != null) {
                        if (title.equals("Detail Transaksi")) onAction.accept(t, "DETAIL");
                        else if (title.equals("Hapus Riwayat")) onAction.accept(t, "DELETE_HISTORY");
                        else if (title.equals("Batalkan Transaksi")) onAction.accept(t, "CANCEL_TRANSACTION");
                    }
                    return true;
                });
                popup.show();
            });
        }
    }

    @Override public int getItemCount() {
        return displayList.size();
    }

    public void filter(String query) {
        displayList = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            displayList.addAll(fullList);
        } else {
            String q = query.toLowerCase().trim();
            for (Transaction t : fullList) {
                if (t.namaItem.toLowerCase().contains(q) || (t.keterangan != null && t.keterangan.toLowerCase().contains(q))) {
                    displayList.add(t);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void filterByTipe(String tipe) {
        displayList = new ArrayList<>();
        for (Transaction t : fullList) {
            if (tipe == null || tipe.equals(t.tipe)) displayList.add(t);
        }
        notifyDataSetChanged();
    }

    private String formatTanggalHeader(String tanggal) {
        try {
            Date d = sdfInput.parse(tanggal);
            Calendar cal = Calendar.getInstance();
            Calendar today = Calendar.getInstance();
            cal.setTime(d);
            if (isSameDay(cal, today)) return "Hari ini";
            today.add(Calendar.DAY_OF_YEAR, -1);
            if (isSameDay(cal, today)) return "Kemarin";
            return sdfHeader.format(d);
        } catch (Exception e) {
            return tanggal;
        }
    }

    private boolean isSameDay(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvNama, tvTanggal, tvJumlah, tvTipe, tvTanggalHeader;
        MaterialCardView cardIcon;
        ImageView ivIcon;
        ImageButton btnMenu;

        VH(@NonNull View v) {
            super(v);
            tvNama          = v.findViewById(R.id.tv_nama);
            tvTanggal       = v.findViewById(R.id.tv_tanggal);
            tvJumlah        = v.findViewById(R.id.tv_jumlah);
            tvTipe          = v.findViewById(R.id.tv_tipe);
            tvTanggalHeader = v.findViewById(R.id.tv_tanggal_header);
            cardIcon        = v.findViewById(R.id.card_icon);
            ivIcon          = v.findViewById(R.id.iv_icon);
            btnMenu         = v.findViewById(R.id.btn_menu);
        }
    }
}
