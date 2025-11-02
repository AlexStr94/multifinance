package com.multifinance.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.multifinance.R;
import com.multifinance.data.model.Account;
import com.multifinance.data.repository.ApiRepository;

import java.util.List;

public class AccountsActivity extends AppCompatActivity {

    private LinearLayout accountsContainer;
    private ApiRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts);

        accountsContainer = findViewById(R.id.accountsContainer);
        repository = new ApiRepository();

        // Получаем токен, например из SharedPreferences
        String token = getSharedPreferences("user_prefs", MODE_PRIVATE).getString("auth_token", "");

        List<Account> accounts = repository.getAccounts(token);

        for (Account account : accounts) {
            addAccountCard(account);
        }
    }

    private void addAccountCard(Account account) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View card = inflater.inflate(R.layout.account_card, accountsContainer, false);

        TextView nameText = card.findViewById(R.id.accountNameTextView);
        TextView balanceText = card.findViewById(R.id.accountBalanceTextView);

        nameText.setText(account.getName());
        balanceText.setText("$" + account.getBalance());

        accountsContainer.addView(card);
    }
}
