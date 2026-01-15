# External Data Strategy

## Overview

This document describes the strategy for leveraging external APIs and assets to minimize local data storage while maintaining a rich user experience. The goal is to avoid maintaining our own database of WoW items, spells, instances, and assets.

## Principles

1. **API-First**: Prefer fetching data on-demand from official sources
2. **Smart Caching**: Cache fetched data with appropriate TTLs
3. **Graceful Fallback**: Provide degraded experience when APIs unavailable
4. **Cost Efficiency**: Minimize API calls through caching and batching

---

## Data Sources

### 1. Blizzard Game Data API

**Base URL**: `https://{region}.api.blizzard.com`

**Authentication**: OAuth2 Client Credentials Flow
- Obtain access token with client_id/client_secret
- Token valid for 24 hours
- Rate limit: 36,000 requests/hour (100/second burst)

#### Endpoints We Use

| Endpoint | Purpose | Cache TTL |
|----------|---------|-----------|
| `/data/wow/item/{id}` | Item details (stats, icons) | 7 days |
| `/data/wow/item-media/{id}` | Item icon URLs | 30 days |
| `/data/wow/spell/{id}` | Spell details | 7 days |
| `/data/wow/spell-media/{id}` | Spell icon URLs | 30 days |
| `/data/wow/journal/instance/index` | List of instances | Until patch |
| `/data/wow/journal/instance/{id}` | Instance details, bosses | Until patch |
| `/data/wow/journal/encounter/{id}` | Encounter details, loot | Until patch |
| `/data/wow/playable-class/index` | Class list | Until expansion |
| `/data/wow/playable-specialization/{id}` | Spec details | Until patch |
| `/data/wow/talent-tree/{specId}` | Talent tree nodes | Until patch |
| `/data/wow/item-appearance/{id}` | Transmog appearance | 30 days |
| `/data/wow/realm/index` | Realm list | 7 days |

#### Profile API Endpoints

| Endpoint | Purpose | Cache TTL |
|----------|---------|-----------|
| `/profile/wow/character/{realm}/{name}` | Character summary | 1 hour |
| `/profile/wow/character/{realm}/{name}/equipment` | Equipped gear | 1 hour |
| `/profile/wow/character/{realm}/{name}/mythic-keystone-profile` | M+ data | 1 hour |
| `/profile/wow/character/{realm}/{name}/achievements` | Achievements | 1 hour |
| `/profile/wow/guild/{realm}/{name}/roster` | Guild roster | 1 hour |

#### Implementation

```kotlin
// BlizzardApiClient.kt
@Service
class BlizzardApiClient(
    private val webClient: WebClient,
    private val cache: CacheManager,
    private val config: BlizzardApiConfig
) {
    suspend fun getItem(itemId: Long): ItemData {
        return cache.getOrFetch("item:$itemId", Duration.ofDays(7)) {
            webClient.get()
                .uri("${config.baseUrl}/data/wow/item/$itemId")
                .header("Authorization", "Bearer ${getAccessToken()}")
                .retrieve()
                .bodyToMono<ItemData>()
                .awaitSingle()
        }
    }

    suspend fun getItemIcon(itemId: Long): String {
        val mediaData = cache.getOrFetch("item-media:$itemId", Duration.ofDays(30)) {
            webClient.get()
                .uri("${config.baseUrl}/data/wow/media/item/$itemId")
                .header("Authorization", "Bearer ${getAccessToken()}")
                .retrieve()
                .bodyToMono<ItemMediaData>()
                .awaitSingle()
        }
        return mediaData.assets.find { it.key == "icon" }?.value ?: ""
    }
}
```

---

### 2. Wowhead Tooltips

**Script URL**: `https://wow.zamimg.com/js/tooltips.js`

**Purpose**: Rich, always-current item/spell tooltips without API calls

#### Integration

```html
<!-- In index.html -->
<script>
const whTooltips = {
    colorLinks: true,      // Color by item quality
    iconizeLinks: true,    // Show icons
    renameLinks: true,     // Update link text
    iconSize: 'small',     // tiny, small, medium, large
    hide: {
        sellprice: true,   // Hide vendor price
        droppedby: false,  // Show drop source
        dropchance: true   // Hide drop %
    }
};
</script>
<script src="https://wow.zamimg.com/js/tooltips.js"></script>
```

#### Link Format

```html
<!-- Basic item link -->
<a href="https://www.wowhead.com/item=207788">Fyrakk's Tainted Rageheart</a>

<!-- With bonus IDs (Mythic) -->
<a href="https://www.wowhead.com/item=207788"
   data-wowhead="bonus=1540:10275&ilvl=489">Fyrakk's Tainted Rageheart</a>

<!-- With gems and enchants -->
<a href="https://www.wowhead.com/item=207788"
   data-wowhead="gems=192985&ench=6643">Fyrakk's Tainted Rageheart</a>
```

#### Vue Component

```vue
<!-- WowheadLink.vue -->
<template>
  <a
    :href="`https://www.wowhead.com/item=${itemId}`"
    :data-wowhead="dataAttributes"
  >
    <slot>{{ itemName }}</slot>
  </a>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  itemId: number
  itemName?: string
  ilvl?: number
  bonus?: number[]
  gems?: number[]
  enchant?: number
}>()

const dataAttributes = computed(() => {
  const parts: string[] = []
  if (props.ilvl) parts.push(`ilvl=${props.ilvl}`)
  if (props.bonus?.length) parts.push(`bonus=${props.bonus.join(':')}`)
  if (props.gems?.length) parts.push(`gems=${props.gems.join(':')}`)
  if (props.enchant) parts.push(`ench=${props.enchant}`)
  return parts.join('&')
})
</script>
```

---

### 3. Warcraft Logs GraphQL API

**Base URL**: `https://www.warcraftlogs.com/api/v2`

**Authentication**: OAuth2 (already integrated)

#### Key Queries

```graphql
# Character performance
query CharacterRankings($name: String!, $serverSlug: String!, $serverRegion: String!) {
  characterData {
    character(name: $name, serverSlug: $serverSlug, serverRegion: $serverRegion) {
      id
      name
      classID
      zoneRankings(zoneID: 38, difficulty: 5)  # Mythic difficulty
    }
  }
}

# Report analysis
query ReportEvents($code: String!, $fightID: Int!) {
  reportData {
    report(code: $code) {
      events(
        fightIDs: [$fightID]
        dataType: Deaths
      ) {
        data
      }
    }
  }
}
```

---

### 4. Raider.IO API

**Base URL**: `https://raider.io/api/v1`

**Authentication**: None required (public API)

#### Endpoints

| Endpoint | Purpose |
|----------|---------|
| `/characters/profile?region=&realm=&name=&fields=` | Character M+ profile |
| `/mythic-plus/runs?season=&region=&dungeon=` | Top runs |
| `/raiding/raid-rankings?raid=&difficulty=&region=` | Guild rankings |

#### Fields Available

- `mythic_plus_scores_by_season:current`
- `mythic_plus_best_runs`
- `mythic_plus_recent_runs`
- `raid_progression`
- `gear`

#### Implementation

```kotlin
@Service
class RaiderIOClient(
    private val webClient: WebClient,
    private val cache: CacheManager
) {
    suspend fun getCharacterProfile(
        region: String,
        realm: String,
        name: String
    ): RaiderIOProfile {
        val cacheKey = "raiderio:$region:$realm:$name"
        return cache.getOrFetch(cacheKey, Duration.ofHours(1)) {
            webClient.get()
                .uri("https://raider.io/api/v1/characters/profile") {
                    it.queryParam("region", region)
                        .queryParam("realm", realm)
                        .queryParam("name", name)
                        .queryParam("fields", "mythic_plus_scores_by_season:current,raid_progression")
                        .build()
                }
                .retrieve()
                .bodyToMono<RaiderIOProfile>()
                .awaitSingle()
        }
    }
}
```

---

### 5. Raidbots Static Data

**Base URL**: `https://www.raidbots.com/static`

**Purpose**: Pre-compiled SimC data for talents, items, instances

#### Available Data

| File | Content |
|------|---------|
| `/data/instances.json` | Raid/dungeon metadata |
| `/data/encounters.json` | Boss encounter data |
| `/data/items-{raidName}.json` | Items from specific raid |

**Note**: This is unofficial; use Blizzard API as primary, Raidbots as supplement.

---

## Caching Strategy

### Cache Layers

1. **Local Memory Cache** (Caffeine)
   - Hot data (frequently accessed)
   - TTL: minutes to hours
   - Size-limited (100MB)

2. **Redis Cache** (Production)
   - Shared across instances
   - TTL: hours to days
   - Persistence optional

3. **Database Cache** (PostgreSQL)
   - Long-lived data (instances, classes)
   - TTL: days to weeks
   - Survives restarts

### Cache Keys

```
item:{itemId}                  -> ItemData (7 days)
item-media:{itemId}            -> IconUrl (30 days)
spell:{spellId}                -> SpellData (7 days)
instance:{instanceId}          -> InstanceData (30 days)
encounter:{encounterId}        -> EncounterData (30 days)
character:{region}:{realm}:{name} -> CharacterProfile (1 hour)
raiderio:{region}:{realm}:{name}  -> RaiderIOProfile (1 hour)
wcl:rankings:{characterId}     -> Rankings (1 hour)
```

### Cache Invalidation

- **Patch Day**: Clear all game data caches
- **Character Update**: Clear on manual refresh or sync
- **API Error**: Serve stale data with warning

---

## Fallback Strategy

### When Blizzard API Unavailable

1. Serve cached data (even if stale)
2. Display warning to user
3. Retry with exponential backoff
4. Log for monitoring

### When Wowhead Unavailable

1. Display basic item info from Blizzard API
2. Use Blizzard icon URLs instead
3. Skip tooltip enhancement

### When External Data Missing

1. Display "Unknown Item #12345" format
2. Allow manual data entry
3. Queue background fetch attempt

---

## Database Schema (Minimal)

We only store references, not full game data:

```sql
-- We store item IDs, not item details
CREATE TABLE wishlist_items (
    id BIGSERIAL PRIMARY KEY,
    raider_id BIGINT REFERENCES raiders(id),
    item_id BIGINT NOT NULL,  -- Blizzard item ID
    upgrade_value DECIMAL(5,2),
    simulation_date TIMESTAMP,
    source VARCHAR(50)  -- 'simc', 'wowaudit', 'manual'
);

-- We store encounter IDs, not encounter details
CREATE TABLE loot_awards (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL,      -- Blizzard item ID
    encounter_id BIGINT,           -- Blizzard encounter ID
    item_level INT,
    difficulty VARCHAR(20),
    awarded_at TIMESTAMP
);

-- Cache table for expensive API calls
CREATE TABLE api_cache (
    cache_key VARCHAR(255) PRIMARY KEY,
    data JSONB NOT NULL,
    fetched_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL
);
```

---

## Cost Analysis

### Blizzard API

- **Free tier**: Unlimited (rate-limited)
- **Considerations**: 36,000 calls/hour is generous
- **Optimization**: Batch requests where possible

### Wowhead

- **Free**: Tooltip script is free to use
- **Terms**: Must link to Wowhead pages

### Warcraft Logs

- **Free tier**: 300 points/day
- **Paid tier**: Unlimited (Patreon)
- **Cost**: ~$10/month for full access

### Raider.IO

- **Free**: Public API is free
- **Terms**: Attribution required

---

## Implementation Phases

### Phase 1: Core Integration
1. Implement BlizzardApiClient with OAuth
2. Add Caffeine cache layer
3. Integrate Wowhead tooltips in Vue
4. Update existing code to use item IDs

### Phase 2: Enhanced Caching
1. Add Redis cache layer
2. Implement cache warming on startup
3. Add cache statistics/monitoring
4. Implement stale-while-revalidate pattern

### Phase 3: Resilience
1. Add circuit breaker pattern
2. Implement fallback chains
3. Add API health monitoring
4. Implement rate limit handling

### Phase 4: Optimization
1. Batch API requests
2. Preload commonly-used data
3. Optimize cache hit rates
4. Add CDN for static assets
