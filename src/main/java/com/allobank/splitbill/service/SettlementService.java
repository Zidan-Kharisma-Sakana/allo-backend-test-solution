package com.allobank.splitbill.service;

import com.allobank.splitbill.service.SettlementCalculator;
import com.allobank.splitbill.dto.SettlementResponse;
import com.allobank.splitbill.repository.ExpenseRepository;
import com.allobank.splitbill.repository.ExpenseShareRepository;
import com.allobank.splitbill.repository.ParticipantRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final BillGroupService groupService;
    private final ParticipantRepository participantRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseShareRepository shareRepository;
    private final SettlementCalculator calculator;

    @Value("${app.github-username:Zidan-Kharisma-Sakana}")
    private String username;

    @org.springframework.transaction.annotation.Transactional(readOnly = true,
            isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public SettlementResponse get(UUID groupId, String sessionId) {
        groupService.get(groupId, sessionId);
        return calculator.calculate(
                participantRepository.findByBillGroupIdOrderByPositionAsc(groupId),
                expenseRepository.findByBillGroupIdOrderByCreatedAtAsc(groupId),
                shareRepository.findByExpense_BillGroup_Id(groupId), username);
    }
}
