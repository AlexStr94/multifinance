package com.multifinance.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.multifinance.R;
import com.multifinance.data.model.Account;
import com.multifinance.data.model.Transaction;
import com.multifinance.data.remote.TransactionRequest;
import com.multifinance.data.repository.ApiRepository;

import java.time.LocalDateTime;
import java.util.List;

public class DashboardActivity extends BaseActivity {

    private TextView tvBalance;
    private TextView tvIncome;
    private TextView tvExpense;
    private TextView tvNoBanks;
    private LinearLayout balanceCard;
    private LinearLayout analyticsCard;
    private LinearLayout newProductCard;
    private Button btnAddBank;
    private ProgressBar progressBar;

    private final ApiRepository repository = new ApiRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        setupHeader();
        setupBottomNavigation();
        initViews();
    }

    private void initViews() {
        tvBalance = findViewById(R.id.tv_balance);
        tvIncome = findViewById(R.id.tv_income_value);
        tvExpense = findViewById(R.id.tv_expense_value);
        tvNoBanks = findViewById(R.id.tv_no_banks);
        balanceCard = findViewById(R.id.balance_card);
        analyticsCard = findViewById(R.id.analytics_card);
        newProductCard = findViewById(R.id.new_product_card);
        btnAddBank = findViewById(R.id.btn_add_bank);
        progressBar = findViewById(R.id.progress_dashboard);

        btnAddBank.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, AddBanksActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAccountsAndUpdateUI();
    }

    private void loadAccountsAndUpdateUI() {
        progressBar.setVisibility(View.VISIBLE);

        repository.getAccountsAsync(this, new ApiRepository.AccountsCallback() {
            @Override
            public void onSuccess(List<Account> accounts) {
                if (accounts == null || accounts.isEmpty()) {
                    runOnUiThread(DashboardActivity.this::showAddBankButton);
                } else {
                    runOnUiThread(() -> showDashboardContent(accounts));
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(DashboardActivity.this, "Ошибка загрузки счетов: " + message, Toast.LENGTH_SHORT).show();
                    showAddBankButton();
                });
            }
        });
    }

    private void showAddBankButton() {
        balanceCard.setVisibility(View.GONE);
        analyticsCard.setVisibility(View.GONE);
        newProductCard.setVisibility(View.GONE);

        tvNoBanks.setVisibility(View.VISIBLE);
        btnAddBank.setVisibility(View.VISIBLE);
    }

    private void showDashboardContent(List<Account> accounts) {
        balanceCard.setVisibility(View.VISIBLE);
        analyticsCard.setVisibility(View.VISIBLE);
        newProductCard.setVisibility(View.VISIBLE);

        tvNoBanks.setVisibility(View.GONE);
        btnAddBank.setVisibility(View.GONE);

        // Подсчёт общего баланса
        double totalBalance = 0;
        for (Account a : accounts) {
            totalBalance += a.getBalance();
        }
        tvBalance.setText(String.format("%.2f ₽", totalBalance));

        balanceCard.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, AccountsActivity.class));
        });

        setupAnalyticsCard(accounts);
    }

    private void setupAnalyticsCard(List<Account> accounts) {
        LocalDateTime startOfMonth = LocalDateTime.now()
                .withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        LocalDateTime endOfMonth = LocalDateTime.now()
                .withDayOfMonth(LocalDateTime.now().toLocalDate().lengthOfMonth())
                .withHour(23).withMinute(59).withSecond(59).withNano(999_999_999);

        progressBar.setVisibility(View.VISIBLE);

        final double[] totalIncome = {0.0};
        final double[] totalExpense = {0.0};
        final int[] totalTransactions = {0};
        final int[] accountsProcessed = {0};

        for (Account account : accounts) {
            TransactionRequest request = new TransactionRequest(
                    account.getId(),
                    account.getBankName(),
                    formatForRequest(startOfMonth),
                    formatForRequest(endOfMonth),
                    1,
                    100
            );

            Gson gson = new Gson();
            Log.d("AnalyticsActivity", "Отправляем TransactionRequest: " + gson.toJson(request));


            repository.getTransactionsAsync(this, request, new ApiRepository.TransactionsCallback() {
                @Override
                public void onSuccess(List<Transaction> transactions) {
                    if (transactions != null) {
                        totalTransactions[0] += transactions.size();

                        for (Transaction tx : transactions) {
                            double amount = tx.getAmountValue();
                            if (tx.isCredit()) {
                                totalIncome[0] += amount;
                            } else if (tx.isDebit()) {
                                totalExpense[0] += Math.abs(amount);
                            }
                        }
                    }
                    accountsProcessed[0]++;
                    if (accountsProcessed[0] == accounts.size()) {
                        // обновляем UI
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            tvIncome.setText(String.format("+%.2f ₽", totalIncome[0]));
                            tvExpense.setText(String.format("-%.2f ₽", totalExpense[0]));
                            // Можно показать количество транзакций, например в tvBalance или отдельном TextView
                            tvBalance.setText(String.format("%.2f ₽ (%d транзакций)", totalIncome[0] - totalExpense[0], totalTransactions[0]));
                        });
                    }
                }

                @Override
                public void onError(String message) {
                    accountsProcessed[0]++;
                    if (accountsProcessed[0] == accounts.size()) {
                        runOnUiThread(() -> progressBar.setVisibility(View.GONE));
                    }
                }
            });
        }
    }


    @Override
    protected int getBottomNavItemId() {
        return R.id.nav_dashboard;
    }

    private String formatForRequest(LocalDateTime dt) {
        return String.format("%02d %02d %d %02d:%02d",
                dt.getDayOfMonth(), dt.getMonthValue(), dt.getYear(), dt.getHour(), dt.getMinute());
    }
}
