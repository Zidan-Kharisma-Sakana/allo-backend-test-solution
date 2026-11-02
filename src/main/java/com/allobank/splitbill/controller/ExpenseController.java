package com.allobank.splitbill.controller;

import com.allobank.splitbill.dto.ExpenseResponse;
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
}
