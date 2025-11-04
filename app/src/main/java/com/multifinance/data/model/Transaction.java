package com.multifinance.data.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction {
    private String id;
    private String accountId;
    private double amount;
    private LocalDateTime date;
    private String description;
    private String category;
}
