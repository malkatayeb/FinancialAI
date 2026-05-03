package com.bankdiscount.financialai.controller;

import com.bankdiscount.financialai.dto.DashboardSummaryResponse;
import com.bankdiscount.financialai.service.FinancialReportingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final FinancialReportingService financialReportingService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> summary() {
        return ResponseEntity.ok(financialReportingService.dashboardSummary());
    }
}
