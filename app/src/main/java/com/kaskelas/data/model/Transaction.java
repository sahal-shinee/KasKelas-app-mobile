package com.kaskelas.data.model;

import com.google.gson.annotations.SerializedName;

public class Transaction {
    public int    id;
    @SerializedName("nama_item")         public String namaItem;
    public double jumlah;
    public String tipe;
    public String tanggal;
    public String keterangan;
    @SerializedName("dicatat_oleh_nama") public String dicatatOlehNama;
    @SerializedName("kategori_nama")     public String kategoriNama;
}
