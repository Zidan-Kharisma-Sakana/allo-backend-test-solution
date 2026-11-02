package com.allobank.splitbill.dto;

import com.allobank.splitbill.model.Expense;
import java.util.UUID;
import java.time.LocalDateTime;
import java.math.BigDecimal;

public record ExpenseResponse(UUID id, UUID billGroupId, UUID payerId, BigDecimal amount, LocalDateTime createdAt) {

    public ExpenseResponse(Expense value) {
        this(value.getId(), value.getBillGroup().getId(), value.getId(), value.getAmount(), value.getCreatedAt());
    }
}
