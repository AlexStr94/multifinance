package com.multifinance.data.repository;

import com.multifinance.data.model.Account;
import com.multifinance.data.model.Transaction;
import com.multifinance.data.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ApiRepository {

    public static final String FILTER_ALL = "all";

    // Моковая авторизация
    public User login(String username, String password) {
        return User.builder()
                .id("1")
                .name(username)
                .token("mock_token_123")
                .build();
    }

    // Получение списка счетов
    public List<Account> getAccounts(String token) {
        List<Account> accounts = new ArrayList<>();
        accounts.add(Account.builder()
                .id("1")
                .name("Сберегательный")
                .balance(1200.50)
                .build());
        accounts.add(Account.builder()
                .id("2")
                .name("Кредитный")
                .balance(3500.75)
                .build());
        return accounts;
    }

    // Получение списка транзакций для конкретного счета
    public List<Transaction> getTransactions(
            String token,
            String accountId,          // "all" — все счета
            LocalDateTime startDate,   // может быть null
            LocalDateTime endDate,     // может быть null
            String category            // null или "all" — без фильтра
    ) {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(Transaction.builder()
                .id("t1")
                .accountId(accountId)
                .amount(-50.0)
                .date(LocalDateTime.now())
                .description("Groceries")
                .category("Авто")
                .build());
        transactions.add(Transaction.builder()
                .id("t2")
                .accountId(accountId)
                .amount(-20.0)
                .date(LocalDateTime.now())
                .description("Taxi")
                .category("Авто")
                .build());
        transactions.add(Transaction.builder()
                .id("t3")
                .accountId(accountId)
                .amount(500.0)
                .date(LocalDateTime.now())
                .description("Salary")
                .category("Авто")
                .build());
        return transactions;
    }


}
