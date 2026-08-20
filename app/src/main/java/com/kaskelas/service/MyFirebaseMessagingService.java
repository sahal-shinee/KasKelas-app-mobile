package com.kaskelas.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.kaskelas.KasKelasApp;
import com.kaskelas.R;
import com.kaskelas.data.api.ApiClient;
import com.kaskelas.data.api.ApiService;
import com.kaskelas.data.api.response.BaseResponse;
import com.kaskelas.ui.auth.LoginActivity;
import com.kaskelas.utils.SessionManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Helper push notification KasKelas.
 *
 * STATUS: Berjalan sebagai plain class karena google-services.json belum ditambahkan.
 *
 * CARA AKTIFKAN PENUH (setelah google-services.json ada):
 *  1. Di build.gradle.kts (root)  → uncomment baris google-services
 *  2. Di app/build.gradle.kts     → uncomment plugin + Firebase dependencies
 *  3. Ubah class ini agar extends FirebaseMessagingService (lihat komentar TODO di bawah)
 *  4. Uncomment baris FirebaseMessaging di kirimTokenKeServer()
 */
// TODO (setelah Firebase aktif): ganti baris di bawah dengan:
// public class MyFirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
public class MyFirebaseMessagingService {

    private static final String TAG = "FCMService";
    private static final AtomicInteger notifIdCounter = new AtomicInteger(1000);

    // ------------------------------------------------------------------ //
    //  Tampilkan notifikasi sistem secara manual (heads-up / pop-up)
    //  Dipanggil dari onMessageReceived saat app FOREGROUND
    // ------------------------------------------------------------------ //
    public static void tampilkanNotifikasi(Context context, String judul, String isi) {
        int notifId = notifIdCounter.getAndIncrement();

        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // Gunakan notifId sebagai requestCode agar setiap notifikasi punya PendingIntent unik
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, notifId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, KasKelasApp.NOTIF_CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(judul)
                        .setContentText(isi)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(isi))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(notifId, builder.build());
        }
    }

    // ------------------------------------------------------------------ //
    //  Kirim FCM token device ke backend agar server bisa push notifikasi
    //  Dipanggil setelah login berhasil & saat token FCM diperbarui
    // ------------------------------------------------------------------ //
    public static void kirimTokenKeServer(Context context) {
        // TODO (setelah Firebase aktif): uncomment kode di bawah ini
        //
        // com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
        //     .addOnSuccessListener(token -> {
        //         if (token != null && !token.isEmpty()) {
        //             kirimTokenKeBackend(context, token);
        //         }
        //     })
        //     .addOnFailureListener(e ->
        //         Log.w(TAG, "Gagal ambil FCM token: " + e.getMessage()));

        Log.d(TAG, "kirimTokenKeServer: menunggu konfigurasi google-services.json");
    }

    // ------------------------------------------------------------------ //
    //  Kirim token ke endpoint PATCH /auth/fcm-token
    // ------------------------------------------------------------------ //
    static void kirimTokenKeBackend(Context context, String token) {
        SessionManager session = new SessionManager(context);
        if (!session.isLoggedIn()) return;

        Map<String, Object> body = new HashMap<>();
        body.put("fcm_token", token);

        ApiService api = ApiClient.getService(context);
        api.saveFcmToken(body).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> c, Response<BaseResponse> r) {
                if (r.isSuccessful()) Log.d(TAG, "FCM token tersimpan di server");
                else Log.w(TAG, "Gagal simpan FCM token: HTTP " + r.code());
            }
            @Override
            public void onFailure(Call<BaseResponse> c, Throwable t) {
                Log.w(TAG, "Error simpan FCM token: " + t.getMessage());
            }
        });
    }
}
