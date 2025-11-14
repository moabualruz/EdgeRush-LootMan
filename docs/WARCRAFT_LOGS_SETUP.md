# Warcraft Logs Integration - Setup Guide

## Overview

This guide walks you through setting up the Warcraft Logs integration for EdgeRush LootMan to enable accurate MAS (Mechanical Adherence Score) calculation using real combat performance data.

## Prerequisites

- Warcraft Logs account with API access
- Guild configured in WoWAudit
- PostgreSQL database running
- EdgeRush LootMan backend deployed

## Step 1: Obtain Warcraft Logs API Credentials

1. Go to https://www.warcraftlogs.com/api/clients
2. Click "Create Client"
3. Fill in the details:
   - **Name**: EdgeRush LootMan
   - **Redirect URLs**: Not needed for server-to-server
   - **Type**: Public Client
4. Save your **Client ID** and **Client Secret**

## Step 2: Configure Environment Variables

Add the following to your `.env` file or environment configuration:

```bash
# Warcraft Logs API Credentials
WARCRAFT_LOGS_CLIENT_ID=your_client_id_here
WARCRAFT_LOGS_CLIENT_SECRET=your_client_secret_here

# Encryption Key (32 bytes for AES-256)
ENCRYPTION_KEY=your_32_byte_encryption_key_here
```

### Generating an Encryption Key

```bash
# Linux/Mac
openssl rand -base64 32

# PowerShell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Minimum 0 -Maximum 256 }))
```

## Step 3: Configure application.yaml

Update your `application.yaml` with Warcraft Logs settings:

```yaml
warcraft-logs:
  enabled: true
  client-id: ${WARCRAFT_LOGS_CLIENT_ID}
  client-secret: ${WARCRAFT_LOGS_CLIENT_SECRET}
  base-url: https://www.warcraftlogs.com/api/v2
  token-url: https://www.warcraftlogs.com/oauth/token
  
  sync:
    cron: "0 0 */6 * * *"  # Every 6 hours
    time-window-days: 30
    included-difficulties:
      - Mythic
      - Heroic
  
  mas:
    dpa-weight: 0.25
    adt-weight: 0.25
    critical-threshold: 1.5
    fallback-mas: 0.0
  
  cache:
    mas-ttl-minutes: 60

encryption:
  key: ${ENCRYPTION_KEY}
```

## Step 4: Configure Guild Settings

Use the REST API to configure your guild:

```bash
curl -X PUT http://localhost:8080/api/warcraft-logs/config/your-guild-id \
  -H "Content-Type: application/json" \
  -d '{
    "guildId": "your-guild-id",
    "enabled": true,
    "guildName": "Your Guild Name",
    "realm": "Area 52",
    "region": "US",
    "syncIntervalHours": 6,
    "includedDifficulties": ["Mythic", "Heroic"],
    "dpaWeight": 0.25,
    "adtWeight": 0.25,
    "criticalThreshold": 1.5
  }'
```

## Step 5: Set Up Character Name Mappings (If Needed)

If character names differ between WoWAudit and Warcraft Logs:

```bash
curl -X POST http://localhost:8080/api/warcraft-logs/config/your-guild-id/character-mapping \
  -H "Content-Type: application/json" \
  -d '{
    "wowauditName": "PlayerOne",
    "wowauditRealm": "AreaFiftyTwo",
    "warcraftLogsName": "PlayerOne",
    "warcraftLogsRealm": "Area52"
  }'
```

## Step 6: Trigger Initial Sync

Manually trigger the first sync:

```bash
curl -X POST http://localhost:8080/api/warcraft-logs/sync/your-guild-id
```

## Step 7: Verify Integration

### Check Health Status

```bash
curl http://localhost:8080/actuator/health/warcraftLogs
```

Expected response:
```json
{
  "status": "UP",
  "details": {
    "enabled_guilds": 1,
    "guilds": {
      "your-guild-id": {
        "lastSync": "2025-11-13T10:30:00Z",
        "status": "OK",
        "hoursSinceSync": 2
      }
    }
  }
}
```

### Check Metrics

```bash
# Sync success count
curl http://localhost:8080/actuator/metrics/warcraft_logs.sync.success

# API latency
curl http://localhost:8080/actuator/metrics/warcraft_logs.api.latency
```

### Query Performance Data

```bash
curl http://localhost:8080/api/warcraft-logs/performance/your-guild-id/CharacterName?realm=Area52
```

## Troubleshooting

### Authentication Failures

**Symptom**: 401/403 errors in logs

**Solution**:
1. Verify Client ID and Secret are correct
2. Check that credentials are properly set in environment variables
3. Ensure no extra spaces or quotes in credentials

### No Data Syncing

**Symptom**: Health check shows "NEVER_SYNCED"

**Solution**:
1. Check guild name and realm are correct (case-sensitive)
2. Verify guild has recent raid logs on Warcraft Logs
3. Check logs for API errors: `docker-compose logs data-sync | grep "Warcraft Logs"`

### Character Not Found

**Symptom**: Performance data returns "No performance data found"

**Solution**:
1. Verify character name spelling matches Warcraft Logs exactly
2. Check if character mapping is needed (realm name format differences)
3. Ensure character has participated in raids within the sync window

### Stale Data

**Symptom**: Health check shows "STALE" status

**Solution**:
1. Check if scheduled sync is running: Look for cron execution in logs
2. Manually trigger sync: `POST /api/warcraft-logs/sync/{guildId}`
3. Verify no API rate limiting is occurring

### High API Latency

**Symptom**: Slow sync times, high latency metrics

**Solution**:
1. Check network connectivity to warcraftlogs.com
2. Verify no rate limiting (429 errors)
3. Consider reducing sync frequency
4. Check if too many guilds are syncing simultaneously

## Configuration Reference

### Guild Configuration Options

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `enabled` | boolean | true | Enable/disable integration for guild |
| `guildName` | string | required | Guild name (case-sensitive) |
| `realm` | string | required | Realm name (spaces allowed) |
| `region` | string | required | Region code (US, EU, KR, TW, CN) |
| `syncIntervalHours` | int | 6 | Hours between automatic syncs |
| `syncTimeWindowDays` | int | 30 | Days of history to sync |
| `includedDifficulties` | array | ["Mythic", "Heroic"] | Raid difficulties to include |
| `dpaWeight` | double | 0.25 | Weight for deaths per attempt in MAS |
| `adtWeight` | double | 0.25 | Weight for avoidable damage in MAS |
| `criticalThreshold` | double | 1.5 | Threshold for auto-zero MAS |
| `fallbackMAS` | double | 0.0 | MAS when no data available |
| `recentPerformanceWeightMultiplier` | double | 2.0 | Weight multiplier for recent performance |
| `recentPerformanceDays` | int | 14 | Days considered "recent" |
| `specAveragePercentile` | int | 50 | Percentile for spec averages |
| `minimumSampleSize` | int | 5 | Minimum fights for valid averages |
| `masCacheTTLMinutes` | int | 60 | Cache duration for MAS scores |

### Difficulty Codes

- `Normal` - Normal difficulty
- `Heroic` - Heroic difficulty
- `Mythic` - Mythic difficulty

### Region Codes

- `US` - Americas
- `EU` - Europe
- `KR` - Korea
- `TW` - Taiwan
- `CN` - China

## Best Practices

1. **Sync Frequency**: 6 hours is recommended. More frequent syncs may hit rate limits.

2. **Time Window**: 30 days provides good balance between data freshness and performance.

3. **Difficulty Selection**: Include only difficulties your guild actively raids.

4. **Character Mappings**: Set up mappings proactively for characters with special characters or realm name differences.

5. **Monitoring**: Set up alerts for:
   - Health check status changes
   - Sync failure metrics
   - API latency spikes

6. **Cache TTL**: 60 minutes balances freshness with performance. Adjust based on your needs.

## Support

For issues or questions:
1. Check application logs: `docker-compose logs data-sync`
2. Review health check: `/actuator/health/warcraftLogs`
3. Check metrics: `/actuator/metrics/warcraft_logs.*`
4. Consult troubleshooting section above

---

**Last Updated**: November 13, 2025  
**Version**: 1.0
