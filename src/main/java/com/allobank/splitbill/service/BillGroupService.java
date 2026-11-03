package com.allobank.splitbill.service;

import static com.allobank.splitbill.exception.ApplicationException.invalidRequest;

import com.allobank.splitbill.dto.BillGroupResponse;
import com.allobank.splitbill.dto.CreateBillGroupRequest;
import com.allobank.splitbill.exception.ApplicationException;
import com.allobank.splitbill.model.BillGroup;
import com.allobank.splitbill.model.Participant;
import com.allobank.splitbill.repository.BillGroupRepository;
import com.allobank.splitbill.repository.GroupMemberRepository;
import com.allobank.splitbill.repository.ParticipantRepository;
import com.allobank.splitbill.util.DatabaseConflict;
import com.allobank.splitbill.util.InviteCodeGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillGroupService {

    private final BillGroupRepository repository;
    private final ParticipantRepository participantRepository;
    private final InviteCodeGenerator codes;
    private final GroupMemberRepository members;
    private final PlatformTransactionManager transactionManager;

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
        return new BillGroupResponse(requireGroup(groupId, sessionId));
    }

    public BillGroupResponse create(CreateBillGroupRequest request, String sessionId) {
        log.info("Creating bill group");
        var transaction = new TransactionTemplate(transactionManager);
        for (int attempt = 0; attempt < 10; attempt++) {
            try {
                return transaction.execute(status -> createOnce(request, sessionId));
            } catch (DataIntegrityViolationException failure) {
                if (!DatabaseConflict.isConstraint(failure, "bill_groups_invite_code_unique")) throw failure;
                log.info("Invite code collision on attempt {} of 10", attempt + 1);
            }
        }
        log.error("Invite retries exhausted");
        throw new ApplicationException(503, "Something went wrong, try again");
    }

    private BillGroupResponse createOnce(CreateBillGroupRequest request, String sessionId) {
        if (request.name() == null || request.name().isBlank() || request.name().trim().length() > 150) {
            log.info("Invalid group name");
            throw invalidRequest();
        }
        if (request.participants() == null || request.participants().isEmpty()) {
            log.info("Missing participants");
            throw invalidRequest();
        }
        for (String name : request.participants()) {
            if (name == null || name.isBlank() || name.trim().length() > 100) {
                log.info("Invalid participant name");
                throw invalidRequest();
            }
        }
        BillGroup group = new BillGroup(request.name().trim());
        group.setInviteCode(codes.generate());
        group = repository.saveAndFlush(group);
        List<Participant> participants = new ArrayList<>();
        for (int i = 0; i < request.participants().size(); i++) {
            participants.add(new Participant(group, request.participants().get(i).trim(), i));
        }
        participantRepository.saveAll(participants);
        members.add(group.getId(), sessionId);
        return new BillGroupResponse(group);
    }

    public BillGroupResponse join(String code, String sessionId) {
        log.info("Joining bill group");
        if (code == null || !code.matches("[A-Z0-9]{6}")) {
            log.info("Invalid invite code");
            throw invalidRequest();
        }
        BillGroup group = repository.findByInviteCode(code).orElseThrow(() ->
                new ApplicationException(404, "Invite code not found"));
        members.add(group.getId(), sessionId);
        return new BillGroupResponse(group);
    }

    public BillGroup requireGroup(UUID groupId, String sessionId) {
        return repository.findAccessibleById(groupId, sessionId)
                .orElseThrow(() -> new ApplicationException(404, "Bill group not found"));
    }
}
