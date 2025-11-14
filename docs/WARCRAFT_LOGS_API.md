# Warcraft Logs API Reference

## Base URL

```
http://localhost:8080/api/warcraft-logs
```

## Authentication

Currently, all endpoints are unauthenticated. In production, implement appropriate authentication/authorization.

## Endpoints

### Configuration Management

#### Get Guild Configuration

```http
GET /api/warcraft-logs/config/{guildId}
```

**Parameters:**
- `guildId` (path) - Guild identifier

**Response:**
```json
{
  "guildId": "my-guild",
  "enabled": true,
  "guildName": "My Guild",
  "realm": "Area 52",
  "region": "US",
  "syncIntervalHours": 6,
  "syncTimeWindowDays": 30,
  "includedDifficulties": ["Mythic", "Heroic"],
  "dpaWeight": 0.25,
  "adtWeight": 0.25,
  "criticalThreshold": 1.5,
  "fallbackMAS": 0.0,
  "fallbackDPA": 0.5,
  "fallbackADT": 10.0,
  "recentPerformanceWeightMultiplier": 2.0,
  "recentPerformanceDays": 14,
  "specAveragePercentile": 50,
  "minimumSampleSize": 5,
  "masCacheTTLMinutes": 60,
  "characterNameMappings": {}
}
```

#### Update Guild Configuration

```http
PUT /api/warcraft-logs/config/{guildId}
```

**Parameters:**
- `guildId` (path) - Guild identifier

**Request Body:**
```json
{
  "guildId": "my-guild",
  "enabled": true,
  "guildName": "My Guild",
  "realm": "Area 52",
  "region": "US",
  "syncIntervalHours": 6,
  "includedDifficulties": ["Mythic"]
}
```

**Response:** Updated configuration object

### Character Mappings

#### Create Character Mapping

```http
POST /api/warcraft-logs/config/{guildId}/character-mapping
```

**Request Body:**
```json
{
  "wowauditName": "PlayerOne",
  "wowauditRealm": "AreaFiftyTwo",
  "warcraftLogsName": "PlayerOne",
  "warcraftLogsRealm": "Area52",
  "createdBy": "admin"
}
```

**Response:**
```json
{
  "status": "success",
  "message": "Character mapping created"
}
```

#### Get Character Mappings

```http
GET /api/warcraft-logs/config/{guildId}/character-mappings
```

**Response:**
```json
[
  {
    "id": 1,
    "wowauditName": "PlayerOne",
    "wowauditRealm": "AreaFiftyTwo",
    "warcraftLogsName": "PlayerOne",
    "warcraftLogsRealm": "Area52",
    "createdAt": "2025-11-13T10:00:00Z"
  }
]
```

#### Delete Character Mapping

```http
DELETE /api/warcraft-logs/config/{guildId}/character-mapping/{mappingId}
```

**Response:**
```json
{
  "status": "success",
  "message": "Character mapping deleted"
}
```

### Sync Management

#### Trigger Manual Sync

```http
POST /api/warcraft-logs/sync/{guildId}
```

**Response:**
```json
{
  "guildId": "my-guild",
  "success": true,
  "reportsProcessed": 5,
  "fightsProcessed": 42,
  "performanceRecordsCreated": 378,
  "duration": "PT15.234S",
  "message": null,
  "error": null
}
```

#### Get Sync Status

```http
GET /api/warcraft-logs/sync/{guildId}/status
```

**Response:**
```json
{
  "guildId": "my-guild",
  "lastSyncTime": "2025-11-13T10:30:00Z",
  "lastReportCode": "abc123xyz"
}
```

### Performance Data

#### Get Character Performance

```http
GET /api/warcraft-logs/performance/{guildId}/{characterName}?realm={realm}
```

**Parameters:**
- `guildId` (path) - Guild identifier
- `characterName` (path) - Character name
- `realm` (query, optional) - Character realm

**Response:**
```json
{
  "characterName": "PlayerOne",
  "characterRealm": "Area52",
  "mas": 0.85,
  "deathsPerAttempt": 0.3,
  "averageAvoidableDamage": 8.5,
  "totalAttempts": 42,
  "totalDeaths": 13,
  "fightCount": 42
}
```

**Error Response:**
```json
{
  "characterName": "PlayerOne",
  "characterRealm": "Area52",
  "error": "No performance data found"
}
```

#### Get Reports

```http
GET /api/warcraft-logs/reports/{guildId}
```

**Response:**
```json
[
  {
    "code": "abc123xyz",
    "title": "Mythic Raid Night",
    "startTime": "2025-11-12T19:00:00Z",
    "endTime": "2025-11-12T23:00:00Z",
    "owner": "RaidLeader",
    "syncedAt": "2025-11-13T10:30:00Z"
  }
]
```

### Health & Monitoring

#### Health Check

```http
GET /actuator/health/warcraftLogs
```

**Response:**
```json
{
  "status": "UP",
  "details": {
    "enabled_guilds": 1,
    "guilds": {
      "my-guild": {
        "lastSync": "2025-11-13T10:30:00Z",
        "status": "OK",
        "hoursSinceSync": 2
      }
    }
  }
}
```

**Status Values:**
- `OK` - Synced within 24 hours
- `STALE` - Last sync > 24 hours ago
- `NEVER_SYNCED` - No sync has occurred

#### Metrics

```http
GET /actuator/metrics/warcraft_logs.{metric}
```

**Available Metrics:**
- `sync.success` - Count of successful syncs
- `sync.failure` - Count of failed syncs
- `api.latency` - API call latency (timer)
- `cache.hit` - Cache hit count
- `cache.miss` - Cache miss count
- `reports.processed` - Total reports processed
- `fights.processed` - Total fights processed
- `performance.records` - Total performance records created

**Example Response:**
```json
{
  "name": "warcraft_logs.sync.success",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 42.0
    }
  ]
}
```

## Error Responses

### 400 Bad Request

```json
{
  "timestamp": "2025-11-13T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid guild configuration",
  "path": "/api/warcraft-logs/config/my-guild"
}
```

### 404 Not Found

```json
{
  "timestamp": "2025-11-13T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Guild configuration not found",
  "path": "/api/warcraft-logs/config/unknown-guild"
}
```

### 500 Internal Server Error

```json
{
  "timestamp": "2025-11-13T10:30:00Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Failed to sync Warcraft Logs data",
  "path": "/api/warcraft-logs/sync/my-guild"
}
```

## Rate Limiting

Warcraft Logs API has rate limits. The integration handles this automatically with:
- Exponential backoff on 429 responses
- Respect for `Retry-After` headers
- Circuit breaker pattern (opens after 50% failure rate)

## Examples

### Complete Setup Flow

```bash
# 1. Configure guild
curl -X PUT http://localhost:8080/api/warcraft-logs/config/my-guild \
  -H "Content-Type: application/json" \
  -d '{
    "guildId": "my-guild",
    "enabled": true,
    "guildName": "My Guild",
    "realm": "Area 52",
    "region": "US"
  }'

# 2. Add character mapping (if needed)
curl -X POST http://localhost:8080/api/warcraft-logs/config/my-guild/character-mapping \
  -H "Content-Type: application/json" \
  -d '{
    "wowauditName": "PlayerOne",
    "wowauditRealm": "AreaFiftyTwo",
    "warcraftLogsName": "PlayerOne",
    "warcraftLogsRealm": "Area52"
  }'

# 3. Trigger initial sync
curl -X POST http://localhost:8080/api/warcraft-logs/sync/my-guild

# 4. Check health
curl http://localhost:8080/actuator/health/warcraftLogs

# 5. Query performance
curl http://localhost:8080/api/warcraft-logs/performance/my-guild/PlayerOne?realm=Area52
```

---

**Last Updated**: November 13, 2025  
**Version**: 1.0
