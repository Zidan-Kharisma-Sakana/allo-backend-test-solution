package com.allobank.splitbill.service;

import com.allobank.splitbill.dto.BillGroupResponse;
import com.allobank.splitbill.model.BillGroup;
import com.allobank.splitbill.repository.BillGroupRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BillGroupService {

    private final BillGroupRepository repository;

    public List<BillGroupResponse> list(String sessionId) {
        List<BillGroup> groups = repository.findByOwnerSessionId(sessionId);
        List<BillGroupResponse> result = new ArrayList<>();
        for (BillGroup group : groups) {
            result.add(new BillGroupResponse(group));
        }
        return result;
    }
}
