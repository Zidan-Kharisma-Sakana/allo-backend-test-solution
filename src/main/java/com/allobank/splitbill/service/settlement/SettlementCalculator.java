package com.allobank.splitbill.service.settlement;

import static com.allobank.splitbill.exception.ApplicationException.invalidRequest;

import com.allobank.splitbill.dto.SettlementResponse.Balance;
import com.allobank.splitbill.dto.SettlementResponse.Transfer;
import com.allobank.splitbill.dto.SettlementResponse;
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
        BigDecimal zero = BigDecimal.ZERO;
        for (Participant participant : participants) {
            paid.put(participant.getId(), zero);
            owed.put(participant.getId(), zero);
        }
        BigDecimal total = zero;
        for (Expense expense : expenses) {
            UUID payerId = expense.getPayer().getId();
            BigDecimal amount = wholeRupiah(expense.getAmount());
            paid.put(payerId, paid.get(payerId).add(amount));
            total = total.add(amount);
        }
        for (ExpenseShare share : shares) {
            UUID participantId = share.getId().getParticipantId();
            owed.put(participantId, owed.get(participantId).add(wholeRupiah(share.getAmount())));
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
        int percentage = (int) (username.toLowerCase(Locale.ROOT).codePoints().asLongStream().sum() % 10);
        BigDecimal charge = total.multiply(BigDecimal.valueOf(percentage)).movePointLeft(2)
                .setScale(0, RoundingMode.DOWN);
        return new SettlementResponse("IDR", total, balances, settlements, percentage, charge);
    }

    private BigDecimal wholeRupiah(BigDecimal amount) {
        if (amount == null || amount.stripTrailingZeros().scale() > 0) {
            throw invalidRequest();
        }
        return amount.setScale(0, RoundingMode.UNNECESSARY);
    }
}
