package com.allobank.splitbill.repository;

import com.allobank.splitbill.model.Expense;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    List<Expense> findByBillGroupIdOrderByCreatedAtAsc(UUID billGroupId);
}
