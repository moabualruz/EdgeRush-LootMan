# WoWAudit API Schema Notes

EdgeRush LootMan consumes multiple WoWAudit endpoints. This document sketches initial DTOs and the mapping to our `docs/wowaudit-raw-schema.json` categories. Real payloads should be captured once API access is live.

## `/guild/characters`
Structure (inferred from spreadsheet + public API docs):
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
        "head": {"item_id": 123, "ilvl": 730, "quality": 4, "enchant": "...", "upgrade_level": 8},
        "neck": {"item_id": 124, "ilvl": 717, ...},
        "best": {"head": {...}},
        "spark": {"head": {...}}
      },
      "statistics": {
        "wcl": {"raid_finder": 98, "normal": 85, "heroic": 60, "mythic": 40},
        "mplus_score": 3000,
        "weekly_highest_mplus": 8,
        "season_highest_mplus": 10,
        "crest_counts": {"runed": 50, "carved": 120, "gilded": 200},
        "vault_slots": {"slot_1": true, ...},
        "renown": {"assembly_of_the_deeps": 12, ...},
        "pvp": {"honor_level": 50, "shuffle_rating": 1200, ...}
      },
      "collectibles": {
        "mounts": 320,
        "toys": 150,
        "unique_pets": 400
      },
      "timestamps": {
        "join_date": "2024-05-12T18:00:00Z",
        "blizzard_last_modified": "2024-10-20T12:00:00Z"
      }
    }
  ]
}
```

### Kotlin DTO skeleton
```kotlin
data class CharactersResponse(
    val characters: List<CharacterDto>
)

data class CharacterDto(
    val name: String,
    val realm: String,
    val region: String,
    val `class`: String,
    val spec: String,
    val role: String,
    val gear: GearDto,
    val statistics: StatisticsDto,
    val collectibles: CollectiblesDto,
    val timestamps: TimestampDto
)
```

## `/guild/loot`
Minimal structure needed for RDF tracking:
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

### Mapping Plan
- `gear` → `gear_equipped`, `gear_best`, `spark_items` categories.
- `statistics.wcl` → `warcraft_logs_scores`.
- `statistics.crest_counts` → `raid_currency`.
- `collectibles` → `collectibles_general` and `collectibles_mounts`.
- `timestamps` → `identity_metadata`.
- Additional arrays (recipes, embellishments) map to new domain objects (`ProfessionRecipe`, `Embellishment`).

Future implementation will add concrete DTO classes under `data-sync-service/src/main/kotlin/com/edgerush/datasync/api/wowaudit`.
