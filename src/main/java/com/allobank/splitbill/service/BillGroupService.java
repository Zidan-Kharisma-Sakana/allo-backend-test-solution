package com.allobank.splitbill.service;

import com.allobank.splitbill.dto.BillGroupResponse;
import com.allobank.splitbill.model.BillGroup;
import com.allobank.splitbill.model.Participant;
import com.allobank.splitbill.dto.CreateBillGroupRequest;
import com.allobank.splitbill.dto.UpdateBillGroupRequest;
import com.allobank.splitbill.repository.ParticipantRepository;
import com.allobank.splitbill.repository.BillGroupRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class BillGroupService {

    private final BillGroupRepository repository;
    private final ParticipantRepository participantRepository;
    private final InviteCodeGenerator codes;
    private final org.springframework.jdbc.core.JdbcTemplate jdbc;
    private final org.springframework.transaction.PlatformTransactionManager transactionManager;

    public List<BillGroupResponse> list(String sessionId) {
        List<BillGroup> groups = repository.findAccessibleBySessionId(sessionId);
        List<BillGroupResponse> result = new ArrayList<>();
        for (BillGroup group : groups) {
            result.add(new BillGroupResponse(group));
        }
        return result;
    }

    public BillGroupResponse get(UUID groupId, String sessionId) {
        BillGroup group = repository.findAccessibleById(groupId, sessionId).orElse(null);
        if (group == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill group not found");
        }
        return new BillGroupResponse(group);
    }

    public BillGroupResponse create(CreateBillGroupRequest request, String sessionId) {
        var transaction = new org.springframework.transaction.support.TransactionTemplate(transactionManager);
        for (int attempt = 0; attempt < 10; attempt++) {
            try {
                return transaction.execute(status -> createOnce(request, sessionId));
            } catch (org.springframework.dao.DataIntegrityViolationException failure) {
                if (!DatabaseConflict.isConstraint(failure, "bill_groups_invite_code_unique")) throw failure;
            }
        }
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Could not generate invite code, try again");
    }

    private BillGroupResponse createOnce(CreateBillGroupRequest request, String sessionId) {
        if (request.name() == null || request.name().isBlank() || request.name().trim().length() > 150) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group name is required, maximum 150 characters");
        }
        if (request.participants() == null || request.participants().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Add at least one participant");
        }
        for (String name : request.participants()) {
            if (name == null || name.isBlank() || name.trim().length() > 100) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Participant name is required, maximum 100 characters");
            }
        }
        BillGroup group = new BillGroup(request.name().trim(), sessionId);
        group.setInviteCode(codes.generate());
        group = repository.saveAndFlush(group);
        List<Participant> participants = new ArrayList<>();
        for (int i = 0; i < request.participants().size(); i++) {
            participants.add(new Participant(group, request.participants().get(i).trim(), i));
        }
        participantRepository.saveAll(participants);
        jdbc.update("INSERT INTO group_members (bill_group_id, session_id) VALUES (?, ?) ON CONFLICT DO NOTHING", group.getId(), sessionId);
        return new BillGroupResponse(group);
    }

    public BillGroupResponse join(String code, String sessionId) {
        if (code == null || !code.matches("[A-Z0-9]{6}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use 6 uppercase letters or numbers");
        }
        BillGroup group = repository.findByInviteCode(code).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Invite code not found"));
        jdbc.update("INSERT INTO group_members (bill_group_id, session_id) VALUES (?, ?) ON CONFLICT DO NOTHING", group.getId(), sessionId);
        return new BillGroupResponse(group);
    }

    public BillGroupResponse update(UUID groupId, UpdateBillGroupRequest request, String sessionId) {
        BillGroup group = repository.findAccessibleById(groupId, sessionId).orElse(null);
        if (group == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill group not found");
        }
        if (request.name() == null || request.name().isBlank() || request.name().trim().length() > 150) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group name is required, maximum 150 characters");
        }
        group.setName(request.name().trim());
        return new BillGroupResponse(repository.save(group));
    }
}
