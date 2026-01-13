# 🚨 WoWAudit Data Mapping Gap Analysis

## Current vs Available Data Comparison

### ✅ **Currently Mapped Data** (~15% of available data)

#### Basic Character Info
- ✅ Name, realm, region, class, spec, role
- ✅ Join date, blizzard last modified
- ✅ Basic gear slots (equipped only)
- ✅ Basic statistics (M+ score, vault slots)
- ✅ Warcraft Logs scores (4 difficulties)
- ✅ Track items (mythic/heroic/normal/LFR)
- ✅ Crest counts (basic)
- ✅ Renown levels (basic)
- ✅ PvP ratings (basic brackets)

### ❌ **Missing Critical Data** (~85% of available data)

#### 1. **Advanced Gear Data** (CRITICAL for FLPS)
```json
// Missing from current implementation:
{
  "best_gear": "Complete best-in-slot tracking for all slots",
  "spark_gear": "Ingenuity Spark crafted gear tracking", 
  "enchant_quality": "Enchant quality levels for all slots",
  "upgrade_levels": "Current upgrade levels (0-12) for all gear",
  "sockets_gems": "Empty sockets, gem list, jewelry sockets",
  "embellishments": "Embellished items and spell effects",
  "tier_summary": "Tier piece item levels and difficulties",
  "stat_distribution": "Crit/Haste/Mastery/Vers percentages"
}
```

#### 2. **Performance Metrics** (CRITICAL for FLPS)
```json
// Missing performance tracking:
{
  "historical_activity": "Historical M+, dungeons, world quests",
  "great_vault_history": "9 slots of vault tracking over time",
  "weekly_seasonal_counters": "Heroic dungeons, delves, M+ activity",
  "dungeon_specific": "Individual dungeon completion and scores"
}
```

#### 3. **Preparation Indicators** (CRITICAL for FLPS)
```json
// Missing preparation data:
{
  "profession_recipes": "400+ learned profession recipes",
  "collectibles": "Achievement points, titles, cutting edge",
  "weekly_events": "Awakening the machine, weekly event completion",
  "raid_currency": "Coffer keys, crests, valorstones",
  "world_quest_activity": "Total quests, dailies completed"
}
```

#### 4. **Advanced Character Data**
```json
// Missing character details:
{
  "identity_metadata": "Race, highest ilvl ever equipped, character ID",
  "achievements": "Achievement points, titles, cutting edge, AotC",
  "community_perks": "Theater troupe participation",
  "visibility_flags": "What data is visible on character",
  "reference_values": "Item level thresholds and references"
}
```

---

## 🎯 **Impact on FLPS Calculation**

### **Current FLPS Algorithm Issues**

```kotlin
// Current ScoreCalculator.kt is using MOCK data because real WoWAudit data isn't mapped:

// ❌ Missing for Attendance Commitment Score (ACS):
- Historical raid attendance tracking
- Excuse tracking and raid signup data
- Long-term participation patterns

// ❌ Missing for Mechanical Adherence Score (MAS):  
- Deaths per attempt data (needs Warcraft Logs integration)
- Avoidable damage percentages
- Spec-specific performance baselines

// ❌ Missing for External Preparation Score (EPS):
- Detailed vault slot unlocking (only has 3/9 slots)
- Comprehensive crest usage tracking
- Heroic boss kill tracking
- Profession preparation indicators

// ❌ Missing for Item Priority Index (IPI):
- Best-in-slot gear comparison
- Tier set bonus tracking
- Upgrade potential analysis
- Embellishment and spark gear consideration

// ❌ Missing for Recency Decay Factor (RDF):
- Complete loot history with proper categorization
- Item tier classification (A/B/C tiers)
- Proper award date tracking
```

### **Mock Data vs Real Data Problem**

The current `ScoreCalculatorTest.kt` passes because it uses **mock JSON files** in `docs/data/` rather than real WoWAudit data. The real WoWAudit integration is incomplete.

---

## 🛠️ **Required Implementation Tasks**

### **Phase 1: Critical Data Mapping** (2-3 weeks)

#### Task 1.1: Enhanced Character Response Mapping
```kotlin
// Expand CharactersResponse.kt to include missing fields:

data class CharacterDto(
    // ... existing fields ...
    
    // Add missing critical fields:
    val achievements: AchievementsDto?,
    val historicalActivity: HistoricalActivityDto?,
    val vaultHistory: VaultHistoryDto?,
    val professionRecipes: List<String>?,
    val upgradeProgress: UpgradeProgressDto?,
    val tierSummary: TierSummaryDto?,
    val statDistribution: StatDistributionDto?,
    val weeklyActivity: WeeklyActivityDto?,
    val raidCurrency: RaidCurrencyDto?
)
```

#### Task 1.2: Advanced Gear Tracking
```kotlin
data class AdvancedGearDto(
    val equipped: EquippedGearDto,
    val best: BestGearDto,           // Currently partially mapped
    val spark: SparkGearDto,         // Currently missing
    val upgradeLevels: UpgradeProgressDto,  // Currently missing
    val enchantQuality: EnchantQualityDto,  // Currently missing
    val embellishments: EmbellishmentsDto,  // Currently missing
    val tierSummary: TierSummaryDto,        // Currently missing
    val statDistribution: StatDistributionDto // Currently missing
)
```

#### Task 1.3: Performance & Activity Tracking
```kotlin
data class PerformanceDto(
    val historicalActivity: HistoricalActivityDto,
    val weeklyCounters: WeeklyCountersDto,
    val dungeonSpecific: Map<String, DungeonStatsDto>,
    val vaultProgress: VaultProgressDto,
    val raidProgression: DetailedRaidProgressDto
)
```

### **Phase 2: Data Transformation for FLPS** (1-2 weeks)

#### Task 2.1: Real Data Integration Service
```kotlin
@Service
class WoWAuditDataTransformationService(
    private val raiderRepository: RaiderRepository,
    private val syncService: WoWAuditSyncService
) {
    
    suspend fun transformToScoreInput(raiderId: Long): RaiderInput {
        val raider = raiderRepository.findById(raiderId) 
            ?: throw NotFoundException("Raider not found")
        
        // Transform real WoWAudit data instead of mock data
        return RaiderInput(
            name = raider.characterName,
            role = Role.valueOf(raider.role),
            
            // ACS: Real attendance calculation from raid data
            attendancePercent = calculateRealAttendance(raiderId),
            
            // MAS: Real performance from Warcraft Logs
            deathsPerAttempt = calculateDeathsFromLogs(raiderId),
            avoidableDamagePct = calculateAvoidableDamage(raiderId),
            
            // EPS: Real preparation from WoWAudit
            vaultSlots = calculateVaultSlots(raiderId),
            crestUsageRatio = calculateCrestUsage(raiderId),
            heroicBossesCleared = calculateHeroicKills(raiderId),
            
            // IPI: Real gear analysis
            tierPiecesOwned = calculateTierPieces(raiderId),
            simulatedGain = calculateUpgradeValue(raiderId),
            
            // RDF: Real loot history
            lastAwards = getRecentLootAwards(raiderId)
        )
    }
}
```

#### Task 2.2: Spec Average Calculation
```kotlin
@Service  
class SpecPerformanceService(
    private val warcraftLogsClient: WarcraftLogsClient
) {
    suspend fun calculateSpecAverages(): Map<String, SpecAverage> {
        // Calculate real spec averages from guild data
        // Instead of using mock data from JSON files
    }
}
```

### **Phase 3: External API Integrations** (2-3 weeks)

#### Task 3.1: Warcraft Logs Integration
```kotlin
@Component
class WarcraftLogsClient(
    private val webClient: WebClient,
    private val properties: SyncProperties
) {
    suspend fun fetchCharacterPerformance(
        characterName: String,
        serverName: String
    ): WarcraftLogsData {
        // Real API integration for performance metrics
        // Deaths per attempt, damage taken, etc.
    }
}
```

#### Task 3.2: RaidBots/Simulation Integration
```kotlin
@Component  
class SimulationClient(
    private val webClient: WebClient
) {
    suspend fun simulateUpgrade(
        character: Character,
        item: Item
    ): SimulationResult {
        // Real simulation data for upgrade values
        // Instead of mock droptimizer data
    }
}
```

---

## 📊 **Data Completeness Scorecard**

| Category | Current | Needed | Gap |
|----------|---------|--------|-----|
| **Basic Character Info** | 90% | 100% | Missing race, achievement points |
| **Gear Tracking** | 25% | 100% | Missing best/spark/upgrades/enchants |
| **Performance Metrics** | 10% | 100% | Missing historical/deaths/damage |
| **Preparation Data** | 15% | 100% | Missing professions/recipes/activity |
| **Attendance Tracking** | 0% | 100% | Missing raid signup/attendance |
| **Loot History** | 50% | 100% | Missing tier classification/categorization |
| **External APIs** | 0% | 100% | Missing WCL/RaidBots integration |

**Overall Completeness: ~20%**

---

## 🎯 **Immediate Action Plan**

### Week 1-2: Data Mapping Expansion
1. **Expand WoWAudit Response Models** - Add missing 85% of fields
2. **Update Database Schema** - Add tables for new data types  
3. **Enhance Sync Service** - Parse and store complete WoWAudit data

### Week 3-4: FLPS Integration
1. **Replace Mock Data** - Use real WoWAudit data in score calculations
2. **Implement Data Transformation** - Convert entities to RaiderInput
3. **Add Missing Calculations** - Real attendance, performance, preparation

### Week 5-6: External APIs
1. **Warcraft Logs Integration** - Real performance metrics
2. **Simulation Service** - Real upgrade value calculations
3. **End-to-End Testing** - Validate complete pipeline

### Critical Success Metrics
- [ ] All 400+ WoWAudit fields mapped and stored
- [ ] FLPS calculations use real data (not mock)  
- [ ] Performance metrics from Warcraft Logs
- [ ] Upgrade simulations from external service
- [ ] Complete attendance tracking from raids
- [ ] Proper tier classification for loot

**The current implementation is essentially a sophisticated mock system. Real production readiness requires implementing the missing 85% of data integration.**