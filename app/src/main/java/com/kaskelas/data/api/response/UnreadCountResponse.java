package com.kaskelas.data.api.response;

public class UnreadCountResponse {
    public boolean   success;
    public CountData data;

    public static class CountData {
        public int count;
    }
}
