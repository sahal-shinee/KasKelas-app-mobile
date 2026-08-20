package com.kaskelas.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.kaskelas.utils.SessionManager;

/**
 * Dijalankan saat HP selesai booting.
 * Menjadwalkan ulang alarm polling notifikasi jika user masih login.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            if (new SessionManager(context).isLoggedIn()) {
                NotificationPollingWorker.jadwalkan(context);
            }
        }
    }
}
