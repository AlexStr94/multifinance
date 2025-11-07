package com.multifinance.data.remote;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthResponse {
    private String token;
    private String type; // "Bearer"
    private int id;
    private String username;
    private String email;
    private String phone;
    private List<String> roles;
}