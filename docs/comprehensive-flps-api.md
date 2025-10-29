# Comprehensive FLPS API Documentation

## Overview
The Enhanced FLPS system provides comprehensive loot distribution scoring with behavioral management and loot ban capabilities.

## Core Features
- ✅ Raw scores AND percentage breakdowns for all components
- ✅ Behavioral scoring with guild leader management
- ✅ Time-limited loot bans
- ✅ Single comprehensive endpoint per guild
- ✅ Perfect score benchmarking
- ✅ Complete eligibility determination

## API Endpoints

### 1. Guild FLPS Report (Main Endpoint)
```
GET /api/flps/{guildId}
```

**Response Example:**
```json
[
  {
    "breakdown": {
      "name": "PlayerName",
      "role": "TANK",
      "acs": 0.95,
      "mas": 0.88,
      "eps": 0.92,
      "rms": 0.765,
      "upgradeValue": 0.85,
      "tierBonus": 0.9,
      "roleMultiplier": 1.2,
      "ipi": 2.1,
      "rdf": 1.0,
      "flps": 1.6065,
      "eligible": true
    },
    "attendanceScore": 0.95,
    "attendancePercentage": 95.0,
    "mechanicalScore": 0.88,
    "mechanicalPercentage": 88.0,
    "preparationScore": 0.92,
    "preparationPercentage": 92.0,
    "rmsScore": 0.765,
    "rmsPercentage": 76.5,
    "upgradeValueScore": 0.85,
    "upgradeValuePercentage": 85.0,
    "tierBonusScore": 0.9,
    "tierBonusPercentage": 90.0,
    "roleMultiplierScore": 1.2,
    "roleMultiplierPercentage": 100.0,
    "ipiScore": 2.1,
    "ipiPercentage": 87.5,
    "behavioralScore": 0.9,
    "behavioralPercentage": 90.0,
    "behavioralBreakdown": {
      "behavioralScore": 0.9,
      "lootBanInfo": {
        "isBanned": false
      },
      "activeActions": [
        {
          "actionType": "DEDUCTION",
          "deductionAmount": 0.1,
          "reason": "Late to raid",
          "appliedBy": "GuildLeader",
          "appliedAt": "2025-10-28T20:00:00",
          "expiresAt": "2025-11-04T20:00:00"
        }
      ]
    },
    "rdfScore": 1.0,
    "rdfPercentage": 100.0,
    "flpsScore": 1.6065,
    "flpsPercentage": 53.55,
    "benchmarkUsed": "THEORETICAL",
    "eligible": true,
    "eligibilityReasons": []
  }
]
```

### 2. Guild Management - Behavioral Actions

#### Apply Behavioral Deduction
```
POST /api/guild/{guildId}/management/behavioral/deduction
```

**Request Body:**
```json
{
  "characterName": "PlayerName",
  "deductionAmount": 0.2,
  "reason": "Late to raid consistently",
  "appliedBy": "GuildLeader",
  "expiresAt": "2025-11-05T20:00:00"
}
```

#### Apply Behavioral Restoration
```
POST /api/guild/{guildId}/management/behavioral/restoration
```

**Request Body:**
```json
{
  "characterName": "PlayerName",
  "deductionAmount": 0.1,
  "reason": "Improved behavior",
  "appliedBy": "GuildLeader",
  "expiresAt": null
}
```

### 3. Guild Management - Loot Bans

#### Ban Character from Loot
```
POST /api/guild/{guildId}/management/loot-ban
```

**Request Body:**
```json
{
  "characterName": "PlayerName",
  "reason": "DKP violation",
  "bannedBy": "GuildLeader",
  "expiresAt": "2025-11-10T20:00:00"
}
```

#### Lift Loot Ban
```
DELETE /api/guild/{guildId}/management/loot-ban/{characterName}
```

### 4. Guild Management - View Active Actions

#### Get Active Behavioral Actions
```
GET /api/guild/{guildId}/management/behavioral/active
```

#### Get Active Loot Bans
```
GET /api/guild/{guildId}/management/loot-bans/active
```

### 5. System Status
```
GET /api/flps/status
```

## Key Benefits

1. **Single Source of Truth**: One endpoint provides all FLPS data per guild
2. **Complete Transparency**: Both raw scores and percentages for every component
3. **Behavioral Management**: Guild leaders can apply time-limited behavioral adjustments
4. **Loot Ban System**: Flexible ban management with automatic expiration
5. **Enhanced Eligibility**: Clear reasoning for why a character is or isn't eligible
6. **Comprehensive Testing**: Unit and integration tests ensure calculation accuracy

## Usage Examples

### Getting Complete Guild FLPS Data
```bash
curl -X GET "http://localhost:8080/api/flps/edgerush"
```

### Managing Player Behavior
```bash
# Apply behavioral deduction
curl -X POST "http://localhost:8080/api/guild/edgerush/management/behavioral/deduction" \
  -H "Content-Type: application/json" \
  -d '{
    "characterName": "PlayerName",
    "deductionAmount": 0.15,
    "reason": "Unprepared for raid",
    "appliedBy": "GuildLeader",
    "expiresAt": "2025-11-07T20:00:00"
  }'

# Ban from loot temporarily
curl -X POST "http://localhost:8080/api/guild/edgerush/management/loot-ban" \
  -H "Content-Type: application/json" \
  -d '{
    "characterName": "PlayerName",
    "reason": "Disruptive behavior",
    "bannedBy": "GuildLeader",
    "expiresAt": "2025-11-05T20:00:00"
  }'
```