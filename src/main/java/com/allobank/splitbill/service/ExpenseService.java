package com.allobank.splitbill.service;

import com.allobank.splitbill.dto.ExpenseResponse;
import com.allobank.splitbill.model.Expense;
import com.allobank.splitbill.model.ExpenseShare;
import com.allobank.splitbill.model.BillGroup;
import com.allobank.splitbill.model.Participant;
import com.allobank.splitbill.dto.CreateExpenseRequest;
import com.allobank.splitbill.service.EqualSplitCalculator;
import com.allobank.splitbill.repository.BillGroupRepository;
import com.allobank.splitbill.repository.ParticipantRepository;
import com.allobank.splitbill.repository.ExpenseShareRepository;
import java.math.BigDecimal;
import java.util.HashSet;
import com.allobank.splitbill.repository.ExpenseRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository repository;
    private final BillGroupService billGroupService;
    private final BillGroupRepository groupRepository;
    private final ParticipantRepository participantRepository;
    private final ExpenseShareRepository shareRepository;
    private final EqualSplitCalculator splitCalculator;

    public List<ExpenseResponse> list(UUID groupId, String sessionId) {
        billGroupService.get(groupId, sessionId);
        List<Expense> items = repository.findByBillGroupIdOrderByCreatedAtAsc(groupId);
        List<ExpenseResponse> result = new ArrayList<>();
        for (Expense expense : items) {
            result.add(new ExpenseResponse(expense));
        }
        return result;
    }

    public ExpenseResponse get(UUID groupId, UUID id, String sessionId) {
        billGroupService.get(groupId, sessionId);
        Expense expense = repository.findById(id).orElse(null);
        if (expense == null || !expense.getBillGroup().getId().equals(groupId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found");
        }
        return new ExpenseResponse(expense);
    }

    public ExpenseResponse create(UUID groupId, CreateExpenseRequest request, String sessionId) {
        BillGroup group = groupRepository.findByIdAndOwnerSessionId(groupId, sessionId).orElse(null);
        if (group == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill group not found");
        }
        if (request.amount() == null || request.amount().signum() <= 0 || request.amount().stripTrailingZeros().scale() > 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be positive with at most two decimal places");
        }
        if (request.participantIds() == null || request.participantIds().isEmpty()
                || request.participantIds().contains(null)
                || new HashSet<>(request.participantIds()).size() != request.participantIds().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select participants without duplicates");
        }
        if (request.description() != null && request.description().length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description cannot exceed 255 characters");
        }
        List<Participant> participants = participantRepository.findByBillGroupIdOrderByPositionAsc(groupId);
        Participant payer = null;
        List<Participant> beneficiaries = new ArrayList<>();
        for (Participant participant : participants) {
            if (participant.getId().equals(request.payerId())) {
                payer = participant;
            }
            if (request.participantIds().contains(participant.getId())) {
                beneficiaries.add(participant);
            }
        }
        if (payer == null || beneficiaries.size() != request.participantIds().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payer and participants must belong to this group");
        }
        List<BigDecimal> amounts = splitCalculator.split(request.amount(), participants.size());
        Expense expense = new Expense(group, payer, request.amount());
        expense.setDescription(request.description());
        expense = repository.save(expense);
        List<ExpenseShare> shares = new ArrayList<>();
        for (int i = 0; i < beneficiaries.size(); i++) {
            shares.add(new ExpenseShare(expense, beneficiaries.get(i), amounts.get(i)));
        }
        shareRepository.saveAll(shares);
        return new ExpenseResponse(expense);
    }
}
