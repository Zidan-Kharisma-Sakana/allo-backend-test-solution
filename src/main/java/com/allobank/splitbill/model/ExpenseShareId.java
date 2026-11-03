package com.allobank.splitbill.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Embeddable
public class ExpenseShareId implements Serializable {

    @Column(name = "expense_id")
    private UUID expenseId;

    @Column(name = "participant_id")
    private UUID participantId;

    public ExpenseShareId(UUID expenseId, UUID participantId) {
        this.expenseId = expenseId;
        this.participantId = participantId;
    }
}
