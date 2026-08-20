package com.kaskelas.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

public class NetworkUtils {
    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
        return nc != null && (
            nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        );
    }
}
