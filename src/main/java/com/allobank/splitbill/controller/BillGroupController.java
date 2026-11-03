package com.allobank.splitbill.controller;

import com.allobank.splitbill.dto.BillGroupResponse;
import com.allobank.splitbill.dto.CreateBillGroupRequest;
import com.allobank.splitbill.dto.UpdateBillGroupRequest;
import org.springframework.http.HttpStatus;
import com.allobank.splitbill.service.BillGroupService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bill-groups")
@RequiredArgsConstructor
public class BillGroupController {

    private final BillGroupService service;

    @GetMapping
    public List<BillGroupResponse> list(HttpSession session) {
        return service.list(session.getId());
    }

    @GetMapping("/{groupId}")
    public BillGroupResponse get(@PathVariable UUID groupId, HttpSession session) {
        return service.get(groupId, session.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BillGroupResponse create(@RequestBody CreateBillGroupRequest request, HttpSession session) {
        return service.create(request, session.getId());
    }

    @PatchMapping("/{groupId}")
    public BillGroupResponse update(@PathVariable UUID groupId, @RequestBody UpdateBillGroupRequest request, HttpSession session) {
        return service.update(groupId, request, session.getId());
    }
}
