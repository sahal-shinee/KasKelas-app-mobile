package com.kaskelas.data.model;

import com.google.gson.annotations.SerializedName;

public class Notification {
    public int    id;
    public String judul;
    public String isi;
    public String tipe;
    @SerializedName("pengirim_nama") public String pengirimNama;
    @SerializedName("is_read")       public int    isRead;
    @SerializedName("created_at")    public String createdAt;
}
