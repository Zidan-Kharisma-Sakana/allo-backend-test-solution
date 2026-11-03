package com.allobank.splitbill.repository;

import com.allobank.splitbill.model.Expense;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    Optional<Expense> findByBillGroupIdAndIdempotencyKey(UUID billGroupId, String idempotencyKey);

    List<Expense> findByBillGroupIdOrderByCreatedAtAsc(UUID billGroupId);
}
