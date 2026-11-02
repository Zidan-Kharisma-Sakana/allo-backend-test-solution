package com.allobank.splitbill.service;

import com.allobank.splitbill.dto.BillGroupResponse;
import com.allobank.splitbill.model.BillGroup;
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
}
