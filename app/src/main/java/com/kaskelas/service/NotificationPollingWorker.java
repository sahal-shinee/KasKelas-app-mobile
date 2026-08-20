package com.kaskelas.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.kaskelas.data.api.ApiClient;
import com.kaskelas.data.api.response.NotificationListResponse;
import com.kaskelas.data.model.Notification;
import com.kaskelas.utils.Constants;
import com.kaskelas.utils.SessionManager;

import java.util.List;

import retrofit2.Response;

/**
 * Worker yang memeriksa notifikasi baru dari server dan menampilkannya
 * sebagai notifikasi sistem (banner / panel notifikasi Android).
 *
 * Dijadwalkan via setExactAndAllowWhileIdle setiap 1 menit —
 * menembus Doze mode sehingga tidak delay meski layar mati.
 */
public class NotificationPollingWorker extends Worker {

    private static final String TAG         = "NotifPolling";
    private static final long   INTERVAL_MS = 60 * 1000L; // 1 menit

    public NotificationPollingWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    // ------------------------------------------------------------------ //
    //  Logika utama: ambil notifikasi dari server, tampilkan yang baru
    // ------------------------------------------------------------------ //
    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        if (!new SessionManager(ctx).isLoggedIn()) return Result.success();

        try {
            Response<NotificationListResponse> response =
                    ApiClient.getService(ctx).getMyNotifications().execute();

            if (!response.isSuccessful() || response.body() == null) return Result.retry();

            List<Notification> list = response.body().data;
            if (list == null || list.isEmpty()) return Result.success();

            SharedPreferences prefs = ctx.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
            int lastId = prefs.getInt(Constants.KEY_LAST_NOTIF_ID, 0);
            int maxId  = lastId;

            for (Notification n : list) {
                if (n.id > maxId) maxId = n.id;
                if (n.id > lastId && n.isRead == 0) {
                    MyFirebaseMessagingService.tampilkanNotifikasi(
                            ctx,
                            n.judul != null ? n.judul : "KasKelas",
                            n.isi   != null ? n.isi   : ""
                    );
                }
            }

            if (maxId > lastId) {
                prefs.edit().putInt(Constants.KEY_LAST_NOTIF_ID, maxId).apply();
            }

            return Result.success();
        } catch (Exception e) {
            Log.w(TAG, "Polling gagal: " + e.getMessage());
            return Result.retry();
        }
    }

    // ------------------------------------------------------------------ //
    //  Jadwalkan pertama kali + langsung cek sekarang
    // ------------------------------------------------------------------ //
    public static void jadwalkan(Context ctx) {
        periksaSekarang(ctx);
        jadwalkanBerikutnya(ctx);
    }

    // ------------------------------------------------------------------ //
    //  Jadwalkan alarm 1 menit berikutnya (dipanggil dari Receiver juga)
    //  setExactAndAllowWhileIdle = menembus Doze mode
    // ------------------------------------------------------------------ //
    public static void jadwalkanBerikutnya(Context ctx) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        PendingIntent pi = getAlarmIntent(ctx);

        // Android 12+: butuh izin canScheduleExactAlarms
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            // Fallback: inexact (mungkin lebih lama tapi tetap jalan)
            am.setInexactRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    INTERVAL_MS,
                    INTERVAL_MS,
                    pi
            );
            return;
        }

        // Exact alarm — menembus Doze mode, tepat 1 menit
        am.setExactAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + INTERVAL_MS,
                pi
        );
    }

    // ------------------------------------------------------------------ //
    //  Cek langsung sekarang via WorkManager (1x, tanpa delay)
    // ------------------------------------------------------------------ //
    public static void periksaSekarang(Context ctx) {
        WorkManager.getInstance(ctx).enqueueUniqueWork(
                Constants.WORK_TAG_POLLING + "_once",
                ExistingWorkPolicy.REPLACE,
                new OneTimeWorkRequest.Builder(NotificationPollingWorker.class).build()
        );
    }

    // ------------------------------------------------------------------ //
    //  Batalkan semua — panggil saat logout
    // ------------------------------------------------------------------ //
    public static void batalkan(Context ctx) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am != null) am.cancel(getAlarmIntent(ctx));
        WorkManager.getInstance(ctx).cancelUniqueWork(Constants.WORK_TAG_POLLING + "_once");
    }

    // ------------------------------------------------------------------ //
    //  Helper PendingIntent
    // ------------------------------------------------------------------ //
    public static PendingIntent getAlarmIntent(Context ctx) {
        Intent intent = new Intent(ctx, NotificationAlarmReceiver.class);
        return PendingIntent.getBroadcast(
                ctx, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}
