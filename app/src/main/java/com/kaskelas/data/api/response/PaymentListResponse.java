package com.kaskelas.data.api.response;

import com.kaskelas.data.model.Payment;
import java.util.List;

public class PaymentListResponse {
    public boolean       success;
    public String        message;
    public List<Payment> data;
}
