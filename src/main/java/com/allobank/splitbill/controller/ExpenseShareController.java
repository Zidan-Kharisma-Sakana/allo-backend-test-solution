package com.allobank.splitbill.controller;

import com.allobank.splitbill.dto.ExpenseShareResponse;
import com.allobank.splitbill.service.ExpenseShareService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bill-groups/{groupId}/expenses/{expenseId}/shares")
@RequiredArgsConstructor
public class ExpenseShareController {

    private final ExpenseShareService service;

    @GetMapping
    public List<ExpenseShareResponse> list(@PathVariable UUID groupId, @PathVariable UUID expenseId, HttpSession session) {
        return service.list(groupId, expenseId, session.getId());
    }
}
