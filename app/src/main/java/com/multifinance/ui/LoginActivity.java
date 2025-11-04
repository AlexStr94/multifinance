package com.multifinance.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.multifinance.R;
import com.multifinance.data.model.User;
import com.multifinance.data.repository.ApiRepository;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private TextView errorTextView;
    private ApiRepository repository;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        repository = new ApiRepository();
        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

        emailEditText = findViewById(R.id.et_email);
        passwordEditText = findViewById(R.id.et_password);
        errorTextView = new TextView(this);
        errorTextView.setTextColor(ContextCompat.getColor(this, R.color.errorText));

        // Добавляем errorTextView в контейнер формы под паролем
        ((View) passwordEditText.getParent()).post(() -> {
            ((View) passwordEditText.getParent()).requestLayout();
            ((android.widget.LinearLayout) passwordEditText.getParent()).addView(errorTextView,
                    ((android.widget.LinearLayout) passwordEditText.getParent()).indexOfChild(passwordEditText) + 1);
        });

        Button loginButton = findViewById(R.id.btn_login);
        Button registerButton = findViewById(R.id.btn_register);

        // Делаем кнопку регистрации неактивной
        registerButton.setEnabled(false);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    private void login() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            errorTextView.setText("Пожалуйста, заполните все поля");
            return;
        }

        User user = repository.login(email, password);
        if (user != null && user.getToken() != null && !user.getToken().isEmpty()) {
            saveToken(user.getToken());
            errorTextView.setText("");

            // Переход на главный экран
            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        } else {
            errorTextView.setText("Неверный логин или пароль");
        }
    }

    private void saveToken(String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("auth_token", token);

        long expiryTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000); // 24 часа
        editor.putLong("token_expiry", expiryTime);

        editor.apply();
    }

    public String getToken() {
        long expiry = sharedPreferences.getLong("token_expiry", 0);
        if (System.currentTimeMillis() > expiry) {
            return null; // токен истек
        }
        return sharedPreferences.getString("auth_token", null);
    }
}
