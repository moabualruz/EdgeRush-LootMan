# EdgeRush LootMan - External Integration Guide

**Last Updated:** January 2026
**Purpose:** Comprehensive guide to all available external data source integrations

---

## Table of Contents

1. [Integration Overview](#integration-overview)
2. [Warcraft Logs API](#warcraft-logs-api) - **IMPLEMENTED**
3. [WoWAudit API](#wowaudit-api) - **IMPLEMENTED**
4. [Blizzard Battle.net API](#blizzard-battlenet-api) - **AVAILABLE**
5. [Raidbots / SimulationCraft](#raidbots--simulationcraft) - **ALTERNATIVES**
6. [RC Loot Council Addon](#rc-loot-council-addon) - **AVAILABLE**
7. [Wowhead Data](#wowhead-data) - **AVAILABLE**
8. [Integration Priority Matrix](#integration-priority-matrix)

---

## Integration Overview

| Platform | Status | Purpose | Authentication |
|----------|--------|---------|----------------|
| **Warcraft Logs** | Implemented | Performance data, MAS scoring | OAuth 2.0 |
| **WoWAudit** | Implemented | Characters, attendance, loot, wishlists | API Key |
| **Blizzard API** | Available | Direct character/guild data | OAuth 2.0 |
| **Raidbots** | Blocked | Simulation/upgrade values | No public API |
| **SimulationCraft** | Alternative | Self-hosted simulations | None (local) |
| **RC Loot Council** | Available | In-game loot history | Export/wowaudit |
| **Wowhead** | Available | Item database, tooltips | None (scraping) |

---

## Warcraft Logs API

**Status:** ✅ FULLY IMPLEMENTED
**Documentation:** [Official API Docs](https://www.warcraftlogs.com/api/docs)

### Overview

Warcraft Logs provides combat log analysis via a GraphQL API. We use it for calculating the **Mechanical Adherence Score (MAS)** component of FLPS.

### Authentication

```
Type: OAuth 2.0 Client Credentials Flow
Token URL: https://www.warcraftlogs.com/oauth/token
API Endpoint: https://www.warcraftlogs.com/api/v2/client
```

**Setup Steps:**
1. Log in to [Warcraft Logs](https://www.warcraftlogs.com)
2. Go to [Client Management](https://www.warcraftlogs.com/api/clients)
3. Click "Create Client"
4. Save `client_id` and `client_secret`
5. Configure in `application.yaml`:
   ```yaml
   warcraftlogs:
     client-id: ${WARCRAFTLOGS_CLIENT_ID}
     client-secret: ${WARCRAFTLOGS_CLIENT_SECRET}
   ```

### Available Data

| Query | Data Retrieved | FLPS Usage |
|-------|----------------|------------|
| `reportData.report` | Combat logs, encounters | Fight analysis |
| `reportData.report.fights` | Individual boss kills | Performance per encounter |
| `reportData.report.rankings` | Parse percentiles | Performance scoring |
| `characterData.character` | Character info | Name mapping |
| `worldData.encounter` | Boss information | Encounter metadata |
| `guildData.guild.attendance` | Guild attendance | Cross-reference |

### GraphQL Example

```graphql
query GetCharacterPerformance($name: String!, $server: String!, $region: String!) {
  characterData {
    character(name: $name, serverSlug: $server, serverRegion: $region) {
      id
      name
      zoneRankings(zoneID: 38) {  # Current raid tier
        bestPerformanceAverage
        medianPerformanceAverage
        rankings {
          encounter { name }
          rankPercent
          totalKills
        }
      }
    }
  }
}
```

### Rate Limits

- **Points per hour:** 3,600 (resets hourly)
- **Points per query:** Varies by complexity
- **Recommendation:** Cache results, sync every 6 hours

### Current Implementation

Location: `data-sync-service/src/main/kotlin/com/edgerush/lootman/integrations/warcraftlogs/`

| File | Purpose |
|------|---------|
| `WarcraftLogsClient.kt` | GraphQL client |
| `WarcraftLogsSyncService.kt` | Scheduled sync |
| `WarcraftLogsPerformanceService.kt` | MAS calculation |

---

## WoWAudit API

**Status:** ✅ FULLY IMPLEMENTED
**Website:** [wowaudit.com](https://wowaudit.com)

### Overview

WoWAudit aggregates character data from Blizzard API and provides spreadsheet-style guild management. We use it as our primary data source for characters, attendance, and loot history.

### Authentication

```
Type: API Key (team-specific)
Access: Admin users only
Location: Team settings page
```

**Setup Steps:**
1. Log in to [WoWAudit](https://wowaudit.com)
2. Navigate to your team's admin settings
3. Copy the API key
4. Configure in `application.yaml`:
   ```yaml
   wowaudit:
     guild-profile-uri: /api/guild/{guildId}
     api-key: ${WOWAUDIT_API_KEY}
   ```

### Available Endpoints

| Endpoint | Data | FLPS Usage |
|----------|------|------------|
| `/api/guild/{id}` | Guild roster | Character list |
| `/api/guild/{id}/characters` | Character details | Gear, specs, progression |
| `/api/guild/{id}/raids` | Raid schedule | Attendance tracking |
| `/api/guild/{id}/attendance` | Attendance records | RMS calculation |
| `/api/guild/{id}/loot` | Loot history | RDF calculation |
| `/api/guild/{id}/wishlists` | Item wishlists | IPI calculation (upgrade proxy) |
| `/api/guild/{id}/applications` | Guild apps | Recruitment data |

### Data Model

```kotlin
// Character from WoWAudit
data class WoWAuditCharacter(
    val id: Long,
    val name: String,
    val realm: String,
    val characterClass: String,
    val spec: String,
    val itemLevel: Int,
    val role: String,
    val wishlist: List<WishlistItem>
)

// Wishlist item (used for upgrade value proxy)
data class WishlistItem(
    val itemId: Long,
    val itemName: String,
    val priority: Int,      // 1-5 priority ranking
    val percentage: Double  // Upgrade value estimate
)
```

### Current Implementation

Location: `data-sync-service/src/main/kotlin/com/edgerush/lootman/integrations/wowaudit/`

| File | Purpose |
|------|---------|
| `WoWAuditClient.kt` | REST client |
| `WoWAuditSyncService.kt` | Scheduled sync |
| `WoWAuditDataTransformerService.kt` | Data transformation |

### Sync Schedule

- **Full sync:** Daily at 4 AM
- **Character sync:** Every 6 hours
- **Loot sync:** After each raid (webhook or manual)

---

## Blizzard Battle.net API

**Status:** ⚪ AVAILABLE (Not Implemented)
**Documentation:** [Developer Portal](https://develop.battle.net/documentation)

### Overview

Direct access to Blizzard's official character and guild data. Currently not implemented because WoWAudit aggregates this data for us, but useful for:
- Real-time gear updates
- Guild roster changes
- Mythic+ scores
- Achievement data

### Authentication

```
Type: OAuth 2.0 Client Credentials
Token URL: https://oauth.battle.net/token
API Base: https://{region}.api.blizzard.com
Regions: us, eu, kr, tw, cn
```

**Setup Steps:**
1. Go to [Battle.net Developer Portal](https://develop.battle.net)
2. Create a new application
3. Copy `client_id` and `client_secret`
4. Request access token:
   ```bash
   curl -X POST https://oauth.battle.net/token \
     -u {client_id}:{client_secret} \
     -d grant_type=client_credentials
   ```

### Profile API Endpoints

| Endpoint | Data | Potential Usage |
|----------|------|-----------------|
| `/profile/wow/character/{realm}/{name}` | Character summary | Basic info |
| `/profile/wow/character/{realm}/{name}/equipment` | Equipped gear | Real-time ilvl |
| `/profile/wow/character/{realm}/{name}/statistics` | Combat stats | Performance metrics |
| `/profile/wow/character/{realm}/{name}/mythic-keystone-profile` | M+ scores | Performance metric |
| `/profile/wow/character/{realm}/{name}/raids` | Raid progression | Boss kills |
| `/data/wow/guild/{realm}/{name}/roster` | Guild members | Roster sync |

### Rate Limits

- **Requests per second:** 100
- **Requests per hour:** 36,000
- **Token validity:** 30 days

### Implementation Priority

**LOW** - WoWAudit already provides this data aggregated. Consider implementing for:
- Faster real-time updates
- Reducing WoWAudit dependency
- M+ score integration (not in WoWAudit)

### Example Implementation

```kotlin
@Component
class BlizzardApiClient(
    private val webClient: WebClient,
    @Value("\${blizzard.client-id}") private val clientId: String,
    @Value("\${blizzard.client-secret}") private val clientSecret: String
) {
    private var accessToken: String? = null
    private var tokenExpiry: Instant = Instant.EPOCH

    suspend fun getCharacterEquipment(realm: String, name: String): EquipmentResponse {
        ensureToken()
        return webClient.get()
            .uri("https://us.api.blizzard.com/profile/wow/character/$realm/$name/equipment")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .awaitBody()
    }

    private suspend fun ensureToken() {
        if (Instant.now().isAfter(tokenExpiry)) {
            refreshToken()
        }
    }
}
```

---

## Raidbots / SimulationCraft

**Status:** ⚠️ BLOCKED (Raidbots) / 🔧 ALTERNATIVE (SimC)

### Raidbots Status

**No public API available.** Raidbots does not offer API keys for external developers.

**Contact Options:**
- Twitter: [@raidbots](https://twitter.com/raidbots)
- Discord: [Raidbots Discord](https://discord.gg/86EF64Q)
- Creator: Dave Hendler (Seriallos)

**What Raidbots Offers (via website only):**
- Top Gear optimization
- Droptimizer (item upgrade values)
- Quick Sim (basic DPS)
- Advanced Sim (custom scripts)

### SimulationCraft Self-Hosting

**Alternative:** Run SimulationCraft locally to calculate upgrade values.

**Requirements:**
- SimulationCraft binary (C++)
- Battle.net API credentials
- Compute resources (1-2 min per sim)

**Available Wrappers:**

| Project | Language | Description |
|---------|----------|-------------|
| [simc-api](https://github.com/cmatheny/simc-api) | Node.js | WebSocket API for cloud SimC |
| [simcraft-api](https://github.com/mckilem/simcraft-api) | Docker | REST API wrapper |
| [autosimulationcraft](https://pypi.org/project/autosimulationcraft/) | Python | Automated sim runner |

### Implementation Options

#### Option A: Self-Hosted SimC (Recommended)

```yaml
# docker-compose.yml addition
simc-worker:
  image: mckilem/simcraft-api:latest
  environment:
    - BLIZZARD_CLIENT_ID=${BLIZZARD_CLIENT_ID}
    - BLIZZARD_CLIENT_SECRET=${BLIZZARD_CLIENT_SECRET}
  ports:
    - "8081:8080"
```

```kotlin
@Service
class SimulationCraftService(
    private val webClient: WebClient
) {
    suspend fun simulateUpgrade(
        characterProfile: String,  // SimC profile string
        itemId: Long
    ): SimulationResult {
        return webClient.post()
            .uri("http://simc-worker:8080/simulate")
            .bodyValue(SimRequest(profile = characterProfile, iterations = 1000))
            .retrieve()
            .awaitBody()
    }
}
```

#### Option B: Item Level Delta Calculation

Simple alternative when simulation isn't feasible:

```kotlin
fun calculateUpgradeValue(currentItem: Item?, newItem: Item): Double {
    if (currentItem == null) return 1.0  // Empty slot = max upgrade

    val ilvlDelta = newItem.itemLevel - currentItem.itemLevel
    return when {
        ilvlDelta >= 20 -> 1.0   // Major upgrade
        ilvlDelta >= 13 -> 0.8   // Significant upgrade
        ilvlDelta >= 7 -> 0.6    // Moderate upgrade
        ilvlDelta >= 1 -> 0.3    // Minor upgrade
        else -> 0.0              // Sidegrade or downgrade
    }
}
```

#### Option C: Stat Weight Calculation

More accurate than ilvl delta:

```kotlin
data class StatWeights(
    val intellect: Double = 1.0,
    val criticalStrike: Double = 0.8,
    val haste: Double = 0.9,
    val mastery: Double = 0.7,
    val versatility: Double = 0.6
)

fun calculateStatUpgrade(currentItem: Item?, newItem: Item, weights: StatWeights): Double {
    val currentScore = currentItem?.let { calculateStatScore(it, weights) } ?: 0.0
    val newScore = calculateStatScore(newItem, weights)
    val delta = newScore - currentScore
    return (delta / newScore).coerceIn(0.0, 1.0)
}
```

### Current Workaround

We use **WoWAudit wishlist percentages** as upgrade value proxy:
- Players rank items 1-5 priority
- WoWAudit calculates percentage based on priority
- Less accurate than simulation but functional

---

## RC Loot Council Addon

**Status:** ⚪ AVAILABLE (Integration Options)
**Website:** [CurseForge](https://www.curseforge.com/wow/addons/rclootcouncil)

### Overview

RC Loot Council is the most popular in-game loot distribution addon. Integration allows:
- Real-time loot decision recording
- Displaying FLPS in voting frames
- Automated history sync

### Integration Methods

#### Method 1: WoWAudit Plugin (Recommended)

The [RCLootCouncil-wowaudit](https://www.curseforge.com/wow/addons/rclootcouncil-wowaudit) plugin automatically syncs loot history to WoWAudit.

**Setup:**
1. Install RCLootCouncil
2. Install RCLootCouncil-wowaudit plugin
3. Configure wowaudit API key in addon settings
4. Loot automatically syncs to our existing WoWAudit integration

#### Method 2: JSON Export

RC Loot Council supports JSON export of loot history.

**Export Format:**
```json
{
  "player": "Playername",
  "date": "2026-01-13",
  "time": "20:30:45",
  "id": "item:212345:::::",
  "itemID": 212345,
  "itemString": "item:212345:0:0:0:0:0:0:0:0:0:0:0:0",
  "response": "Best in Slot",
  "votes": 5,
  "class": "PALADIN",
  "instance": "Nerub-ar Palace-Mythic",
  "boss": "Queen Ansurek",
  "gear1": "item:211234::::::",
  "gear2": "item:211235::::::",
  "responseID": 1,
  "isAwardReason": false
}
```

**Import Endpoint:**
```kotlin
@PostMapping("/api/loot/import/rclootcouncil")
fun importRCLootHistory(@RequestBody history: List<RCLootEntry>): ImportResult {
    return lootImportService.importFromRCLootCouncil(history)
}
```

#### Method 3: Custom Addon (Future)

Create a companion addon that:
- Displays FLPS scores in voting frame
- Sends loot decisions via HTTP to our API
- Receives score updates in real-time

**AceEvent Hooks:**
```lua
-- Hook into loot award events
RCLootCouncil:RegisterMessage("RCMLAwardSuccess", function(_, session, winner, response, ...)
    -- Send to EdgeRush API
    SendHTTPRequest("POST", "http://api.edgerush.local/loot/award", {
        winner = winner,
        itemLink = itemLink,
        response = response
    })
end)
```

---

## Wowhead Data

**Status:** ⚪ AVAILABLE (Supplementary)
**Website:** [Wowhead](https://www.wowhead.com)

### Overview

Wowhead provides the most comprehensive WoW item database. Useful for:
- Item tooltips in UI
- Item stat data
- Drop location information
- Set bonus information

### Integration Methods

#### Method 1: Tooltip Script (Frontend)

```html
<script>
  const whTooltips = {
    colorLinks: true,
    iconizeLinks: true,
    renameLinks: true
  };
</script>
<script src="https://wow.zamimg.com/js/tooltips.js"></script>

<!-- Usage -->
<a href="https://www.wowhead.com/item=212345">Item Name</a>
```

#### Method 2: XML Scraping (Backend)

```kotlin
@Service
class WowheadService(
    private val webClient: WebClient
) {
    suspend fun getItemData(itemId: Long): WowheadItem {
        val xml = webClient.get()
            .uri("https://www.wowhead.com/item=$itemId&xml")
            .retrieve()
            .awaitBody<String>()
        return parseWowheadXml(xml)
    }
}
```

#### Method 3: Third-Party Libraries

| Library | Language | Description |
|---------|----------|-------------|
| [iamcal/Wowhead-API](https://github.com/iamcal/Wowhead-API) | PHP | Screen scraping library |
| [WoWDatabaseSitesAPI](https://github.com/Twintop/WoWDatabaseSitesAPI) | C# | XML tooltip parser |
| [wow-classic-items](https://github.com/nexus-devs/wow-classic-items) | Node.js | Item database (Classic) |

### Use Cases for FLPS

| Use Case | Data Needed | Wowhead Provides |
|----------|-------------|------------------|
| Tier set tracking | Set bonus items | Yes |
| Item slot mapping | Equipment slots | Yes |
| BiS lists | Item stats, drop sources | Yes |
| Upgrade paths | Item comparisons | Partially |

---

## Integration Priority Matrix

### Recommended Implementation Order

| Priority | Integration | Effort | Impact | Status |
|----------|-------------|--------|--------|--------|
| **P0** | Warcraft Logs | Done | High | ✅ Implemented |
| **P0** | WoWAudit | Done | Critical | ✅ Implemented |
| **P1** | RC Loot Council (wowaudit) | Low | Medium | ⚪ Easy via plugin |
| **P2** | Self-hosted SimC | Medium | High | ⚪ Recommended |
| **P3** | Blizzard API | Medium | Low | ⚪ Redundant with WoWAudit |
| **P4** | Wowhead tooltips | Low | Low | ⚪ UI enhancement |
| **--** | Raidbots API | N/A | High | ❌ Blocked |

### Current FLPS Data Sources

| FLPS Component | Primary Source | Fallback |
|----------------|----------------|----------|
| **Attendance** | WoWAudit | Manual import |
| **Performance (Parse)** | Warcraft Logs | WoWAudit score |
| **Performance (MAS)** | Warcraft Logs | Default 0.5 |
| **Upgrade Value** | WoWAudit wishlist % | Item level delta |
| **Loot History** | WoWAudit | RC Loot Council export |
| **Character Data** | WoWAudit | Blizzard API |

### Recommended Next Steps

1. **Immediate:** Install RCLootCouncil-wowaudit plugin for real-time loot sync
2. **Short-term:** Implement self-hosted SimC for accurate upgrade values
3. **Medium-term:** Add Blizzard API for M+ scores (not in WoWAudit)
4. **Long-term:** Custom RC Loot Council addon with FLPS display

---

## Configuration Reference

### Environment Variables

```bash
# Warcraft Logs (Required)
WARCRAFTLOGS_CLIENT_ID=your_client_id
WARCRAFTLOGS_CLIENT_SECRET=your_client_secret

# WoWAudit (Required)
WOWAUDIT_GUILD_ID=your_guild_id
WOWAUDIT_API_KEY=your_api_key

# Blizzard API (Optional)
BLIZZARD_CLIENT_ID=your_client_id
BLIZZARD_CLIENT_SECRET=your_client_secret
BLIZZARD_REGION=us  # or eu, kr, tw

# SimulationCraft (Optional)
SIMC_ENABLED=false
SIMC_ENDPOINT=http://localhost:8081
```

### application.yaml

```yaml
integrations:
  warcraftlogs:
    enabled: true
    client-id: ${WARCRAFTLOGS_CLIENT_ID}
    client-secret: ${WARCRAFTLOGS_CLIENT_SECRET}
    sync-interval: PT6H  # Every 6 hours

  wowaudit:
    enabled: true
    guild-id: ${WOWAUDIT_GUILD_ID}
    api-key: ${WOWAUDIT_API_KEY}
    sync-interval: PT6H

  blizzard:
    enabled: false
    client-id: ${BLIZZARD_CLIENT_ID:}
    client-secret: ${BLIZZARD_CLIENT_SECRET:}
    region: ${BLIZZARD_REGION:us}

  simulationcraft:
    enabled: false
    endpoint: ${SIMC_ENDPOINT:http://localhost:8081}
    iterations: 1000
    timeout: PT2M
```

---

## Appendix: API Quick Reference

### Warcraft Logs GraphQL

```graphql
# Get character rankings
query { characterData { character(name: "X", serverSlug: "Y", serverRegion: "Z") {
  zoneRankings { rankings { rankPercent } }
}}}

# Get report fights
query { reportData { report(code: "ABC123") {
  fights { name encounterID kill }
}}}
```

### WoWAudit REST

```bash
GET /api/guild/{id}              # Guild info
GET /api/guild/{id}/characters   # Roster
GET /api/guild/{id}/attendance   # Attendance
GET /api/guild/{id}/loot         # Loot history
GET /api/guild/{id}/wishlists    # Wishlists
```

### Blizzard REST

```bash
GET /profile/wow/character/{realm}/{name}           # Summary
GET /profile/wow/character/{realm}/{name}/equipment # Gear
GET /profile/wow/character/{realm}/{name}/statistics # Stats
GET /data/wow/guild/{realm}/{name}/roster           # Guild roster
```
