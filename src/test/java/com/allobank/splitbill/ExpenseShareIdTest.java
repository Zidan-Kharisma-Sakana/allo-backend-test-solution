package com.allobank.splitbill;

import com.allobank.splitbill.model.ExpenseShareId;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ExpenseShareIdTest {

    @Test
    void differentParticipantsHaveDifferentShareIds() {
        UUID expenseId = UUID.randomUUID();
        ExpenseShareId first = new ExpenseShareId(expenseId, UUID.randomUUID());
        ExpenseShareId second = new ExpenseShareId(expenseId, UUID.randomUUID());
        assertNotEquals(first, second);
    }
}
