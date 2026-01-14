# API Reference

**Version**: 2.0.0
**Last Updated**: January 14, 2026
**Base URL**: `http://localhost:8080` (Development) | `https://api.edgerush.com` (Production)

---

## Table of Contents

1. [Overview](#overview)
2. [Common Patterns](#common-patterns)
3. [Endpoint Reference by Domain](#endpoint-reference-by-domain)
   - [Application Management](#1-application-management)
   - [Raider Data](#2-raider-data)
   - [Loot Management](#3-loot-management)
   - [Guild and Team](#4-guild-and-team)
   - [Raid Management](#5-raid-management)
   - [FLPS System](#6-flps-system)
   - [Attendance](#7-attendance)
   - [Snapshots](#8-snapshots)
   - [Other Controllers](#9-other-controllers)
4. [Error Handling](#error-handling)
5. [OpenAPI Documentation](#openapi-documentation)

---

## Overview

EdgeRush LootMan provides a comprehensive REST API with **43 controllers** organized by domain. The API follows RESTful conventions with consistent CRUD operations and pagination support.

### Controller Summary

| Domain | Controllers | Description |
|--------|-------------|-------------|
| Application Management | 4 | Guild applications and related data |
| Raider Data | 11 | Raider profiles and related statistics |
| Loot Management | 6 | Loot awards, bans, and history |
| Guild and Team | 4 | Guild configuration and team management |
| Raid Management | 3 | Raids, encounters, and signups |
| FLPS System | 2 | Score calculation and modifiers |
| Attendance | 2 | Attendance tracking and statistics |
| Snapshots | 3 | Data snapshots (period, wishlist, WoWAudit) |
| Other | 8 | Simulation, sync, behavioral, etc. |

---

## Common Patterns

### Pagination

All list endpoints support pagination with these query parameters:

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | 0 | Page number (0-indexed) |
| `size` | int | configurable | Page size |

**Response Format (PagedResponse)**:
```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8
}
```

### Standard CRUD Endpoints

Most entity controllers extend `BaseCrudController` and provide these standard endpoints:

| Method | Path | Description | Response |
|--------|------|-------------|----------|
| GET | `/` | List all (paginated) | `PagedResponse<T>` |
| GET | `/{id}` | Get by ID | Entity |
| POST | `/` | Create | 201 Created |
| PUT | `/{id}` | Update | Entity |
| DELETE | `/{id}` | Delete | 204 No Content |
| GET | `/{id}/exists` | Check existence | `{ exists: boolean }` |

### Request/Response DTOs

Each controller uses specific DTOs:
- `Create{Entity}Request` - Creation request body
- `Update{Entity}Request` - Update request body
- `{Entity}Response` - Response body

---

## Endpoint Reference by Domain

### 1. Application Management

Manage guild applications from prospective members.

#### ApplicationController
**Base Path**: `/api/applications`
**Tag**: Application

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Find all applications (paginated) |
| GET | `/{id}` | Find application by ID |
| POST | `/` | Create an application |
| PUT | `/{id}` | Update an application |
| DELETE | `/{id}` | Delete an application |
| GET | `/{id}/exists` | Check if application exists |
| GET | `/status/{status}` | Find applications by status (paginated) |

#### ApplicationAltController
**Base Path**: `/api/application-alts`
**Tag**: ApplicationAlt

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Find all application alts (paginated) |
| GET | `/{id}` | Find application alt by ID |
| POST | `/` | Create an application alt |
| PUT | `/{id}` | Update an application alt |
| DELETE | `/{id}` | Delete an application alt |
| GET | `/{id}/exists` | Check if application alt exists |
| GET | `/application/{applicationId}` | Find alts by application ID (paginated) |

#### ApplicationQuestionController
**Base Path**: `/api/application-questions`
**Tag**: ApplicationQuestion

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Find all application questions (paginated) |
| GET | `/{id}` | Find by ID |
| POST | `/` | Create |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |
| GET | `/{id}/exists` | Check exists |
| GET | `/application/{applicationId}` | Find by application (paginated) |

#### ApplicationQuestionFileController
**Base Path**: `/api/application-question-files`
**Tag**: ApplicationQuestionFile

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Find all application question files (paginated) |
| GET | `/{id}` | Find by ID |
| POST | `/` | Create |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |
| GET | `/{id}/exists` | Check exists |
| GET | `/application/{applicationId}` | Find by application (paginated) |

---

### 2. Raider Data

Manage raider profiles and related statistics.

#### RaiderController
**Base Path**: `/api/v1/raiders`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Create a new raider |
| GET | `/{id}` | Get raider by ID |
| PUT | `/{id}` | Update raider |
| DELETE | `/{id}` | Delete raider |
| GET | `/guild/{guildId}/all` | Get all raiders for a guild (non-paginated) |
| GET | `/guild/{guildId}` | Get raiders for a guild (paginated) |

#### RaiderEntityController
**Base Path**: `/api/raider-entities`
**Tag**: RaiderEntity

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Find all raiders (paginated) |
| GET | `/{id}` | Find raider by ID |
| POST | `/` | Create a new raider |
| PUT | `/{id}` | Update raider |
| DELETE | `/{id}` | Delete raider |
| GET | `/{id}/exists` | Check if raider exists |
| GET | `/realm/{realm}` | Find raiders by realm (paginated) |
| GET | `/region/{region}` | Find raiders by region (paginated) |
| GET | `/realm/{realm}/count` | Count raiders for a realm |

#### RaiderCrestCountController
**Base Path**: `/api/raider-crest-counts`
**Tag**: RaiderCrestCount

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Find all raider crest counts (paginated) |
| GET | `/{id}` | Find by ID |
| POST | `/` | Create |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |
| GET | `/{id}/exists` | Check exists |
| GET | `/raider/{raiderId}` | Find by raider ID (paginated) |

#### RaiderPvpBracketController
**Base Path**: `/api/raider-pvp-brackets`
**Tag**: RaiderPvpBracket

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Find all PvP bracket stats (paginated) |
| GET | `/{id}` | Find by ID |
| POST | `/` | Create |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |
| GET | `/{id}/exists` | Check exists |
| GET | `/raider/{raiderId}` | Find by raider ID (paginated) |

#### RaiderRaidProgressController
**Base Path**: `/api/raider-raid-progress`
**Tag**: RaiderRaidProgress

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Find all raid progress (paginated) |
| GET | `/{id}` | Find by ID |
| POST | `/` | Create |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |
| GET | `/{id}/exists` | Check exists |
| GET | `/raider/{raiderId}` | Find by raider ID (paginated) |

#### RaiderRenownController
**Base Path**: `/api/raider-renown`
**Tag**: RaiderRenown

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Find all raider renown (paginated) |
| GET | `/{id}` | Find by ID |
| POST | `/` | Create |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |
| GET | `/{id}/exists` | Check exists |
| GET | `/raider/{raiderId}` | Find by raider ID (paginated) |

#### RaiderTrackItemController
**Base Path**: `/api/raider-track-items`
**Tag**: RaiderTrackItem

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Find all track items (paginated) |
| GET | `/{id}` | Find by ID |
| POST | `/` | Create |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |
| GET | `/{id}/exists` | Check exists |
| GET | `/raider/{raiderId}` | Find by raider ID (paginated) |

#### RaiderWarcraftLogController
**Base Path**: `/api/raider-warcraft-logs`
**Tag**: RaiderWarcraftLog

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Find all Warcraft Logs scores (paginated) |
| GET | `/{id}` | Find by ID |
| POST | `/` | Create |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |
| GET | `/{id}/exists` | Check exists |
| GET | `/raider/{raiderId}` | Find by raider ID (paginated) |

#### RaiderGearItemController
**Base Path**: `/api/raider-gear-items`
**Tag**: RaiderGearItem

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Find all gear items (paginated) |
| GET | `/{id}` | Find by ID |
| POST | `/` | Create |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |
| GET | `/{id}/exists` | Check exists |
| GET | `/raider/{raiderId}` | Find by raider ID (paginated) |
| GET | `/raider/{raiderId}/gear-set/{gearSet}` | Find by raider and gear set (paginated) |

#### RaiderStatisticsController
**Base Path**: `/api/raider-statistics`
**Tag**: RaiderStatistics

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Find all raider statistics (paginated) |
| GET | `/{id}` | Find by ID |
| POST | `/` | Create |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |
| GET | `/{id}/exists` | Check exists |
| GET | `/raider/{raiderId}` | Find by raider ID |

#### RaiderVaultSlotController
**Base Path**: `/api/raider-vault-slots`
**Tag**: RaiderVaultSlot

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Find all vault slots (paginated) |
| GET | `/{id}` | Find by ID |
| POST | `/` | Create |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |
| GET | `/{id}/exists` | Check exists |
| GET | `/raider/{raiderId}` | Find by raider (paginated) |
| GET | `/raider/{raiderId}/unlocked` | Find unlocked slots by raider (paginated) |
| GET | `/raider/{raiderId}/count` | Count vault slots for raider |

---

### 3. Loot Management

Manage loot awards, bans, and history.

#### LootController
**Base Path**: `/api/v1/loot`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/awards` | Award loot to a raider |
| GET | `/guilds/{guildId}/history` | Get guild loot history |
| GET | `/raiders/{raiderId}/history` | Get raider loot history |
| POST | `/bans` | Create a loot ban |
| DELETE | `/bans/{banId}` | Remove a loot ban |
| GET | `/raiders/{raiderId}/bans` | Get active bans for raider |
| GET | `/awards/all` | Get all loot awards (non-paginated) |
| GET | `/awards` | Get loot awards (paginated) |
| GET | `/awards/{awardId}` | Get specific loot award |
| DELETE | `/awards/{awardId}` | Revoke loot award |
| GET | `/bans/{banId}` | Get specific loot ban |
| PUT | `/bans/{banId}` | Update loot ban |

**Query Parameters for History**:
- `activeOnly` (boolean, default: false): Filter to active awards only

#### LootAwardController
**Base Path**: `/api/loot-awards`
**Tag**: LootAward

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Find all loot awards (paginated) |
| GET | `/{id}` | Find by ID |
| POST | `/` | Create |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |
| GET | `/{id}/exists` | Check exists |
| GET | `/raider/{raiderId}` | Find by raider (paginated) |
| GET | `/item/{itemId}` | Find by item (paginated) |
| GET | `/tier/{tier}` | Find by tier (paginated) |
| GET | `/raider/{raiderId}/count` | Count by raider |

#### LootBanController
**Base Path**: `/api/loot-bans`
**Tag**: LootBan

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Find all loot bans (paginated) |
| GET | `/{id}` | Find by ID |
| POST | `/` | Create |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |
| GET | `/{id}/exists` | Check exists |
| GET | `/guild/{guildId}` | Find by guild (paginated) |
| GET | `/guild/{guildId}/active` | Find active by guild (paginated) |
| GET | `/guild/{guildId}/check/{characterName}` | Check if character is banned |
| GET | `/guild/{guildId}/count` | Count by guild |

#### LootAwardBonusIdController
**Base Path**: `/api/loot-award-bonus-ids`
**Tag**: LootAwardBonusId

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Find all (paginated) |
| GET | `/{id}` | Find by ID |
| POST | `/` | Create |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |
| GET | `/{id}/exists` | Check exists |
| GET | `/loot-award/{lootAwardId}` | Find by loot award (paginated) |

#### LootAwardOldItemController
**Base Path**: `/api/loot-award-old-items`
**Tag**: LootAwardOldItem

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Find all (paginated) |
| GET | `/{id}` | Find by ID |
| POST | `/` | Create |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |
| GET | `/{id}/exists` | Check exists |
| GET | `/loot-award/{lootAwardId}` | Find by loot award (paginated) |

#### LootAwardWishDataController
**Base Path**: `/api/loot-award-wish-data`
**Tag**: LootAwardWishData

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Find all (paginated) |
| GET | `/{id}` | Find by ID |
| POST | `/` | Create |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |
| GET | `/{id}/exists` | Check exists |
| GET | `/loot-award/{lootAwardId}` | Find by loot award (paginated) |

---

### 4. Guild and Team

Manage guild configurations and team structures.

#### GuildController
**Base Path**: `/api/v1/guilds`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Create a new guild |
| GET | `/{id}` | Get guild by ID |
| PUT | `/{id}` | Update guild |
| DELETE | `/{id}` | Delete guild |
| GET | `/` | List all guilds |
| GET | `/active` | List active guilds only |

#### GuildConfigurationController
**Base Path**: `/api/guild-configurations`
**Tag**: GuildConfiguration

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Find all configurations (paginated) |
| GET | `/{id}` | Find by ID |
| GET | `/guild/{guildId}` | Find by guild ID |
| POST | `/` | Create |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |
| GET | `/{id}/exists` | Check exists |
| GET | `/active` | Find active configurations (paginated) |
| PUT | `/{id}/benchmark` | Update benchmark configuration |
| PUT | `/guild/{guildId}/sync-status` | Update sync status |

#### TeamMetadataController
**Base Path**: `/api/team-metadata`
**Tag**: TeamMetadata

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Find all team metadata (paginated) |
| GET | `/{teamId}` | Find by team ID |
| POST | `/` | Create |
| PUT | `/{teamId}` | Update |
| DELETE | `/{teamId}` | Delete |
| GET | `/{teamId}/exists` | Check exists |
| GET | `/guild/{guildId}` | Find by guild (paginated) |
| GET | `/region/{region}` | Find by region (paginated) |
| GET | `/guild/{guildId}/count` | Count by guild |

#### TeamRaidDayController
**Base Path**: `/api/team-raid-days`
**Tag**: TeamRaidDay

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Find all raid days (paginated) |
| GET | `/{id}` | Find by ID |
| POST | `/` | Create |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |
| GET | `/{id}/exists` | Check exists |
| GET | `/team/{teamId}` | Find by team (paginated) |
| GET | `/team/{teamId}/count` | Count by team |

---

### 5. Raid Management

Manage raids, encounters, and signups.

#### RaidController
**Base Path**: `/api/v1/raids`
**Tag**: Raids

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Get all raids (paginated) |
| GET | `/{id}` | Get raid by ID |
| POST | `/` | Create raid |
| PUT | `/{id}` | Update raid |
| DELETE | `/{id}` | Delete raid |
| GET | `/{id}/exists` | Check exists |
| GET | `/team/{teamId}` | Get raids by team (paginated) |
| GET | `/date-range` | Get raids by date range (paginated) |
| GET | `/team/{teamId}/count` | Count by team |

**Query Parameters for Date Range**:
- `startDate` (ISO date): Start date
- `endDate` (ISO date): End date

#### RaidEncounterController
**Base Path**: `/api/v1/raid-encounters`
**Tag**: Raid Encounters

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Get all encounters (paginated) |
| GET | `/{id}` | Get encounter by ID |
| POST | `/` | Create encounter |
| PUT | `/{id}` | Update encounter |
| DELETE | `/{id}` | Delete encounter |
| GET | `/{id}/exists` | Check exists |
| GET | `/raid/{raidId}` | Get encounters by raid (paginated) |
| GET | `/raid/{raidId}/enabled` | Get enabled encounters by raid (paginated) |
| GET | `/raid/{raidId}/count` | Count by raid |

#### RaidSignupController
**Base Path**: `/api/v1/raid-signups`
**Tag**: Raid Signups

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Get all signups (paginated) |
| GET | `/{id}` | Get signup by ID |
| POST | `/` | Create signup |
| PUT | `/{id}` | Update signup |
| DELETE | `/{id}` | Delete signup |
| GET | `/{id}/exists` | Check exists |
| GET | `/raid/{raidId}` | Get signups by raid (paginated) |
| GET | `/raid/{raidId}/selected` | Get selected signups by raid (paginated) |
| GET | `/character/{characterId}` | Get signups by character (paginated) |
| GET | `/raid/{raidId}/count` | Count by raid |

---

### 6. FLPS System

FLPS (Final Loot Priority Score) calculation and configuration.

#### FlpsController
**Base Path**: Multiple paths (legacy and v1)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/flps/{guildId}` | Get FLPS report (legacy) |
| GET | `/api/v1/flps/guilds/{guildId}/report` | Get FLPS report (v1) |
| GET | `/api/flps/{guildId}/benchmarks` | Get perfect score benchmarks |
| GET | `/api/flps/status` | Get system status (legacy) |
| GET | `/api/v1/flps/status` | Get system status (v1) |

**Response Structure (FLPS Report)**:
```json
{
  "guildId": "guild-123",
  "raiders": [...],
  "generatedAt": "2026-01-14T10:00:00Z"
}
```

#### FlpsModifierController
**Base Path**: `/api/v1/flps-modifiers`
**Tag**: FLPS Modifiers

**Default Modifier Endpoints**:

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/defaults` | Get all default modifiers (paginated) |
| GET | `/defaults/{id}` | Get default modifier by ID |
| POST | `/defaults` | Create default modifier |
| PUT | `/defaults/{id}` | Update default modifier |
| DELETE | `/defaults/{id}` | Delete default modifier |
| GET | `/defaults/{id}/exists` | Check exists |
| GET | `/defaults/category/{category}` | Get by category (paginated) |

**Guild Modifier Endpoints**:

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/guilds` | Get all guild modifiers (paginated) |
| GET | `/guilds/{id}` | Get guild modifier by ID |
| POST | `/guilds` | Create guild modifier |
| PUT | `/guilds/{id}` | Update guild modifier |
| DELETE | `/guilds/{id}` | Delete guild modifier |
| GET | `/guilds/{id}/exists` | Check exists |
| GET | `/guilds/guild/{guildId}` | Get by guild (paginated) |
| GET | `/guilds/guild/{guildId}/category/{category}` | Get by guild and category (paginated) |
| GET | `/guilds/guild/{guildId}/count` | Count by guild |

---

### 7. Attendance

Track and report raid attendance.

#### AttendanceController
**Base Path**: `/api/v1/attendance`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/track` | Track attendance |
| GET | `/raiders/{raiderId}/report` | Get attendance report |
| GET | `/{recordId}` | Get attendance record by ID |
| PUT | `/{recordId}` | Update attendance record |
| DELETE | `/{recordId}` | Delete attendance record |
| GET | `/raider/{raiderId}` | Get raider attendance history |
| GET | `/guild/{guildId}/summary` | Get guild attendance summary |

**Query Parameters for Report**:
- `guildId` (required): Guild identifier
- `startDate` (required, ISO date): Start date
- `endDate` (required, ISO date): End date
- `instance` (optional): Raid instance name
- `encounter` (optional): Encounter name (requires instance)

#### AttendanceStatController
**Base Path**: `/api/attendance-stats`
**Tag**: AttendanceStat

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Find all attendance stats (paginated) |
| GET | `/{id}` | Find by ID |
| POST | `/` | Create |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |
| GET | `/{id}/exists` | Check exists |
| GET | `/character/{characterId}` | Find by character (paginated) |
| GET | `/team/{teamId}` | Find by team (paginated) |
| GET | `/season/{seasonId}` | Find by season (paginated) |
| GET | `/character/{characterId}/count` | Count by character |

---

### 8. Snapshots

Data snapshots for point-in-time records.

#### PeriodSnapshotController
**Base Path**: `/api/period-snapshots`
**Tag**: PeriodSnapshot

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Find all (paginated) |
| GET | `/{id}` | Find by ID |
| POST | `/` | Create |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |
| GET | `/{id}/exists` | Check exists |
| GET | `/team/{teamId}` | Find by team (paginated) |

#### WishlistSnapshotController
**Base Path**: `/api/wishlist-snapshots`
**Tag**: WishlistSnapshot

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Find all (paginated) |
| GET | `/{id}` | Find by ID |
| POST | `/` | Create |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |
| GET | `/{id}/exists` | Check exists |
| GET | `/raider/{raiderId}` | Find by raider (paginated) |
| GET | `/team/{teamId}` | Find by team (paginated) |

#### WoWAuditSnapshotController
**Base Path**: `/api/wowaudit-snapshots`
**Tag**: WoWAuditSnapshot

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Find all (paginated) |
| GET | `/{id}` | Find by ID |
| POST | `/` | Create |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |
| GET | `/{id}/exists` | Check exists |
| GET | `/endpoint` | Find by endpoint (paginated) |

---

### 9. Other Controllers

#### SimulationController
**Base Path**: `/api/v1/simulation`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/guilds/{guildId}/characters/{characterName}` | Submit simulation |
| GET | `/requests/{requestId}` | Get simulation status |
| GET | `/guilds/{guildId}/characters/{characterName}/realms/{characterRealm}/results` | Get simulation results |
| GET | `/guilds/{guildId}/pending` | Get pending simulations |
| POST | `/execute-pending` | Execute pending simulations |
| GET | `/status` | Get simulation service status |

#### SyncRunController
**Base Path**: `/api/sync-runs`
**Tag**: SyncRun

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Find all sync runs (paginated) |
| GET | `/{id}` | Find by ID |
| POST | `/` | Create |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |
| GET | `/{id}/exists` | Check exists |
| GET | `/source/{source}` | Find by source (paginated) |
| GET | `/status/{status}` | Find by status (paginated) |
| GET | `/source/{source}/count` | Count by source |

#### BehavioralActionController
**Base Path**: `/api/behavioral-actions`
**Tag**: BehavioralAction

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Find all (paginated) |
| GET | `/{id}` | Find by ID |
| POST | `/` | Create |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |
| GET | `/{id}/exists` | Check exists |
| GET | `/guild/{guildId}` | Find by guild (paginated) |
| GET | `/guild/{guildId}/active` | Find active by guild (paginated) |
| GET | `/guild/{guildId}/character/{characterName}` | Find by character (paginated) |
| GET | `/guild/{guildId}/character/{characterName}/total-deduction` | Get total deduction |
| GET | `/guild/{guildId}/count` | Count by guild |

#### CharacterHistoryController
**Base Path**: `/api/character-history`
**Tag**: CharacterHistory

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Find all (paginated) |
| GET | `/{id}` | Find by ID |
| POST | `/` | Create |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |
| GET | `/{id}/exists` | Check exists |
| GET | `/character/{characterId}` | Find by character (paginated) |
| GET | `/team/{teamId}` | Find by team (paginated) |
| GET | `/character/{characterId}/count` | Count by character |

#### GuestController
**Base Path**: `/api/guests`
**Tag**: Guest

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Find all (paginated) |
| GET | `/{id}` | Find by ID |
| POST | `/` | Create |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |
| GET | `/{id}/exists` | Check exists |

#### HistoricalActivityController
**Base Path**: `/api/historical-activities`
**Tag**: HistoricalActivity

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Find all (paginated) |
| GET | `/{id}` | Find by ID |
| POST | `/` | Create |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |
| GET | `/{id}/exists` | Check exists |
| GET | `/character/{characterId}` | Find by character (paginated) |
| GET | `/team/{teamId}` | Find by team (paginated) |

#### GearController
**Base Path**: `/api/v1/gear`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/raider/{raiderId}` | Get current equipped gear |
| GET | `/raider/{raiderId}/type/{type}` | Get gear by type (EQUIPPED or BEST) |
| POST | `/raider/{raiderId}` | Create gear |
| PUT | `/raider/{raiderId}` | Update gear |

#### WishlistController
**Base Path**: `/api/v1/wishlists`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/raider/{raiderId}` | Get raider's wishlist |
| POST | `/` | Create wishlist |
| PUT | `/raider/{raiderId}` | Update wishlist |
| DELETE | `/raider/{raiderId}` | Delete wishlist |

---

## Error Handling

### Standard Error Response

```json
{
  "timestamp": "2026-01-14T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Entity with ID 1 not found",
  "path": "/api/v1/raiders/1"
}
```

### HTTP Status Codes

| Code | Description | Usage |
|------|-------------|-------|
| 200 OK | Success | GET, PUT, PATCH |
| 201 Created | Resource created | POST |
| 204 No Content | Success (no body) | DELETE |
| 400 Bad Request | Invalid input | Validation errors |
| 404 Not Found | Resource not found | Entity missing |
| 500 Internal Server Error | Server error | Unexpected errors |

---

## OpenAPI Documentation

All endpoints are documented with OpenAPI 3.0 annotations.

### Access Documentation

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

### Controller Tags

Each controller is tagged for organization in Swagger UI:

| Tag | Description |
|-----|-------------|
| Application | Guild applications |
| ApplicationAlt | Application alt characters |
| ApplicationQuestion | Application questions |
| ApplicationQuestionFile | Application question files |
| RaiderEntity | Raider entity management |
| RaiderCrestCount | Raider crest counts |
| RaiderPvpBracket | PvP bracket stats |
| RaiderRaidProgress | Raid progress |
| RaiderRenown | Renown data |
| RaiderTrackItem | Track items |
| RaiderWarcraftLog | Warcraft Logs scores |
| RaiderGearItem | Gear items |
| RaiderStatistics | Raider statistics |
| RaiderVaultSlot | Vault slots |
| LootAward | Loot awards |
| LootBan | Loot bans |
| LootAwardBonusId | Award bonus IDs |
| LootAwardOldItem | Award old items |
| LootAwardWishData | Award wish data |
| GuildConfiguration | Guild configurations |
| TeamMetadata | Team metadata |
| TeamRaidDay | Team raid days |
| Raids | Raid management |
| Raid Encounters | Encounter management |
| Raid Signups | Signup management |
| FLPS Modifiers | FLPS modifier management |
| AttendanceStat | Attendance statistics |
| PeriodSnapshot | Period snapshots |
| WishlistSnapshot | Wishlist snapshots |
| WoWAuditSnapshot | WoWAudit snapshots |
| SyncRun | Sync run management |
| BehavioralAction | Behavioral actions |
| CharacterHistory | Character history |
| Guest | Guest management |
| HistoricalActivity | Historical activities |

---

## Quick Reference

### Total Endpoints Summary

| Category | Count |
|----------|-------|
| **Total Controllers** | 43 |
| **GET Endpoints** | ~150 |
| **POST Endpoints** | ~45 |
| **PUT Endpoints** | ~45 |
| **DELETE Endpoints** | ~45 |

### Common Query Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `page` | int | Page number (0-indexed) |
| `size` | int | Page size |
| `guildId` | string | Guild identifier |
| `raiderId` | long | Raider identifier |
| `startDate` | date | Start date (ISO format) |
| `endDate` | date | End date (ISO format) |

---

**Document Version**: 2.0.0
**Last Updated**: January 14, 2026
**Status**: Complete
