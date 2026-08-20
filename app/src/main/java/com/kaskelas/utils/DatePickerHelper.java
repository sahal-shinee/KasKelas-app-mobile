package com.kaskelas.utils;

import android.app.DatePickerDialog;
import android.content.Context;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Calendar;
import java.util.Locale;
import com.kaskelas.R;

/**
 * Helper untuk menampilkan DatePickerDialog
 * dan mengisi TextInputEditText dengan format yyyy-MM-dd.
 */
public class DatePickerHelper {

    /**
     * Pasang date picker ke sebuah TextInputEditText.
     * Saat field di-klik, kalender muncul.
     */
    public static void attach(Context context, TextInputEditText field) {
        attach(context, field, null);
    }

    /**
     * Pasang date picker dengan callback setelah tanggal dipilih.
     */
    public static void attach(Context context, TextInputEditText field,
                              OnDateSelectedListener listener) {
        // Tidak bisa diketik manual — harus lewat picker
        field.setFocusable(false);
        field.setClickable(true);
        field.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_calendar, 0);

        field.setOnClickListener(v -> showPicker(context, field, listener));
    }

    private static void showPicker(Context context, TextInputEditText field,
                                   OnDateSelectedListener listener) {
        Calendar cal = Calendar.getInstance();

        // Jika field sudah ada isi, parse dulu supaya picker buka di tanggal yang sama
        String existing = field.getText() != null ? field.getText().toString() : "";
        if (existing.matches("\\d{4}-\\d{2}-\\d{2}")) {
            String[] parts = existing.split("-");
            cal.set(Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]) - 1,
                    Integer.parseInt(parts[2]));
        }

        new DatePickerDialog(context,
            (view, year, month, dayOfMonth) -> {
                String date = String.format(Locale.getDefault(),
                    "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                field.setText(date);
                if (listener != null) listener.onDateSelected(date);
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    public interface OnDateSelectedListener {
        void onDateSelected(String date);
    }
}
