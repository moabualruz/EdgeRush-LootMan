# WoWAudit Spreadsheet Intake Notes

This section documents the structure of `wowaudit/War Within Spreadsheet.xlsx` so we can migrate any useful automation into EdgeRush LootMan. The file is provided only as an analytical reference—production systems must pull data directly from WoWAudit APIs and persist it in our own database (no reliance on manual exports).

## Workbook Overview
- **Summary** – guild health indicators (refresh status, tracked members, Patreon gating), hyperlinks to roster/raid pages, and column labels reused across other sheets.
- **Overview / Great Vault & Gear / Roster / Single View / Raids / Professions** – presentation layers that pivot the raw data into dashboards. These sheets rely heavily on named ranges, cell comments, and hyperlinks to WoWAudit, Warcraft Logs, Raider.IO, SimpleArmory, and Wowhead.
- **Settings** – contains configuration cells and instructions (including references to adding your own WoWAudit API key on the website). No direct API calls originate from Excel; comments clarify that WoWAudit performs the queries server-side.
- **raw_data** – primary data sheet populated from WoWAudit exports. First row holds metadata (refresh timestamp, guild status flags, endpoints). Subsequent rows enumerate characters with hundreds of columns covering gear slots, WCL ranks, vault progress, crest counts, raid progression, PvP stats, renown, etc.

## Data Connectivity Findings
- No `WEBSERVICE` or `Power Query` calls: Excel is not hitting the API directly. Data is injected via WoWAudit's export and refreshed through their web tooling.
- Hyperlinks are generated with formulas (e.g., `HYPERLINK("https://wowaudit.com/...", ...)`, `HYPERLINK("https://wowhead.com/item=", ...)`) for quick navigation.
- Comments mention a Patreon tier controlling refresh cadence and instructions to retrieve API keys from WoWAudit.
- Image references (`https://data.wowaudit.com/img/...`) supply class/role icons and UI affordances (refresh arrows, update badges).

## Column Highlights (raw_data)
- Metadata: guild name, region, roster/raid URLs, refresh timestamp, status flags (`good|All added members...`).
- Gear slots: `head_ilvl`, `head_id`, `head_name`, `head_quality`, replicated for every slot plus "best" versions.
- Performance: `WCL_*`, `raids_*`, `m+_score`, weekly/season counts for dungeons, delves, etc.
- Preparation flags: crest usage, enchant qualities, sockets, Great Vault slots, weekly world quest counts.
- Progression: renown per faction, campaign steps, raid boss kill strings, Cutting Edge/AotC trackers.
- PvP: ratings, weekly/season games, honorable kills.
- Professions: primary/secondary names, profession-specific counts.

Refer to `docs/data/mock_flps_output.json` and `docs/flps-walkthrough.md` for how we’re modeling similar data in Kotlin.

## Migration Considerations
1. **Data Source** – instead of parsing Excel, ingest WoWAudit data via API (`/guild/characters`, `/guild/loot`, `/guild/sims`, etc.) into our Kotlin service.
2. **Schema Mapping** – map `raw_data` columns to strongly typed models (`RaiderInput`). Identify the subset needed for FLPS while preserving additional metrics for future dashboards.
3. **Hyperlinks & Assets** – replicate link construction in the web UI (Flutter front end) using the same base URLs. Store icon mappings (role/class) in code or a configuration table.
4. **Status Indicators** – convert the summary "good|" / "warn|" strings into structured status objects for the dashboard.
5. **Refresh Cadence** – follow WoWAudit’s update schedule; expose last-refresh timestamps and allow manual refresh requests via the Kotlin service.
6. **Communications** – port the instructional text (API key guidance, Discord link) into our onboarding documentation or Settings panel.

## Next Actions
- Define the exact `RaiderInput` JSON schema expected from the WoWAudit API and verify it covers the fields currently populated in `raw_data`.
- Build Kotlin deserializers for WoWAudit responses and populate test fixtures mirroring the Excel columns.
- Decide which Excel-driven visualizations should be replicated in the interim dashboard vs. deferred to the eventual Flutter front end.
