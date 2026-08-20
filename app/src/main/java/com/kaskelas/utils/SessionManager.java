package com.kaskelas.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs  = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveSession(String token, String role, int userId, String nis, String nama) {
        editor.putString(Constants.KEY_TOKEN,   token);
        editor.putString(Constants.KEY_ROLE,    role);
        editor.putInt   (Constants.KEY_USER_ID, userId);
        editor.putString(Constants.KEY_NIS,     nis);
        editor.putString(Constants.KEY_NAMA,    nama);
        editor.apply();
    }

    public String  getToken()  { return prefs.getString(Constants.KEY_TOKEN,  null); }
    public String  getRole()   { return prefs.getString(Constants.KEY_ROLE,   null); }
    public String  getNis()    { return prefs.getString(Constants.KEY_NIS,    null); }
    public String  getNama()   { return prefs.getString(Constants.KEY_NAMA,   null); }
    public int     getUserId() { return prefs.getInt   (Constants.KEY_USER_ID, -1);  }

    public boolean isLoggedIn() { return getToken() != null; }
    public boolean isBendahara(){ return Constants.ROLE_BENDAHARA.equals(getRole()); }

    public void clearSession() { editor.clear().apply(); }
}
