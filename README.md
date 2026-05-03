# Financial AI Agent (Bank Discount)

Secure, containerized **Java 21** + **Spring Boot 3.3** backend that exposes a conversational **financial advisor** backed by **LangChain4j** and a **local Ollama** LLM. Domain data (transactions and budgets) lives in an in-memory **H2** database; the model answers using **tool calls** against real repository state.

## Tool-calling architecture

1. **`FinancialAgent`** (`com.bankdiscount.financialai.service.FinancialAgent`) is a LangChain4j **`@AiService`** interface. It declares the advisor **system message**, accepts a **`@MemoryId`** (conversation key) and **`@UserMessage`**, and is wired with **`tools = "bankingService"`** (the Spring bean name for `BankingService`) so the runtime can register those `@Tool` methods. The POM includes **`langchain4j-spring-boot-starter`** alongside **`langchain4j-ollama-spring-boot-starter`** so `@AiService`, chat memory, and Ollama are all on the classpath.
2. **`BankingService`** (`com.bankdiscount.financialai.service.BankingService`) is a Spring **`@Service`** whose methods are annotated with **`@Tool`**. Each tool has a short natural-language description so the model knows when to invoke it:
   - **`getBalance()`** — sums all transaction amounts (credits minus debits) and returns a plain decimal string.
   - **`getTransactionsByCategory(String category)`** — returns recent transactions for a category as readable lines.
   - **`checkBudgetStatus(String category)`** — compares category spend (absolute flow for debits) to the configured **`Budget.monthlyLimit`** and reports remaining headroom or **exact overage**.
3. **Memory** — `LangChain4jConfig` exposes a **`ChatMemoryProvider`** that builds **`MessageWindowChatMemory.withMaxMessages(20)`** per `memoryId`. The REST API forwards an optional **`sessionId`** (default `default`) so multiple chats stay isolated.
4. **Flow** — Client POST **`/api/chat`** → controller calls **`financialAgent.chat(memoryId, message)`** → LangChain4j orchestrates the Ollama chat model, may emit one or more tool calls → **`BankingService`** runs JPA queries → tool results are fed back to the model → final natural-language reply is returned.

This keeps **numeric truth** in the database layer while the LLM handles language and synthesis.

## Prerequisites

- **Docker** and **Docker Compose** (recommended path), or local **JDK 21** (Amazon Corretto is used in the Dockerfile) + **Maven** + **Ollama** on port **11434**.
- The compose file pulls **`llama3.2`** once the Ollama daemon is reachable; the **health check** waits until that model appears in `ollama list` before starting the Spring container.

## Run with Docker Compose

From the project root:

```bash
docker compose up --build
```

- **Backend:** `http://localhost:8080`
- **Ollama:** `http://localhost:11434`
- First startup may take several minutes while **`ollama pull llama3.2`** completes; the backend waits for the Ollama health check.

### Example request

```bash
curl -s -X POST http://localhost:8080/api/chat ^
  -H "Content-Type: application/json" ^
  -d "{\"message\":\"What is my balance and am I over budget on Food?\",\"sessionId\":\"user-1\"}"
```

Response shape:

```json
{
  "reply": "...",
  "sessionId": "user-1"
}
```

## Run locally (without Docker for the app)

1. Start Ollama and ensure **`llama3.2`** is available (`ollama pull llama3.2`).
2. `mvn spring-boot-run`  
   Uses `application.yml` with **`langchain4j.ollama.chat-model.base-url: http://localhost:11434`**.

## API & CORS

- **`POST /api/chat`** — body: `{ "message": "string", "sessionId": "optional" }`.
- **`WebMvcConfig`** enables **CORS** for **`/api/**`** with permissive origins/methods/headers so a **Lovable** (or any) frontend on another origin can call the API during development.

## Data seeding

On first empty database, **`DataInitializer`** loads realistic dummy rows (salary, rent, Wolt, grocery, Netflix, utilities) and matching **budgets**. H2 console is enabled for local inspection (`/h2-console` with JDBC URL from `application.yml`).

## Project layout

| Package / area | Role |
|----------------|------|
| `entity` | JPA **`Transaction`**, **`Budget`** |
| `repository` | Spring Data JPA |
| `service` | **`BankingService`** (tools), **`FinancialAgent`** (`@AiService`) |
| `controller` | **`AgentController`** — `/api/chat` |
| `config` | CORS, LangChain4j memory, data seed |
| `dto` | **`ChatRequest`**, **`ChatResponse`** |

## Troubleshooting

If **`mvn package`** fails with **`Could not transfer artifact`** / **`Connect timed out`** to an internal mirror (for example a corporate Nexus with **`mirrorOf=*`** in your user `~/.m2/settings.xml`), this repository includes **`.mvn/maven.config`**, which tells Maven to use **`.mvn/central-settings.xml`** (Maven Central only) when you run **`mvn` from the project root**. That bypasses the broken mirror for this build while still using your normal **`~/.m2/repository`** cache.

If your IDE still resolves against the corporate mirror, point it at the same **`central-settings.xml`** or run **`mvn`** from the terminal here. You can delete **`.mvn/maven.config`** if you prefer to use only your global settings.

## Security note

CORS is wide open for developer convenience. Lock this down (specific origins, credentials policy) before any production deployment.
