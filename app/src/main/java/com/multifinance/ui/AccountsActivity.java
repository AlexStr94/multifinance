package com.multifinance.ui;

import android.content.Intent;
import android.os.Bundle;
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

    private static final String TOKEN = "mock_token_123";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts);
        setupHeader();
        setupBottomNavigation();

        recyclerAccounts = findViewById(R.id.recycler_accounts);
        recyclerAccounts.setLayoutManager(new LinearLayoutManager(this));

        repository = new ApiRepository();

        adapter = new AccountsAdapter(new ArrayList<>(), account -> {
            Intent intent = new Intent(AccountsActivity.this, TransactionsActivity.class);
            intent.putExtra("account_id", account.getId());
            startActivity(intent);
        });
        recyclerAccounts.setAdapter(adapter);

        loadAccounts();
    }

    private void loadAccounts() {
        try {
            List<Account> accounts = repository.getAccounts(TOKEN);
            adapter.setItems(accounts);
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(this, "Ошибка загрузки счетов", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected int getBottomNavItemId() {
        return R.id.nav_accounts;
    }

}
