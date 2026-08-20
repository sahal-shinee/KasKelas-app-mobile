package com.kaskelas.ui.bendahara.notifikasi;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.kaskelas.R;
import com.kaskelas.data.model.Notification;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class NotifikasiAdapter extends RecyclerView.Adapter<NotifikasiAdapter.VH> {

    public interface OnDeleteListener {
        void onDelete(Notification notification, int position);
    }

    private final List<Notification> list;
    private OnDeleteListener deleteListener;
    private boolean showDeleteButtons = false;

    public NotifikasiAdapter(List<Notification> list) {
        this.list = new ArrayList<>(list);
    }

    public void setOnDeleteListener(OnDeleteListener listener) {
        this.deleteListener = listener;
    }

    /** Hapus item secara lokal dari adapter (setelah API sukses) */
    public void removeAt(int position) {
        if (position >= 0 && position < list.size()) {
            list.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, list.size());
        }
    }

    /** Kosongkan semua item (hapus semua) */
    public void clearAll() {
        int size = list.size();
        list.clear();
        notifyItemRangeRemoved(0, size);
    }

    public List<Notification> getList() {
        return list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notifikasi, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Notification n = list.get(position);
        Context ctx = h.itemView.getContext();

        h.tvJudul.setText(n.judul);
        h.tvIsi.setText(n.isi);
        h.tvPengirim.setText("Dari: " + n.pengirimNama);

        // Waktu relatif
        h.tvWaktuRelatif.setText(getRelativeTime(n.createdAt));

        // Tipe label + warna + ikon
        h.tvTipeLabel.setText(n.tipe);
        switch (n.tipe) {
            case "TAGIHAN":
                h.tvTipeLabel.setTextColor(ContextCompat.getColor(ctx, R.color.color_error));
                h.flIconContainer.setBackground(
                        ContextCompat.getDrawable(ctx, R.drawable.bg_notif_icon_tagihan));
                h.ivTipeIcon.setImageResource(R.drawable.ic_tagihan);
                break;
            case "AGENDA":
                h.tvTipeLabel.setTextColor(ContextCompat.getColor(ctx, R.color.color_warning));
                h.flIconContainer.setBackground(
                        ContextCompat.getDrawable(ctx, R.drawable.bg_notif_icon_agenda));
                // Diganti dari ic_agenda ke ic_calendar karena file ic_agenda tidak ditemukan
                h.ivTipeIcon.setImageResource(R.drawable.ic_calendar);
                break;
            default: // PENGUMUMAN
                h.tvTipeLabel.setTextColor(ContextCompat.getColor(ctx, R.color.color_primary));
                h.flIconContainer.setBackground(
                        ContextCompat.getDrawable(ctx, R.drawable.bg_notif_icon_pengumuman));
                // Diganti dari ic_announcement ke ic_bell karena file ic_announcement tidak ditemukan
                h.ivTipeIcon.setImageResource(R.drawable.ic_bell);
                break;
        }

        // Unread: alpha penuh, card sedikit lebih terang / ada strip
        h.itemView.setAlpha(n.isRead == 1 ? 0.55f : 1.0f);

        // Tombol hapus per item
        h.btnHapusItem.setVisibility(View.VISIBLE);
        h.btnHapusItem.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos != RecyclerView.NO_ID && deleteListener != null) {
                deleteListener.onDelete(n, pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ─── Helper: waktu relatif ──────────────────────────────────────────────

    /**
     * Mengubah string createdAt (format "yyyy-MM-dd HH:mm:ss" atau "yyyy-MM-dd HH:mm:ss.S")
     * menjadi label relatif seperti "10:45 AM", "Yesterday", "2 days ago".
     */
    private String getRelativeTime(String createdAt) {
        if (createdAt == null || createdAt.isEmpty()) return "";

        String[] formats = {
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss.S",
                "yyyy-MM-dd HH:mm",
                "yyyy-MM-dd"
        };

        Date date = null;
        for (String fmt : formats) {
            try {
                date = new SimpleDateFormat(fmt, Locale.getDefault()).parse(createdAt);
                break;
            } catch (ParseException ignored) { }
        }

        if (date == null) return createdAt;

        long now      = System.currentTimeMillis();
        long diff     = now - date.getTime();
        long diffMins = TimeUnit.MILLISECONDS.toMinutes(diff);
        long diffHours= TimeUnit.MILLISECONDS.toHours(diff);
        long diffDays = TimeUnit.MILLISECONDS.toDays(diff);

        if (diffMins < 1)   return "Baru saja";
        if (diffMins < 60)  return diffMins + " mnt lalu";
        if (diffDays == 0)  return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date);
        if (diffDays == 1)  return "Yesterday";
        if (diffDays <  7)  return diffDays + " days ago";

        return new SimpleDateFormat("dd MMM", Locale.getDefault()).format(date);
    }

    // ─── ViewHolder ─────────────────────────────────────────────────────────

    static class VH extends RecyclerView.ViewHolder {
        TextView tvJudul, tvIsi, tvWaktuRelatif, tvPengirim, tvTipeLabel;
        FrameLayout flIconContainer;
        ImageView ivTipeIcon;
        ImageButton btnHapusItem;

        VH(@NonNull View v) {
            super(v);
            tvJudul         = v.findViewById(R.id.tv_judul);
            tvIsi           = v.findViewById(R.id.tv_isi);
            tvWaktuRelatif  = v.findViewById(R.id.tv_waktu_relatif);
            tvPengirim      = v.findViewById(R.id.tv_pengirim);
            tvTipeLabel     = v.findViewById(R.id.tv_tipe_label);
            flIconContainer = v.findViewById(R.id.fl_icon_container);
            ivTipeIcon      = v.findViewById(R.id.iv_tipe_icon);
            btnHapusItem    = v.findViewById(R.id.btn_hapus_item);
        }
    }
}
