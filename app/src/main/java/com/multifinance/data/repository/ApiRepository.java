package com.multifinance.data.repository;

import android.util.Log;

import com.multifinance.data.model.Account;
import com.multifinance.data.model.Transaction;
import com.multifinance.data.model.User;
import com.multifinance.data.remote.ApiClient;
import com.multifinance.data.remote.AuthApi;
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

    private final AuthApi authApi;

    public ApiRepository() {
        authApi = ApiClient.getClient().create(AuthApi.class);
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

    /**
     * Интерфейс обратного вызова для авторизации/регистрации
     */
    public interface AuthCallback {
        void onSuccess(User user);
        void onError(String message);
    }

    public List<Account> getAccounts(String token) {
        List<Account> accounts = new ArrayList<>();
        accounts.add(Account.builder()
                .id("1")
                .name("Сберегательный")
                .balance(1200.50)
                .build());
        accounts.add(Account.builder()
                .id("2")
                .name("Кредитный")
                .balance(3500.75)
                .build());
        return accounts;
    }

    // Получение списка транзакций для конкретного счета
    public List<Transaction> getTransactions(
            String token,
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
