package com.allobank.splitbill.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final BillGroupService groupService;

    public String get(UUID groupId, String sessionId) {
        groupService.get(groupId, sessionId);
        // Settlement calculation will be added in iteration 2.
        return "";
    }
}
