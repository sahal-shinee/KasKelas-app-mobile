package com.kaskelas.data.api.response;

import com.kaskelas.data.model.Notification;
import java.util.List;

public class NotificationListResponse {
    public boolean            success;
    public String             message;
    public List<Notification> data;
}
