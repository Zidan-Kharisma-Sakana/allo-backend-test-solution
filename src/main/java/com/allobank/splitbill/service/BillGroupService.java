package com.allobank.splitbill.service;

import com.allobank.splitbill.dto.BillGroupResponse;
import com.allobank.splitbill.model.BillGroup;
import com.allobank.splitbill.repository.BillGroupRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
}
