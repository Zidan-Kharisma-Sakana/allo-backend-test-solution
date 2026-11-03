package com.allobank.splitbill.dto;

import com.allobank.splitbill.model.BillGroup;
import java.util.UUID;
import java.time.LocalDateTime;

public record BillGroupResponse(UUID id, String name, String currency, LocalDateTime createdAt) {

    public BillGroupResponse(BillGroup value) {
        this(value.getId(), value.getName(), value.getCurrency(), value.getCreatedAt());
    }
}
