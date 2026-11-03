package com.allobank.splitbill.service;

import com.allobank.splitbill.dto.BillGroupResponse;
import com.allobank.splitbill.model.BillGroup;
import com.allobank.splitbill.model.Participant;
import com.allobank.splitbill.dto.CreateBillGroupRequest;
import com.allobank.splitbill.repository.ParticipantRepository;
import com.allobank.splitbill.repository.BillGroupRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillGroupService {
    private final com.allobank.splitbill.repository.GroupMemberRepository members;

    private final BillGroupRepository repository;
    private final ParticipantRepository participantRepository;

    public List<BillGroupResponse> list(String sessionId) {
        log.info("Listing bill groups");
        List<BillGroup> groups = repository.findAccessibleBySessionId(sessionId);
        List<BillGroupResponse> result = new ArrayList<>();
        for (BillGroup group : groups) {
            result.add(new BillGroupResponse(group));
        }
        return result;
    }

    public BillGroupResponse get(UUID groupId, String sessionId) {
        log.info("Fetching bill group");
        BillGroup group = repository.findAccessibleById(groupId, sessionId).orElse(null);
        if (group == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill group not found");
        }
        return new BillGroupResponse(group);
    }

    public BillGroupResponse create(CreateBillGroupRequest request, String sessionId) {
        log.info("Creating bill group");
        if (request.name() == null || request.name().isBlank() || request.name().trim().length() > 150) {
            log.info("Invalid group name");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
        }
        if (request.participants() == null || request.participants().isEmpty()) {
            log.info("Missing participants");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
        }
        for (String name : request.participants()) {
            if (name == null || name.isBlank() || name.trim().length() > 100) {
                log.info("Invalid participant name");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
            }
        }
        BillGroup group = repository.save(new BillGroup(request.name().trim()));
        List<Participant> participants = new ArrayList<>();
        for (int i = 0; i < request.participants().size(); i++) {
            participants.add(new Participant(group, request.participants().get(i).trim(), i));
        }
        participantRepository.saveAll(participants);
        members.add(group.getId(), sessionId);
        return new BillGroupResponse(group);
    }
}
