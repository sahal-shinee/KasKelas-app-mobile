package com.kaskelas;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import com.kaskelas.service.NotificationPollingWorker;
import com.kaskelas.utils.SessionManager;

/**
 * Application class — dijalankan sekali saat app pertama kali dibuka.
 * Di sini kita daftarkan Notification Channel yang wajib ada di Android 8+
 * agar push notification dari FCM bisa ditampilkan.
 */
public class KasKelasApp extends Application {

    public static final String NOTIF_CHANNEL_ID   = "kaskelas_notif";
    public static final String NOTIF_CHANNEL_NAME = "KasKelas Notifikasi";

    @Override
    public void onCreate() {
        super.onCreate();
        buatNotificationChannel();
        // Lanjutkan polling jika sesi masih aktif (misal: app restart setelah di-kill)
        if (new SessionManager(this).isLoggedIn()) {
            NotificationPollingWorker.jadwalkan(this);
        }
    }

    private void buatNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                NOTIF_CHANNEL_ID,
                NOTIF_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH   // muncul sebagai heads-up (pop-up di atas layar)
        );
        channel.setDescription("Notifikasi tagihan dan pengumuman dari bendahara");
        channel.enableVibration(true);
        channel.setShowBadge(true);   // tampilkan badge angka di icon app

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }
}
