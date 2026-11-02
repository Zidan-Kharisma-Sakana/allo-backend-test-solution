package com.allobank.splitbill.repository;

import com.allobank.splitbill.model.ExpenseShare;
import com.allobank.splitbill.model.ExpenseShareId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseShareRepository extends JpaRepository<ExpenseShare, ExpenseShareId> {

    List<ExpenseShare> findByExpenseId(UUID expenseId);

    List<ExpenseShare> findByExpense_BillGroup_Id(UUID billGroupId);
}
