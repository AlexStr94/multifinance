package com.multifinance.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.multifinance.R;
import com.multifinance.data.model.Bank;
import com.multifinance.data.repository.ApiRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Экран выбора или просмотра банков.
 */
public class AddBanksActivity extends BaseActivity {

    private RecyclerView recyclerBanks;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private BanksAdapter adapter;
    private ApiRepository repository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_banks);

        setupHeader();
        setupBottomNavigation();

        recyclerBanks = findViewById(R.id.recycler_banks);
        progressBar = findViewById(R.id.progress_banks);
        tvEmpty = findViewById(R.id.tv_empty_banks);

        recyclerBanks.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BanksAdapter(new ArrayList<>(), this::onBankClicked);
        recyclerBanks.setAdapter(adapter);

        repository = new ApiRepository();

        loadBanks();
    }

    private void loadBanks() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        recyclerBanks.setVisibility(View.GONE);

        repository.getBanksAsync(this, new ApiRepository.BanksCallback() {
            @Override
            public void onSuccess(List<Bank> banks) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);

                    if (banks == null || banks.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        adapter.setItems(banks);
                        recyclerBanks.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AddBanksActivity.this, message, Toast.LENGTH_SHORT).show();
                    tvEmpty.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    /**
     * Обработка клика по банку
     */
    private void onBankClicked(Bank bank) {
        progressBar.setVisibility(View.VISIBLE);

        String token = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getString("auth_token", null);

        repository.createConsent(token, bank.getName(), new ApiRepository.ConsentCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AddBanksActivity.this,
                            "Банк успешно добавлен!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(AddBanksActivity.this, DashboardActivity.class);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AddBanksActivity.this, message, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    protected int getBottomNavItemId() {
        return R.id.nav_accounts;
    }
}
