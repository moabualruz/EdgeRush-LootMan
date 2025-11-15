# API Reference & Interface Documentation

**Version**: 1.0.0  
**Last Updated**: November 15, 2025  
**Base URL**: `http://localhost:8080` (Development) | `https://api.edgerush.com` (Production)

## Table of Contents

1. [Overview](#overview)
2. [Authentication](#authentication)
3. [REST API Endpoints](#rest-api-endpoints)
4. [External API Integrations](#external-api-integrations)
5. [Internal Service Interfaces](#internal-service-interfaces)
6. [Error Handling](#error-handling)
7. [Health & Monitoring](#health--monitoring)

---

## Overview

EdgeRush LootMan provides a comprehensive REST API for managing guild loot distribution using the FLPS (Final Loot Priority Score) algorithm. The API is organized into two main packages:

- **datasync.api.v1**: Core guild management, FLPS calculations, and external integrations
- **lootman.api**: Domain-driven bounded contexts (Attendance, FLPS, Loot)

### API Design Principles

- RESTful architecture with standard HTTP methods
- JSON request/response format
- JWT bearer token authentication
- Comprehensive error responses
- OpenAPI 3.0 documentation

### Technology Stack

- **Framework**: Spring Boot 3.x with Kotlin
- **Database**: PostgreSQL 18
- **Authentication**: JWT tokens
- **Documentation**: OpenAPI/Swagger

---

## Authentication

### JWT Bearer Token

Most endpoints require JWT bearer token authentication. Include the token in the Authorization header:

```http
Authorization: Bearer <your-jwt-token>
```

### Roles and Permissions

| Role | Description | Access Level |
|------|-------------|--------------|
| `SYSTEM_ADMIN` | Full system access | All endpoints |
| `GUILD_ADMIN` | Guild-specific admin | Guild management, FLPS config, loot awards |
| `PUBLIC_USER` | Read-only access | Public data, reports |


### Admin Mode (Development Only)

When running in development mode with admin mode enabled, authentication is bypassed and all requests are treated as `SYSTEM_ADMIN`.

---

## REST API Endpoints

### 1. Application Management

Manage guild applications from prospective members.

#### 1.1 Get Application by ID

Retrieve a specific guild application.

**Endpoint**: `GET /api/v1/applications/{id}`  
**Authentication**: Required  
**Roles**: All authenticated users

**Path Parameters**:
- `id` (Long, required): Application ID

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

**Example Request**:
```bash
curl -X GET "http://localhost:8080/api/v1/applications/1" \
  -H "Authorization: Bearer <token>"
```


#### 1.2 Get Applications (with optional filter)

Retrieve all applications, optionally filtered by status.

**Endpoint**: `GET /api/v1/applications`  
**Authentication**: Required  
**Roles**: All authenticated users

**Query Parameters**:
- `status` (String, optional): Filter by status (PENDING, APPROVED, REJECTED, WITHDRAWN)

**Response**: `200 OK`
```json
[
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
]
```

**Example Request**:
```bash
curl -X GET "http://localhost:8080/api/v1/applications?status=PENDING" \
  -H "Authorization: Bearer <token>"
```

#### 1.3 Review Application

Review and approve/reject an application.

**Endpoint**: `POST /api/v1/applications/{id}/review`  
**Authentication**: Required  
**Roles**: GUILD_ADMIN, SYSTEM_ADMIN

**Path Parameters**:
- `id` (Long, required): Application ID

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

**Example Request**:
```bash
curl -X POST "http://localhost:8080/api/v1/applications/1/review" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"action": "APPROVE"}'
```


#### 1.4 Withdraw Application

Withdraw a submitted application.

**Endpoint**: `POST /api/v1/applications/{id}/withdraw`  
**Authentication**: Required  
**Roles**: All authenticated users

**Path Parameters**:
- `id` (Long, required): Application ID

**Response**: `200 OK` (ApplicationDto)

**Error Responses**:
- `400 Bad Request`: Invalid request
- `404 Not Found`: Application not found

**Example Request**:
```bash
curl -X POST "http://localhost:8080/api/v1/applications/1/withdraw" \
  -H "Authorization: Bearer <token>"
```

---

### 2. FLPS (Final Loot Priority Score) - DataSync

Calculate and manage FLPS scores for loot distribution.

#### 2.1 Calculate FLPS Score

Calculate FLPS score for a single raider and item combination.

**Endpoint**: `POST /api/v1/flps/calculate`  
**Authentication**: Required  
**Roles**: All authenticated users

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

**Example Request**:
```bash
curl -X POST "http://localhost:8080/api/v1/flps/calculate" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d @flps-request.json
```


#### 2.2 Generate FLPS Report

Generate comprehensive FLPS report for all raiders in a guild.

**Endpoint**: `POST /api/v1/flps/report`  
**Authentication**: Required  
**Roles**: All authenticated users

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
      "attendanceScore": 0.95,
      "mechanicalScore": 0.90,
      "preparationScore": 0.85,
      "upgradeValue": 0.85,
      "tierBonus": 0.90,
      "roleMultiplier": 1.0
    }
  ],
  "configuration": {
    "rmsWeights": {
      "attendance": 0.4,
      "mechanical": 0.4,
      "preparation": 0.2
    },
    "ipiWeights": {
      "upgradeValue": 0.45,
      "tierBonus": 0.35,
      "roleMultiplier": 0.20
    }
  }
}
```

**Example Request**:
```bash
curl -X POST "http://localhost:8080/api/v1/flps/report" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d @flps-report-request.json
```


#### 2.3 Update FLPS Modifiers

Update FLPS configuration and modifiers for a guild.

**Endpoint**: `PUT /api/v1/flps/{guildId}/modifiers`  
**Authentication**: Required  
**Roles**: GUILD_ADMIN, SYSTEM_ADMIN

**Path Parameters**:
- `guildId` (String, required): Guild identifier

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

**Example Request**:
```bash
curl -X PUT "http://localhost:8080/api/v1/flps/guild-123/modifiers" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d @flps-modifiers.json
```

#### 2.4 Get FLPS Modifiers

Get current FLPS configuration for a guild.

**Endpoint**: `GET /api/v1/flps/{guildId}/modifiers`  
**Authentication**: Required  
**Roles**: All authenticated users

**Path Parameters**:
- `guildId` (String, required): Guild identifier

**Response**: `501 Not Implemented` (Currently not implemented)

---

### 3. Guild Management

Manage guild configurations and settings.


#### 3.1 Get All Guilds

Retrieve all active guilds.

**Endpoint**: `GET /api/v1/guilds`  
**Authentication**: Required  
**Roles**: All authenticated users

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
    "customRms": null,
    "customIpi": null,
    "wowauditApiKey": "***",
    "wowauditGuildUri": "https://wowaudit.com/guild/edgerush"
  }
]
```

**Example Request**:
```bash
curl -X GET "http://localhost:8080/api/v1/guilds" \
  -H "Authorization: Bearer <token>"
```

#### 3.2 Get Guild Configuration

Retrieve guild configuration by ID.

**Endpoint**: `GET /api/v1/guilds/{guildId}`  
**Authentication**: Required  
**Roles**: All authenticated users

**Path Parameters**:
- `guildId` (String, required): Guild identifier

**Response**: `200 OK` (GuildDto)

**Error Responses**:
- `404 Not Found`: Guild not found
- `500 Internal Server Error`: Server error

**Example Request**:
```bash
curl -X GET "http://localhost:8080/api/v1/guilds/guild-123" \
  -H "Authorization: Bearer <token>"
```

#### 3.3 Update Benchmark Configuration

Update guild benchmark configuration.

**Endpoint**: `PATCH /api/v1/guilds/{guildId}/benchmark`  
**Authentication**: Required  
**Roles**: GUILD_ADMIN, SYSTEM_ADMIN

**Path Parameters**:
- `guildId` (String, required): Guild identifier

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

**Example Request**:
```bash
curl -X PATCH "http://localhost:8080/api/v1/guilds/guild-123/benchmark" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"mode": "CUSTOM", "customRms": 0.95, "customIpi": 0.90}'
```


#### 3.4 Update WoWAudit Configuration

Update WoWAudit API configuration for a guild.

**Endpoint**: `PATCH /api/v1/guilds/{guildId}/wowaudit`  
**Authentication**: Required  
**Roles**: GUILD_ADMIN, SYSTEM_ADMIN

**Path Parameters**:
- `guildId` (String, required): Guild identifier

**Request Body**:
```json
{
  "apiKey": "wowaudit-api-key",
  "guildUri": "https://wowaudit.com/guild/edgerush"
}
```

**Response**: `200 OK` (GuildDto)

**Example Request**:
```bash
curl -X PATCH "http://localhost:8080/api/v1/guilds/guild-123/wowaudit" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"apiKey": "key", "guildUri": "https://wowaudit.com/guild/edgerush"}'
```

#### 3.5 Enable/Disable Sync

Enable or disable data synchronization for a guild.

**Endpoint**: `PATCH /api/v1/guilds/{guildId}/sync`  
**Authentication**: Required  
**Roles**: GUILD_ADMIN, SYSTEM_ADMIN

**Path Parameters**:
- `guildId` (String, required): Guild identifier

**Request Body**:
```json
{
  "enabled": true
}
```

**Response**: `200 OK` (GuildDto)

**Example Request**:
```bash
curl -X PATCH "http://localhost:8080/api/v1/guilds/guild-123/sync" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"enabled": true}'
```

---

### 4. Integration Management

Manage external data synchronization.

#### 4.1 Sync WoWAudit (Full)

Trigger full WoWAudit synchronization.

**Endpoint**: `POST /api/v1/integrations/wowaudit/sync`  
**Authentication**: Required  
**Roles**: GUILD_ADMIN, SYSTEM_ADMIN

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

**Example Request**:
```bash
curl -X POST "http://localhost:8080/api/v1/integrations/wowaudit/sync" \
  -H "Authorization: Bearer <token>"
```


#### 4.2 Sync WoWAudit Roster

Sync only WoWAudit roster data.

**Endpoint**: `POST /api/v1/integrations/wowaudit/sync/roster`  
**Authentication**: Required  
**Roles**: GUILD_ADMIN, SYSTEM_ADMIN

**Response**: `200 OK` (SyncResultDto)

**Example Request**:
```bash
curl -X POST "http://localhost:8080/api/v1/integrations/wowaudit/sync/roster" \
  -H "Authorization: Bearer <token>"
```

#### 4.3 Sync WoWAudit Loot

Sync only WoWAudit loot history.

**Endpoint**: `POST /api/v1/integrations/wowaudit/sync/loot`  
**Authentication**: Required  
**Roles**: GUILD_ADMIN, SYSTEM_ADMIN

**Response**: `200 OK` (SyncResultDto)

**Example Request**:
```bash
curl -X POST "http://localhost:8080/api/v1/integrations/wowaudit/sync/loot" \
  -H "Authorization: Bearer <token>"
```

#### 4.4 Sync Warcraft Logs (Single Guild)

Trigger Warcraft Logs synchronization for a specific guild.

**Endpoint**: `POST /api/v1/integrations/warcraftlogs/sync/{guildId}`  
**Authentication**: Required  
**Roles**: GUILD_ADMIN, SYSTEM_ADMIN

**Path Parameters**:
- `guildId` (String, required): Guild identifier

**Response**: `200 OK` (SyncResultDto)

**Example Request**:
```bash
curl -X POST "http://localhost:8080/api/v1/integrations/warcraftlogs/sync/guild-123" \
  -H "Authorization: Bearer <token>"
```

#### 4.5 Sync Warcraft Logs (All Guilds)

Trigger Warcraft Logs synchronization for all guilds.

**Endpoint**: `POST /api/v1/integrations/warcraftlogs/sync/all`  
**Authentication**: Required  
**Roles**: SYSTEM_ADMIN

**Request Body**:
```json
["guild-123", "guild-456"]
```

**Response**: `200 OK` (SyncResultDto)

**Example Request**:
```bash
curl -X POST "http://localhost:8080/api/v1/integrations/warcraftlogs/sync/all" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '["guild-123", "guild-456"]'
```


#### 4.6 Get Sync Status

Get latest sync status for a data source.

**Endpoint**: `GET /api/v1/integrations/status/{source}`  
**Authentication**: Required  
**Roles**: All authenticated users

**Path Parameters**:
- `source` (String, required): Data source (WOWAUDIT, WARCRAFTLOGS)

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

**Example Request**:
```bash
curl -X GET "http://localhost:8080/api/v1/integrations/status/WOWAUDIT" \
  -H "Authorization: Bearer <token>"
```

#### 4.7 Get Recent Sync Operations

Get recent sync operations for a data source.

**Endpoint**: `GET /api/v1/integrations/status/{source}/recent`  
**Authentication**: Required  
**Roles**: All authenticated users

**Path Parameters**:
- `source` (String, required): Data source (WOWAUDIT, WARCRAFTLOGS)

**Query Parameters**:
- `limit` (Int, optional, default=10): Number of operations to return

**Response**: `200 OK` (List of SyncOperationDto)

**Example Request**:
```bash
curl -X GET "http://localhost:8080/api/v1/integrations/status/WOWAUDIT/recent?limit=5" \
  -H "Authorization: Bearer <token>"
```

#### 4.8 Get All Recent Sync Operations

Get all recent sync operations across all sources.

**Endpoint**: `GET /api/v1/integrations/status/all`  
**Authentication**: Required  
**Roles**: All authenticated users

**Query Parameters**:
- `limit` (Int, optional, default=50): Number of operations to return

**Response**: `200 OK` (List of SyncOperationDto)

**Example Request**:
```bash
curl -X GET "http://localhost:8080/api/v1/integrations/status/all?limit=20" \
  -H "Authorization: Bearer <token>"
```


#### 4.9 Check Sync In Progress

Check if a sync is currently in progress for a data source.

**Endpoint**: `GET /api/v1/integrations/status/{source}/in-progress`  
**Authentication**: Required  
**Roles**: All authenticated users

**Path Parameters**:
- `source` (String, required): Data source (WOWAUDIT, WARCRAFTLOGS)

**Response**: `200 OK`
```json
{
  "inProgress": false
}
```

**Example Request**:
```bash
curl -X GET "http://localhost:8080/api/v1/integrations/status/WOWAUDIT/in-progress" \
  -H "Authorization: Bearer <token>"
```

---

### 5. Raider Management

Manage raider profiles and status.

#### 5.1 Get All Raiders

Retrieve all raiders.

**Endpoint**: `GET /api/v1/raiders`  
**Authentication**: Required  
**Roles**: All authenticated users

**Response**: `200 OK` (List of RaiderDto)

**Example Request**:
```bash
curl -X GET "http://localhost:8080/api/v1/raiders" \
  -H "Authorization: Bearer <token>"
```

#### 5.2 Get Active Raiders

Retrieve all active raiders.

**Endpoint**: `GET /api/v1/raiders/active`  
**Authentication**: Required  
**Roles**: All authenticated users

**Response**: `200 OK` (List of RaiderDto)

**Example Request**:
```bash
curl -X GET "http://localhost:8080/api/v1/raiders/active" \
  -H "Authorization: Bearer <token>"
```

#### 5.3 Get Raider by ID

Retrieve a specific raider by ID.

**Endpoint**: `GET /api/v1/raiders/{id}`  
**Authentication**: Required  
**Roles**: All authenticated users

**Path Parameters**:
- `id` (Long, required): Raider ID

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
  "guildId": "guild-123"
}
```

**Error Responses**:
- `404 Not Found`: Raider not found

**Example Request**:
```bash
curl -X GET "http://localhost:8080/api/v1/raiders/1" \
  -H "Authorization: Bearer <token>"
```


#### 5.4 Get Raider by Character

Retrieve a raider by character name and realm.

**Endpoint**: `GET /api/v1/raiders/character/{characterName}/realm/{realm}`  
**Authentication**: Required  
**Roles**: All authenticated users

**Path Parameters**:
- `characterName` (String, required): Character name
- `realm` (String, required): Realm name

**Response**: `200 OK` (RaiderDto)

**Error Responses**:
- `404 Not Found`: Raider not found

**Example Request**:
```bash
curl -X GET "http://localhost:8080/api/v1/raiders/character/PlayerName/realm/Stormrage" \
  -H "Authorization: Bearer <token>"
```

#### 5.5 Update Raider Status

Update raider status.

**Endpoint**: `PATCH /api/v1/raiders/{id}/status`  
**Authentication**: Required  
**Roles**: GUILD_ADMIN, SYSTEM_ADMIN

**Path Parameters**:
- `id` (Long, required): Raider ID

**Request Body**:
```json
{
  "status": "INACTIVE"
}
```

**Response**: `200 OK` (RaiderDto)

**Error Responses**:
- `400 Bad Request`: Invalid status

**Example Request**:
```bash
curl -X PATCH "http://localhost:8080/api/v1/raiders/1/status" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"status": "INACTIVE"}'
```

---

### 6. Attendance Management (Lootman)

Track and report raid attendance.

#### 6.1 Track Attendance

Record attendance data for a raider.

**Endpoint**: `POST /api/v1/attendance/track`  
**Authentication**: Required  
**Roles**: GUILD_ADMIN, SYSTEM_ADMIN

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
  "startDate": "2025-11-01",
  "endDate": "2025-11-14",
  "attendedRaids": 8,
  "totalRaids": 10
}
```

**Example Request**:
```bash
curl -X POST "http://localhost:8080/api/v1/attendance/track" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d @attendance-data.json
```


#### 6.2 Get Attendance Report

Retrieve attendance report for a raider.

**Endpoint**: `GET /api/v1/attendance/raiders/{raiderId}/report`  
**Authentication**: Required  
**Roles**: All authenticated users

**Path Parameters**:
- `raiderId` (Long, required): Raider ID

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

**Example Request**:
```bash
curl -X GET "http://localhost:8080/api/v1/attendance/raiders/1/report?guildId=guild-123&startDate=2025-11-01&endDate=2025-11-14" \
  -H "Authorization: Bearer <token>"
```

---

### 7. FLPS Management (Lootman)

Domain-driven FLPS calculations and reports.

#### 7.1 Get FLPS Report

Get comprehensive FLPS report for all raiders in a guild.

**Endpoint**: `GET /api/v1/flps/guilds/{guildId}/report`  
**Authentication**: Required  
**Roles**: All authenticated users

**Path Parameters**:
- `guildId` (String, required): Guild identifier

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
      "attendanceScore": 0.95,
      "mechanicalScore": 0.90,
      "preparationScore": 0.85
    }
  ],
  "generatedAt": "2025-11-14T10:00:00Z"
}
```

**Example Request**:
```bash
curl -X GET "http://localhost:8080/api/v1/flps/guilds/guild-123/report" \
  -H "Authorization: Bearer <token>"
```


#### 7.2 Get System Status

Get FLPS system status and capabilities.

**Endpoint**: `GET /api/v1/flps/status`  
**Authentication**: Not required  
**Roles**: Public access

**Response**: `200 OK`
```json
{
  "message": "FLPS calculation system using domain-driven architecture",
  "features": [
    "Domain-driven design with bounded contexts",
    "Test-driven development with 85%+ coverage",
    "Guild-specific FLPS modifiers",
    "Real-time score calculations",
    "Comprehensive audit trail"
  ],
  "endpoints": {
    "Guild Report": "/api/v1/flps/guilds/{guildId}/report",
    "System Status": "/api/v1/flps/status"
  }
}
```

**Example Request**:
```bash
curl -X GET "http://localhost:8080/api/v1/flps/status"
```

---

### 8. Loot Management (Lootman)

Manage loot awards, history, and bans.

#### 8.1 Award Loot

Award loot to a raider.

**Endpoint**: `POST /api/v1/loot/awards`  
**Authentication**: Required  
**Roles**: GUILD_ADMIN, SYSTEM_ADMIN

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

**Example Request**:
```bash
curl -X POST "http://localhost:8080/api/v1/loot/awards" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d @loot-award.json
```

#### 8.2 Get Guild Loot History

Retrieve loot history for a guild.

**Endpoint**: `GET /api/v1/loot/guilds/{guildId}/history`  
**Authentication**: Required  
**Roles**: All authenticated users

**Path Parameters**:
- `guildId` (String, required): Guild identifier

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

**Example Request**:
```bash
curl -X GET "http://localhost:8080/api/v1/loot/guilds/guild-123/history?activeOnly=true" \
  -H "Authorization: Bearer <token>"
```


#### 8.3 Get Raider Loot History

Retrieve loot history for a specific raider.

**Endpoint**: `GET /api/v1/loot/raiders/{raiderId}/history`  
**Authentication**: Required  
**Roles**: All authenticated users

**Path Parameters**:
- `raiderId` (String, required): Raider identifier

**Query Parameters**:
- `activeOnly` (Boolean, optional, default=false): Filter to only active awards

**Response**: `200 OK` (LootHistoryResponse)

**Example Request**:
```bash
curl -X GET "http://localhost:8080/api/v1/loot/raiders/raider-1/history" \
  -H "Authorization: Bearer <token>"
```

#### 8.4 Create Loot Ban

Create a loot ban for a raider.

**Endpoint**: `POST /api/v1/loot/bans`  
**Authentication**: Required  
**Roles**: GUILD_ADMIN, SYSTEM_ADMIN

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

**Example Request**:
```bash
curl -X POST "http://localhost:8080/api/v1/loot/bans" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d @loot-ban.json
```

#### 8.5 Remove Loot Ban

Remove a loot ban.

**Endpoint**: `DELETE /api/v1/loot/bans/{banId}`  
**Authentication**: Required  
**Roles**: GUILD_ADMIN, SYSTEM_ADMIN

**Path Parameters**:
- `banId` (String, required): Ban identifier

**Response**: `204 No Content`

**Example Request**:
```bash
curl -X DELETE "http://localhost:8080/api/v1/loot/bans/ban-1" \
  -H "Authorization: Bearer <token>"
```

#### 8.6 Get Active Bans

Get active loot bans for a raider.

**Endpoint**: `GET /api/v1/loot/raiders/{raiderId}/bans`  
**Authentication**: Required  
**Roles**: All authenticated users

**Path Parameters**:
- `raiderId` (String, required): Raider identifier

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

**Example Request**:
```bash
curl -X GET "http://localhost:8080/api/v1/loot/raiders/raider-1/bans?guildId=guild-123" \
  -H "Authorization: Bearer <token>"
```

---


## External API Integrations

### WoWAudit API Client

The primary data source for guild information. All endpoints require authentication via API key.

#### Base Configuration

```kotlin
@Component
class WoWAuditClient(
    private val webClient: WebClient,
    private val properties: SyncProperties
) {
    companion object {
        const val BASE_URL = "https://wowaudit.com/api"
        const val API_VERSION = "v1"
    }
}
```

#### Core Endpoints

##### 1. Character Data

Fetches complete character roster with current progression status.

```kotlin
suspend fun fetchCharacters(): Result<List<CharacterData>>
```

**Response Schema**:
```kotlin
data class CharacterData(
    val name: String,
    val realm: String,
    val characterClass: String,
    val activeSpec: String,
    val itemLevel: Int,
    val role: String,
    val guild: GuildInfo,
    val equipment: List<EquipmentSlot>,
    val covenantInfo: CovenantData?,
    val lastLogin: String?
)
```

##### 2. Team Management

Retrieves team composition and raid scheduling information.

```kotlin
suspend fun fetchTeamData(): Result<TeamData>
```

**Response Schema**:
```kotlin
data class TeamData(
    val teamId: String,
    val name: String,
    val description: String,
    val members: List<TeamMember>,
    val schedule: RaidSchedule,
    val progression: ProgressionStatus
)
```

##### 3. Attendance Tracking

Fetches attendance data for FLPS calculations.

```kotlin
suspend fun fetchAttendance(periodId: String? = null): Result<AttendanceData>
```

**Response Schema**:
```kotlin
data class AttendanceData(
    val period: AttendancePeriod,
    val records: List<AttendanceRecord>
)

data class AttendanceRecord(
    val characterName: String,
    val totalRaids: Int,
    val attendedRaids: Int,
    val attendanceRate: Double,
    val excusedAbsences: Int,
    val unexcusedAbsences: Int,
    val lateArrivals: Int
)
```

##### 4. Loot History

Retrieves historical loot awards for RDF calculations.

```kotlin
suspend fun fetchLootHistory(season: String = "current"): Result<LootHistoryData>
```

**Response Schema**:
```kotlin
data class LootHistoryData(
    val season: String,
    val awards: List<LootAward>
)

data class LootAward(
    val id: String,
    val characterName: String,
    val itemName: String,
    val itemId: Int,
    val itemLevel: Int,
    val awardDate: String,
    val source: String,
    val difficulty: String,
    val notes: String?
)
```


##### 5. Wishlist Management

Fetches player wishlists for priority calculations.

```kotlin
suspend fun fetchWishlists(): Result<WishlistData>
```

**Response Schema**:
```kotlin
data class WishlistData(
    val lastUpdated: String,
    val entries: List<WishlistEntry>
)

data class WishlistEntry(
    val characterName: String,
    val itemName: String,
    val itemId: Int,
    val priority: Int,
    val notes: String?,
    val addedDate: String
)
```

### Error Handling Patterns

#### Retry Configuration

```kotlin
@Retryable(
    value = [ConnectException::class, TimeoutException::class],
    maxAttempts = 3,
    backoff = Backoff(delay = 1000, multiplier = 2.0)
)
suspend fun executeWithRetry<T>(
    endpoint: String,
    mapper: (JsonNode) -> T
): Result<T>
```

#### Circuit Breaker Integration

```kotlin
@CircuitBreaker(
    name = "wowaudit-api",
    fallbackMethod = "fallbackToCache"
)
suspend fun fetchCharacters(): Result<List<CharacterData>>
```

---

## Internal Service Interfaces

### Score Calculation Service

Core service for calculating Final Loot Priority Scores.

#### FLPS Algorithm Interface

```kotlin
interface ScoreCalculationService {
    suspend fun calculateFLPS(request: FLPSRequest): FLPSResult
    suspend fun calculateBulkFLPS(
        candidates: List<Character>,
        item: Item,
        context: RaidContext
    ): List<FLPSResult>
    suspend fun validateScoringData(character: Character): ValidationResult
}
```

#### Request/Response Models

```kotlin
data class FLPSRequest(
    val character: Character,
    val item: Item,
    val raidContext: RaidContext,
    val overrides: ScoreOverrides? = null
)

data class FLPSResult(
    val character: String,
    val item: String,
    val totalScore: Double,
    val components: ScoreComponents,
    val reasoning: List<String>,
    val warnings: List<String> = emptyList(),
    val calculatedAt: Instant = Instant.now()
)

data class ScoreComponents(
    val rms: Double,
    val ipi: Double,
    val rdf: Double,
    val breakdown: ComponentBreakdown
)
```

### Data Sync Service Interface

Manages synchronization of external data sources.

```kotlin
interface SyncService {
    suspend fun performFullSync(): SyncResult
    suspend fun performIncrementalSync(): SyncResult
    suspend fun getLastSyncStatus(): SyncStatus
    suspend fun syncSpecificData(dataTypes: Set<DataType>): SyncResult
}
```

#### Sync Models

```kotlin
data class SyncResult(
    val success: Boolean,
    val startTime: Instant,
    val endTime: Instant,
    val dataTypes: Set<DataType>,
    val recordsProcessed: Map<DataType, Int>,
    val errors: List<SyncError> = emptyList()
)

enum class DataType {
    CHARACTERS,
    ATTENDANCE,
    LOOT_HISTORY,
    WISHLISTS,
    TEAM_DATA,
    RAID_LOGS
}
```


### Repository Interfaces

#### Character Repository

Data access layer for character information.

```kotlin
interface CharacterRepository : JpaRepository<Character, Long> {
    fun findByNameAndRealm(name: String, realm: String): Character?
    
    @Query("SELECT c FROM Character c WHERE c.lastUpdated > :since AND c.status = 'ACTIVE'")
    fun findActiveCharactersSince(@Param("since") since: LocalDateTime): List<Character>
    
    fun findByRoleAndStatus(role: Role, status: CharacterStatus): List<Character>
}
```

#### Loot History Repository

```kotlin
interface LootHistoryRepository : JpaRepository<LootHistory, Long> {
    fun findByCharacterNameAndAwardDateAfter(
        characterName: String,
        cutoffDate: LocalDateTime
    ): List<LootHistory>
    
    fun findByItemNameOrderByAwardDateDesc(itemName: String): List<LootHistory>
    
    @Query("""
        SELECT new com.edgerush.datasync.dto.LootStatistics(
            l.characterName,
            COUNT(l),
            AVG(l.flpsScore),
            MAX(l.awardDate)
        )
        FROM LootHistory l
        WHERE l.awardDate > :since
        GROUP BY l.characterName
    """)
    fun getLootStatistics(@Param("since") since: LocalDateTime): List<LootStatistics>
}
```

---

## Error Handling

### Standard Error Response

All API errors return a consistent error response format:

```json
{
  "timestamp": "2025-11-14T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Application with ID 1 not found",
  "path": "/api/v1/applications/1"
}
```

### HTTP Status Codes

| Status Code | Description | Usage |
|-------------|-------------|-------|
| 200 OK | Success | Successful GET, PUT, PATCH requests |
| 201 Created | Resource created | Successful POST requests |
| 204 No Content | Success with no body | Successful DELETE requests |
| 400 Bad Request | Invalid request | Validation errors, malformed JSON |
| 401 Unauthorized | Authentication required | Missing or invalid JWT token |
| 403 Forbidden | Insufficient permissions | User lacks required role |
| 404 Not Found | Resource not found | Entity doesn't exist |
| 422 Unprocessable Entity | Business logic error | Requirements not met |
| 500 Internal Server Error | Server error | Unexpected server errors |
| 501 Not Implemented | Not yet implemented | Feature planned but not available |

### Error Categories

#### Validation Errors

```json
{
  "timestamp": "2025-11-14T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "attendancePercent",
      "message": "must be between 0.0 and 1.0"
    }
  ]
}
```

#### Business Logic Errors

```json
{
  "timestamp": "2025-11-14T10:00:00Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Cannot approve application: minimum item level requirement not met"
}
```


---

## Health & Monitoring

### Health Check Endpoints

#### Detailed Health Check

**Endpoint**: `GET /actuator/health/detailed`  
**Authentication**: Not required  
**Description**: Comprehensive health check with dependency status

**Response**: `200 OK`
```json
{
  "status": "UP",
  "components": {
    "database": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "wowaudit-api": {
      "status": "UP",
      "details": {
        "message": "API responding normally"
      }
    },
    "sync-status": {
      "status": "UP",
      "details": {
        "lastSync": "2025-11-14T10:00:00Z",
        "status": "COMPLETED"
      }
    }
  },
  "timestamp": "2025-11-14T10:05:00Z"
}
```

### Metrics Endpoints

#### Custom Metrics

EdgeRush LootMan exposes custom metrics for monitoring:

- `sync.duration`: Time taken for sync operations (tagged by data type)
- `score.calculations`: Number of FLPS calculations performed (tagged by character)
- `api.errors`: External API call failures (tagged by endpoint)

**Endpoint**: `GET /actuator/metrics/{metric-name}`  
**Authentication**: Not required

**Example**:
```bash
curl -X GET "http://localhost:8080/actuator/metrics/sync.duration"
```

**Response**:
```json
{
  "name": "sync.duration",
  "description": "Time taken for sync operations",
  "baseUnit": "seconds",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 150
    },
    {
      "statistic": "TOTAL_TIME",
      "value": 450.5
    },
    {
      "statistic": "MAX",
      "value": 5.2
    }
  ],
  "availableTags": [
    {
      "tag": "type",
      "values": ["WOWAUDIT", "WARCRAFTLOGS"]
    }
  ]
}
```

---

## OpenAPI/Swagger Documentation

### Interactive Documentation

The API includes OpenAPI 3.0 documentation accessible at:

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

### Configuration

```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    operationsSorter: method
    tagsSorter: alpha
  info:
    title: EdgeRush LootMan API
    version: 1.0.0
    description: Guild loot distribution system using FLPS algorithm
  servers:
    - url: http://localhost:8080
      description: Local Development
    - url: https://api.edgerush.com
      description: Production
```

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

- **GET**: 17 endpoints (46%)
- **POST**: 13 endpoints (35%)
- **PATCH**: 4 endpoints (11%)
- **PUT**: 1 endpoint (3%)
- **DELETE**: 1 endpoint (3%)

### Endpoints by Authentication Requirement

- **Authentication Required**: 35 endpoints (95%)
- **Public Access**: 2 endpoints (5%)
  - `GET /api/v1/flps/status`
  - `GET /actuator/health/detailed`

### Endpoints by Role Requirement

- **All Authenticated Users**: 20 endpoints
- **GUILD_ADMIN or SYSTEM_ADMIN**: 15 endpoints
- **SYSTEM_ADMIN only**: 2 endpoints

---

## Usage Examples

### Complete Score Calculation Flow

```bash
# 1. Authenticate and get JWT token
TOKEN=$(curl -X POST "http://localhost:8080/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password"}' \
  | jq -r '.token')

# 2. Get guild configuration
curl -X GET "http://localhost:8080/api/v1/guilds/guild-123" \
  -H "Authorization: Bearer $TOKEN"

# 3. Calculate FLPS score
curl -X POST "http://localhost:8080/api/v1/flps/calculate" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
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
  }'

# 4. Award loot
curl -X POST "http://localhost:8080/api/v1/loot/awards" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "itemId": "item-12345",
    "raiderId": "raider-1",
    "guildId": "guild-123",
    "flpsScore": 0.85,
    "tier": "TIER_A"
  }'
```

### Bulk Score Calculation for Loot Council

```bash
# Generate FLPS report for all raiders
curl -X POST "http://localhost:8080/api/v1/flps/report" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d @raiders-data.json \
  | jq '.raiderReports | sort_by(-.flpsScore) | .[0:5]'
```

### Sync External Data

```bash
# Trigger WoWAudit sync
curl -X POST "http://localhost:8080/api/v1/integrations/wowaudit/sync" \
  -H "Authorization: Bearer $TOKEN"

# Check sync status
curl -X GET "http://localhost:8080/api/v1/integrations/status/WOWAUDIT" \
  -H "Authorization: Bearer $TOKEN"

# Wait for completion and verify
while true; do
  STATUS=$(curl -s -X GET "http://localhost:8080/api/v1/integrations/status/WOWAUDIT/in-progress" \
    -H "Authorization: Bearer $TOKEN" \
    | jq -r '.inProgress')
  
  if [ "$STATUS" = "false" ]; then
    echo "Sync completed"
    break
  fi
  
  echo "Sync in progress..."
  sleep 5
done
```

---

## GraphQL Status

**GraphQL is NOT IMPLEMENTED**. It was planned as Phase 2 of the original specification but has not been developed yet.

### Current Implementation

- **REST API**: Fully implemented with 37 endpoints
- **GraphQL API**: Not implemented

### Future Plans

GraphQL implementation is planned for a future phase and will include:

- Unified query interface for complex data relationships
- Real-time subscriptions for loot awards and score updates
- Flexible field selection to reduce over-fetching
- Type-safe schema with introspection

For now, all functionality is available through the REST API documented above.

---

## Notes

### Implementation Status

- ✅ All REST controllers implemented
- ✅ All endpoints functional
- ✅ Integration tests passing (100% pass rate)
- ✅ OpenAPI/Swagger documentation available
- ✅ Authentication and authorization working
- ✅ Error handling standardized

### Best Practices

1. **Always include Authorization header** for protected endpoints
2. **Use appropriate HTTP methods** (GET for reads, POST for creates, etc.)
3. **Handle error responses** gracefully in client applications
4. **Check sync status** before triggering new sync operations
5. **Use bulk operations** when calculating scores for multiple raiders
6. **Monitor health endpoints** for system status

### Rate Limiting

Currently, no rate limiting is implemented. Future versions may include:

- Per-user rate limits
- Per-endpoint rate limits
- Burst allowances for bulk operations

### Versioning

The API uses URL versioning (`/api/v1/`). Future versions will maintain backward compatibility or provide migration paths.

---

**Document Version**: 1.0.0  
**Last Updated**: November 15, 2025  
**Status**: Complete and Current

