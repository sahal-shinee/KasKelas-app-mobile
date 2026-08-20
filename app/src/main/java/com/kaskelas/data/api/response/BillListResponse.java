package com.kaskelas.data.api.response;

import com.kaskelas.data.model.Bill;
import java.util.List;

public class BillListResponse {
    public boolean    success;
    public String     message;
    public List<Bill> data;
}
