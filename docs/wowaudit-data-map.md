# WoWAudit Data Coverage Map

The sample spreadsheet `wowaudit/War Within Spreadsheet.xlsx` (analysis only) mirrors a WoWAudit `raw_data` export from a prior expansion. EdgeRush LootMan’s data-sync service must ingest every metric available there—and more—directly via WoWAudit’s API plus supplemental sources (Warcraft Logs, Raidbots, etc.). No production workflow should depend on manual exports; fetch data programmatically and persist it in our database.

This document catalogues the column groups observed in `raw_data`, the inferred WoWAudit API sources, and follow-up actions to guarantee parity.

> Machine-readable column inventory: `docs/wowaudit-raw-schema.json` tags every spreadsheet column index with a semantic category (gear, renown, PvP, currencies, etc.) so Kotlin deserializers can assert full coverage.

## 1. Workbook Metadata
- **Columns**: Timestamp, refresh status (`good|...`), Patreon tier, guild URLs.
- **Source**: WoWAudit export metadata (likely `/guild/profile` or `/guild/overview`).
- **Action**: Capture refresh timestamps + status flags in our persistence layer and expose them in dashboards.

## 2. Character Identity & Core Stats
- **Columns**: `name`, `race`, `gender`, `faction`, `class`, `role`, `realm`, `join_date`, `rank`, `note`.
- **Source**: `/guild/characters` (WoWAudit), with realm-normalised slugs.
- **Action**: Mirror all demographic fields in `RaiderSnapshot`. Preserve officer notes for dashboards (respect privacy permissions).

## 3. Gear Slots & Qualities
- **Columns**: For every armour slot (head → off-hand) – current ILvl, item ID, name, quality, enchant levels, socket lists, upgrade levels, plus “best in bags” variants.
- **Source**: `/guild/gear` (via `/guild/characters` gear payload) and `/guild/best` if available.
- **Action**: Define Kotlin models for `GearSlot`, track upgrade levels, enchant metadata, sockets, spark variants, epic gems, and resonant items.

## 4. Simulation & Preparation Metrics
- **Columns**: `m+_score`, weekly/season dungeon counts, world quest totals, Great Vault slots (`great_vault_slot_*`), crest counts (`runed`, `carved`, `gilded`), `ingenuity_sparks_equipped`, `mythic_track_items`, etc.
- **Source**: `/guild/characters` (WoWAudit raw stats), with crest usage from `/guild/vault` + manual submissions.
- **Action**: Verify each crest, spark, and vault slot field is computable via API; backfill with manual updates if needed.

## 5. Mechanical & Performance Stats
- **Columns**: Warcraft Logs brackets (`WCL_*`), raid kill strings, weekly raid counts, PvP ratings (`2v2`, `3v3`, `shuffle`), `honorable_kills`.
- **Source**: Combination of WoWAudit’s Logs integration (`/guild/logs`), Warcraft Logs API, Blizzard PvP API.
- **Action**: Ensure data-sync queries Warcraft Logs for DPA/ADT (already required for MAS) and stores rating history.

## 6. Pet/Mount/Collectible Progress
- **Columns**: `mounts`, `unique_pets`, `lvl_25_pets`, `achievement_points`, `toys_owned`.
- **Source**: WoWAudit character summary (Blizzard collections API).
- **Action**: Map to optional enrichment tables; maintain parity for dashboards.

## 7. Renown, Campaign & World Progress
- **Columns**: `assembly_of_the_deeps_renown`, `council_of_dornogal_renown`, `worldsoul_memories`, `worldsoul_weekly`, `weekly_event_completed`, `campaign_progress`.
- **Source**: WoWAudit / Blizzard world data endpoints.
- **Action**: Confirm API coverage. If missing, request WoWAudit endpoint or implement manual import.

## 8. Professions & Crafting
- **Columns**: `profession_1`, `profession_2`, `professions_visible`, `reshii_wraps_*`, `circlet_*`, `manaforge_vandals_renown`.
- **Source**: Blizzard profession API + WoWAudit.
- **Action**: Build models for profession-specific columns to keep parity; consider long-term storage for recipe tracking.

## 9. Spark & Track Items
- **Columns**: `spark_head_*`, `spark_trinket_*`, `spark_main_hand_*`, track counts (mythic/heroic/normal/world).
- **Source**: WoWAudit upgrade tracker endpoints.
- **Action**: Align with FLPS item priority logic; store track level for resource planning.

## 10. Miscellaneous Flags
- **Columns**: `summary_visible`, `roster_visible`, `overview_visible`, `vault_visible`, `raids_visible`, `gallywix_mount`, `ansurek_mount`, `dimensius_mount`, `raid_buff_percentage`, `crit_gear_percentage`, `reference_values` (baseline ILvls), etc.
- **Source**: Spreadsheet-specific metadata; some rely on achievements / mount ownership from Blizzard APIs.
- **Action**: Represent visibility toggles in EdgeRush UI configuration; confirm mount tracking via WoWAudit payloads. Treat baseline numeric columns (captured under `reference_values` in the schema file) as calibration constants for dashboards.

## Mapping Roadmap
1. **Endpoint Inventory** – Document all WoWAudit API routes and sample payloads (see `docs/kotlin-data-sync.md`). Request additional endpoints if any spreadsheet column isn’t covered.
2. **Model Expansion** – Extend `RaiderInput` and related domain models to include every raw_data field. Introduce typed data classes (e.g., `GearSlot`, `VaultProgress`, `RenownState`).
3. **Deserializer Tests** – Add JSON fixtures derived from WoWAudit API responses to unit tests, asserting field population against `docs/wowaudit-raw-schema.json`.
4. **Parity Dashboard** – Build automated checks comparing API-derived JSON to spreadsheet columns to guarantee nothing is dropped.
5. **Preferred Enhancements** – Identify “more” data beyond the spreadsheet (e.g., live boss pull metrics, attendance tardiness breakdowns) and queue for future ingestion.

## References
- `docs/wowaudit-spreadsheet.md` – qualitative notes on workbook structure.
- `docs/automation-notes.md` – environment configuration for WoWAudit API access.
- `docs/kotlin-data-sync.md` – service architecture and testing plan.
