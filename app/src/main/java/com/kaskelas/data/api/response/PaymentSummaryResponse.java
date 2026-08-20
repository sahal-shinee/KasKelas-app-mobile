package com.kaskelas.data.api.response;

import com.kaskelas.data.model.PaymentSummary;
import java.util.List;

public class PaymentSummaryResponse {
    public boolean             success;
    public String              message;
    public List<PaymentSummary> data;
}
