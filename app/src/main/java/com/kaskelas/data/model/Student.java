package com.kaskelas.data.model;

import com.google.gson.annotations.SerializedName;

public class Student {
    public int    id;
    public String nis;
    public String nama;
    @SerializedName("nomor_absen")     public int nomorAbsen;
    @SerializedName("total_tunggakan") public int totalTunggakan;
    @SerializedName("total_tunggakan_nominal") public double totalTunggakanNominal;
    @SerializedName("jumlah_belum_bayar") public int jumlahBelumBayar;

    // Field tambahan untuk sinkronisasi akurat jika tersedia dari API
    @SerializedName("total_tagihan") public int totalTagihan;
    @SerializedName("tagihan_lunas") public int tagihanLunas;
}
