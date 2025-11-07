package com.multifinance.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.multifinance.R;
import com.multifinance.data.model.User;
import com.multifinance.data.repository.ApiRepository;

import java.util.Collections;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText phoneEditText;
    private EditText passwordEditText;
    private TextView errorTextView;
    private Button registerButton;
    private Button backToLoginButton;

    private ApiRepository repository;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        repository = new ApiRepository();
        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

        usernameEditText = findViewById(R.id.et_username);
        emailEditText = findViewById(R.id.et_email);
        phoneEditText = findViewById(R.id.et_phone);
        passwordEditText = findViewById(R.id.et_password);

        errorTextView = new TextView(this);
        errorTextView.setTextColor(ContextCompat.getColor(this, R.color.errorText));

        // Добавляем errorTextView под полем пароля (как в LoginActivity)
        ((View) passwordEditText.getParent()).post(() -> {
            ((android.widget.LinearLayout) passwordEditText.getParent()).addView(
                    errorTextView,
                    ((android.widget.LinearLayout) passwordEditText.getParent()).indexOfChild(passwordEditText) + 1
            );
        });

        registerButton = findViewById(R.id.btn_register);
        backToLoginButton = findViewById(R.id.btn_back_login);

        registerButton.setOnClickListener(v -> register());
        backToLoginButton.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void register() {
        // Сброс ошибки
        errorTextView.setText("");

        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            errorTextView.setText("Пожалуйста, заполните все поля");
            return;
        }

        // Блокируем кнопку и показываем простой индикатор (текст)
        registerButton.setEnabled(false);
        String prevText = registerButton.getText().toString();
        registerButton.setText("Регистрируем...");

        // В данном бекенде достаточно отправить ROLE_USER
        repository.register(
                username,
                email,
                phone,
                Collections.singletonList("ROLE_USER"),
                password,
                new ApiRepository.AuthCallback() {
                    @Override
                    public void onSuccess(User user) {
                        // Сервер вернул 200 OK — регистрация успешна.
                        runOnUiThread(() -> {
                            Toast.makeText(RegisterActivity.this, "Регистрация прошла успешно", Toast.LENGTH_LONG).show();

                            // Переходим на экран логина и передаём email для автозаполнения
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            intent.putExtra("email", email);
                            startActivity(intent);
                            finish();
                        });
                    }

                    @Override
                    public void onError(String message) {
                        runOnUiThread(() -> {
                            // Показываем сообщение об ошибке и восстанавливаем кнопку
                            errorTextView.setText(message != null ? message : "Ошибка регистрации");
                            registerButton.setEnabled(true);
                            registerButton.setText(prevText);
                        });
                    }
                }
        );
    }
}
