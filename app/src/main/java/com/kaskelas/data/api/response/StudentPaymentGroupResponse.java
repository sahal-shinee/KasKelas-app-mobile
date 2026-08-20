package com.kaskelas.data.api.response;

import com.kaskelas.data.model.StudentPaymentGroup;
import java.util.List;

public class StudentPaymentGroupResponse {
    public boolean                   success;
    public String                    message;
    public List<StudentPaymentGroup> data;
}
