package com.kaskelas.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/** Satu kategori tagihan beserta semua periodenya untuk satu siswa */
public class StudentPaymentGroup {
    @SerializedName("kategori_id")             public int    kategoriId;
    @SerializedName("kategori_nama")           public String kategoriNama;
    @SerializedName("total_periode")           public int    totalPeriode;
    public int lunas, menunggak, belum;
    @SerializedName("total_tunggakan_nominal") public double totalTunggakanNominal;
    public List<PeriodeItem> periode;

    public static class PeriodeItem {
        @SerializedName("payment_id")          public int    paymentId;
        @SerializedName("periode_label")       public String periodeLabel;
        @SerializedName("tanggal_jatuh_tempo") public String tanggalJatuhTempo;
        @SerializedName("tanggal_bayar")       public String tanggalBayar;
        public String status;
        public double nominal;
        public int    urutan; // urutan ke-berapa dalam kategori
    }
}
