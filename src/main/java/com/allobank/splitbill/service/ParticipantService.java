package com.allobank.splitbill.service;

import com.allobank.splitbill.dto.ParticipantResponse;
import com.allobank.splitbill.model.Participant;
import com.allobank.splitbill.repository.ParticipantRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final ParticipantRepository repository;
    private final BillGroupService billGroupService;

    public List<ParticipantResponse> list(UUID groupId, String sessionId) {
        log.info("Listing participants");
        billGroupService.get(groupId, sessionId);
        List<Participant> particpants = repository.findByBillGroupIdOrderByPositionAsc(groupId);
        List<ParticipantResponse> result = new ArrayList<>();
        for (Participant participant : particpants) {
            result.add(new ParticipantResponse(participant));
        }
        return result;
    }
}
