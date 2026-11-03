package com.allobank.splitbill.service;

import static com.allobank.splitbill.exception.ApplicationException.invalidRequest;

import com.allobank.splitbill.dto.CreateExpenseRequest;
import com.allobank.splitbill.service.split.SplitCalculator;
import com.allobank.splitbill.dto.ExpenseResponse;
import com.allobank.splitbill.exception.ApplicationException;
import com.allobank.splitbill.model.BillGroup;
import com.allobank.splitbill.model.Expense;
import com.allobank.splitbill.model.ExpenseShare;
import com.allobank.splitbill.model.Participant;
import com.allobank.splitbill.repository.ExpenseRepository;
import com.allobank.splitbill.repository.ExpenseShareRepository;
import com.allobank.splitbill.repository.ParticipantRepository;
import com.allobank.splitbill.util.DatabaseConflict;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
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
public class ExpenseService {

    private final ExpenseRepository repository;
    private final BillGroupService access;
    private final ParticipantRepository participantRepository;
    private final ExpenseShareRepository shareRepository;
    private final SplitCalculator splitCalculator;
    private final PlatformTransactionManager transactionManager;

    public List<ExpenseResponse> list(UUID groupId, String sessionId) {
        log.info("Listing expenses");
        access.requireGroup(groupId, sessionId);
        List<Expense> items = repository.findByBillGroupIdOrderByCreatedAtAsc(groupId);
        List<ExpenseResponse> result = new ArrayList<>();
        for (Expense expense : items) {
            result.add(new ExpenseResponse(expense));
        }
        return result;
    }

    void requireExpense(UUID groupId, UUID id, String sessionId) {
        access.requireGroup(groupId, sessionId);
        Expense expense = repository.findById(id).orElse(null);
        if (expense == null || !expense.getBillGroup().getId().equals(groupId)) {
            throw new ApplicationException(404, "Expense not found");
        }
    }

    public ExpenseResponse create(UUID groupId, CreateExpenseRequest request, String sessionId, String key) {
        log.info("Creating expense");
        var transaction = new TransactionTemplate(transactionManager);
        try {
            return transaction.execute(status -> createOnce(groupId, request, sessionId, key));
        } catch (DataIntegrityViolationException failure) {
            if (!DatabaseConflict.isConstraint(failure, "expenses_group_request_key_unique")) throw failure;
            log.info("Recovering concurrent expense request");
            // The losing transaction has rolled back. Read the committed winner in a new one.
            return transaction.execute(status -> {
                access.requireGroup(groupId, sessionId);
                Expense previous = repository.findByBillGroupIdAndIdempotencyKey(groupId, key).orElseThrow(() -> failure);
                return new ExpenseResponse(previous);
            });
        }
    }

    private ExpenseResponse createOnce(UUID groupId, CreateExpenseRequest request, String sessionId, String key) {
        BillGroup group = access.requireGroup(groupId, sessionId);
        if (key == null || key.isBlank() || key.length() > 128) {
            log.info("Invalid idempotency key");
            throw invalidRequest();
        }
        Expense saved = repository.findByBillGroupIdAndIdempotencyKey(groupId, key).orElse(null);
        if (saved != null) {
            log.info("Replaying expense request");
            return new ExpenseResponse(saved);
        }
        validateInput(request);
        List<UUID> selectedIds = request.allocations() == null
                ? request.participantIds() : new ArrayList<>(request.allocations().keySet());
        List<Participant> participants = participantRepository.findByBillGroupIdOrderByPositionAsc(groupId);
        Participant payer = null;
        List<Participant> beneficiaries = new ArrayList<>();
        for (Participant participant : participants) {
            if (participant.getId().equals(request.payerId())) {
                payer = participant;
            }
            if (selectedIds.contains(participant.getId())) {
                beneficiaries.add(participant);
            }
        }
        if (payer == null || beneficiaries.size() != selectedIds.size()) {
            log.info("Payer or participants not in group");
            throw invalidRequest();
        }
        log.info("Splitting expense");
        BigDecimal amount = request.amount().setScale(0, RoundingMode.UNNECESSARY);
        List<BigDecimal> amounts;
        if (request.allocations() == null) {
            amounts = splitCalculator.splitEqual(amount, beneficiaries.size());
        } else {
            log.info("Applying exact split");
            amounts = splitCalculator.splitExact(amount,
                    beneficiaries.stream().map(Participant::getId).toList(), request.allocations());
        }
        Expense expense = new Expense(group, payer, amount);
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

    private void validateInput(CreateExpenseRequest request) {
        if (request.amount() == null || request.amount().signum() <= 0 || request.amount().stripTrailingZeros().scale() > 0) {
            log.info("Invalid amount");
            throw invalidRequest();
        }
        if (request.allocations() != null) {
            if (request.allocations().isEmpty() || request.allocations().keySet().stream().anyMatch(id -> id == null)
                    || request.participantIds() != null) {
                log.info("Invalid exact split");
                throw invalidRequest();
            }
        } else if (request.participantIds() == null || request.participantIds().isEmpty()
                || request.participantIds().contains(null)
                || new HashSet<>(request.participantIds()).size() != request.participantIds().size()) {
            log.info("Invalid participants");
            throw invalidRequest();
        }
        if (request.description() != null && request.description().length() > 255) {
            log.info("Description too long");
            throw invalidRequest();
        }
    }

}
