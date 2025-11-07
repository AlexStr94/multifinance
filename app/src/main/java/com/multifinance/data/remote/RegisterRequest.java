package com.multifinance.data.remote;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    private String username;
    private String email;
    private String phone;
    private List<String> roles;
    private String password;

    public RegisterRequest(String username, String email, String phone, List<String> roles, String password) {
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.roles = roles;
        this.password = password;
    }

}
