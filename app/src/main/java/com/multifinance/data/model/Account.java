package com.multifinance.data.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Модель банковского счёта пользователя
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Account {
    private String id;
    private String number;
    private String type;
    private double balance;
    private String bankName;

    public String getDisplayName() {
        // Пример: "vbank • 1042048"
        if (number != null && number.length() > 6) {
            return bankName + " • " + number.substring(number.length() - 6);
        } else {
            return bankName != null ? bankName : "Счёт";
        }
    }
}
