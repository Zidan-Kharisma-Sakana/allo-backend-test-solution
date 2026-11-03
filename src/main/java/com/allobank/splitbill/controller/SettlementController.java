package com.allobank.splitbill.controller;

import com.allobank.splitbill.dto.SettlementResponse;
import com.allobank.splitbill.service.SettlementService;
import jakarta.servlet.http.HttpSession;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bill-groups/{groupId}/settlement")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService service;

    @GetMapping
    public SettlementResponse get(@PathVariable UUID groupId, HttpSession session) {
        return service.get(groupId, session.getId());
    }
}
