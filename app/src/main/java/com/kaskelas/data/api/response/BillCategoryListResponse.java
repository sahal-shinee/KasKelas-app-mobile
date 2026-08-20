package com.kaskelas.data.api.response;

import com.kaskelas.data.model.BillCategory;
import java.util.List;

public class BillCategoryListResponse {
    public boolean           success;
    public String            message;
    public List<BillCategory> data;
}
