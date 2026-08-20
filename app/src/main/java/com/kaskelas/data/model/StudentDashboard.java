package com.kaskelas.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class StudentDashboard {
    @SerializedName("total_tunggakan")   public double        totalTunggakan;
    @SerializedName("jumlah_tunggakan")  public int           jumlahTunggakan;
    @SerializedName("tagihan_aktif")     public List<Payment> tagihanAktif;
    // FIX: field kas kelas sekarang ikut dikirim dari server
    @SerializedName("saldo_kas")         public double        saldoKas;
    @SerializedName("total_pemasukan")   public double        totalPemasukan;
    @SerializedName("total_pengeluaran") public double        totalPengeluaran;
}
