# Local Setup Instructions

Follow these steps to run EdgeRush LootMan locally using Docker:

## Prerequisites
- Docker Engine 20.10+ (with Compose V2)
- `.env.local` populated with required variables (see below)
- Ability to pull Docker images (`gradle:8.10.1-jdk21`, `postgres:18`, `nginx:1.27`)

### Required Environment Variables (`.env.local`)
```
WOWAUDIT_API_KEY=your_key
WOWAUDIT_GUILD_URI=https://wowaudit.com/REGION/REALM/GUILD/profile
SYNC_RUN_ON_STARTUP=true
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=edgerush
POSTGRES_USER=edgerush
POSTGRES_PASSWORD=edgerush
WARCRAFT_LOGS_CLIENT_ID=
WARCRAFT_LOGS_CLIENT_SECRET=
RAIDBOTS_API_KEY=
```

## Start the Stack
```
docker compose --env-file .env.local up --build
```
This launches:
- `postgres`: primary datastore
- `data-sync`: Gradle container running `gradle :data-sync-service:bootRun`
- `nginx`: reverse proxy exposing the API at `http://localhost/api/`

> Note: if you change the Postgres image version, run `docker compose down -v` first to drop the old data volume. Ensure the volume is mounted at `/var/lib/postgresql` (not `/var/lib/postgresql/data`) for PostgreSQL 18 compatibility.

## Verify
- Check logs: `docker compose logs data-sync`
- Access health probe (once actuator enabled): `http://localhost/api/actuator/health`

## Stopping
```
docker compose down
```

For detailed persistence info see `docs/persistence-plan.md`; scheduling details are in `docs/automation-notes.md`.
