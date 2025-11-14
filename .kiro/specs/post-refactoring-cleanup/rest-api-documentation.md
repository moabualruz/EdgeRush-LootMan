# REST API Documentation

**Generated**: November 14, 2025  
**Version**: 1.0.0  
**Base URL**: `http://localhost:8080` (Development) | `https://api.edgerush.com` (Production)

## Overview

This document provides comprehensive documentation for all REST API endpoints in the EdgeRush LootMan system after the domain-driven design refactoring. The API is organized into two main packages:

- **datasync.api.v1**: Original package with core guild management functionality
- **lootman.api**: New domain-driven package with bounded context architecture

## Authentication

Most endpoints require JWT bearer token authentication. Include the token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

### Roles

- **SYSTEM_ADMIN**: Full system access
- **GUILD_ADMIN**: Guild-specific administrative access  
- **PUBLIC_USER**: Read-only access to public data

## API Endpoints

### 1. Application Management

**Controller**: `ApplicationController`  
**Package**: `com.edgerush.datasync.api.v1`  
**Base Path**: `/api/v1/applications`

#### 1.1 Get Application by ID

**Endpoint**: `GET /api/v1/applications/{id}`  
**Description**: Retrieve a specific guild application by its ID  
**Authentication**: Required

**Path Parameters**:
- `id` (Long): Application ID

**Response**: `200 OK`
```json
{
  "id": 1,
  "characterName": "PlayerName",
  "realm": "Stormrage",
  "class": "Mage",
  "spec": "Fire",
  "status": "PENDING",
  "submittedAt": "2025-11-14T10:00:00Z",
  "reviewedAt": null,
  "notes": "Looking to join progression team"
}
```

**Error Responses**:
- `404 Not Found`: Application not found
- `500 Internal Server Error`: Server error

#### 1.2 Get Applications (with optional filter)

**Endpoint**: `GET /api/v1/applications`  
**Description**: Retrieve all applications, optionally filtered by status  
**Authentication**: Required

**Query Parameters**:
- `status` (String, optional): Filter by application status (PENDING, APPROVED, REJECTED, WITHDRAWN)

**Response**: `200 OK`
```json
[
  {
    "id": 1,
    "characterName": "PlayerName",
    "status": "PENDING",
    ...
  }
]
```

#### 1.3 Review Application

**Endpoint**: `POST /api/v1/applications/{id}/review`  
**Description**: Review and approve/reject an application  
**Authentication**: Required (GUILD_ADMIN or SYSTEM_ADMIN)

**Path Parameters**:
- `id` (Long): Application ID

**Request Body**:
```json
{
  "action": "APPROVE"
}
```

**Response**: `200 OK` (ApplicationDto)

**Error Responses**:
- `400 Bad Request`: Invalid action
- `404 Not Found`: Application not found
- `422 Unprocessable Entity`: Requirements not met
- `500 Internal Server Error`: Server error

#### 1.4 Withdraw Application

**Endpoint**: `POST /api/v1/applications/{id}/withdraw`  
**Description**: Withdraw a submitted application  
**Authentication**: Required

**Path Parameters**:
- `id` (Long): Application ID

**Response**: `200 OK` (ApplicationDto)

**Error Responses**:
- `400 Bad Request`: Invalid request
- `404 Not Found`: Application not found
- `500 Internal Server Error`: Server error

---

### 2. FLPS (Final Loot Priority Score) - DataSync

**Controller**: `FlpsController`  
**Package**: `com.edgerush.datasync.api.v1`  
**Base Path**: `/api/v1/flps`

#### 2.1 Calculate FLPS Score

**Endpoint**: `POST /api/v1/flps/calculate`  
**Description**: Calculate FLPS score for a single raider and item combination  
**Authentication**: Required

**Request Body**:
```json
{
  "guildId": "guild-123",
  "attendancePercent": 0.95,
  "deathsPerAttempt": 0.5,
  "specAvgDpa": 1.2,
  "avoidableDamagePct": 0.15,
  "specAvgAdt": 0.20,
  "vaultSlots": 3,
  "crestUsageRatio": 0.8,
  "heroicKills": 10,
  "simulatedGain": 0.05,
  "specBaseline": 1000000,
  "tierPiecesOwned": 2,
  "role": "DPS",
  "recentLootCount": 1
}
```

**Response**: `200 OK`
```json
{
  "flpsScore": 0.85,
  "raiderMerit": 0.92,
  "itemPriority": 0.88,
  "recencyDecay": 0.95,
  "attendanceScore": 0.95,
  "mechanicalScore": 0.90,
  "preparationScore": 0.85,
  "upgradeValue": 0.85,
  "tierBonus": 0.90,
  "roleMultiplier": 1.0
}
```

#### 2.2 Generate FLPS Report

**Endpoint**: `POST /api/v1/flps/report`  
**Description**: Generate comprehensive FLPS report for all raiders in a guild  
**Authentication**: Required

**Request Body**:
```json
{
  "guildId": "guild-123",
  "raiders": [
    {
      "raiderId": "raider-1",
      "raiderName": "PlayerOne",
      "attendancePercent": 0.95,
      "deathsPerAttempt": 0.5,
      ...
    }
  ]
}
```

**Response**: `200 OK`
```json
{
  "guildId": "guild-123",
  "raiderReports": [
    {
      "raiderId": "raider-1",
      "raiderName": "PlayerOne",
      "flpsScore": 0.85,
      "raiderMerit": 0.92,
      "itemPriority": 0.88,
      "recencyDecay": 0.95,
      ...
    }
  ],
  "configuration": {
    "rmsWeights": {
      "attendance": 0.4,
      "mechanical": 0.4,
      "preparation": 0.2
    },
    ...
  }
}
```

#### 2.3 Update FLPS Modifiers

**Endpoint**: `PUT /api/v1/flps/{guildId}/modifiers`  
**Description**: Update FLPS configuration and modifiers for a guild  
**Authentication**: Required (GUILD_ADMIN or SYSTEM_ADMIN)

**Path Parameters**:
- `guildId` (String): Guild identifier

**Request Body**:
```json
{
  "rmsWeights": {
    "attendance": 0.4,
    "mechanical": 0.4,
    "preparation": 0.2
  },
  "ipiWeights": {
    "upgradeValue": 0.45,
    "tierBonus": 0.35,
    "roleMultiplier": 0.20
  },
  "thresholds": {
    "eligibilityAttendance": 0.75,
    "eligibilityActivity": 0.5
  },
  "roleMultipliers": {
    "tank": 1.2,
    "healer": 1.1,
    "dps": 1.0
  },
  "recencyPenalties": {
    "tierA": 0.5,
    "tierB": 0.7,
    "tierC": 0.9,
    "recoveryRate": 0.1
  }
}
```

**Response**: `204 No Content`

#### 2.4 Get FLPS Modifiers

**Endpoint**: `GET /api/v1/flps/{guildId}/modifiers`  
**Description**: Get current FLPS configuration for a guild  
**Authentication**: Required

**Path Parameters**:
- `guildId` (String): Guild identifier

**Response**: `501 Not Implemented` (Currently not implemented)

---

### 3. Guild Management

**Controller**: `GuildController`  
**Package**: `com.edgerush.datasync.api.v1`  
**Base Path**: `/api/v1/guilds`

#### 3.1 Get All Guilds

**Endpoint**: `GET /api/v1/guilds`  
**Description**: Retrieve all active guilds  
**Authentication**: Required

**Response**: `200 OK`
```json
[
  {
    "guildId": "guild-123",
    "name": "EdgeRush",
    "realm": "Stormrage",
    "region": "US",
    "syncEnabled": true,
    "benchmarkMode": "THEORETICAL",
    ...
  }
]
```

#### 3.2 Get Guild Configuration

**Endpoint**: `GET /api/v1/guilds/{guildId}`  
**Description**: Retrieve guild configuration by ID  
**Authentication**: Required

**Path Parameters**:
- `guildId` (String): Guild identifier

**Response**: `200 OK` (GuildDto)

**Error Responses**:
- `404 Not Found`: Guild not found
- `500 Internal Server Error`: Server error

#### 3.3 Update Benchmark Configuration

**Endpoint**: `PATCH /api/v1/guilds/{guildId}/benchmark`  
**Description**: Update guild benchmark configuration  
**Authentication**: Required (GUILD_ADMIN or SYSTEM_ADMIN)

**Path Parameters**:
- `guildId` (String): Guild identifier

**Request Body**:
```json
{
  "mode": "CUSTOM",
  "customRms": 0.95,
  "customIpi": 0.90
}
```

**Response**: `200 OK` (GuildDto)

**Error Responses**:
- `400 Bad Request`: Invalid configuration
- `500 Internal Server Error`: Server error

#### 3.4 Update WoWAudit Configuration

**Endpoint**: `PATCH /api/v1/guilds/{guildId}/wowaudit`  
**Description**: Update WoWAudit API configuration for a guild  
**Authentication**: Required (GUILD_ADMIN or SYSTEM_ADMIN)

**Path Parameters**:
- `guildId` (String): Guild identifier

**Request Body**:
```json
{
  "apiKey": "wowaudit-api-key",
  "guildUri": "https://wowaudit.com/guild/edgerush"
}
```

**Response**: `200 OK` (GuildDto)

#### 3.5 Enable/Disable Sync

**Endpoint**: `PATCH /api/v1/guilds/{guildId}/sync`  
**Description**: Enable or disable data synchronization for a guild  
**Authentication**: Required (GUILD_ADMIN or SYSTEM_ADMIN)

**Path Parameters**:
- `guildId` (String): Guild identifier

**Request Body**:
```json
{
  "enabled": true
}
```

**Response**: `200 OK` (GuildDto)

---

### 4. Integration Management

**Controller**: `IntegrationController`  
**Package**: `com.edgerush.datasync.api.v1`  
**Base Path**: `/api/v1/integrations`

#### 4.1 Sync WoWAudit (Full)

**Endpoint**: `POST /api/v1/integrations/wowaudit/sync`  
**Description**: Trigger full WoWAudit synchronization  
**Authentication**: Required (GUILD_ADMIN or SYSTEM_ADMIN)

**Response**: `200 OK`
```json
{
  "success": true,
  "source": "WOWAUDIT",
  "startTime": "2025-11-14T10:00:00Z",
  "endTime": "2025-11-14T10:05:00Z",
  "recordsProcessed": 150,
  "errors": []
}
```

#### 4.2 Sync WoWAudit Roster

**Endpoint**: `POST /api/v1/integrations/wowaudit/sync/roster`  
**Description**: Sync only WoWAudit roster data  
**Authentication**: Required (GUILD_ADMIN or SYSTEM_ADMIN)

**Response**: `200 OK` (SyncResultDto)

#### 4.3 Sync WoWAudit Loot

**Endpoint**: `POST /api/v1/integrations/wowaudit/sync/loot`  
**Description**: Sync only WoWAudit loot history  
**Authentication**: Required (GUILD_ADMIN or SYSTEM_ADMIN)

**Response**: `200 OK` (SyncResultDto)

#### 4.4 Sync Warcraft Logs (Single Guild)

**Endpoint**: `POST /api/v1/integrations/warcraftlogs/sync/{guildId}`  
**Description**: Trigger Warcraft Logs synchronization for a specific guild  
**Authentication**: Required (GUILD_ADMIN or SYSTEM_ADMIN)

**Path Parameters**:
- `guildId` (String): Guild identifier

**Response**: `200 OK` (SyncResultDto)

#### 4.5 Sync Warcraft Logs (All Guilds)

**Endpoint**: `POST /api/v1/integrations/warcraftlogs/sync/all`  
**Description**: Trigger Warcraft Logs synchronization for all guilds  
**Authentication**: Required (SYSTEM_ADMIN)

**Request Body**:
```json
["guild-123", "guild-456"]
```

**Response**: `200 OK` (SyncResultDto)

#### 4.6 Get Sync Status

**Endpoint**: `GET /api/v1/integrations/status/{source}`  
**Description**: Get latest sync status for a data source  
**Authentication**: Required

**Path Parameters**:
- `source` (String): Data source (WOWAUDIT, WARCRAFTLOGS)

**Response**: `200 OK`
```json
{
  "id": 1,
  "source": "WOWAUDIT",
  "status": "COMPLETED",
  "startTime": "2025-11-14T10:00:00Z",
  "endTime": "2025-11-14T10:05:00Z",
  "recordsProcessed": 150,
  "errors": []
}
```

**Error Responses**:
- `400 Bad Request`: Invalid source
- `404 Not Found`: No sync operations found

#### 4.7 Get Recent Sync Operations

**Endpoint**: `GET /api/v1/integrations/status/{source}/recent`  
**Description**: Get recent sync operations for a data source  
**Authentication**: Required

**Path Parameters**:
- `source` (String): Data source (WOWAUDIT, WARCRAFTLOGS)

**Query Parameters**:
- `limit` (Int, optional, default=10): Number of operations to return

**Response**: `200 OK` (List of SyncOperationDto)

#### 4.8 Get All Recent Sync Operations

**Endpoint**: `GET /api/v1/integrations/status/all`  
**Description**: Get all recent sync operations across all sources  
**Authentication**: Required

**Query Parameters**:
- `limit` (Int, optional, default=50): Number of operations to return

**Response**: `200 OK` (List of SyncOperationDto)

#### 4.9 Check Sync In Progress

**Endpoint**: `GET /api/v1/integrations/status/{source}/in-progress`  
**Description**: Check if a sync is currently in progress for a data source  
**Authentication**: Required

**Path Parameters**:
- `source` (String): Data source (WOWAUDIT, WARCRAFTLOGS)

**Response**: `200 OK`
```json
{
  "inProgress": false
}
```

---

### 5. Raider Management

**Controller**: `RaiderController`  
**Package**: `com.edgerush.datasync.api.v1`  
**Base Path**: `/api/v1/raiders`

#### 5.1 Get All Raiders

**Endpoint**: `GET /api/v1/raiders`  
**Description**: Retrieve all raiders  
**Authentication**: Required

**Response**: `200 OK` (List of RaiderDto)

#### 5.2 Get Active Raiders

**Endpoint**: `GET /api/v1/raiders/active`  
**Description**: Retrieve all active raiders  
**Authentication**: Required

**Response**: `200 OK` (List of RaiderDto)

#### 5.3 Get Raider by ID

**Endpoint**: `GET /api/v1/raiders/{id}`  
**Description**: Retrieve a specific raider by ID  
**Authentication**: Required

**Path Parameters**:
- `id` (Long): Raider ID

**Response**: `200 OK`
```json
{
  "id": 1,
  "characterName": "PlayerName",
  "realm": "Stormrage",
  "class": "Mage",
  "spec": "Fire",
  "role": "DPS",
  "status": "ACTIVE",
  "itemLevel": 450,
  ...
}
```

**Error Responses**:
- `404 Not Found`: Raider not found
- `500 Internal Server Error`: Server error

#### 5.4 Get Raider by Character

**Endpoint**: `GET /api/v1/raiders/character/{characterName}/realm/{realm}`  
**Description**: Retrieve a raider by character name and realm  
**Authentication**: Required

**Path Parameters**:
- `characterName` (String): Character name
- `realm` (String): Realm name

**Response**: `200 OK` (RaiderDto)

**Error Responses**:
- `404 Not Found`: Raider not found
- `500 Internal Server Error`: Server error

#### 5.5 Update Raider Status

**Endpoint**: `PATCH /api/v1/raiders/{id}/status`  
**Description**: Update raider status  
**Authentication**: Required (GUILD_ADMIN or SYSTEM_ADMIN)

**Path Parameters**:
- `id` (Long): Raider ID

**Request Body**:
```json
{
  "status": "INACTIVE"
}
```

**Response**: `200 OK` (RaiderDto)

**Error Responses**:
- `400 Bad Request`: Invalid status
- `500 Internal Server Error`: Server error

---

### 6. Attendance Management (Lootman)

**Controller**: `AttendanceController`  
**Package**: `com.edgerush.lootman.api.attendance`  
**Base Path**: `/api/v1/attendance`

#### 6.1 Track Attendance

**Endpoint**: `POST /api/v1/attendance/track`  
**Description**: Record attendance data for a raider  
**Authentication**: Required (GUILD_ADMIN or SYSTEM_ADMIN)

**Request Body**:
```json
{
  "raiderId": 1,
  "guildId": "guild-123",
  "instance": "Amirdrassil",
  "encounter": "Fyrakk",
  "startDate": "2025-11-01",
  "endDate": "2025-11-14",
  "attendedRaids": 8,
  "totalRaids": 10
}
```

**Response**: `201 Created`
```json
{
  "id": 1,
  "raiderId": 1,
  "guildId": "guild-123",
  "instance": "Amirdrassil",
  "encounter": "Fyrakk",
  "attendanceRate": 0.80,
  ...
}
```

#### 6.2 Get Attendance Report

**Endpoint**: `GET /api/v1/attendance/raiders/{raiderId}/report`  
**Description**: Retrieve attendance report for a raider  
**Authentication**: Required

**Path Parameters**:
- `raiderId` (Long): Raider ID

**Query Parameters**:
- `guildId` (String, required): Guild identifier
- `startDate` (Date, required): Start date (ISO format: yyyy-MM-dd)
- `endDate` (Date, required): End date (ISO format: yyyy-MM-dd)
- `instance` (String, optional): Filter by raid instance
- `encounter` (String, optional): Filter by specific encounter (requires instance)

**Response**: `200 OK`
```json
{
  "raiderId": 1,
  "guildId": "guild-123",
  "startDate": "2025-11-01",
  "endDate": "2025-11-14",
  "totalRaids": 10,
  "attendedRaids": 8,
  "attendanceRate": 0.80,
  "byInstance": {
    "Amirdrassil": {
      "totalRaids": 10,
      "attendedRaids": 8,
      "attendanceRate": 0.80
    }
  }
}
```

---

### 7. FLPS Management (Lootman)

**Controller**: `FlpsController`  
**Package**: `com.edgerush.lootman.api.flps`  
**Base Path**: `/api/v1/flps`

#### 7.1 Get FLPS Report

**Endpoint**: `GET /api/v1/flps/guilds/{guildId}/report`  
**Description**: Get comprehensive FLPS report for all raiders in a guild  
**Authentication**: Required

**Path Parameters**:
- `guildId` (String): Guild identifier

**Response**: `200 OK`
```json
{
  "guildId": "guild-123",
  "raiders": [
    {
      "raiderId": 1,
      "raiderName": "PlayerOne",
      "flpsScore": 0.85,
      "raiderMerit": 0.92,
      "itemPriority": 0.88,
      "recencyDecay": 0.95,
      "eligible": true,
      ...
    }
  ],
  "generatedAt": "2025-11-14T10:00:00Z"
}
```

#### 7.2 Get System Status

**Endpoint**: `GET /api/v1/flps/status`  
**Description**: Get FLPS system status and capabilities  
**Authentication**: Not required

**Response**: `200 OK`
```json
{
  "message": "FLPS calculation system using domain-driven architecture",
  "features": [
    "Domain-driven design with bounded contexts",
    "Test-driven development with 85%+ coverage",
    "Guild-specific FLPS modifiers",
    ...
  ],
  "endpoints": {
    "Guild Report": "/api/v1/flps/guilds/{guildId}/report",
    "System Status": "/api/v1/flps/status"
  }
}
```

---

### 8. Loot Management (Lootman)

**Controller**: `LootController`  
**Package**: `com.edgerush.lootman.api.loot`  
**Base Path**: `/api/v1/loot`

#### 8.1 Award Loot

**Endpoint**: `POST /api/v1/loot/awards`  
**Description**: Award loot to a raider  
**Authentication**: Required (GUILD_ADMIN or SYSTEM_ADMIN)

**Request Body**:
```json
{
  "itemId": "item-12345",
  "raiderId": "raider-1",
  "guildId": "guild-123",
  "flpsScore": 0.85,
  "tier": "TIER_A"
}
```

**Response**: `201 Created`
```json
{
  "id": "award-1",
  "itemId": "item-12345",
  "raiderId": "raider-1",
  "guildId": "guild-123",
  "flpsScore": 0.85,
  "tier": "TIER_A",
  "awardedAt": "2025-11-14T10:00:00Z"
}
```

#### 8.2 Get Guild Loot History

**Endpoint**: `GET /api/v1/loot/guilds/{guildId}/history`  
**Description**: Retrieve loot history for a guild  
**Authentication**: Required

**Path Parameters**:
- `guildId` (String): Guild identifier

**Query Parameters**:
- `activeOnly` (Boolean, optional, default=false): Filter to only active awards

**Response**: `200 OK`
```json
{
  "awards": [
    {
      "id": "award-1",
      "itemId": "item-12345",
      "raiderId": "raider-1",
      "flpsScore": 0.85,
      "tier": "TIER_A",
      "awardedAt": "2025-11-14T10:00:00Z"
    }
  ]
}
```

#### 8.3 Get Raider Loot History

**Endpoint**: `GET /api/v1/loot/raiders/{raiderId}/history`  
**Description**: Retrieve loot history for a specific raider  
**Authentication**: Required

**Path Parameters**:
- `raiderId` (String): Raider identifier

**Query Parameters**:
- `activeOnly` (Boolean, optional, default=false): Filter to only active awards

**Response**: `200 OK` (LootHistoryResponse)

#### 8.4 Create Loot Ban

**Endpoint**: `POST /api/v1/loot/bans`  
**Description**: Create a loot ban for a raider  
**Authentication**: Required (GUILD_ADMIN or SYSTEM_ADMIN)

**Request Body**:
```json
{
  "raiderId": "raider-1",
  "guildId": "guild-123",
  "reason": "Excessive absences",
  "expiresAt": "2025-12-14T10:00:00Z"
}
```

**Response**: `201 Created`
```json
{
  "id": "ban-1",
  "raiderId": "raider-1",
  "guildId": "guild-123",
  "reason": "Excessive absences",
  "createdAt": "2025-11-14T10:00:00Z",
  "expiresAt": "2025-12-14T10:00:00Z",
  "active": true
}
```

#### 8.5 Remove Loot Ban

**Endpoint**: `DELETE /api/v1/loot/bans/{banId}`  
**Description**: Remove a loot ban  
**Authentication**: Required (GUILD_ADMIN or SYSTEM_ADMIN)

**Path Parameters**:
- `banId` (String): Ban identifier

**Response**: `204 No Content`

#### 8.6 Get Active Bans

**Endpoint**: `GET /api/v1/loot/raiders/{raiderId}/bans`  
**Description**: Get active loot bans for a raider  
**Authentication**: Required

**Path Parameters**:
- `raiderId` (String): Raider identifier

**Query Parameters**:
- `guildId` (String, required): Guild identifier

**Response**: `200 OK`
```json
{
  "bans": [
    {
      "id": "ban-1",
      "raiderId": "raider-1",
      "guildId": "guild-123",
      "reason": "Excessive absences",
      "createdAt": "2025-11-14T10:00:00Z",
      "expiresAt": "2025-12-14T10:00:00Z",
      "active": true
    }
  ]
}
```

---

## OpenAPI/Swagger Documentation

The API includes OpenAPI 3.0 documentation accessible at:

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

### Configuration

The OpenAPI configuration includes:

- **Title**: EdgeRush LootMan API
- **Version**: 1.0.0
- **Security**: JWT Bearer authentication
- **Servers**:
  - Local Development: `http://localhost:8080`
  - Production: `https://api.edgerush.com`

### Admin Mode

When running in development mode with admin mode enabled, authentication is bypassed and all requests are treated as SYSTEM_ADMIN. This is indicated in the API documentation.

---

## API Summary

### Endpoint Count by Controller

| Controller | Package | Endpoints | Base Path |
|------------|---------|-----------|-----------|
| ApplicationController | datasync.api.v1 | 4 | /api/v1/applications |
| FlpsController | datasync.api.v1 | 4 | /api/v1/flps |
| GuildController | datasync.api.v1 | 5 | /api/v1/guilds |
| IntegrationController | datasync.api.v1 | 9 | /api/v1/integrations |
| RaiderController | datasync.api.v1 | 5 | /api/v1/raiders |
| AttendanceController | lootman.api.attendance | 2 | /api/v1/attendance |
| FlpsController | lootman.api.flps | 2 | /api/v1/flps |
| LootController | lootman.api.loot | 6 | /api/v1/loot |

**Total Endpoints**: 37

### Endpoints by HTTP Method

- **GET**: 17 endpoints
- **POST**: 13 endpoints
- **PATCH**: 4 endpoints
- **PUT**: 1 endpoint
- **DELETE**: 1 endpoint

### Endpoints by Authentication Requirement

- **Authentication Required**: 35 endpoints
- **Public Access**: 2 endpoints (FLPS status)

---

## Comparison with Pre-Refactoring API

### Status

No pre-refactoring API inventory was found in the codebase. A comparison cannot be performed at this time.

### Recommendation

To perform a comparison:
1. Locate or recreate pre-refactoring API documentation
2. Compare endpoint paths, methods, and functionality
3. Identify any removed or changed endpoints
4. Document breaking changes

---

## Notes

### Implementation Status

- All REST controllers are implemented and present in the codebase
- Integration tests are currently failing (100% failure rate)
- Endpoints may have runtime issues that need to be addressed
- Manual testing recommended once integration tests are fixed

### GraphQL Status

GraphQL is **NOT IMPLEMENTED**. It was planned as Phase 2 of the original specification. REST API is the only current implementation.

### Future Enhancements

1. Complete implementation of `GET /api/v1/flps/{guildId}/modifiers` endpoint
2. Add pagination support for list endpoints
3. Add filtering and sorting capabilities
4. Implement rate limiting
5. Add request/response validation
6. Enhance error responses with detailed error codes

---

**Document Version**: 1.0.0  
**Last Updated**: November 14, 2025  
**Status**: Complete - Ready for Review
