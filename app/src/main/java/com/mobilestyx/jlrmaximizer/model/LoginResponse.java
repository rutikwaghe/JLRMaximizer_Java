package com.mobilestyx.jlrmaximizer.model;

public class LoginResponse {


    private int userid;
    private String email;
    private String username;
    private String msg;

    public int getUser_id() {
        return userid;
    }

    public void setUser_id(int userid) {
        this.userid = userid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMsg() { return msg; }
}