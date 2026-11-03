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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository repository;
    private final BillGroupService billGroupService;
    private final BillGroupRepository groupRepository;
    private final ParticipantRepository participantRepository;
    private final ExpenseShareRepository shareRepository;
    private final EqualSplitCalculator splitCalculator;
    private final org.springframework.transaction.PlatformTransactionManager transactionManager;

    public List<ExpenseResponse> list(UUID groupId, String sessionId) {
        log.info("Listing expenses");
        billGroupService.get(groupId, sessionId);
        List<Expense> items = repository.findByBillGroupIdOrderByCreatedAtAsc(groupId);
        List<ExpenseResponse> result = new ArrayList<>();
        for (Expense expense : items) {
            result.add(new ExpenseResponse(expense));
        }
        return result;
    }

    void requireExpense(UUID groupId, UUID id, String sessionId) {
        billGroupService.get(groupId, sessionId);
        Expense expense = repository.findById(id).orElse(null);
        if (expense == null || !expense.getBillGroup().getId().equals(groupId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found");
        }
    }



    public ExpenseResponse create(UUID groupId, CreateExpenseRequest request, String sessionId, String key) {
        log.info("Creating expense");
        var transaction = new org.springframework.transaction.support.TransactionTemplate(transactionManager);
        try {
            return transaction.execute(status -> createOnce(groupId, request, sessionId, key));
        } catch (org.springframework.dao.DataIntegrityViolationException failure) {
            if (!DatabaseConflict.isConstraint(failure, "expenses_group_request_key_unique")) throw failure;
            log.info("Recovering concurrent expense request");
            // The losing transaction has rolled back. Read the committed winner in a new one.
            return transaction.execute(status -> {
                billGroupService.get(groupId, sessionId);
                Expense previous = repository.findByBillGroupIdAndIdempotencyKey(groupId, key).orElseThrow(() -> failure);
                return new ExpenseResponse(previous);
            });
        }
    }

    private ExpenseResponse createOnce(UUID groupId, CreateExpenseRequest request, String sessionId, String key) {
        BillGroup group = groupRepository.findAccessibleById(groupId, sessionId).orElse(null);
        if (group == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill group not found");
        }
        if (request.amount() == null || request.amount().signum() <= 0 || request.amount().stripTrailingZeros().scale() > 2) {
            log.info("Invalid amount");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
        }
        if (request.participantIds() == null || request.participantIds().isEmpty()
                || request.participantIds().contains(null)
                || new HashSet<>(request.participantIds()).size() != request.participantIds().size()) {
            log.info("Invalid participants");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
        }
        if (request.description() != null && request.description().length() > 255) {
            log.info("Description too long");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
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
            log.info("Payer or participants not in group");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
        }
        if (key == null || key.isBlank() || key.length() > 128) {
            log.info("Invalid idempotency key");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
        }
        Expense previous = repository.findByBillGroupIdAndIdempotencyKey(groupId, key).orElse(null);
        if (previous != null) {
            log.info("Replaying expense request");
            return new ExpenseResponse(previous);
        }
        log.info("Splitting expense");
        List<BigDecimal> amounts = splitCalculator.split(request.amount(), beneficiaries.size());
        Expense expense = new Expense(group, payer, request.amount());
        expense.setIdempotencyKey(key);
        expense.setDescription(request.description());
        expense = repository.save(expense);
        List<ExpenseShare> shares = new ArrayList<>();
        for (int i = 0; i < beneficiaries.size(); i++) {
            shares.add(new ExpenseShare(expense, beneficiaries.get(i), amounts.get(i)));
        }
        shareRepository.saveAllAndFlush(shares);
        return new ExpenseResponse(expense);
    }
}
