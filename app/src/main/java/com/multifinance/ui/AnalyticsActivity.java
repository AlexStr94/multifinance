package com.multifinance.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.gson.Gson;
import com.multifinance.R;
import com.multifinance.data.model.Account;
import com.multifinance.data.model.Transaction;
import com.multifinance.data.remote.TransactionRequest;
import com.multifinance.data.repository.ApiRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

/**
 * Экран аналитики с фильтрацией по счету, категории и периоду.
 * Асинхронные вызовы через ApiRepository.
 */
public class AnalyticsActivity extends BaseActivity {

    private PieChart pieChart;
    private RecyclerView recyclerCategories;
    private ImageButton btnDatePicker;
    private TextView tvDateRange;
    private Spinner spinnerAccount, spinnerCategory;

    private ApiRepository repository;
    private List<Account> accounts = new ArrayList<>();
    private List<Transaction> allTransactions = new ArrayList<>();

    private boolean showExpenses = true;
    private LocalDateTime selectedStart;
    private LocalDateTime selectedEnd;

    private String selectedAccountId = ApiRepository.FILTER_ALL;
    private String selectedCategory = ApiRepository.FILTER_ALL;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        setupHeader();
        setupBottomNavigation();

        pieChart = findViewById(R.id.pie_chart);
        recyclerCategories = findViewById(R.id.recycler_categories);
        recyclerCategories.setLayoutManager(new LinearLayoutManager(this));

        btnDatePicker = findViewById(R.id.btn_date_picker);
        tvDateRange = findViewById(R.id.tv_date_range);

        spinnerAccount = findViewById(R.id.spinner_account);
        spinnerCategory = findViewById(R.id.spinner_category);

        repository = new ApiRepository();

        findViewById(R.id.btn_expenses).setOnClickListener(v -> {
            if (!showExpenses) {
                showExpenses = true;
                updateButtons();
                updateAnalytics();
            }
        });

        findViewById(R.id.btn_income).setOnClickListener(v -> {
            if (showExpenses) {
                showExpenses = false;
                updateButtons();
                updateAnalytics();
            }
        });

        setDefaultMonthRange();
        setupSpinners();
        btnDatePicker.setOnClickListener(v -> showDateRangePicker());

        loadAccountsAndTransactionsAsync();
        updateButtons();
    }

    private void updateButtons() {
        findViewById(R.id.btn_expenses).setBackgroundTintList(
                showExpenses ? getColorStateList(R.color.primary) : getColorStateList(R.color.surface));
        findViewById(R.id.btn_income).setBackgroundTintList(
                !showExpenses ? getColorStateList(R.color.primary) : getColorStateList(R.color.surface));

        ((TextView)findViewById(R.id.btn_expenses)).setTextColor(
                showExpenses ? getColor(android.R.color.white) : getColor(R.color.primaryText));
        ((TextView)findViewById(R.id.btn_income)).setTextColor(
                !showExpenses ? getColor(android.R.color.white) : getColor(R.color.primaryText));
    }

    private void setDefaultMonthRange() {
        LocalDate now = LocalDate.now();
        LocalDate first = now.withDayOfMonth(1);
        LocalDate last = now.withDayOfMonth(now.lengthOfMonth());
        selectedStart = LocalDateTime.of(first, LocalTime.MIN);
        selectedEnd = LocalDateTime.of(last, LocalTime.MAX);
        tvDateRange.setText(formatRangeLabel(selectedStart, selectedEnd));
    }

    private String formatRangeLabel(LocalDateTime start, LocalDateTime end) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return start.format(f) + " — " + end.format(f);
    }

    private void setupSpinners() {
        spinnerAccount.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedAccountId = (position == 0) ? ApiRepository.FILTER_ALL :
                        extractIdFromDisplay((String) spinnerAccount.getSelectedItem());
                updateAnalytics();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        spinnerCategory.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String sel = (String) spinnerCategory.getSelectedItem();
                selectedCategory = (sel == null || sel.equalsIgnoreCase("Все категории")) ? ApiRepository.FILTER_ALL : sel;
                updateAnalytics();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private String extractIdFromDisplay(String display) {
        int open = display.lastIndexOf('(');
        int close = display.lastIndexOf(')');
        return (open != -1 && close > open) ? display.substring(open + 1, close) : ApiRepository.FILTER_ALL;
    }

    private void loadAccountsAndTransactionsAsync() {
        repository.getAccountsAsync(this, new ApiRepository.AccountsCallback() {
            @Override
            public void onSuccess(List<Account> accountList) {
                accounts = accountList;
                populateAccountSpinner();
                loadAllTransactionsAsync();
            }
            @Override
            public void onError(String message) {
                Toast.makeText(AnalyticsActivity.this, "Ошибка загрузки счетов: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAllTransactionsAsync() {
        allTransactions.clear();
        int[] remaining = {accounts.size()};
        Set<String> categories = new LinkedHashSet<>();
        categories.add("Все категории");

        for (Account account : accounts) {
            TransactionRequest request = new TransactionRequest(
                    account.getId(),
                    account.getBankName(),
                    formatForRequest(selectedStart),
                    formatForRequest(selectedEnd),
                    1,
                    100
            );
            Gson gson = new Gson();
            Log.d("AnalyticsActivity", "Отправляем TransactionRequest: " + gson.toJson(request));

            repository.getTransactionsAsync(this, request, new ApiRepository.TransactionsCallback() {
                @Override
                public void onSuccess(List<Transaction> transactions) {
                    if (transactions != null) {
                        allTransactions.addAll(transactions);
                        for (Transaction t : transactions) {
                            if (t.getTransactionInformation() != null && !t.getTransactionInformation().trim().isEmpty())
                                categories.add(t.getTransactionInformation());
                        }
                    }
                    remaining[0]--;
                    if (remaining[0] == 0) runOnUiThread(() -> populateCategorySpinner(categories));
                }

                @Override
                public void onError(String message) {
                    remaining[0]--;
                    if (remaining[0] == 0) runOnUiThread(() -> populateCategorySpinner(categories));
                }
            });
        }
    }

    private void populateAccountSpinner() {
        List<String> items = new ArrayList<>();
        items.add("Все счета");
        for (Account a : accounts) items.add(a.getDisplayName() + " (" + a.getId() + ")");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAccount.setAdapter(adapter);
    }

    private void populateCategorySpinner(Set<String> categories) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new ArrayList<>(categories));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
        updateAnalytics();
    }

    private void updateAnalytics() {
        List<Transaction> filtered = new ArrayList<>();
        for (Transaction t : allTransactions) {
            boolean matchesType = showExpenses ? t.isDebit() : t.isCredit();
            boolean matchesAccount = selectedAccountId.equals(ApiRepository.FILTER_ALL) || t.getAccountId().equals(selectedAccountId);
            boolean matchesCategory = selectedCategory.equals(ApiRepository.FILTER_ALL) ||
                    (t.getTransactionInformation() != null && t.getTransactionInformation().equals(selectedCategory));

            if (matchesType && matchesAccount && matchesCategory) filtered.add(t);
        }

        if (filtered.isEmpty()) {
            pieChart.clear();
            recyclerCategories.setAdapter(null);
            return;
        }

        Map<String, Double> grouped = new HashMap<>();
        for (Transaction t : filtered) {
            String cat = (t.getTransactionInformation() != null && !t.getTransactionInformation().trim().isEmpty())
                    ? t.getTransactionInformation() : "Без категории";
            grouped.put(cat, grouped.getOrDefault(cat, 0.0) + Math.abs(t.getAmountValue()));
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> e : grouped.entrySet()) entries.add(new PieEntry(e.getValue().floatValue(), e.getKey()));

        PieDataSet dataSet = new PieDataSet(entries, showExpenses ? "Расходы" : "Доходы");
        dataSet.setSliceSpace(2f);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setData(data);
        pieChart.invalidate();

        recyclerCategories.setAdapter(new CategorySummaryAdapter(grouped));
    }


    private void showDateRangePicker() {
        try {
            MaterialDatePicker.Builder<androidx.core.util.Pair<Long, Long>> builder =
                    MaterialDatePicker.Builder.dateRangePicker()
                            .setTitleText("Выберите диапазон дат")
                            .setSelection(
                                    new androidx.core.util.Pair<>(
                                            selectedStart.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                                            selectedEnd.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                    )
                            );

            MaterialDatePicker<androidx.core.util.Pair<Long, Long>> picker = builder.build();
            picker.show(getSupportFragmentManager(), "range_picker");

            picker.addOnPositiveButtonClickListener(selection -> {
                if (selection != null) {
                    Long startMillis = selection.first;
                    Long endMillis = selection.second;
                    selectedStart = LocalDateTime.ofInstant(Instant.ofEpochMilli(startMillis), ZoneId.systemDefault())
                            .with(LocalTime.MIN);
                    selectedEnd = LocalDateTime.ofInstant(Instant.ofEpochMilli(endMillis), ZoneId.systemDefault())
                            .with(LocalTime.MAX);
                    tvDateRange.setText(formatRangeLabel(selectedStart, selectedEnd));
                    loadAllTransactionsAsync();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(this, "Ошибка открытия календаря", Toast.LENGTH_SHORT).show();
        }
    }


    private String formatForRequest(LocalDateTime dt) {
        return String.format("%02d %02d %d %02d:%02d",
                dt.getDayOfMonth(), dt.getMonthValue(), dt.getYear(), dt.getHour(), dt.getMinute());
    }

    @Override
    protected int getBottomNavItemId() {
        return R.id.nav_analytics;
    }
}
