package com.multifinance.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.multifinance.R;
import com.multifinance.data.model.Account;
import com.multifinance.data.model.Transaction;
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
import java.util.Set;

/**
 * Экран транзакций с фильтрами по счёту, категории и периоду.
 * Фильтрация выполняется в ApiRepository.
 */
public class TransactionsActivity extends BaseActivity {

    private Spinner spinnerAccount;
    private Spinner spinnerCategory;
    private ImageButton btnDatePicker;
    private TextView tvDateRange;
    private RecyclerView recyclerTransactions;

    private ApiRepository repository;
    private List<Account> accounts = new ArrayList<>();
    private List<Transaction> currentTransactions = new ArrayList<>();
    private TransactionsAdapter adapter;

    private static final String TOKEN = "mock_token_123";

    // Текущие фильтры
    private String selectedAccountId = ApiRepository.FILTER_ALL;
    private String selectedCategory = ApiRepository.FILTER_ALL;
    private LocalDateTime selectedStart = null;
    private LocalDateTime selectedEnd = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);
        String passedAccountId = getIntent().getStringExtra("account_id");
        if (passedAccountId != null) {
            selectedAccountId = passedAccountId;
        }
        setupHeader();
        setupBottomNavigation();

        repository = new ApiRepository();

        spinnerAccount = findViewById(R.id.spinner_account);
        spinnerCategory = findViewById(R.id.spinner_category);
        btnDatePicker = findViewById(R.id.btn_date_picker);
        tvDateRange = findViewById(R.id.tv_date_range);
        recyclerTransactions = findViewById(R.id.recycler_transactions);

        recyclerTransactions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionsAdapter(new ArrayList<>());
        recyclerTransactions.setAdapter(adapter);

        // по умолчанию текущий месяц
        setDefaultMonthRange();

        // слушатели
        btnDatePicker.setOnClickListener(v -> showRangePicker());

        spinnerAccount.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedAccountId = ApiRepository.FILTER_ALL;
                } else {
                    String display = (String) spinnerAccount.getSelectedItem();
                    int open = display.lastIndexOf('(');
                    int close = display.lastIndexOf(')');
                    if (open != -1 && close != -1 && close > open) {
                        selectedAccountId = display.substring(open + 1, close);
                    } else {
                        selectedAccountId = ApiRepository.FILTER_ALL;
                    }
                }
                loadTransactionsWithFilters();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        spinnerCategory.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String sel = (String) spinnerCategory.getSelectedItem();
                if (sel == null || sel.equalsIgnoreCase("Все категории")) {
                    selectedCategory = ApiRepository.FILTER_ALL;
                } else {
                    selectedCategory = sel;
                }
                loadTransactionsWithFilters();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        // начальная загрузка данных
        loadInitialData();
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

    private void loadInitialData() {
        // 1. Счета
        accounts = repository.getAccounts(TOKEN);
        List<String> accountDisplay = new ArrayList<>();
        accountDisplay.add("Все счета");
        for (Account a : accounts) {
            accountDisplay.add(a.getName() + " (" + a.getId() + ")");
        }
        ArrayAdapter<String> accountsAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, accountDisplay);
        accountsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAccount.setAdapter(accountsAdapter);

        // 2. Первичная загрузка транзакций и категорий
        loadTransactionsWithFiltersAndPopulateCategories();
    }

    private void loadTransactionsWithFilters() {
        try {
            List<Transaction> result = repository.getTransactions(
                    TOKEN,
                    selectedAccountId != null ? selectedAccountId : ApiRepository.FILTER_ALL,
                    selectedStart,
                    selectedEnd,
                    selectedCategory != null ? selectedCategory : ApiRepository.FILTER_ALL
            );
            if (result == null) result = new ArrayList<>();
            currentTransactions = result;
            adapter.setItems(currentTransactions);
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(this, "Ошибка загрузки транзакций", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadTransactionsWithFiltersAndPopulateCategories() {
        try {
            List<Transaction> result = repository.getTransactions(
                    TOKEN,
                    ApiRepository.FILTER_ALL,
                    selectedStart,
                    selectedEnd,
                    ApiRepository.FILTER_ALL
            );
            if (result == null) result = new ArrayList<>();
            currentTransactions = result;
            adapter.setItems(currentTransactions);

            // формируем категории
            Set<String> categories = new LinkedHashSet<>();
            categories.add("Все категории");
            for (Transaction t : currentTransactions) {
                if (t.getCategory() != null && !t.getCategory().trim().isEmpty()) {
                    categories.add(t.getCategory());
                }
            }
            ArrayAdapter<String> catAdapter =
                    new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>(categories));
            catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategory.setAdapter(catAdapter);
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(this, "Ошибка загрузки транзакций", Toast.LENGTH_SHORT).show();
        }
    }

    private void showRangePicker() {
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
                    loadTransactionsWithFilters();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(this, "Ошибка открытия календаря", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected int getBottomNavItemId() {
        return R.id.nav_accounts;
    }

}
