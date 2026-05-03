package com.bankdiscount.financialai.controller;

import com.bankdiscount.financialai.dto.TransactionResponse;
import com.bankdiscount.financialai.service.FinancialReportingService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final FinancialReportingService financialReportingService;

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> listTransactions() {
        return ResponseEntity.ok(financialReportingService.listAllTransactions());
    }
}
