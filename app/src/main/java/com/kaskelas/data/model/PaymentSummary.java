package com.kaskelas.data.model;

import com.google.gson.annotations.SerializedName;

public class PaymentSummary {
    @SerializedName("bill_id")       public int    billId;
    @SerializedName("periode_label") public String periodeLabel;
    @SerializedName("tanggal_jatuh_tempo") public String tanggalJatuhTempo;
    @SerializedName("kategori_id")   public int    kategoriId;
    public String kategori;
    public double nominal;
    public int    lunas;
    public int    menunggak;
    public int    belum;
    @SerializedName("total_siswa")   public int totalSiswa;
}
