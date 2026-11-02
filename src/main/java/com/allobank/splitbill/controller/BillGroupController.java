package com.allobank.splitbill.controller;

import com.allobank.splitbill.dto.BillGroupResponse;
import com.allobank.splitbill.service.BillGroupService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
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
}
