package com.kaskelas.data.model;

import com.google.gson.annotations.SerializedName;

/** Satu baris di daftar siswa untuk tagihan tertentu */
public class BillPaymentStatus {
    @SerializedName("payment_id")   public int    paymentId;
    public String status;
    @SerializedName("tanggal_bayar") public String tanggalBayar;
    @SerializedName("siswa_id")     public int    siswaId;
    @SerializedName("siswa_nama")   public String siswaNama;
    public String nis;
    @SerializedName("nomor_absen")  public int    nomorAbsen;
}
