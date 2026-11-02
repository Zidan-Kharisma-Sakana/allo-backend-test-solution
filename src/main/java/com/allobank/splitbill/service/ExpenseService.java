package com.allobank.splitbill.service;

import com.allobank.splitbill.dto.ExpenseResponse;
import com.allobank.splitbill.model.Expense;
import com.allobank.splitbill.repository.ExpenseRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository repository;
    private final BillGroupService billGroupService;

    public List<ExpenseResponse> list(UUID groupId, String sessionId) {
        log.info("Listing expenses");
        billGroupService.get(groupId, sessionId);
        List<Expense> items = repository.findByBillGroupIdOrderByCreatedAtAsc(groupId);
        List<ExpenseResponse> result = new ArrayList<>();
        for (Expense expense : items) {
            result.add(new ExpenseResponse(expense));
        }
        return result;
    }

    void requireExpense(UUID groupId, UUID id, String sessionId) {
        billGroupService.get(groupId, sessionId);
        Expense expense = repository.findById(id).orElse(null);
        if (expense == null || !expense.getBillGroup().getId().equals(groupId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found");
        }

    }
}
