package com.kaskelas.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.kaskelas.utils.SessionManager;

/**
 * Menerima alarm dari AlarmManager setiap 1 menit.
 * Setelah polling, langsung jadwalkan alarm berikutnya (self-reschedule).
 * Pola ini memastikan setExactAndAllowWhileIdle terus berjalan
 * dan menembus Doze mode meski layar mati.
 */
public class NotificationAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (new SessionManager(context).isLoggedIn()) {
            // Cek notifikasi baru
            NotificationPollingWorker.periksaSekarang(context);
            // Jadwalkan alarm 1 menit berikutnya
            NotificationPollingWorker.jadwalkanBerikutnya(context);
        }
    }
}
