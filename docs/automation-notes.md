# Automation Notes & API Setup

## WoWAudit API
- Navigate to `Settings → API Access` in WoWAudit and generate a guild API key.
- Store the key in environment variables when running scripts (`export WOWAUDIT_API_KEY=...`).
- Base URL: `https://wowaudit.com/v1`
- Core endpoints (GET unless noted):
  - `/guild/attendance`
  - `/guild/loot`
  - `/guild/sims`
  - `/guild/characters`
  - `/guild/wishlists`
  - `/guild/tier`
- Payload format: JSON. Include header `Authorization: Bearer <key>`.
- Rate limits: documented at `Settings → API Access`—cache responses to avoid spikes.
- Guild profile URI placeholder: `https://wowaudit.com/REGION/REALM/GUILD/profile` (set via `WOWAUDIT_GUILD_URI`; keep actual value in local `.env`, not in committed data). Rotate the key immediately if it leaks.

## Warcraft Logs API
- Create a client at `https://www.warcraftlogs.com/accounts/changeuser`.
- Use OAuth 2.0 client credentials flow to fetch tokens.
- Endpoints of interest: `/v2/client` GraphQL for fights, deaths, damage taken.
- Cache spec averages per encounter to reduce duplicated queries.

## Raidbots API
- Requires personal or guild API key (Premium).
- Endpoint: `https://www.raidbots.com/sim/report/<id>.json`
- Respect raidbots rate limits; prefer raiders supplying URLs when possible.

## Environment Setup
Create `.env.example` (do not commit secrets):
```
WOWAUDIT_API_KEY=replace_me
WOWAUDIT_GUILD_URI=https://wowaudit.com/REGION/REALM/GUILD/profile
WARCRAFT_LOGS_CLIENT_ID=replace_me
WARCRAFT_LOGS_CLIENT_SECRET=replace_me
RAIDBOTS_API_KEY=replace_me
```

Load environment variables in Spring Boot using `@ConfigurationProperties` or environment injection (`SyncProperties` binds `WOWAUDIT_BASE_URL` and `WOWAUDIT_GUILD_URI`).

## Starter Service Template
- Create a Kotlin Spring Boot module (suggested name: `data-sync-service`).
- Expose `CommandLineRunner` beans that call WoWAudit, Warcraft Logs, and Raidbots APIs and persist snapshots to application storage (`tmp/data` or PostgreSQL staging tables).
- Configure schedulers with `@EnableScheduling` for recurring syncs.
- Use WebClient with resilience patterns (retry/backoff).

## Scheduling
- Use Spring Boot `@Scheduled` annotations for in-cluster jobs, or containerize and schedule via cron/GitHub Actions when needed.
- Example cron (container invocation daily at 04:00):
  ```
  0 4 * * * docker run --rm \
    -e WOWAUDIT_API_KEY=*** \
    registry/edgerush/data-sync:latest \
    --since 2025-10-01 >> /var/log/elmlog 2>&1
  ```

## Data Hygiene
- Validate incoming JSON against expected schema before processing.
- Store historical snapshots (e.g., S3 bucket) for audit trails.
- Flag raiders with missing data in Discord using automation (future enhancement).

## Local Testing Tips
- Run `./gradlew :data-sync-service:test` to execute unit tests (once Gradle wrapper is added).
- Use the mock fixtures in `docs/data/` to simulate WoWAudit, Logs, and Droptimizer responses without hitting live APIs.
- Database: run PostgreSQL via Docker Compose for real sync tests (`docker compose --env-file .env.local up db`). Provide an optional SQLite profile for quick local experiments, but default to PostgreSQL 18 (volume mounted at `/var/lib/postgresql`). Set `SYNC_RUN_ON_STARTUP=true` when you want a one-time sync on boot; otherwise scheduled execution is driven by `sync.cron`.
