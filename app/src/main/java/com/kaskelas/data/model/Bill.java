package com.kaskelas.data.model;

import com.google.gson.annotations.SerializedName;

public class Bill {
    public int    id;
    @SerializedName("bill_category_id")    public int    billCategoryId;
    @SerializedName("periode_label")       public String periodeLabel;
    @SerializedName(value = "tanggal_jatuh_tempo", alternate = {"due_date"}) 
    public String tanggalJatuhTempo;
    
    public String kategori;
    
    @SerializedName(value = "nominal", alternate = {"amount", "nominal_tagihan"})
    public double nominal;
    
    public String tipe;
}
