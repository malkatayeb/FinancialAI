package com.bankdiscount.financialai.repository;

import com.bankdiscount.financialai.entity.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findAllByOrderByDateDescIdDesc();

    List<Transaction> findByCategoryIgnoreCaseOrderByDateDesc(String category);

    @Query("select coalesce(sum(t.amount), 0) from Transaction t")
    BigDecimal sumAllAmounts();

    @Query("select coalesce(sum(t.amount), 0) from Transaction t where lower(t.category) = lower(:category)")
    BigDecimal sumAmountsByCategory(String category);

    @Query(
            "select coalesce(-sum(case when t.amount < 0 then t.amount else 0 end), 0) from Transaction t "
                    + "where t.date >= :start and t.date <= :end")
    BigDecimal sumMonthlyExpenses(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query(
            "select t.category, coalesce(-sum(case when t.amount < 0 then t.amount else 0 end), 0) from Transaction t "
                    + "where t.date >= :start and t.date <= :end group by t.category")
    List<Object[]> sumSpendingByCategoryForMonth(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query(
            "select coalesce(-sum(case when t.amount < 0 then t.amount else 0 end), 0) from Transaction t "
                    + "where lower(t.category) = lower(:category) and t.date >= :start and t.date <= :end")
    BigDecimal sumMonthlySpendingInCategory(
            @Param("category") String category, @Param("start") LocalDate start, @Param("end") LocalDate end);
}
