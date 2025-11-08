package com.multifinance.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.multifinance.R;
import com.multifinance.data.model.Account;
import com.multifinance.data.model.Transaction;
import com.multifinance.data.repository.ApiRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Главный экран (Дашборд) — отображает общий баланс, доходы, расходы
 * и предлагает добавить банк, если аккаунты отсутствуют.
 */
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
            Intent intent = new Intent(DashboardActivity.this, AddBanksActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAccountsAndUpdateUI();
    }

    /**
     * Загружает список счетов пользователя и обновляет интерфейс.
     */
    private void loadAccountsAndUpdateUI() {
        progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                // Теперь getAccounts сам получает токен из SharedPreferences
                List<Account> accounts = repository.getAccounts(this);

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);

                    if (accounts == null) {
                        // Токен не найден → отправляем на логин
                        Toast.makeText(this, "Сессия истекла, пожалуйста, войдите снова", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                        return;
                    }

                    if (accounts.isEmpty()) {
                        showAddBankButton();
                    } else {
                        showDashboardContent(accounts);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Ошибка загрузки счетов", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    /**
     * Показывает экран, если у пользователя нет подключённых банков.
     */
    private void showAddBankButton() {
        balanceCard.setVisibility(View.GONE);
        analyticsCard.setVisibility(View.GONE);
        newProductCard.setVisibility(View.GONE);

        tvNoBanks.setVisibility(View.VISIBLE);
        btnAddBank.setVisibility(View.VISIBLE);
    }

    /**
     * Показывает экран с балансом, аналитикой и другими разделами.
     */
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
            Intent intent = new Intent(DashboardActivity.this, AccountsActivity.class);
            startActivity(intent);
        });

        setupAnalyticsCard();
    }

    /**
     * Подсчёт и отображение доходов / расходов за текущий месяц.
     */
    private void setupAnalyticsCard() {
        LocalDateTime startOfMonth = LocalDateTime.now()
                .withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        LocalDateTime endOfMonth = LocalDateTime.now()
                .withDayOfMonth(LocalDateTime.now().toLocalDate().lengthOfMonth())
                .withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        new Thread(() -> {
            List<Transaction> transactions = repository.getTransactions(
                    ApiRepository.FILTER_ALL,
                    startOfMonth,
                    endOfMonth,
                    ApiRepository.FILTER_ALL
            );

            double income = 0;
            double expenses = 0;

            for (Transaction t : transactions) {
                if (t.getAmount() > 0) income += t.getAmount();
                else expenses += Math.abs(t.getAmount());
            }

            double finalIncome = income;
            double finalExpenses = expenses;

            runOnUiThread(() -> {
                tvIncome.setText(String.format("+%.2f ₽", finalIncome));
                tvExpense.setText(String.format("-%.2f ₽", finalExpenses));

                analyticsCard.setOnClickListener(v -> {
                    Intent intent = new Intent(DashboardActivity.this, AnalyticsActivity.class);
                    startActivity(intent);
                });
            });
        }).start();
    }

    @Override
    protected int getBottomNavItemId() {
        return R.id.nav_dashboard;
    }
}
