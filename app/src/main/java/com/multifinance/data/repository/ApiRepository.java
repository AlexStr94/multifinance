package com.multifinance.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.JsonObject;
import com.multifinance.data.model.Account;
import com.multifinance.data.model.Bank;
import com.multifinance.data.model.Transaction;
import com.multifinance.data.model.User;
import com.multifinance.data.remote.ApiClient;
import com.multifinance.data.remote.MiltiBankApi;
import com.multifinance.data.remote.LoginRequest;
import com.multifinance.data.remote.RegisterRequest;
import com.multifinance.data.remote.AuthResponse;
import com.multifinance.data.remote.RegisterResponse;
import com.multifinance.data.remote.TransactionRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiRepository {
    public static final String FILTER_ALL = "all";

    private final MiltiBankApi authApi;

    public ApiRepository() {
        authApi = ApiClient.getClient().create(MiltiBankApi.class);
    }

    // ===================== Авторизация =====================
    public void loginAsync(String email, String password, AuthCallback callback) {
        LoginRequest request = new LoginRequest(email, password);
        Call<AuthResponse> call = authApi.login(request);

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();

                    User user = new User();
                    user.setId(String.valueOf(authResponse.getId()));
                    user.setUsername(authResponse.getUsername());
                    user.setEmail(authResponse.getEmail());
                    user.setPhone(authResponse.getPhone());
                    user.setRoles(authResponse.getRoles());
                    user.setToken(authResponse.getToken());

                    callback.onSuccess(user);
                } else {
                    callback.onError("Ошибка входа: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError("Ошибка сети: " + t.getMessage());
            }
        });
    }

    // ===================== Регистрация =====================
    public void register(String username, String email, String phone, List<String> roles,
                         String password, AuthCallback callback) {

        RegisterRequest request = new RegisterRequest(username, email, phone, roles, password);
        Call<RegisterResponse> call = authApi.register(request);

        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful()) {
                    User user = new User();
                    user.setUsername(username);
                    user.setEmail(email);
                    user.setPhone(phone);
                    user.setRoles(roles);
                    callback.onSuccess(user);
                } else {
                    callback.onError("Ошибка регистрации: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                callback.onError("Ошибка сети: " + t.getMessage());
            }
        });
    }

    // ===================== Банки =====================
    public void getBanksAsync(Context context, BanksCallback callback) {
        String token = getToken(context);
        if (token == null || token.isEmpty()) {
            callback.onError("Отсутствует токен авторизации.");
            return;
        }

        Call<List<Bank>> call = authApi.getBanks("Bearer " + token);
        call.enqueue(new Callback<List<Bank>>() {
            @Override
            public void onResponse(Call<List<Bank>> call, Response<List<Bank>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Ошибка загрузки банков: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Bank>> call, Throwable t) {
                callback.onError("Ошибка сети: " + t.getMessage());
            }
        });
    }

    // ===================== Счета =====================
    public void getAccountsAsync(Context context, AccountsCallback callback) {
        String token = getToken(context);
        if (token == null || token.isEmpty()) {
            callback.onError("Отсутствует токен авторизации.");
            return;
        }

        Call<List<Account>> call = authApi.getAccounts("Bearer " + token);
        call.enqueue(new Callback<List<Account>>() {
            @Override
            public void onResponse(Call<List<Account>> call, Response<List<Account>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Ошибка загрузки счетов: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Account>> call, Throwable t) {
                callback.onError("Ошибка сети: " + t.getMessage());
            }
        });
    }

    // ===================== Транзакции =====================
    public void getTransactionsAsync(Context context, TransactionRequest request, TransactionsCallback callback) {
        String token = getToken(context);
        if (token == null || token.isEmpty()) {
            callback.onError("Отсутствует токен авторизации.");
            return;
        }

        Call<List<Transaction>> call = authApi.getTransactions("Bearer " + token, request);
        call.enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Ошибка загрузки транзакций: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {
                callback.onError("Ошибка сети: " + t.getMessage());
            }
        });
    }

    // ===================== Согласие =====================
    public void createConsent(String token, String bankName, ConsentCallback callback) {
        JsonObject body = new JsonObject();
        body.addProperty("bankName", bankName);

        Call<Void> call = authApi.createConsent("Bearer " + token, body);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) callback.onSuccess();
                else callback.onError("Ошибка создания согласия: " + response.code());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Ошибка сети: " + t.getMessage());
            }
        });
    }

    // ===================== Вспомогательные =====================
    @Nullable
    private static String getToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        return prefs.getString("auth_token", null);
    }

    // ===================== Callback интерфейсы =====================
    public interface ConsentCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface AuthCallback {
        void onSuccess(User user);
        void onError(String message);
    }

    public interface BanksCallback {
        void onSuccess(List<Bank> banks);
        void onError(String message);
    }

    public interface AccountsCallback {
        void onSuccess(List<Account> accounts);
        void onError(String message);
    }

    public interface TransactionsCallback {
        void onSuccess(List<Transaction> transactions);
        void onError(String message);
    }
}
