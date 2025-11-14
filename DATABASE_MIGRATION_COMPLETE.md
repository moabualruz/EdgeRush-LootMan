# Database Migration Status - Complete ✅

**Date**: November 14, 2025  
**Status**: ALL MIGRATIONS APPLIED SUCCESSFULLY

## Migration Summary

### Development Database (`edgerush`)
- **Status**: ✅ All migrations applied
- **Total Migrations**: 17
- **Total Tables**: 46
- **Connection**: `jdbc:postgresql://localhost:5432/edgerush`

### Test Database (`edgerush_test`)
- **Status**: ✅ All migrations applied
- **Total Migrations**: 17
- **Total Tables**: 46
- **Connection**: `jdbc:postgresql://localhost:5432/edgerush_test`

## Applied Migrations

| Version | Description | Status |
|---------|-------------|--------|
| V0001 | Initial schema | ✅ Applied |
| V0002 | Add raiders table | ✅ Applied |
| V0003 | Add attendance stats | ✅ Applied |
| V0004 | Add loot awards | ✅ Applied |
| V0005 | Add behavioral actions | ✅ Applied |
| V0006 | Add loot bans | ✅ Applied |
| V0007 | Add guild configurations | ✅ Applied |
| V0008 | Add FLPS modifiers | ✅ Applied |
| V0009 | Add historical activity | ✅ Applied |
| V0010 | Add character history | ✅ Applied |
| V0011 | Add wishlist snapshots | ✅ Applied |
| V0012 | Add raids and signups | ✅ Applied |
| V0013 | Add applications | ✅ Applied |
| V0014 | Add encryption keys | ✅ Applied |
| V0015 | Add sync runs | ✅ Applied |
| **V0016** | **Add Warcraft Logs tables** | ✅ Applied |
| **V0017** | **Add Raidbots tables** | ✅ Applied |

## Database Schema

### Core Tables (46 total)

#### Raider Management
- `raiders` - Core raider information
- `raider_gear_items` - Current gear
- `raider_track_items` - Track-specific items
- `raider_crest_counts` - Crest currency
- `raider_vault_slots` - Weekly vault rewards
- `raider_pvp_bracket_stats` - PvP statistics
- `raider_raid_progress` - Raid progression
- `raider_renown` - Faction renown
- `raider_statistics` - General statistics
- `raider_warcraft_logs` - Warcraft Logs linkage

#### Attendance & Activity
- `attendance_stats` - Attendance tracking
- `historical_activity` - Activity history
- `character_history` - Character changes
- `raids` - Raid events
- `raid_encounters` - Boss encounters
- `raid_signups` - Raid signups

#### Loot Management
- `loot_awards` - Loot distribution history
- `loot_award_bonus_ids` - Item bonus IDs
- `loot_award_old_items` - Replaced items
- `loot_award_wish_data` - Wishlist data
- `wishlist_snapshots` - Wishlist history
- `loot_bans` - Loot restrictions

#### Behavioral System
- `behavioral_actions` - Behavioral tracking
- `flps_default_modifiers` - Default FLPS config
- `flps_guild_modifiers` - Guild-specific config

#### Guild Management
- `guild_configurations` - Guild settings
- `team_metadata` - Team information
- `team_raid_days` - Raid schedule
- `period_snapshots` - Period data

#### Applications
- `applications` - Guild applications
- `application_alts` - Alt characters
- `application_questions` - Application questions
- `application_question_files` - Question attachments
- `guests` - Guest raiders

#### System Tables
- `sync_runs` - Sync execution history
- `wowaudit_snapshots` - WoWAudit data snapshots
- `encryption_keys` - Encrypted credentials
- `flyway_schema_history` - Migration tracking

#### Warcraft Logs Integration (V0016)
- `warcraft_logs_config` - Per-guild WCL configuration
- `warcraft_logs_reports` - Synced combat log reports
- `warcraft_logs_fights` - Individual boss fights
- `warcraft_logs_performance` - Performance metrics (MAS)
- `warcraft_logs_character_mappings` - Name resolution

#### Raidbots Integration (V0017)
- `raidbots_config` - Per-guild Raidbots configuration
- `raidbots_simulations` - Simulation requests
- `raidbots_results` - Simulation results (upgrade values)

## Verification Commands

### Check Migration Status
```bash
docker exec edgerush-postgres psql -U edgerush -d edgerush -c "SELECT version, description, installed_on FROM flyway_schema_history ORDER BY installed_rank;"
```

### List All Tables
```bash
docker exec edgerush-postgres psql -U edgerush -d edgerush -c "SELECT tablename FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename;"
```

### Check Table Count
```bash
docker exec edgerush-postgres psql -U edgerush -d edgerush -c "SELECT COUNT(*) FROM pg_tables WHERE schemaname = 'public';"
```

### Verify Warcraft Logs Tables
```bash
docker exec edgerush-postgres psql -U edgerush -d edgerush -c "\d warcraft_logs_config"
docker exec edgerush-postgres psql -U edgerush -d edgerush -c "\d warcraft_logs_performance"
```

### Verify Raidbots Tables
```bash
docker exec edgerush-postgres psql -U edgerush -d edgerush -c "\d raidbots_config"
docker exec edgerush-postgres psql -U edgerush -d edgerush -c "\d raidbots_results"
```

## Migration Process

### How Migrations Are Applied

1. **Automatic on Application Startup**
   - Flyway runs automatically when Spring Boot starts
   - Checks `flyway_schema_history` for applied migrations
   - Applies any pending migrations in order

2. **Test Execution**
   - Tests use separate `edgerush_test` database
   - Migrations applied automatically before first test
   - Database state preserved between tests via `@Transactional`

### Migration Files Location
```
data-sync-service/src/main/resources/db/migration/postgres/
├── V0001__initial_schema.sql
├── V0002__add_raiders_table.sql
├── ...
├── V0016__add_warcraft_logs_tables.sql
└── V0017__add_raidbots_tables.sql
```

## Database Configuration

### Development (`application.yaml`)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/edgerush
    username: edgerush
    password: edgerush
  flyway:
    enabled: true
    locations: classpath:db/migration/postgres
    baseline-on-migrate: true
```

### Test (`application-test.properties`)
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/edgerush_test
spring.datasource.username=edgerush
spring.datasource.password=edgerush
spring.flyway.locations=classpath:db/migration/postgres
spring.flyway.baseline-on-migrate=true
```

## Docker Setup

### PostgreSQL Container
```yaml
services:
  postgres:
    image: postgres:18
    environment:
      POSTGRES_USER: edgerush
      POSTGRES_PASSWORD: edgerush
      POSTGRES_DB: edgerush
    ports:
      - "5432:5432"
```

### Container Status
```bash
docker-compose ps
# NAME                IMAGE         STATUS
# edgerush-postgres   postgres:18   Up
```

## Troubleshooting

### If Migrations Fail

1. **Check Database Connection**
   ```bash
   docker exec -it edgerush-postgres psql -U edgerush -d edgerush
   ```

2. **View Migration History**
   ```sql
   SELECT * FROM flyway_schema_history ORDER BY installed_rank;
   ```

3. **Repair Failed Migration**
   ```bash
   ./gradlew flywayRepair
   ```

4. **Clean and Reapply (DESTRUCTIVE)**
   ```bash
   docker exec edgerush-postgres psql -U edgerush -d postgres -c "DROP DATABASE edgerush;"
   docker exec edgerush-postgres psql -U edgerush -d postgres -c "CREATE DATABASE edgerush;"
   ./gradlew :data-sync-service:bootRun
   ```

## Next Steps

1. ✅ All migrations applied successfully
2. ✅ Test database configured and isolated
3. ✅ Warcraft Logs tables ready for use
4. ✅ Raidbots tables ready for use
5. ⏭️ Ready for feature development

## Notes

- Migrations are versioned and immutable
- Never modify existing migration files
- Always create new migrations for schema changes
- Test database is automatically cleaned between test runs
- Both databases use PostgreSQL 18
