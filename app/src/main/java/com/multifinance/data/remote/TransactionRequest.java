package com.multifinance.data.remote;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionRequest {
    private String accountId;
    private String bankName;
    private String startDateTime;
    private String endDateTime;
    private int pageNumber;
    private int pageSize;
}
