package com.bankdiscount.financialai.controller;

import com.bankdiscount.financialai.dto.CategorySpendingResponse;
import com.bankdiscount.financialai.service.FinancialReportingService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/budget")
@RequiredArgsConstructor
public class BudgetSpendingController {

    private final FinancialReportingService financialReportingService;

    @GetMapping("/spending")
    public ResponseEntity<List<CategorySpendingResponse>> spendingByCategory() {
        return ResponseEntity.ok(financialReportingService.spendingByCategoryForMonth());
    }
}
