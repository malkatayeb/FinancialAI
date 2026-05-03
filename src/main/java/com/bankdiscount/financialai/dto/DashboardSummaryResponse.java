package com.bankdiscount.financialai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {

    private double totalBalance;
    private double monthlyExpenses;
    /** 0–100: higher means budgets are healthier (more headroom vs limits) for the month. */
    private double budgetHealth;
}
