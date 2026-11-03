package com.allobank.splitbill.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "expense_shares")
public class ExpenseShare {

    @EmbeddedId
    private ExpenseShareId id;

    @MapsId("expenseId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;

    @MapsId("participantId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @Column(nullable = false)
    private BigDecimal amount;

    public ExpenseShare(Expense expense, Participant participant, BigDecimal amount) {
        this.id = new ExpenseShareId(expense.getId(), participant.getId());
        this.expense = expense;
        this.participant = participant;
        this.amount = amount;
    }
}
