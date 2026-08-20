// ============================================================
// File: data/api/response/BaseResponse.java
// ============================================================
package com.kaskelas.data.api.response;

public class BaseResponse {
    public boolean success;
    public String  message;

    // Field tambahan untuk error validasi urutan pembayaran
    public boolean blocked;
    @com.google.gson.annotations.SerializedName("periode_belum")
    public String  periodeBelum;
    @com.google.gson.annotations.SerializedName("jumlah_belum")
    public int     jumlahBelum;
}
