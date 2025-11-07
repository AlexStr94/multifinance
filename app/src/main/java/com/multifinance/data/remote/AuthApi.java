package com.multifinance.data.remote;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {

    @POST("/api/auth/sign-up")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @POST("/api/auth/sign-in")
    Call<AuthResponse> login(@Body LoginRequest request);

}
