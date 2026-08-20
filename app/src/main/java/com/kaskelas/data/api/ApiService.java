package com.kaskelas.data.api;

import com.kaskelas.data.api.response.*;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // ---- AUTH ----
    @POST("auth/login")
    Call<LoginResponse> login(@Body Map<String, String> body);

    @POST("auth/logout")
    Call<BaseResponse> logout();

    @PATCH("auth/fcm-token")
    Call<BaseResponse> saveFcmToken(@Body Map<String, Object> body);

    // ---- STUDENTS ----
    @GET("students")
    Call<StudentListResponse> getStudents(@Query("filter") String filter);

    @POST("students")
    Call<BaseResponse> addStudent(@Body Map<String, Object> body);

    @PUT("students/{id}")
    Call<BaseResponse> updateStudent(@Path("id") int id, @Body Map<String, Object> body);

    @DELETE("students/{id}")
    Call<BaseResponse> deleteStudent(@Path("id") int id);

    @GET("students/{id}/payments")
    Call<StudentPaymentGroupResponse> getStudentPayments(@Path("id") int id);

    // ---- BILL CATEGORIES ----
    @GET("bill-categories")
    Call<BillCategoryListResponse> getBillCategories();

    @POST("bill-categories")
    Call<BaseResponse> addBillCategory(@Body Map<String, Object> body);

    @PUT("bill-categories/{id}")
    Call<BaseResponse> updateBillCategory(@Path("id") int id, @Body Map<String, Object> body);

    @DELETE("bill-categories/{id}")
    Call<BaseResponse> deleteBillCategory(@Path("id") int id);

    @DELETE("bill-categories/{id}/force")
    Call<BaseResponse> forceDeleteBillCategory(@Path("id") int id);

    // ---- BILL ----
    @GET("bills")
    Call<BillListResponse> getBills(@Query("kategori_id") Integer kategoriId,
                                    @Query("periode")     String  periode);

    @POST("bills/generate")
    Call<BaseResponse> generateBill(@Body Map<String, Object> body);

    // ---- PAYMENTS ----
    @POST("payments")
    Call<BaseResponse> recordPayment(@Body Map<String, Object> body);

    @GET("payments/my")
    Call<PaymentListResponse> getMyPayments(@Query("kategori_id") Integer kategoriId);

    @GET("payments/summary")
    Call<PaymentSummaryResponse> getPaymentSummary();

    // NEW: daftar siswa + status untuk satu tagihan
    @GET("payments/bill/{billId}")
    Call<BillPaymentStatusResponse> getPaymentsByBill(@Path("billId") int billId);

    // NEW: ubah status satu payment
    @PATCH("payments/{id}/status")
    Call<BaseResponse> updatePaymentStatus(@Path("id") int paymentId,
                                           @Body Map<String, Object> body);

    // ---- TRANSACTIONS ----
    @GET("transactions")
    Call<TransactionListResponse> getTransactions();

    @POST("transactions")
    Call<BaseResponse> addTransaction(@Body Map<String, Object> body);

    @DELETE("transactions/{id}")
    Call<BaseResponse> deleteTransaction(@Path("id") int id, @Query("keep_balance") boolean keepBalance);

    // ---- DASHBOARD ----
    @GET("dashboard/treasurer")
    Call<TreasurerDashboardResponse> getTreasurerDashboard();

    @GET("dashboard/student")
    Call<StudentDashboardResponse> getStudentDashboard();

    // ---- NOTIFICATIONS ----
    @GET("notifications/my")
    Call<NotificationListResponse> getMyNotifications();

    @POST("notifications")
    Call<BaseResponse> sendNotification(@Body Map<String, Object> body);

    @PATCH("notifications/{id}/read")
    Call<BaseResponse> markNotificationRead(@Path("id") int id);

    @GET("notifications/unread-count")
    Call<UnreadCountResponse> getUnreadCount();

    @DELETE("notifications/{id}")
    Call<BaseResponse> deleteNotification(@Path("id") int id);

    @DELETE("notifications/all")
    Call<BaseResponse> deleteAllNotifications();
}
