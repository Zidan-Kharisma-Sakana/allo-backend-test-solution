package com.allobank.splitbill.service;

import com.allobank.splitbill.dto.ExpenseShareResponse;
import com.allobank.splitbill.model.ExpenseShare;
import com.allobank.splitbill.repository.ExpenseShareRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseShareService {

    private final ExpenseShareRepository repository;
    private final ExpenseService expenseService;

    public List<ExpenseShareResponse> list(UUID groupId, UUID expenseId, String sessionId) {
        log.info("Listing expense shares");
        expenseService.requireExpense(groupId, expenseId, sessionId);
        List<ExpenseShare> shares = repository.findByExpenseId(expenseId);
        List<ExpenseShareResponse> result = new ArrayList<>();
        for (ExpenseShare share : shares) {
            result.add(new ExpenseShareResponse(share));
        }
        return result;
    }
}
