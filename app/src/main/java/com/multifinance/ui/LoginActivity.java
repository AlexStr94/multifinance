package com.multifinance.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.multifinance.R;
import com.multifinance.data.model.User;
import com.multifinance.data.repository.ApiRepository;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;
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

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        errorTextView = findViewById(R.id.errorTextView);
        ImageView logoImageView = findViewById(R.id.logoImageView);
        Button loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    private void login() {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        User user = repository.login(username, password);
        if (user != null && user.getToken() != null && !user.getToken().isEmpty()) {
            saveToken(user.getToken());
            errorTextView.setText("");

            // Переход на главный экран
            Intent intent = new Intent(LoginActivity.this, AccountsActivity.class);
            startActivity(intent);
            finish();
        } else {
            errorTextView.setText("Неверный логин или пароль");
        }
    }

    private void saveToken(String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("auth_token", token);

        // Можно добавить хранение времени жизни токена
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
