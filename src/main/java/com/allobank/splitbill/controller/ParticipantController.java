package com.allobank.splitbill.controller;

import com.allobank.splitbill.dto.ParticipantResponse;
import com.allobank.splitbill.service.ParticipantService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bill-groups/{groupId}/participants")
@RequiredArgsConstructor
public class ParticipantController {

    private final ParticipantService service;

    @GetMapping
    public List<ParticipantResponse> list(@PathVariable UUID groupId, HttpSession session) {
        return service.list(groupId, session.getId());
    }
}
