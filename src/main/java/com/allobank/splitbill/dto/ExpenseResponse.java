package com.allobank.splitbill.dto;

import com.allobank.splitbill.model.Expense;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ExpenseResponse(UUID id, UUID billGroupId, UUID payerId, BigDecimal amount, String description, LocalDateTime createdAt) {

    public ExpenseResponse(Expense value) {
        this(value.getId(), value.getBillGroup().getId(), value.getPayer().getId(), value.getAmount(), value.getDescription(), value.getCreatedAt());
    }
}
