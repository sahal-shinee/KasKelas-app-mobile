package com.kaskelas.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtils {
    private static final Locale LOCALE_ID = new Locale("id", "ID");

    public static String formatRupiah(double amount) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(LOCALE_ID);
        return nf.format(amount);
    }

    public static String formatRupiahShort(double amount) {
        if (amount >= 1_000_000) return String.format(LOCALE_ID, "Rp %.1fJt", amount / 1_000_000);
        if (amount >= 1_000)    return String.format(LOCALE_ID, "Rp %.0fRb", amount / 1_000);
        return String.format(LOCALE_ID, "Rp %.0f", amount);
    }
}
