package com.allobank.splitbill.controller;

import com.allobank.splitbill.dto.ExpenseResponse;
import com.allobank.splitbill.dto.CreateExpenseRequest;
import org.springframework.http.HttpStatus;
import com.allobank.splitbill.service.ExpenseService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bill-groups/{groupId}/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService service;

    @GetMapping
    public List<ExpenseResponse> list(@PathVariable UUID groupId, HttpSession session) {
        return service.list(groupId, session.getId());
    }

    @GetMapping("/{expenseId}")
    public ExpenseResponse get(@PathVariable UUID groupId, @PathVariable UUID expenseId, HttpSession session) {
        return service.get(groupId, expenseId, session.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse create(@PathVariable UUID groupId, @RequestBody CreateExpenseRequest request, @RequestHeader("Idempotency-Key") String key, HttpSession session) {
        return service.create(groupId, request, session.getId(), key);
    }
}
