package com.multifinance.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.multifinance.R;
import com.multifinance.data.model.Transaction;
import com.multifinance.data.repository.ApiRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnalyticsActivity extends BaseActivity {

    private ApiRepository repository = new ApiRepository();
    private PieChart pieChart;
    private RecyclerView recyclerCategories;
    private Button btnExpenses, btnIncome;
    private boolean showExpenses = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        setupHeader();
        setupBottomNavigation();

        // Инициализация элементов
        pieChart = findViewById(R.id.pie_chart);
        recyclerCategories = findViewById(R.id.recycler_categories);
        recyclerCategories.setLayoutManager(new LinearLayoutManager(this));

        btnExpenses = findViewById(R.id.btn_expenses);
        btnIncome = findViewById(R.id.btn_income);

        // Обработчики нажатий
        btnExpenses.setOnClickListener(v -> {
            if (!showExpenses) {
                showExpenses = true;
                updateButtons();
                loadAnalytics();
            }
        });

        btnIncome.setOnClickListener(v -> {
            if (showExpenses) {
                showExpenses = false;
                updateButtons();
                loadAnalytics();
            }
        });

        updateButtons();
        loadAnalytics();
    }

    private void updateButtons() {
        if (showExpenses) {
            btnExpenses.setBackgroundTintList(getColorStateList(R.color.primary));
            btnExpenses.setTextColor(getColor(android.R.color.white));
            btnIncome.setBackgroundTintList(getColorStateList(R.color.surface));
            btnIncome.setTextColor(getColor(R.color.primaryText));
        } else {
            btnIncome.setBackgroundTintList(getColorStateList(R.color.primary));
            btnIncome.setTextColor(getColor(android.R.color.white));
            btnExpenses.setBackgroundTintList(getColorStateList(R.color.surface));
            btnExpenses.setTextColor(getColor(R.color.primaryText));
        }
    }

    private void loadAnalytics() {
        LocalDateTime start = LocalDateTime.now().withDayOfMonth(1);
        LocalDateTime end = LocalDateTime.now().withDayOfMonth(
                LocalDateTime.now().toLocalDate().lengthOfMonth()
        );

        List<Transaction> transactions = repository.getTransactions(
                "mock_token_123",
                ApiRepository.FILTER_ALL,
                start,
                end,
                ApiRepository.FILTER_ALL
        );

        // Фильтрация по типу
        List<Transaction> filtered = transactions.stream()
                .filter(t -> showExpenses ? t.getAmount() < 0 : t.getAmount() > 0)
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            Toast.makeText(this, "Нет данных", Toast.LENGTH_SHORT).show();
            pieChart.clear();
            recyclerCategories.setAdapter(null);
            return;
        }

        // Группировка по категориям
        Map<String, Double> grouped = filtered.stream()
                .collect(Collectors.groupingBy(
                        Transaction::getDescription,
                        Collectors.summingDouble(t -> Math.abs(t.getAmount()))
                ));

        // Подготовка данных для графика
        List<PieEntry> entries = new ArrayList<>();
        grouped.forEach((category, sum) -> entries.add(new PieEntry(sum.floatValue(), category)));

        PieDataSet dataSet = new PieDataSet(entries, showExpenses ? "Расходы" : "Доходы");
        dataSet.setSliceSpace(2f);
        dataSet.setValueTextSize(12f);
        PieData data = new PieData(dataSet);

        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setData(data);
        pieChart.invalidate();

        // Отображаем список категорий
        recyclerCategories.setAdapter(new CategorySummaryAdapter(grouped));
    }

    @Override
    protected int getBottomNavItemId() {
        return R.id.nav_analytics;
    }
}
