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
import com.multifinance.data.model.Account;
import com.multifinance.data.repository.ApiRepository;

import java.util.ArrayList;
import java.util.List;

public class AccountsActivity extends BaseActivity {

    private RecyclerView recyclerAccounts;
    private AccountsAdapter adapter;
    private ApiRepository repository;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts);

        setupHeader();
        setupBottomNavigation();

        recyclerAccounts = findViewById(R.id.recycler_accounts);
        progressBar = findViewById(R.id.progress_accounts);
        tvEmpty = findViewById(R.id.tv_empty_accounts);

        recyclerAccounts.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AccountsAdapter(new ArrayList<>(), account -> {
            Intent intent = new Intent(AccountsActivity.this, TransactionsActivity.class);
            intent.putExtra("account_id", account.getId());
            startActivity(intent);
        });
        recyclerAccounts.setAdapter(adapter);

        repository = new ApiRepository();

        loadAccounts();
    }

    private void loadAccounts() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerAccounts.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        new Thread(() -> {
            try {
                // 🔹 Токен теперь берётся внутри репозитория
                List<Account> accounts = repository.getAccounts(this);

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);

                    if (accounts == null || accounts.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        recyclerAccounts.setVisibility(View.GONE);
                    } else {
                        adapter.setItems(accounts);
                        recyclerAccounts.setVisibility(View.VISIBLE);
                        tvEmpty.setVisibility(View.GONE);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Ошибка загрузки счетов", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }


    @Override
    protected int getBottomNavItemId() {
        return R.id.nav_accounts;
    }
}
