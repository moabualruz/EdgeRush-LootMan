# Persistence Plan

EdgeRush LootMan stores all synchronized data in a first-party database so that dashboards, APIs, and historical queries are independent of external spreadsheets. This plan outlines the initial persistence strategy.

## 1. Datastore Overview
- **Primary database**: PostgreSQL 18 (containerized via Docker Compose).
- **Developer fallback**: SQLite file-based store for quick smoke tests (`spring.profiles.active=sqlite`). Not recommended for shared environments.
- **Future ingress**: nginx reverse proxy will front all internal services to simplify port management and TLS termination.

## 2. Docker Compose Stack
- `docker-compose.yml` provisions a `postgres` service with persistent volume `postgres_data`.
- Environment variables (`POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_PORT`) are configurable through `.env` or shell exports.
- Start locally: `docker compose up -d postgres`.

## 3. Spring Boot Configuration
- `application.yaml` defaults to PostgreSQL using env-driven connection settings.
- `application-sqlite.yaml` provides a lightweight profile for `jdbc:sqlite`.
- `SyncProperties` encapsulates runtime config; secret values live only in `.env` files.
- Flyway migrations (classpath `db/migration/postgres`) bootstrap schema on application start.

## 4. Initial Schema (V0001)
Goal: capture enough structure for FLPS scoring snapshots and loot history while remaining flexible.

Tables proposed in `V0001__init.sql`:
- `raiders` – canonical roster with role, class, last sync metadata, and unique character key.
- `loot_awards` – awarded items with item id, tier classification, RDF cooldown data, and foreign key to `raiders`.
- `wishlist_snapshots` – stores raw wishlist payloads per character for audit/comparison.
- `attendance_stats` – tracks raid attendance metrics per character and sync.
- `raids`, `raid_signups`, `raid_encounters` – detailed raid schedule with sign-up rosters and encounter toggles.
- `historical_activity` – keystone/vault progress by character and period.
- `guests` – recurring guest roster tracked by WoWAudit.
- `applications`, `application_alts`, `application_questions` – recruitment intake data with answers and attachments.
- `wowaudit_snapshots` – raw JSON payload archive for auditing and reprocessing.
- `sync_runs` – audit of synchronization attempts (source, status, duration, error payloads).

Indexes will target frequent lookup paths (`raiders(character_name)`, `loot_awards(item_id)` etc.).

## 5. Data Flow
1. Data-sync service pulls WoWAudit → transforms to internal models → upserts into PostgreSQL.
2. Warcraft Logs Droptimizer outputs augment the same transaction.
3. FLPS output snapshots persist to `loot_awards` (for contested items) and a separate materialized view or table planned in Phase 2.

## 6. SQLite Considerations
- Schema parity maintained by reusing the same migration logic (Flyway disabled, fallback SQL script to be added in a future iteration).
- Writes are single-user; only enable for local development.

## 7. Next Steps
- Wire repositories / DAO layer (Spring Data JDBC).
- Implement transactional sync workflow with per-source upsert and conflict detection.
- Add nightly job to archive expired sync run logs.
- Evaluate partitioning / retention policies once data volume is understood.

Refer to `docs/automation-notes.md` for operational commands, and `docs/kotlin-data-sync.md` for integration requirements.
