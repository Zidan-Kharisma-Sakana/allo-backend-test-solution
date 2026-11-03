package com.allobank.splitbill.service;

import com.allobank.splitbill.dto.ExpenseShareResponse;
import com.allobank.splitbill.model.ExpenseShare;
import com.allobank.splitbill.model.ExpenseShareId;
import com.allobank.splitbill.repository.ExpenseShareRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ExpenseShareService {

    private final ExpenseShareRepository repository;
    private final ExpenseService expenseService;

    public List<ExpenseShareResponse> list(UUID groupId, UUID expenseId, String sessionId) {
        expenseService.get(groupId, expenseId, sessionId);
        List<ExpenseShare> shares = repository.findByExpenseId(expenseId);
        List<ExpenseShareResponse> result = new ArrayList<>();
        for (ExpenseShare share : shares) {
            result.add(new ExpenseShareResponse(share));
        }
        return result;
    }

    public ExpenseShareResponse get(UUID groupId, UUID expenseId, UUID participantId, String sessionId) {
        expenseService.get(groupId, expenseId, sessionId);
        ExpenseShareId id = new ExpenseShareId(expenseId, participantId);
        ExpenseShare share = repository.findById(id).orElse(null);
        if (share == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense share not found");
        }
        return new ExpenseShareResponse(share);
    }
}
