package com.kaskelas.data.api.response;

import com.kaskelas.data.model.Transaction;
import java.util.List;

public class TransactionListResponse {
    public boolean           success;
    public String            message;
    public List<Transaction> data;
}
