package com.allobank.splitbill.dto;

import com.allobank.splitbill.model.Participant;
import java.time.LocalDateTime;
import java.util.UUID;

public record ParticipantResponse(UUID id, UUID billGroupId, String name, int position, LocalDateTime createdAt) {

    public ParticipantResponse(Participant value) {
        this(value.getId(), value.getBillGroup().getId(), value.getName(), value.getPosition(), value.getCreatedAt());
    }
}
