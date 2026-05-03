package com.bankdiscount.financialai.service;

import com.bankdiscount.financialai.entity.Transaction;
import com.bankdiscount.financialai.repository.BudgetRepository;
import com.bankdiscount.financialai.repository.TransactionRepository;
import dev.langchain4j.agent.tool.Tool;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BankingService {

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final FinancialReportingService financialReportingService;

    @Tool("Returns the current account balance as the sum of all posted transactions (credits minus debits).")
    public String getBalance() {
        BigDecimal balance = transactionRepository.sumAllAmounts();
        return balance.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    @Tool("Returns all transactions for a spending or income category, newest first.")
    public String getTransactionsByCategory(String category) {
        List<Transaction> rows = transactionRepository.findByCategoryIgnoreCaseOrderByDateDesc(category);
        if (rows.isEmpty()) {
            return "No transactions found for category: " + category;
        }
        return rows.stream().map(Transaction::toString).collect(Collectors.joining("\n"));
    }

    @Tool("Checks spending in a category against its monthly budget limit for the current calendar month. Use when the user asks about budget, limits, or overspending.")
    public String checkBudgetStatus(String category) {
        YearMonth ym = financialReportingService.currentReportingMonth();
        LocalDate start = financialReportingService.reportingMonthStart(ym);
        LocalDate end = financialReportingService.reportingMonthEnd(ym);

        BigDecimal spent = transactionRepository.sumMonthlySpendingInCategory(category, start, end);

        return budgetRepository
                .findByCategoryIgnoreCase(category)
                .map(budget -> formatBudgetComparison(category, spent, budget.getMonthlyLimit(), ym))
                .orElseGet(() -> "No budget configured for category: "
                        + category
                        + ". Spending in "
                        + ym
                        + " is "
                        + spent.setScale(2, RoundingMode.HALF_UP).toPlainString()
                        + ".");
    }

    private String formatBudgetComparison(String category, BigDecimal spent, BigDecimal limit, YearMonth ym) {
        BigDecimal lim = limit.setScale(2, RoundingMode.HALF_UP);
        BigDecimal s = spent.setScale(2, RoundingMode.HALF_UP);
        BigDecimal remaining = lim.subtract(s);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal over = remaining.negate();
            return "Category "
                    + category
                    + " ("
                    + ym
                    + "): spent "
                    + s.toPlainString()
                    + " versus monthly limit "
                    + lim.toPlainString()
                    + ". Over budget by "
                    + over.setScale(2, RoundingMode.HALF_UP).toPlainString()
                    + ".";
        }
        return "Category "
                + category
                + " ("
                + ym
                + "): spent "
                + s.toPlainString()
                + " versus monthly limit "
                + lim.toPlainString()
                + ". Remaining budget: "
                + remaining.setScale(2, RoundingMode.HALF_UP).toPlainString()
                + ".";
    }
}
