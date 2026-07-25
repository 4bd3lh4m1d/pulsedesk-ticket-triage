# PulseDesk — Ticket Triage

PulseDesk is a comment-to-ticket triage backend built for an IBM internship exercise. Users submit free-text feedback comments; an AI analysis step decides whether each comment describes an actionable problem, and if so, automatically creates a support ticket with a title, category, priority, and one-sentence summary.

**Deployed URL:** _to be added after deployment_

## Tech stack

- Java 25
- Spring Boot (Spring Framework 7, built with Gradle)
- Embedded H2 database (in-memory)
- Hugging Face Inference Providers chat completions API (`Qwen/Qwen3-4B-Instruct-2507`)

## Architecture

Classic layered design:

```
Controller  →  Service  →  Repository (Spring Data JPA / H2)
                  │
                  └─→  AiAnalysisService (interface)
                          ├─ HuggingFaceAiAnalysisService   (default profile)
                          └─ StubAnalysisService            (profile: stub)
```

`AiAnalysisService` is the seam: the real implementation calls the Hugging Face chat completions endpoint and parses a strict JSON verdict out of the model's reply; the stub is a deterministic keyword matcher. Which one is active is chosen by Spring profile, so the rest of the application never knows the difference.

When a comment is posted, it is stored immediately, then analyzed. The comment's status ends as `ANALYZED` (ticket created if warranted) or `ANALYSIS_FAILED` (AI unreachable — the comment is kept, no 500 returned).

## Setup

Requires **JDK 25**.

1. Clone the repo:
   ```bash
   git clone <repo-url>
   cd pulsedesk-ticket-triage
   ```
2. Create a **fine-grained Hugging Face token** scoped to *"Make calls to Inference Providers"* (Settings → Access Tokens on huggingface.co) and export it:
   ```bash
   export HF_API_TOKEN=hf_xxx        # PowerShell: $env:HF_API_TOKEN = "hf_xxx"
   ```
3. Run:
   ```bash
   ./gradlew bootRun
   ```

The app starts on `http://localhost:8080` (a minimal test UI is served at `/`).

### Run with the stub (free, instant, no token needed)

```bash
./gradlew bootRun --args='--spring.profiles.active=stub'
```

The stub triages by keywords (crash/error → BUG, charge/bill → BILLING, etc.) with zero external calls — useful for demos and offline development.

### Run with Docker

```bash
docker build -t pulsedesk .
docker run -d --env-file .env -p 8080:8080 pulsedesk
```

The token is intentionally not baked into the image — put `HF_API_TOKEN=hf_xxx` in a local `.env` file (gitignored) and pass it at runtime.

## API

| Method | Path | Description |
|--------|------|-------------|
| POST | `/comments` | Submit a comment; triggers AI triage |
| GET | `/comments` | List all comments with their analysis status |
| GET | `/tickets` | List all auto-created tickets |
| GET | `/tickets/{id}` | Fetch a single ticket |

**POST /comments** — request:

```json
{ "text": "The app crashes when I upload attachments over 5MB", "channel": "email" }
```

`text` is required (max 2000 chars); `channel` is optional. Response `201 Created`:

```json
{
  "id": 1,
  "text": "The app crashes when I upload attachments over 5MB",
  "channel": "email",
  "status": "ANALYZED",
  "createdAt": "2026-07-25T20:00:26Z"
}
```

**GET /tickets** — response `200 OK`:

```json
[
  {
    "id": 1,
    "commentId": 1,
    "title": "App Crashes on Large Attachment Upload",
    "category": "BUG",
    "priority": "HIGH",
    "summary": "The app crashes when uploading attachments larger than 5MB.",
    "createdAt": "2026-07-25T20:00:27Z"
  }
]
```

`GET /tickets/{id}` returns the same shape for one ticket (`404` if it does not exist). Categories: `BUG`, `FEATURE`, `BILLING`, `ACCOUNT`, `OTHER`. Priorities: `LOW`, `MEDIUM`, `HIGH`.

## Tests

```bash
./gradlew test
```

Covers the service layer, API error handling, an end-to-end integration test, and a regression test for slow/timed-out Hugging Face responses (stubbed local HTTP server — no real network calls).

## Known trade-offs

- **`ddl-auto: update` instead of a real migration tool** (Flyway/Liquibase). Fine for an exercise with an in-memory H2 database — which also means all data is lost on restart — but a production deployment would need versioned migrations and a persistent database.
- **Hugging Face free-tier rate limits.** The serverless Inference Providers tier throttles aggressively and response latency varies widely; the client read timeout is set to 180 s to absorb slow responses. Under sustained load you would need a paid tier or a self-hosted model.
