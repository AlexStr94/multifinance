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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    /**
     * 🔐 Авторизация пользователя (асинхронно)
     */
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
                    String errorMsg = "Ошибка входа: " + response.code();
                    Log.e("ApiRepository", errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Log.e("ApiRepository", "Ошибка сети при входе", t);
                callback.onError("Ошибка сети: " + t.getMessage());
            }
        });
    }

    /**
     * 🧾 Регистрация пользователя (асинхронно)
     */
    public void register(String username, String email, String phone, List<String> roles,
                         String password, AuthCallback callback) {

        RegisterRequest request = new RegisterRequest(username, email, phone, roles, password);
        Call<RegisterResponse> call = authApi.register(request);

        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful()) {
                    // Сервер возвращает 200 OK без тела — считаем успехом
                    User user = new User();
                    user.setUsername(username);
                    user.setEmail(email);
                    user.setPhone(phone);
                    user.setRoles(roles);
                    callback.onSuccess(user);
                } else {
                    String errorMsg = "Ошибка регистрации: " + response.code();
                    Log.e("ApiRepository", errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Log.e("ApiRepository", "Ошибка сети при регистрации", t);
                callback.onError("Ошибка сети: " + t.getMessage());
            }
        });
    }

    public void getBanksAsync(Context context, BanksCallback callback) {
        String token = getToken(context);

        if (token == null || token.isEmpty()) {
            callback.onError("Отсутствует токен авторизации. Пожалуйста, войдите снова.");
            return;
        }

        MiltiBankApi api = ApiClient.getClient().create(MiltiBankApi.class);
        Call<List<Bank>> call = api.getBanks("Bearer " + token);

        call.enqueue(new Callback<List<Bank>>() {
            @Override
            public void onResponse(Call<List<Bank>> call, Response<List<Bank>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    String errorMsg = "Ошибка загрузки банков: " + response.code();
                    Log.e("ApiRepository", errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<List<Bank>> call, Throwable t) {
                Log.e("ApiRepository", "Ошибка сети при получении банков", t);
                callback.onError("Ошибка сети: " + t.getMessage());
            }
        });
    }

    @Nullable
    private static String getToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String token = prefs.getString("auth_token", null);
        return token;
    }


    /**
     * Получает список счетов текущего пользователя с сервера.
     * Возвращает пустой список в случае ошибки.
     */
    /**
     * Получает список счетов текущего пользователя с сервера.
     * Возвращает пустой список в случае ошибки.
     */
    public List<Account> getAccounts(Context context) {
        List<Account> accounts = new ArrayList<>();

        String token = getToken(context);
        if (token == null || token.isEmpty()) {
            Log.e("ApiRepository", "❌ Отсутствует токен авторизации. Пользователь не вошёл в систему.");
            return accounts;
        }

        try {
            Call<List<Account>> call = authApi.getAccounts("Bearer " + token);
            Response<List<Account>> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                accounts = response.body();
            } else {
                Log.e("ApiRepository", "Ошибка загрузки счетов: " + response.code());
            }
        } catch (IOException e) {
            Log.e("ApiRepository", "Ошибка сети при получении счетов", e);
        }

        return accounts;
    }


    public void createConsent(String token, String bankName, ConsentCallback callback) {
        MiltiBankApi api = ApiClient.getClient().create(MiltiBankApi.class);

        JsonObject body = new JsonObject();
        body.addProperty("bankName", bankName);

        Call<Void> call = api.createConsent("Bearer " + token, body);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError("Ошибка создания согласия: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Ошибка сети: " + t.getMessage());
            }
        });
    }

    public interface ConsentCallback {
        void onSuccess();
        void onError(String message);
    }


    /**
     * Интерфейс обратного вызова для авторизации/регистрации
     */
    public interface AuthCallback {
        void onSuccess(User user);
        void onError(String message);
    }

    public interface BanksCallback {
        void onSuccess(List<Bank> banks);
        void onError(String message);
    }

    // Получение списка транзакций для конкретного счета
    public List<Transaction> getTransactions(
            String accountId,          // "all" — все счета
            LocalDateTime startDate,   // может быть null
            LocalDateTime endDate,     // может быть null
            String category            // null или "all" — без фильтра
    ) {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(Transaction.builder()
                .id("t1")
                .accountId(accountId)
                .amount(-50.0)
                .date(LocalDateTime.now())
                .description("Groceries")
                .category("Авто")
                .build());
        transactions.add(Transaction.builder()
                .id("t2")
                .accountId(accountId)
                .amount(-20.0)
                .date(LocalDateTime.now())
                .description("Taxi")
                .category("Авто")
                .build());
        transactions.add(Transaction.builder()
                .id("t3")
                .accountId(accountId)
                .amount(500.0)
                .date(LocalDateTime.now())
                .description("Salary")
                .category("Авто")
                .build());
        return transactions;
    }
}
