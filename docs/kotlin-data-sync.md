# Kotlin Data Sync Service Outline

This document sketches the architecture for the MVP automation layer, implemented with Kotlin + Spring Boot. It replaces the earlier Python script concept.

## 1. Module Overview
- Project name: `data-sync-service`
- Framework: Spring Boot (3.x), Kotlin, Gradle (KTS)
- Key dependencies:
  - `spring-boot-starter-webflux` (WebClient + reactive backpressure)
  - `spring-boot-starter-actuator`
  - `spring-boot-starter-validation`
  - `spring-boot-starter-json`
  - `kotlinx-coroutines-reactor` (optional for suspending WebClient calls)
  - `spring-retry` + `resilience4j` for retries and circuit breaking
- Packaging: Runnable jar / Docker image triggered via CLI args or scheduled tasks.

## 2. Responsibilities
1. Fetch roster, attendance, loot, and simulation data from WoWAudit `/guild` endpoints.
2. Augment mechanical metrics with Warcraft Logs GraphQL queries and Wipefest summaries.
3. Normalize External Preparation metrics (vault slots, crest usage, heroic clears).
4. Compute RMS, IPI, and RDF values for each raider.
5. Mirror the full WoWAudit `raw_data` schema (every gear/performance/prep column) in EdgeRush domain models.
6. Provide resilient API clients (`WoWAuditClient`) with retry-aware error handling for 429/5xx responses.
7. Persist synchronized data locally (default: PostgreSQL via Docker Compose; SQLite permitted for quick developer tests) with migration support.
8. Publish optional Discord webhooks summarizing updates.

## 3. Component Diagram
- `DataSyncApplication` – Spring Boot entry point, enables scheduling.
- `WoWAuditClient` – Typed WebClient wrapper (interfaces + DTOs in `client.wowaudit` package).
- `WarcraftLogsClient`, `RaidbotsClient` – Similar typed clients.
- `DataNormalizer` – Maps external DTOs into internal domain models (`RaiderSnapshot`, `LootEvent`, `SimulationResult`).
- `ScoreCalculator` – Pure Kotlin service computing RMS/IPI/RDF using logic in `docs/system-overview.md`.
- `PersistenceProvider` – Abstraction for writing output (filesystem JSON, PostgreSQL, or both).
- `DiscordNotifier` – Posts summary messages (optional bean toggled via config).

## 4. Scheduling Strategy
- Use `@EnableScheduling` and `@Scheduled(cron = "...")` for default cadence.
- Support on-demand execution via `CommandLineRunner` when run with `--once`.
- Properties example (`application.yaml`):
  ```yaml
  sync:
    cron: "0 0 4 * * *"
    wowaudit:
      base-url: https://api.wowaudit.com/v1
    raidbots:
      enabled: true
  ```

## 5. Configuration & Secrets
- Use `spring.config.import=optional:file:.env[.properties]` for local development.
- Bind credentials with `@ConfigurationProperties("credentials")`.
- Provide example in `.env.example` (already listed in `docs/automation-notes.md`).
- Required environment variables:
  - `WOWAUDIT_API_KEY` – guild API token.
  - `WOWAUDIT_BASE_URL` – optional override (defaults to WoWAudit production).
  - `WOWAUDIT_GUILD_URI` – guild profile URL (`https://wowaudit.com/REGION/REALM/GUILD/profile` placeholder); keep the real value in local `.env`, not committed sources.
  - Future: `WOWAUDIT_EXPORT_FIELDS` flag to assert parity with spreadsheet schema.

## 6. Testing Plan
- **Unit Tests**
  - `ScoreCalculatorTests` – Cover RMS/IPI/RDF edge cases; use mock dataset from `docs/data/`.
  - `WoWAuditClientTests` – Deserialize sample responses, validate schema changes.
  - `RawDataParityTests` – Compare deserialized payloads against the column inventory in `docs/wowaudit-data-map.md`.
  - `RdfDecayTests` – Confirm week-by-week penalty behaviour.
  - WebClient resilience tests using `MockWebServer` to assert retry/backoff paths.
- **Integration Tests**
  - Spring Boot Test slicing with MockWebServer (OkHttp) to simulate API responses.
  - Ensure timeout/retry logic functions under network errors.
- **Contract Tests**
  - Snapshot tests to detect schema drift between WoWAudit/Logs payloads and DTOs.
- **Performance Checks**
  - Measure sync run using Kotlin JMH or simple timers to ensure raid-night responsiveness.

## 7. Deliverables for Phase 1
- Gradle project scaffold (no business logic yet) with:
  - Basic `DataSyncApplication.kt`
  - DTO placeholders for external APIs
  - Failing unit test referencing mock dataset as acceptance guard
- Documentation updates describing how to run the service locally.

## 8. Persistence & Deployment
- **Primary Database**: PostgreSQL (run via Docker Compose). Provide migration scripts (Flyway/Liquibase) and connection settings in `application.yaml`.
- **Developer Shortcut**: Allow switching to SQLite for local smoke tests by setting `spring.profiles.active=sqlite`; keep schema identical and document limitations.
- **Container Orchestration**: Encourage docker-compose stack including data-sync service, PostgreSQL, and optional nginx reverse proxy aggregating service ports.
- **Data Retention**: Snapshot API responses before transformation to support audits and replay.

## 9. Future Enhancements
- Plug in PostgreSQL persistence and expose REST endpoints for other services.
- Stream FLPS updates to a message bus (e.g., Kafka) for real-time dashboards.
- Containerize and deploy via Kubernetes CronJobs or GitHub Actions workflow.
- Implement structured logging + distributed tracing (OpenTelemetry).
- Integrate Discord webhook publisher using payloads in `docs/discord-templates.md`.
- Provide optional nginx reverse proxy to consolidate service ports for external access.
