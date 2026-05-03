package com.bankdiscount.financialai.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService(tools = "bankingService")
public interface FinancialAgent {

    @SystemMessage(
            """
            You are a professional financial advisor for Bank Discount. Use the provided tools to fetch real account data. Always be precise with numbers. If a user is over budget, notify them politely with the exact difference. Be concise.
            """)
    String chat(@MemoryId String memoryId, @UserMessage String userMessage);
}
