package com.bankdiscount.financialai.repository;

import com.bankdiscount.financialai.entity.Budget;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    Optional<Budget> findByCategoryIgnoreCase(String category);

    List<Budget> findAllByOrderByCategoryAsc();
}
