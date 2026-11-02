package com.allobank.splitbill.dto;

import com.allobank.splitbill.model.ExpenseShare;
import java.util.UUID;
import java.math.BigDecimal;

public record ExpenseShareResponse(UUID expenseId, UUID participantId, BigDecimal amount) {

    public ExpenseShareResponse(ExpenseShare value) {
        this(value.getId().getExpenseId(), value.getId().getParticipantId(), value.getAmount());
    }
}
