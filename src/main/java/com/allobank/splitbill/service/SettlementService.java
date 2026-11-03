package com.allobank.splitbill.service;

import com.allobank.splitbill.dto.SettlementResponse;
import com.allobank.splitbill.service.settlement.SettlementCalculator;
import com.allobank.splitbill.repository.ExpenseRepository;
import com.allobank.splitbill.repository.ExpenseShareRepository;
import com.allobank.splitbill.repository.ParticipantRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

    private final BillGroupService access;
    private final ParticipantRepository participantRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseShareRepository shareRepository;
    private final SettlementCalculator calculator;

    @Value("${app.github-username:Zidan-Kharisma-Sakana}")
    private String username;

    public SettlementResponse get(UUID groupId, String sessionId) {
        log.info("Calculating settlement");
        access.requireGroup(groupId, sessionId);
        return calculator.calculate(
                participantRepository.findByBillGroupIdOrderByPositionAsc(groupId),
                expenseRepository.findByBillGroupIdOrderByCreatedAtAsc(groupId),
                shareRepository.findByExpense_BillGroup_Id(groupId), username);
    }
}
