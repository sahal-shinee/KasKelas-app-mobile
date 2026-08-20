package com.kaskelas.data.api.response;

public class LoginResponse {
    public boolean success;
    public String  message;
    public Data    data;

    public static class Data {
        public String token;
        public User   user;
    }

    public static class User {
        public int    id;
        public String nis;
        public String nama;
        public String role;
        public int    nomor_absen;
    }
}
