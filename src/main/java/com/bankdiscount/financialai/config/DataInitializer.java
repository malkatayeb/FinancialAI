package com.bankdiscount.financialai.config;

import com.bankdiscount.financialai.entity.Budget;
import com.bankdiscount.financialai.entity.Transaction;
import com.bankdiscount.financialai.repository.BudgetRepository;
import com.bankdiscount.financialai.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;

    @Bean
    CommandLineRunner seedFinancialData() {
        return args -> {
            if (transactionRepository.count() > 0) {
                return;
            }

            LocalDate may = LocalDate.of(2026, 5, 1);

            transactionRepository.save(tx(
                    may.plusDays(0),
                    new BigDecimal("19250.00"),
                    "Salary — Bank Discount payroll",
                    "Income",
                    "Completed"));
            transactionRepository.save(tx(
                    may.plusDays(2),
                    new BigDecimal("-5400.00"),
                    "Rent — landlord transfer",
                    "Housing",
                    "Completed"));
            transactionRepository.save(tx(
                    may.plusDays(3),
                    new BigDecimal("-198.40"),
                    "Wolt — dinners",
                    "Dining",
                    "Completed"));
            transactionRepository.save(tx(
                    may.plusDays(4),
                    new BigDecimal("-287.60"),
                    "Shufersal — weekly groceries",
                    "Groceries",
                    "Completed"));
            transactionRepository.save(tx(
                    may.plusDays(5),
                    new BigDecimal("-54.90"),
                    "Netflix subscription",
                    "Entertainment",
                    "Completed"));
            transactionRepository.save(tx(
                    may.plusDays(6),
                    new BigDecimal("-62.00"),
                    "Gett — airport run",
                    "Transport",
                    "Completed"));
            transactionRepository.save(tx(
                    may.plusDays(8),
                    new BigDecimal("-124.50"),
                    "Wolt — office lunch",
                    "Dining",
                    "Pending"));
            transactionRepository.save(tx(
                    may.plusDays(10),
                    new BigDecimal("-412.30"),
                    "Shufersal — bulk shop",
                    "Groceries",
                    "Completed"));
            transactionRepository.save(tx(
                    may.plusDays(12),
                    new BigDecimal("-48.00"),
                    "Gett — city rides",
                    "Transport",
                    "Completed"));
            transactionRepository.save(tx(
                    may.plusDays(14),
                    new BigDecimal("-59.99"),
                    "Netflix + add-on",
                    "Entertainment",
                    "Pending"));

            budgetRepository.save(Budget.builder().category("Dining").monthlyLimit(new BigDecimal("900.00")).build());
            budgetRepository.save(Budget.builder().category("Groceries").monthlyLimit(new BigDecimal("1400.00")).build());
            budgetRepository.save(Budget.builder().category("Housing").monthlyLimit(new BigDecimal("5800.00")).build());
            budgetRepository.save(Budget.builder().category("Entertainment").monthlyLimit(new BigDecimal("250.00")).build());
            budgetRepository.save(Budget.builder().category("Transport").monthlyLimit(new BigDecimal("400.00")).build());
        };
    }

    private static Transaction tx(LocalDate date, BigDecimal amount, String description, String category, String status) {
        return Transaction.builder()
                .date(date)
                .amount(amount)
                .description(description)
                .category(category)
                .status(status)
                .build();
    }
}
