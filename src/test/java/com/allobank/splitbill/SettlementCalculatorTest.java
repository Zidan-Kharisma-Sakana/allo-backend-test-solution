package com.allobank.splitbill;

import com.allobank.splitbill.dto.SettlementResponse.Transfer;
import com.allobank.splitbill.model.Expense;
import com.allobank.splitbill.model.ExpenseShare;
import com.allobank.splitbill.model.Participant;
import com.allobank.splitbill.service.SettlementCalculator;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SettlementCalculatorTest {

    private final SettlementCalculator calculator = new SettlementCalculator();
    private final Participant alice = participant("Alice", 0);
    private final Participant bob = participant("Bob", 1);
    private final Participant cara = participant("Cara", 2);

    @Test
    void noExpenses() {
        var result = calculator.calculate(List.of(alice, bob), List.of(), List.of(), "a");

        assertEquals(new BigDecimal("0.00"), result.totalExpenses());
        assertTrue(result.settlements().isEmpty());
        result.balances().forEach(b -> assertEquals(new BigDecimal("0.00"), b.balance()));
    }

    @Test
    void settleDebts() {
        Expense lunch = expense(alice, "100.00");
        Expense taxi = expense(alice, "20.00");
        Expense ticket = expense(bob, "15.00");
        var result = calculator.calculate(List.of(alice, bob, cara), List.of(lunch, taxi, ticket),
                List.of(share(lunch, alice, "33.34"), share(lunch, bob, "33.33"),
                        share(lunch, cara, "33.33"), share(taxi, alice, "10.00"),
                        share(taxi, bob, "10.00"), share(ticket, cara, "15.00")), "a");

        assertEquals(new BigDecimal("135.00"), result.totalExpenses());
        assertEquals(List.of(
                new Transfer(bob.getId(), alice.getId(), new BigDecimal("28.33")),
                new Transfer(cara.getId(), alice.getId(), new BigDecimal("48.33"))),
                result.settlements());
    }

    @Test
    void serviceCharge() {
        Expense lunch = expense(alice, "100.00");
        var result = calculator.calculate(List.of(alice, bob), List.of(lunch),
                List.of(share(lunch, bob, "100.00")), "A");

        // Lowercase 'a' is 97, so the charge is 7%.
        assertEquals(7, result.serviceChargePct());
        assertEquals(new BigDecimal("7.00"), result.serviceChargeAmount());
        assertEquals(List.of(new Transfer(bob.getId(), alice.getId(), new BigDecimal("100.00"))),
                result.settlements());
    }

    @Test
    void alreadySettled() {
        Expense lunch = expense(alice, "20.00");
        Expense taxi = expense(bob, "20.00");
        var result = calculator.calculate(List.of(alice, bob), List.of(lunch, taxi),
                List.of(share(lunch, alice, "10.00"), share(lunch, bob, "10.00"),
                        share(taxi, alice, "10.00"), share(taxi, bob, "10.00")), "a");

        assertEquals(new BigDecimal("40.00"), result.totalExpenses());
        assertTrue(result.settlements().isEmpty());
        result.balances().forEach(b -> assertEquals(new BigDecimal("0.00"), b.balance()));
    }

    @Test
    void twoCreditors() {
        Expense lunch = expense(alice, "60.00");
        Expense ticket = expense(bob, "30.00");
        var result = calculator.calculate(List.of(alice, bob, cara), List.of(lunch, ticket),
                List.of(share(lunch, alice, "20.00"), share(lunch, bob, "20.00"),
                        share(lunch, cara, "20.00"), share(ticket, cara, "30.00")), "a");

        assertEquals(List.of(
                new Transfer(cara.getId(), alice.getId(), new BigDecimal("40.00")),
                new Transfer(cara.getId(), bob.getId(), new BigDecimal("10.00"))),
                result.settlements());
    }

    private Participant participant(String name, int position) {
        Participant participant = new Participant(null, name, position);
        participant.setId(UUID.randomUUID());
        return participant;
    }

    private Expense expense(Participant payer, String amount) {
        Expense expense = new Expense(null, payer, new BigDecimal(amount));
        expense.setId(UUID.randomUUID());
        return expense;
    }

    private ExpenseShare share(Expense expense, Participant participant, String amount) {
        return new ExpenseShare(expense, participant, new BigDecimal(amount));
    }
}
