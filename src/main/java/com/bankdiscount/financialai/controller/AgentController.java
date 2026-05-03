package com.bankdiscount.financialai.controller;

import com.bankdiscount.financialai.dto.ChatRequest;
import com.bankdiscount.financialai.dto.ChatResponse;
import com.bankdiscount.financialai.service.FinancialAgent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AgentController {

    private final FinancialAgent financialAgent;

    /** Preferred: JSON body `{ "message", "sessionId?" }`. */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chatPost(@RequestBody(required = false) ChatRequest request) {
        String message = request != null ? request.getMessage() : null;
        String sessionId = request != null ? request.getSessionId() : null;
        return runChat(message, sessionId);
    }

    /**
     * Same chat flow for frontends that call {@code GET /api/chat?message=...&sessionId=...}
     * (avoids 405 when the UI uses GET).
     */
    @GetMapping("/chat")
    public ResponseEntity<ChatResponse> chatGet(
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String msg,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String text,
            @RequestParam(required = false) String sessionId) {
        String resolved = firstNonBlank(message, msg, q, text);
        return runChat(resolved, sessionId);
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (StringUtils.hasText(v)) {
                return v;
            }
        }
        return null;
    }

    private ResponseEntity<ChatResponse> runChat(String message, String sessionId) {
        if (!StringUtils.hasText(message)) {
            return ResponseEntity.badRequest()
                    .body(ChatResponse.builder()
                            .reply(
                                    "No user text was received. Browsers do not send a JSON body with GET — "
                                            + "use POST /api/chat with body {\"message\":\"your question\"}, "
                                            + "or GET /api/chat?message=your+question (aliases: msg, q, text).")
                            .sessionId(safeSessionId(sessionId))
                            .build());
        }
        String memoryId = safeSessionId(sessionId);
        String reply = financialAgent.chat(memoryId, message.trim());
        return ResponseEntity.ok(ChatResponse.builder().reply(reply).sessionId(memoryId).build());
    }

    private static String safeSessionId(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return "default";
        }
        return sessionId.trim();
    }
}
