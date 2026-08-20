package com.kaskelas.utils;

public class Constants {
    public static final String BASE_URL = "http://192.168.0.138/kaskelas/api/v1/";
    public static final int    CONNECT_TIMEOUT = 15;  // detik
    public static final int    READ_TIMEOUT    = 30;  // detik
    public static final int    WRITE_TIMEOUT   = 30;  // detik

    // SharedPreferences keys
    public static final String PREF_NAME    = "KasKelasPref";
    public static final String KEY_TOKEN    = "jwt_token";
    public static final String KEY_ROLE     = "user_role";
    public static final String KEY_NIS      = "user_nis";
    public static final String KEY_NAMA     = "user_nama";
    public static final String KEY_USER_ID  = "user_id";

    // Roles
    public static final String ROLE_BENDAHARA = "BENDAHARA";
    public static final String ROLE_SISWA     = "SISWA";

    // Notification polling
    public static final String KEY_LAST_NOTIF_ID = "last_notif_id";
    public static final String WORK_TAG_POLLING  = "notif_polling";
}
