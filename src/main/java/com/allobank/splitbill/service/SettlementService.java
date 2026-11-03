package com.allobank.splitbill.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

    private final BillGroupService groupService;

    public String get(UUID groupId, String sessionId) {
        log.info("Calculating settlement");
        groupService.get(groupId, sessionId);
        // Settlement calculation will be added in iteration 2.
        return "";
    }
}
