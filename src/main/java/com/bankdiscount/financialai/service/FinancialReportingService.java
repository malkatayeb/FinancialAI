package com.bankdiscount.financialai.service;

import com.bankdiscount.financialai.dto.CategorySpendingResponse;
import com.bankdiscount.financialai.dto.DashboardSummaryResponse;
import com.bankdiscount.financialai.dto.TransactionResponse;
import com.bankdiscount.financialai.entity.Budget;
import com.bankdiscount.financialai.entity.Transaction;
import com.bankdiscount.financialai.repository.BudgetRepository;
import com.bankdiscount.financialai.repository.TransactionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FinancialReportingService {

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;

    @Transactional(readOnly = true)
    public List<TransactionResponse> listAllTransactions() {
        return transactionRepository.findAllByOrderByDateDescIdDesc().stream()
                .map(this::toTransactionResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse dashboardSummary() {
        YearMonth month = YearMonth.now(ZoneId.systemDefault());
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        BigDecimal totalBalance = transactionRepository.sumAllAmounts();
        BigDecimal monthlyExpenses = transactionRepository.sumMonthlyExpenses(start, end);
        double health = computeBudgetHealth(start, end);

        return DashboardSummaryResponse.builder()
                .totalBalance(toDouble(totalBalance))
                .monthlyExpenses(toDouble(monthlyExpenses))
                .budgetHealth(health)
                .build();
    }

    @Transactional(readOnly = true)
    public List<CategorySpendingResponse> spendingByCategoryForMonth() {
        YearMonth month = YearMonth.now(ZoneId.systemDefault());
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        return transactionRepository.sumSpendingByCategoryForMonth(start, end).stream()
                .map(row -> CategorySpendingResponse.builder()
                        .category((String) row[0])
                        .spent(toDouble((BigDecimal) row[1]))
                        .build())
                .filter(cs -> cs.getSpent() > 0.005d)
                .sorted((a, b) -> Double.compare(b.getSpent(), a.getSpent()))
                .collect(Collectors.toList());
    }

    /** Same calendar month as dashboard (for AI tool alignment). */
    public YearMonth currentReportingMonth() {
        return YearMonth.now(ZoneId.systemDefault());
    }

    public LocalDate reportingMonthStart(YearMonth ym) {
        return ym.atDay(1);
    }

    public LocalDate reportingMonthEnd(YearMonth ym) {
        return ym.atEndOfMonth();
    }

    private double computeBudgetHealth(LocalDate start, LocalDate end) {
        List<Budget> budgets = budgetRepository.findAllByOrderByCategoryAsc();
        if (budgets.isEmpty()) {
            return 100.0;
        }
        double sum = 0;
        int n = 0;
        for (Budget b : budgets) {
            BigDecimal spent = transactionRepository.sumMonthlySpendingInCategory(
                    b.getCategory(), start, end);
            BigDecimal limit = b.getMonthlyLimit();
            if (limit == null || limit.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            double ratio = spent.divide(limit, 4, RoundingMode.HALF_UP).doubleValue();
            double piece = Math.max(0.0, Math.min(100.0, 100.0 * (1.0 - Math.min(1.0, ratio))));
            sum += piece;
            n++;
        }
        if (n == 0) {
            return 100.0;
        }
        return round2(sum / n);
    }

    private TransactionResponse toTransactionResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .date(t.getDate() != null ? t.getDate().format(ISO_DATE) : null)
                .description(t.getDescription())
                .amount(toDouble(t.getAmount()))
                .category(t.getCategory())
                .status(t.getStatus())
                .build();
    }

    private static double toDouble(BigDecimal v) {
        if (v == null) {
            return 0.0;
        }
        return v.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
