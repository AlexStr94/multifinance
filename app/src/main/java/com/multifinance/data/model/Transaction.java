package com.multifinance.data.model;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction {

    private String accountId;

    private String amount;

    private String valueDateTime;
    private String transactionInformation;
    private String creditDebitIndicator;

    public double getAmountValue() {
        try {
            return Double.parseDouble(amount);
        } catch (Exception e) {
            return 0.0;
        }
    }

    public boolean isCredit() {
        return "Credit".equalsIgnoreCase(creditDebitIndicator);
    }

    public boolean isDebit() {
        return "Debit".equalsIgnoreCase(creditDebitIndicator);
    }
}
