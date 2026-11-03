package com.allobank.splitbill.dto;

import com.allobank.splitbill.model.BillGroup;
import java.time.LocalDateTime;
import java.util.UUID;

public record BillGroupResponse(UUID id, String name, String currency, String inviteCode, LocalDateTime createdAt) {

    public BillGroupResponse(BillGroup value) {
        this(value.getId(), value.getName(), value.getCurrency(), value.getInviteCode(), value.getCreatedAt());
    }
}
