package com.kaskelas.data.model;

import com.google.gson.annotations.SerializedName;

public class BillCategory {
    public int    id;
    public String nama;
    public String tipe;
    public String periode;
    public double nominal;
    
    @SerializedName("tanggal_mulai")       public String tanggalMulai;
    @SerializedName("tanggal_jatuh_tempo") public String tanggalJatuhTempo;
    @SerializedName("is_active")           public int    isActive;
    @SerializedName("created_at")          public String createdAt;

    // Fields untuk akses cepat ke tagihan terakhir (digunakan di TagihanFragment)
    @SerializedName("latest_bill_id")      public int    latestBillId;
    @SerializedName("latest_periode")      public String latestPeriode;
}
