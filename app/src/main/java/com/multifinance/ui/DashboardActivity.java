package com.multifinance.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.multifinance.R;
import com.multifinance.data.model.Transaction;
import com.multifinance.data.repository.ApiRepository;

import java.time.LocalDateTime;
import java.util.List;

public class DashboardActivity extends BaseActivity {

    private TextView tvBalance;
    private TextView tvIncome;
    private TextView tvExpense;
    private final ApiRepository repository = new ApiRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        setupHeader();
        setupBottomNavigation();

        tvBalance = findViewById(R.id.tv_balance);
        tvIncome = findViewById(R.id.tv_income_value);
        tvExpense = findViewById(R.id.tv_expense_value);

        // Карточка баланса
        LinearLayout balanceCard = findViewById(R.id.balance_card);
        balanceCard.setOnClickListener(v -> {
            // Переход на страницу со счетами
            Intent intent = new Intent(DashboardActivity.this, AccountsActivity.class);
            startActivity(intent);
        });

        // Временные данные
        tvBalance.setText("250 000 ₽");

        setupAnalyticsCard();

        LinearLayout newProductCard = findViewById(R.id.new_product_card);
        newProductCard.setOnClickListener(v ->
                Toast.makeText(this, "Открытие новых продуктов пока в разработке", Toast.LENGTH_SHORT).show()
        );

        LinearLayout analyticsCard = findViewById(R.id.analytics_card);
        analyticsCard.setOnClickListener(v ->
                Toast.makeText(this, "Раздел аналитики пока в разработке", Toast.LENGTH_SHORT).show()
        );
    }

    /**
     * Подсчёт и отображение доходов / расходов на основе транзакций
     */
    private void setupAnalyticsCard() {
        // Получаем текущий месяц
        LocalDateTime startOfMonth = LocalDateTime.now()
                .withDayOfMonth(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        LocalDateTime endOfMonth = LocalDateTime.now()
                .withDayOfMonth(LocalDateTime.now().toLocalDate().lengthOfMonth())
                .withHour(23)
                .withMinute(59)
                .withSecond(59)
                .withNano(999999999);

        // Получаем транзакции за текущий месяц по всем счетам
        List<Transaction> transactions = repository.getTransactions(
                "mock_token_123",
                ApiRepository.FILTER_ALL, // все счета
                startOfMonth,
                endOfMonth,
                ApiRepository.FILTER_ALL  // все категории
        );

        double income = 0;
        double expenses = 0;

        for (Transaction t : transactions) {
            if (t.getAmount() > 0) {
                income += t.getAmount();
            } else {
                expenses += Math.abs(t.getAmount());
            }
        }

        // Обновляем UI
        tvIncome.setText(String.format("+%.2f ₽", income));
        tvExpense.setText(String.format("-%.2f ₽", expenses));
    }

    @Override
    protected int getBottomNavItemId() {
        return R.id.nav_dashboard;
    }
}
