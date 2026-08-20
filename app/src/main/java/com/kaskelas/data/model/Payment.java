package com.kaskelas.data.model;

import com.google.gson.annotations.SerializedName;

public class Payment {
    public int    id;
    @SerializedName("user_id")             public int    userId;
    @SerializedName("bill_id")             public int    billId;
    public String status;
    @SerializedName("tanggal_bayar")       public String tanggalBayar;
    @SerializedName("periode_label")       public String periodeLabel;
    
    @SerializedName(value = "tanggal_jatuh_tempo", alternate = {"due_date"}) 
    public String tanggalJatuhTempo;
    
    public String kategori;
    
    @SerializedName(value = "nominal", alternate = {"amount", "nominal_tagihan"})
    public double nominal;

    // Menampung relasi tagihan, mendukung nama 'bill' atau 'tagihan' dari JSON
    @SerializedName(value = "bill", alternate = {"tagihan"})
    public Bill bill;
}
