package com.multifinance.data.remote;

import com.google.gson.JsonObject;
import com.multifinance.data.model.Account;
import com.multifinance.data.model.Bank;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface MiltiBankApi {

    @POST("/api/auth/sign-up")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @POST("/api/auth/sign-in")
    Call<AuthResponse> login(@Body LoginRequest request);

    @GET("/api/banks/")
    Call<List<Bank>> getBanks(@Header("Authorization") String token);

    @GET("/api/accounts/")
    Call<List<Account>> getAccounts(@Header("Authorization") String token);

    @POST("/api/consents/create")
    Call<Void> createConsent(
            @Header("Authorization") String token,
            @Body JsonObject request
    );
}
