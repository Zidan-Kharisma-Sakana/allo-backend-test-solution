package com.allobank.splitbill.service;

import com.allobank.splitbill.dto.ParticipantResponse;
import com.allobank.splitbill.model.Participant;
import com.allobank.splitbill.repository.ParticipantRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final ParticipantRepository repository;
    private final BillGroupService billGroupService;

    public List<ParticipantResponse> list(UUID groupId, String sessionId) {
        billGroupService.get(groupId, sessionId);
        List<Participant> particpants = repository.findByBillGroupIdOrderByPositionAsc(groupId);
        List<ParticipantResponse> result = new ArrayList<>();
        for (Participant participant : particpants) {
            result.add(new ParticipantResponse(participant));
        }
        return result;
    }

    public ParticipantResponse get(UUID groupId, UUID id, String sessionId) {
        billGroupService.get(groupId, sessionId);
        Participant participant = repository.findById(id).orElse(null);
        if (participant == null || !participant.getBillGroup().getId().equals(groupId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Participant not found");
        }
        return new ParticipantResponse(participant);
    }
}
