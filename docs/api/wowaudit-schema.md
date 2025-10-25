# WoWAudit API & Persistence Map

EdgeRush LootMan ingests the WoWAudit v1 API to mirror roster, raid, loot, and recruitment data. This document captures the live endpoint surface (based on the Twisting Nether “DoD” example payloads from Autumn 2025), highlights key response fields, and records which normalized tables receive each slice of data.

## Endpoint Reference

| Endpoint | Purpose | Notable Fields | Persistence |
| --- | --- | --- | --- |
| `GET /v1/applications`<br>`GET /v1/applications/{id}` | Recruitment pipeline, with optional detail (alts, Q&A, file uploads). | `id`, `applied_at`, `status`, `role`, `age`, `country`, `battletag`, `discord_id`, `main_character{ name, realm, region, class, race, faction, level }`, `alts[]`, `questions[].files[]`. | `applications`, `application_alts`, `application_questions`, `application_question_files`. |
| `GET /v1/attendance` | Attendance stats per character, scoped via query params (`instance`, `encounter`, `start_date`, `end_date`). | Character identity, raid/encounter counts, attendance & selection percentages. | `attendance_stats` (plus `character_region`, `team_id`, `season_id`, `period_id`). |
| `GET /v1/raids`<br>`GET /v1/raids/{id}` | Raid schedule plus detailed signups & encounter toggles. | `id`, `date`, `start_time`, `end_time`, `instance`, `optional`, `difficulty`, `status`, `present_size`, `total_size`, `notes`, `signups[].character{ id, name, realm, region, class, role, guest }`, `signups[].{ status, comment, selected }`, `encounters[]`. | `raids`, `raid_signups` (now includes `character_guest`), `raid_encounters`. |
| `GET /v1/characters` | Active roster with demographic + tracking metadata. | `id`, `name`, `realm`, `class`, `role`, `rank`, `status`, `note`, `blizzard_id`, `tracking_since`. | `raiders` and dependent stat tables (`raider_statistics`, `raider_gear_items`, `raider_crest_counts`, `raider_track_items`, `raider_vault_slots`, `raider_renown`, `raider_pvp_bracket_stats`, `raider_raid_progress`, `raider_warcraft_logs`). |
| `GET /v1/historical_data` | Keystone-period activity per character. | `period`, nested `characters[].data` (dungeon runs, world quests, vault options). | `historical_activity` (`data_json` keeps raw payload). |
| `GET /v1/historical_data/{id}` | Full activity history for a single character. | Character identity, chronological `history[]`, and `best_gear` snapshot. | Same `historical_activity` store; downstream analytics pivot by period. |
| `GET /v1/wishlists`<br>`GET /v1/wishlists/{id}` | Droptimizer wishlists by configuration/difficulty/specialization. | Character identity, `wishlists[].instances[].difficulties[].wishlist.encounters[].items[].wishes[]`. | `wishlist_snapshots` (raw JSON, keyed by character + team metadata). |
| `POST /v1/wishlists` | Upload Droptimizer reports (configurations, replace flags). | `report_id`, `character_id`, `configuration_name`, `replace_manual_edits`, `clear_conduits`. | Processed into `wishlist_snapshots` and downstream analytics. |
| `GET /v1/guests` | Guest characters for ad-hoc raid signups. | `id`, `name`, `realm`, `class`, `role`, `blizzard_id`, `tracking_since`. | `guests`. |
| `GET /v1/loot_history/{season}` | Loot awards with RCLootCouncil metadata. | `history_items[].{ id, rclootcouncil_id, item_id, name, icon, slot, quality, difficulty, discarded, character_id, awarded_by_* }`, `response_type`, `bonus_ids[]`, `old_items[]`, `wish_data[]`, `wish_value`. | `loot_awards`, `loot_award_bonus_ids`, `loot_award_old_items`, `loot_award_wish_data`. |
| `GET /v1/period` | Current keystone period and active season metadata. | `current_period`, `period_id`, `season_id`, optional `current_season{ id, name, start_date, end_date, kind, expansion, keystone_season_id, pvp_season_id, first_period_id }`. | `period_snapshots` (linked to `team_id`). |
| `GET /v1/team` | Team profile, Blizzard refresh timestamps, raid night schedule, wishlist epoch. | `id`, `guild_id`, `guild_name`, `name`, `url`, `last_refreshed.{ blizzard, percentiles, mythic_plus }`, `raid_days[]`, `wishlist_updated_at`. | `team_metadata` (new columns for guild name, URL, refresh timestamps, wishlist epoch), `team_raid_days` (new child table). |

### Notes on Persistence Updates

- **Team metadata** now retains `guild_name`, `url`, `last_refreshed_{blizzard,percentiles,mythic_plus}`, and `wishlist_updated_at` (epoch parsed to UTC). A new `team_raid_days` table stores each scheduled raid night (`week_day`, `start_time`, `end_time`, `current_instance`, `difficulty`, `active_from`, `synced_at`).
- **Raid signups** capture the boolean `character_guest` flag in addition to class/role, enabling downstream attendance and access-control analytics.

## Representative Payloads

The following JSON snippets remain useful for DTO scaffolding and test fixtures.

### `/v1/characters`
```json
{
  "characters": [
    {
      "name": "MageA",
      "realm": "twisting-nether",
      "region": "eu",
      "class": "Mage",
      "spec": "Fire",
      "role": "DPS",
      "gear": {
        "head": {"item_id": 123, "ilvl": 730, "quality": 4, "enchant": "...", "upgrade_level": 8}
      },
      "statistics": {
        "wcl": {"raid_finder": 98, "normal": 85, "heroic": 60, "mythic": 40},
        "mplus_score": 3000,
        "weekly_highest_mplus": 8,
        "season_highest_mplus": 10,
        "crest_counts": {"runed": 50, "carved": 120, "gilded": 200},
        "vault_slots": {"slot_1": true, "slot_2": false, "slot_3": false}
      },
      "collectibles": {"mounts": 320, "toys": 150, "unique_pets": 400},
      "timestamps": {
        "join_date": "2024-05-12T18:00:00Z",
        "blizzard_last_modified": "2024-10-20T12:00:00Z"
      }
    }
  ]
}
```

### `/v1/loot_history`
```json
{
  "loot": [
    {
      "character": "MageA",
      "item": {"id": 12345, "name": "Fyr'alath"},
      "tier": "A",
      "awarded_at": "2025-10-24T18:45:00Z"
    }
  ]
}
```

## Workflow Checklist

1. Capture fresh payloads per endpoint whenever WoWAudit updates its contract and refresh this document.
2. Mirror new fields in both the Kotlin DTOs (under `data-sync-service/src/main/kotlin/com/edgerush/datasync/api/wowaudit`) and the Flyway migrations (`data-sync-service/src/main/resources/db/migration/postgres`).
3. Add regression tests against stored JSON fixtures so deserialisers fail loudly when the payload changes.
4. Keep `docs/wowaudit-raw-schema.json` in sync to guarantee spreadsheet parity.
