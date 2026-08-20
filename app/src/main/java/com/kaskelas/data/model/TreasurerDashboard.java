package com.kaskelas.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TreasurerDashboard {
    public double saldo;
    @SerializedName("total_pemasukan")   public double totalPemasukan;
    @SerializedName("total_pengeluaran") public double totalPengeluaran;
    @SerializedName("transaksi_terbaru") public List<Transaction> transaksiTerbaru;
    @SerializedName("chart_bulanan")     public List<ChartData>   chartBulanan;

    public static class ChartData {
        public String bulan;
        public double pemasukan;
        public double pengeluaran;
    }
}
