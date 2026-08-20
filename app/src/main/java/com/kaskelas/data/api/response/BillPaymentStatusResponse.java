package com.kaskelas.data.api.response;

import com.kaskelas.data.model.BillPaymentStatus;
import java.util.List;

public class BillPaymentStatusResponse {
    public boolean                 success;
    public String                  message;
    public List<BillPaymentStatus> data;
}
