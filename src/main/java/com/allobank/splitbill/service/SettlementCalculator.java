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
        return new SettlementResponse("IDR", total, balances, List.of(), 0, zero);
    }
}
