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
import java.util.Set;

/**
 * Экран транзакций с фильтрами по счёту, категории и периоду.
 * Работает с новым асинхронным ApiRepository.
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

    private String selectedAccountId = ApiRepository.FILTER_ALL;
    private String selectedCategory = ApiRepository.FILTER_ALL;
    private LocalDateTime selectedStart;
    private LocalDateTime selectedEnd;

    private boolean showExpenses = true;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

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

        setDefaultMonthRange();

        btnDatePicker.setOnClickListener(v -> showRangePicker());

        spinnerAccount.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) selectedAccountId = ApiRepository.FILTER_ALL;
                else {
                    String display = (String) spinnerAccount.getSelectedItem();
                    int open = display.lastIndexOf('(');
                    int close = display.lastIndexOf(')');
                    selectedAccountId = (open != -1 && close > open) ? display.substring(open + 1, close) : ApiRepository.FILTER_ALL;
                }
                loadTransactionsWithFilters();
            }

            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        spinnerCategory.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String sel = (String) spinnerCategory.getSelectedItem();
                selectedCategory = (sel == null || sel.equalsIgnoreCase("Все категории")) ? ApiRepository.FILTER_ALL : sel;
                loadTransactionsWithFilters();
            }

            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        loadAccounts();
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

    private void loadAccounts() {
        repository.getAccountsAsync(this, new ApiRepository.AccountsCallback() {
            @Override
            public void onSuccess(List<Account> accountList) {
                accounts = accountList;
                List<String> display = new ArrayList<>();
                display.add("Все счета");
                for (Account a : accounts) display.add(a.getDisplayName());

                ArrayAdapter<String> accountsAdapter = new ArrayAdapter<>(TransactionsActivity.this,
                        android.R.layout.simple_spinner_item, display);
                accountsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerAccount.setAdapter(accountsAdapter);

                loadTransactionsAndPopulateCategories();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(TransactionsActivity.this, "Ошибка загрузки счетов: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTransactionsWithFilters() {
        if (accounts.isEmpty()) return;

        List<Transaction> allTransactions = new ArrayList<>();
        int[] remaining = {accounts.size()};

        for (Account account : accounts) {
            if (!selectedAccountId.equals(ApiRepository.FILTER_ALL) &&
                    !selectedAccountId.equals(account.getId())) {
                remaining[0]--;
                continue;
            }

            TransactionRequest request = new TransactionRequest(
                    account.getId(),
                    account.getBankName(),
                    formatForRequest(selectedStart),
                    formatForRequest(selectedEnd),
                    0,  // 0 или 1 — зависит от API, оставляем как было
                    100
            );

            repository.getTransactionsAsync(this, request, new ApiRepository.TransactionsCallback() {
                @Override
                public void onSuccess(List<Transaction> transactions) {
                    if (transactions != null) {
                        for (Transaction t : transactions) {
                            // Добавляем только транзакции, которые соответствуют выбранному типу
                            if ((showExpenses && t.isDebit()) || (!showExpenses && t.isCredit())) {
                                allTransactions.add(t);
                            }
                        }
                    }
                    remaining[0]--;
                    if (remaining[0] == 0) applyCategoryFilter(allTransactions);
                }

                @Override
                public void onError(String message) {
                    remaining[0]--;
                    if (remaining[0] == 0) applyCategoryFilter(allTransactions);
                }
            });
        }
    }


    private void applyCategoryFilter(List<Transaction> allTransactions) {
        List<Transaction> filtered = new ArrayList<>();
        for (Transaction t : allTransactions) {
            if (selectedCategory.equals(ApiRepository.FILTER_ALL) ||
                    (t.getTransactionInformation() != null &&
                            t.getTransactionInformation().equals(selectedCategory))) {
                filtered.add(t);
            }
        }

        runOnUiThread(() -> {
            currentTransactions = filtered;
            adapter.setItems(currentTransactions);
        });
    }

    private void loadTransactionsAndPopulateCategories() {
        if (accounts.isEmpty()) return;

        List<Transaction> allTransactions = new ArrayList<>();
        Set<String> categories = new LinkedHashSet<>();
        categories.add("Все категории");

        int[] remaining = {accounts.size()};

        for (Account account : accounts) {
            TransactionRequest request = new TransactionRequest(
                    account.getId(),
                    account.getBankName(),
                    formatForRequest(selectedStart),
                    formatForRequest(selectedEnd),
                    0,
                    200
            );

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
                    if (remaining[0] == 0) updateUI(allTransactions, categories);
                }

                @Override
                public void onError(String message) {
                    remaining[0]--;
                    if (remaining[0] == 0) updateUI(allTransactions, categories);
                }
            });
        }
    }

    private void updateUI(List<Transaction> transactions, Set<String> categories) {
        runOnUiThread(() -> {
            currentTransactions = transactions;
            adapter.setItems(currentTransactions);

            ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, new ArrayList<>(categories));
            catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategory.setAdapter(catAdapter);
        });
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

    private String formatForRequest(LocalDateTime dt) {
        return String.format("%02d %02d %d %02d:%02d",
                dt.getDayOfMonth(), dt.getMonthValue(), dt.getYear(),
                dt.getHour(), dt.getMinute());
    }

    @Override
    protected int getBottomNavItemId() {
        return R.id.nav_accounts;
    }
}
