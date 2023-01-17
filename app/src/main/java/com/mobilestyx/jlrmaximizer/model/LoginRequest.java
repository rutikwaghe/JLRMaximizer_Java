package com.mobilestyx.jlrmaximizer.model;

public class LoginRequest {

    private String userid;
    private String password;

    public String getUsername() {
        return userid;
    }

    public void setUsername(String username) {
        this.userid = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}