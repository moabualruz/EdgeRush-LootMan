# WoWAudit Complete API Reference

## Overview

This document provides comprehensive documentation for all WoWAudit API endpoints based on the official API documentation. This reference is essential for understanding the full scope of available data and identifying implementation gaps in the EdgeRush LootMan system.

## Authentication

All API endpoints require authentication using either:
- **Authorization Header**: `Bearer <api_key>`
- **Query Parameter**: `?api_key=<api_key>`

## API Endpoints

### Applications

#### GET /v1/applications
**Purpose**: Show all applications without details
**Parameters**: None
**Response**: Array of application summaries with basic info
**Use Case**: Recruitment pipeline overview

```json
{
  "applications": [
    {
      "id": 1,
      "applied_at": "2024-10-01T19:50:49.000Z",
      "status": "submitted",
      "role": "damage",
      "age": 23,
      "country": "The Netherlands",
      "battletag": "Applicant#1234",
      "discord_id": "ApplicantDiscord#2468",
      "main_character": {
        "name": "Sheday",
        "realm": "Stormrage",
        "region": "EU",
        "class": "Paladin",
        "race": "Human",
        "faction": "Alliance",
        "level": 60
      }
    }
  ]
}
```

#### GET /v1/applications/{id}
**Purpose**: Show single application with full details
**Parameters**: Application ID
**Response**: Complete application data including alts, questions, and files
**Use Case**: Detailed recruitment review

#### PUT /v1/applications/{id}
**Purpose**: Update application status
**Parameters**: Application ID, status, notes, message
**Response**: Confirmation of update
**Use Case**: Recruitment workflow management

#### DELETE /v1/applications/{id}
**Purpose**: Delete application
**Parameters**: Application ID
**Response**: Confirmation of deletion
**Use Case**: Cleanup rejected applications

### Raids

#### GET /v1/attendance
**Purpose**: Show attendance statistics
**Parameters**: instance, encounter, start_date, end_date
**Response**: Character attendance data with percentages
**Use Case**: **CRITICAL FOR FLPS RMS** - Attendance tracking for merit scoring

```json
{
  "instance": "Castle Nathria",
  "encounter": "Shriekwing",
  "start_date": "2020-01-31",
  "end_date": "2021-12-31",
  "characters": [
    {
      "id": 3,
      "name": "Tute",
      "class": "Priest",
      "role": "Heal",
      "attended_amount_of_raids": null,
      "total_amount_of_raids": null,
      "attended_percentage": 0,
      "selected_amount_of_encounters": null,
      "total_amount_of_encounters": null,
      "selected_percentage": 0
    }
  ]
}
```

#### GET /v1/raids
**Purpose**: Show list of raids without details
**Parameters**: include_past (boolean)
**Response**: Array of raid summaries
**Use Case**: Raid calendar and scheduling

#### POST /v1/raids
**Purpose**: Create a single raid
**Parameters**: Raid details (date, time, instance, difficulty)
**Response**: Created raid with full details including signups and encounters
**Use Case**: Raid scheduling

#### GET /v1/raids/{id}
**Purpose**: Show single raid with full details
**Parameters**: Raid ID
**Response**: Complete raid data with signups, encounters, selections
**Use Case**: **CRITICAL FOR FLPS** - Individual raid attendance and selection data

#### PUT /v1/raids/{id}
**Purpose**: Update a single raid
**Parameters**: Raid ID, status, encounter changes, signup changes
**Response**: Updated raid data
**Use Case**: Raid management and attendance tracking

#### DELETE /v1/raids/{id}
**Purpose**: Delete a single raid
**Parameters**: Raid ID
**Response**: Confirmation of deletion
**Use Case**: Cleanup cancelled raids

### Characters

#### GET /v1/characters
**Purpose**: Show all characters in roster
**Parameters**: None
**Response**: Array of character summaries
**Use Case**: **CRITICAL FOR FLPS** - Roster management and tracking

```json
[
  {
    "id": 4,
    "name": "Bexey",
    "realm": "Stormrage",
    "class": "Mage",
    "role": "Ranged",
    "rank": "Main",
    "status": "tracking",
    "note": null,
    "blizzard_id": 13345678,
    "tracking_since": "2024-10-01T19:51:31.000Z"
  }
]
```

#### POST /v1/characters
**Purpose**: Track a single character
**Parameters**: Character details (name, realm, role, rank, note)
**Response**: Created character data
**Use Case**: Roster expansion

#### PUT /v1/characters/{id}
**Purpose**: Update a single character
**Parameters**: Character ID, role, rank, note
**Response**: Confirmation of update
**Use Case**: Character management

#### DELETE /v1/characters/{id}
**Purpose**: Untrack a single character
**Parameters**: Character ID
**Response**: Confirmation of removal
**Use Case**: Roster cleanup

#### GET /v1/historical_data
**Purpose**: Show all characters' activity in given period
**Parameters**: period (integer)
**Response**: **CRITICAL FOR FLPS** - Character activity data including dungeons, world quests, vault options
**Use Case**: **ESSENTIAL FOR RMS** - Preparation and performance tracking

```json
{
  "period": 978,
  "characters": [
    {
      "id": 4,
      "name": "Bexey",
      "realm": "Stormrage",
      "data": {
        "dungeons_done": [
          {
            "level": 21,
            "dungeon": 402
          }
        ],
        "world_quests_done": 45,
        "vault_options": {
          "raids": {
            "option_1": 226,
            "option_2": null,
            "option_3": null
          },
          "dungeons": {
            "option_1": 213,
            "option_2": null,
            "option_3": null
          },
          "world": {
            "option_1": 220,
            "option_2": null,
            "option_3": null
          }
        }
      }
    }
  ]
}
```

#### GET /v1/historical_data/{id}
**Purpose**: Show entire activity history of single character
**Parameters**: Character ID
**Response**: **CRITICAL FOR FLPS** - Complete character history with best gear
**Use Case**: **ESSENTIAL FOR IPI** - Gear tracking and upgrade analysis

```json
{
  "character": {
    "id": 1,
    "name": "Sheday",
    "realm": "Stormrage"
  },
  "history": [
    {
      "dungeons_done": [],
      "world_quests_done": 45,
      "vault_options": {
        "raids": {
          "option_1": 226,
          "option_2": null,
          "option_3": null
        }
      }
    }
  ],
  "best_gear": {
    "main_hand": {
      "ilvl": 226,
      "id": 179330,
      "name": "Zin'khas, Blade of the Fallen God",
      "quality": 4
    },
    "off_hand": {
      "ilvl": 220,
      "id": 174315,
      "name": "Chyrus's Crest of Hope",
      "quality": 4
    }
  }
}
```

### Wishlists

#### GET /v1/wishlists
**Purpose**: Show basic loot wishlist of all characters
**Parameters**: None
**Response**: **CRITICAL FOR FLPS IPI** - All character wishlists with item priorities
**Use Case**: **ESSENTIAL FOR IPI** - Item priority calculation

#### POST /v1/wishlists
**Purpose**: Upload Droptimizer report
**Parameters**: Report data (report_id, character_id, configuration_name, etc.)
**Response**: Confirmation of upload
**Use Case**: Automated wishlist management

#### GET /v1/wishlists/{id}
**Purpose**: Show detailed loot wishlist of single character
**Parameters**: Character ID
**Response**: **CRITICAL FOR FLPS IPI** - Detailed character wishlist data
**Use Case**: **ESSENTIAL FOR IPI** - Individual item priority analysis

```json
{
  "id": 1,
  "name": "Sheday",
  "realm": "Stormrage",
  "wishlists": [
    {
      "name": "Single target",
      "fight_style": "Patchwerk",
      "allow_sockets": true,
      "weight": 1,
      "instances": [
        {
          "id": 25,
          "name": "Amirdrassil, the Dream's Hope",
          "difficulties": [
            {
              "difficulty": "mythic",
              "wishlist": {
                "encounters": [
                  {
                    "name": "Gnarlroot",
                    "items": [
                      {
                        "name": "Branch of the Tormented Ancient",
                        "id": 207169,
                        "slot": "trinket",
                        "percentage": 0,
                        "absolute": 0,
                        "wishes": [
                          {
                            "specialization": "Retribution",
                            "weight": 100,
                            "upgrade": "huge",
                            "percentage": null,
                            "absolute": null,
                            "comment": "BIS"
                          }
                        ]
                      }
                    ]
                  }
                ]
              }
            }
          ]
        }
      ]
    }
  ]
}
```

#### DELETE /v1/wishlists/{id}
**Purpose**: Delete all wishlist info of single character
**Parameters**: Character ID
**Response**: Confirmation of deletion
**Use Case**: Wishlist cleanup

### Guests

#### GET /v1/guests
**Purpose**: Show all guests associated with team
**Parameters**: None
**Response**: Array of guest character data
**Use Case**: Guest roster management

#### POST /v1/guests
**Purpose**: Add single guest to team
**Parameters**: Guest details (name, realm, role)
**Response**: Created guest data
**Use Case**: Guest management

#### DELETE /v1/guests/{id}
**Purpose**: Remove single guest from team
**Parameters**: Guest ID
**Response**: Confirmation of removal
**Use Case**: Guest cleanup

### General

#### GET /v1/loot_history/{id}
**Purpose**: Show entire loot history for a season
**Parameters**: Season ID
**Response**: **CRITICAL FOR FLPS RDF** - Complete loot award history
**Use Case**: **ESSENTIAL FOR RDF** - Recent loot tracking for decay factor

```json
{
  "history_items": [
    {
      "id": 2,
      "rclootcouncil_id": "1715197229-1",
      "item_id": 207149,
      "name": "Phlegethic Girdle",
      "icon": "inv_plate_raiddeathknightemerald_d_01_belt",
      "slot": "Waist",
      "quality": "epic",
      "character_id": 2,
      "awarded_by_character_id": 1,
      "awarded_by_name": "Sheday-Stormrage",
      "awarded_at": "2024-10-01T19:52:58.006Z",
      "difficulty": "mythic",
      "discarded": false,
      "response_type": {
        "id": 3,
        "name": "Main",
        "rgba": "rgba(0, 255, 0, 1)",
        "excluded": false
      },
      "bonus_ids": [
        "6652",
        "10533",
        "7981",
        "10340",
        "10884",
        "1602",
        "8767"
      ],
      "old_items": [
        {
          "item_id": 207139,
          "bonus_ids": [
            "6652",
            "10533",
            "7981",
            "10340",
            "10884",
            "1602",
            "8767"
          ]
        }
      ],
      "same_response_amount": 3,
      "note": "bis",
      "wish_data": [
        {
          "value": 2902,
          "spec_name": "Holy",
          "spec_icon": "spell_holy_holybolt"
        }
      ],
      "wish_value": 2902
    }
  ]
}
```

#### GET /v1/period
**Purpose**: Returns current Blizzard keystone period and season
**Parameters**: None
**Response**: Current period and season data
**Use Case**: Temporal context for data queries

#### GET /v1/team
**Purpose**: Show details of current team
**Parameters**: None
**Response**: Team metadata including raid schedule
**Use Case**: Guild configuration and scheduling

```json
{
  "name": "Test",
  "id": 2,
  "guild_name": "Avalerion",
  "url": "https://wowaudit.com/eu/stormrage/avalerion/test",
  "last_refreshed": {
    "blizzard": "2021-03-20T14:31:30.000+00:00",
    "percentiles": "2021-03-20T14:33:49.000+00:00",
    "mythic_plus": "2021-03-20T14:35:54.000+00:00"
  },
  "raid_days": [
    {
      "week_day": "Sunday",
      "start_time": "20:30",
      "end_time": "23:30",
      "current_instance": "Amirdrassil, the Dream's Hope",
      "difficulty": "Mythic",
      "active_from": "2024-10-01"
    }
  ],
  "wishlist_updated_at": 1727812444
}
```

## Data Mapping for FLPS Components

### RMS (Raider Merit Score) Data Sources
- **Attendance**: `/v1/attendance` - Provides raid attendance percentages
- **Performance**: `/v1/historical_data` - Dungeon levels, world quest completion
- **Preparation**: `/v1/historical_data` - Vault options, activity tracking

### IPI (Item Priority Index) Data Sources
- **Upgrade Value**: `/v1/wishlists/{id}` - Item upgrade percentages and weights
- **Tier Impact**: `/v1/historical_data/{id}` - Current gear and item levels
- **Role Multiplier**: `/v1/characters` - Character roles and classes

### RDF (Recency Decay Factor) Data Sources
- **Loot History**: `/v1/loot_history/{id}` - Recent loot awards with timestamps
- **Item Awards**: Character-specific loot tracking with award dates

## Implementation Priority Matrix

### Critical Missing Endpoints (FLPS Blockers)
1. **`/v1/attendance`** - Essential for RMS attendance component
2. **`/v1/historical_data`** - Essential for RMS preparation and IPI gear analysis
3. **`/v1/historical_data/{id}`** - Essential for individual character gear tracking
4. **`/v1/loot_history/{id}`** - Essential for RDF recent loot calculation
5. **`/v1/wishlists`** - Essential for IPI item priority calculation
6. **`/v1/wishlists/{id}`** - Essential for detailed item priority analysis

### Important Missing Endpoints (Enhancement)
1. **`/v1/raids`** - Raid calendar and scheduling
2. **`/v1/raids/{id}`** - Individual raid details
3. **`/v1/team`** - Guild configuration and metadata
4. **`/v1/period`** - Temporal context for data queries

### Optional Missing Endpoints (Future Features)
1. **`/v1/applications/*`** - Recruitment management
2. **`/v1/guests/*`** - Guest roster management
3. **Raid Management Endpoints** - POST/PUT/DELETE operations

## Data Coverage Analysis

### Currently Implemented (~15%)
- Basic character roster (`/v1/characters` partial)
- Limited wishlist data (partial implementation)

### Missing Implementation (~85%)
- **Attendance tracking** - 0% implemented
- **Historical activity data** - 0% implemented
- **Individual character history** - 0% implemented
- **Loot history** - 0% implemented
- **Detailed wishlists** - 0% implemented
- **Raid management** - 0% implemented
- **Team metadata** - 0% implemented
- **Guest management** - 0% implemented
- **Application management** - 0% implemented

## Recommended Implementation Order

1. **Phase 1 (FLPS Critical)**: Attendance, Historical Data, Loot History, Detailed Wishlists
2. **Phase 2 (Enhanced FLPS)**: Individual Character History, Raid Details, Team Metadata
3. **Phase 3 (Management Features)**: Raid Management, Guest Management, Application Management

This documentation provides the complete picture of available WoWAudit data and clearly identifies the massive gaps in current implementation that are blocking FLPS from using real data instead of mock test files.