package com.allobank.splitbill.service;

import com.allobank.splitbill.dto.SettlementResponse;
import com.allobank.splitbill.dto.SettlementResponse.Balance;
import com.allobank.splitbill.dto.SettlementResponse.Transfer;
import com.allobank.splitbill.model.Expense;
import com.allobank.splitbill.model.ExpenseShare;
import com.allobank.splitbill.model.Participant;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class SettlementCalculator {

    public SettlementResponse calculate(List<Participant> participants, List<Expense> expenses,
                                        List<ExpenseShare> shares, String username) {
        Map<UUID, BigDecimal> paid = new LinkedHashMap<>();
        Map<UUID, BigDecimal> owed = new LinkedHashMap<>();
        BigDecimal zero = new BigDecimal("0.00");
        for (Participant participant : participants) {
            paid.put(participant.getId(), zero);
            owed.put(participant.getId(), zero);
        }
        BigDecimal total = zero;
        for (Expense expense : expenses) {
            UUID payerId = expense.getPayer().getId();
            paid.put(payerId, paid.get(payerId).add(expense.getAmount()));
            total = total.add(expense.getAmount());
        }
        for (ExpenseShare share : shares) {
            UUID participantId = share.getId().getParticipantId();
            owed.put(participantId, owed.get(participantId).add(share.getAmount()));
        }
        List<Balance> balances = new ArrayList<>();
        Map<UUID, BigDecimal> remaining = new LinkedHashMap<>();
        for (Participant participant : participants) {
            UUID id = participant.getId();
            BigDecimal balance = paid.get(id).subtract(owed.get(id));
            balances.add(new Balance(id, participant.getName(), paid.get(id), owed.get(id), balance));
            remaining.put(id, balance);
        }
        List<Transfer> settlements = new ArrayList<>();
        for (Participant debtor : participants) {
            for (Participant creditor : participants) {
                BigDecimal debt = remaining.get(debtor.getId());
                BigDecimal credit = remaining.get(creditor.getId());
                if (debt.signum() < 0 && credit.signum() > 0) {
                    BigDecimal amount = debt.negate().min(credit);
                    settlements.add(new Transfer(debtor.getId(), creditor.getId(), amount));
                    remaining.put(debtor.getId(), debt.add(amount));
                    remaining.put(creditor.getId(), credit.subtract(amount));
                }
            }
        }
        return new SettlementResponse("IDR", total, balances, settlements, 0, zero);
    }
}
