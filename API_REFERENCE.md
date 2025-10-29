# API Reference & Interface Documentation

## 🔌 External API Integrations

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
```kotlin
/**
 * Fetches complete character roster with current progression status.
 * 
 * @return List of all guild members with equipment and spec information
 * @throws ApiConnectionException if request fails or times out
 * @throws DataValidationException if response format is invalid
 */
suspend fun fetchCharacters(): Result<List<CharacterData>> {
    return executeWithRetry("/characters") { response ->
        response.mapToCharacterList()
            .also { validateCharacterData(it) }
    }
}

// Response Schema
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
```kotlin
/**
 * Retrieves team composition and raid scheduling information.
 * 
 * @return Current team structure with role assignments
 */
suspend fun fetchTeamData(): Result<TeamData> {
    return executeWithRetry("/team") { response ->
        response.mapToTeamData()
    }
}

// Response Schema
data class TeamData(
    val teamId: String,
    val name: String,
    val description: String,
    val members: List<TeamMember>,
    val schedule: RaidSchedule,
    val progression: ProgressionStatus
)

data class TeamMember(
    val characterName: String,
    val role: String,
    val rank: String,
    val joinDate: String,
    val status: String // active, inactive, trial, etc.
)
```

##### 3. Attendance Tracking
```kotlin
/**
 * Fetches attendance data for FLPS calculations.
 * 
 * @param periodId Optional specific time period (defaults to current)
 * @return Attendance records with raid participation percentages
 */
suspend fun fetchAttendance(periodId: String? = null): Result<AttendanceData> {
    val endpoint = periodId?.let { "/attendance/$it" } ?: "/attendance"
    return executeWithRetry(endpoint) { response ->
        response.mapToAttendanceData()
    }
}

// Response Schema
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
```kotlin
/**
 * Retrieves historical loot awards for RDF calculations.
 * 
 * @param season WoW season/expansion identifier
 * @return Complete loot distribution history with timestamps
 */
suspend fun fetchLootHistory(season: String = "current"): Result<LootHistoryData> {
    return executeWithRetry("/loot_history/$season") { response ->
        response.mapToLootHistory()
    }
}

// Response Schema  
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
    val source: String, // raid, dungeon, etc.
    val difficulty: String,
    val notes: String?
)
```

##### 5. Wishlist Management
```kotlin
/**
 * Fetches player wishlists for priority calculations.
 * 
 * @return Current wishlist entries with priority rankings
 */
suspend fun fetchWishlists(): Result<WishlistData> {
    return executeWithRetry("/wishlists") { response ->
        response.mapToWishlistData()
    }
}

// Response Schema
data class WishlistData(
    val lastUpdated: String,
    val entries: List<WishlistEntry>
)

data class WishlistEntry(
    val characterName: String,
    val itemName: String,
    val itemId: Int,
    val priority: Int, // 1 = highest priority
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
): Result<T> {
    return runCatching {
        val response = webClient
            .get()
            .uri("$baseUrl/$endpoint")
            .header("Authorization", "Bearer ${properties.wowaudit.apiKey}")
            .header("X-API-Version", API_VERSION)
            .retrieve()
            .awaitBody<JsonNode>()
            .also { validateResponse(it) }
        
        mapper(response)
    }.onFailure { exception ->
        logger.error("API call failed for endpoint: $endpoint", exception)
        metrics.incrementCounter("api_errors", "endpoint", endpoint)
    }
}
```

#### Circuit Breaker Integration
```kotlin
@CircuitBreaker(
    name = "wowaudit-api",
    fallbackMethod = "fallbackToCache"
)
suspend fun fetchCharacters(): Result<List<CharacterData>> {
    // Primary implementation
}

suspend fun fallbackToCache(exception: Exception): Result<List<CharacterData>> {
    logger.warn("WoWAudit API unavailable, falling back to cache", exception)
    return cacheService.getLastKnownCharacters()
}
```

---

## 🏠 Internal API Interfaces

### Score Calculation Service

#### FLPS Algorithm Interface
```kotlin
/**
 * Core service for calculating Final Loot Priority Scores.
 * 
 * The FLPS algorithm is the heart of EdgeRush LootMan, determining
 * fair and transparent loot distribution based on multiple factors.
 */
interface ScoreCalculationService {
    
    /**
     * Calculates complete FLPS for a character-item combination.
     * 
     * @param request Complete calculation context
     * @return Detailed score breakdown with reasoning
     */
    suspend fun calculateFLPS(request: FLPSRequest): FLPSResult
    
    /**
     * Calculates scores for multiple candidates simultaneously.
     * Optimized for loot council decision-making scenarios.
     */
    suspend fun calculateBulkFLPS(
        candidates: List<Character>,
        item: Item,
        context: RaidContext
    ): List<FLPSResult>
    
    /**
     * Validates that all required data is available for scoring.
     */
    suspend fun validateScoringData(character: Character): ValidationResult
}

// Request/Response Models
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
    val rms: Double, // Raider Merit Score
    val ipi: Double, // Item Priority Index  
    val rdf: Double, // Recency Decay Factor
    val breakdown: ComponentBreakdown
)

data class ComponentBreakdown(
    val attendance: AttendanceScore,
    val performance: PerformanceScore,
    val preparation: PreparationScore,
    val upgradeValue: UpgradeScore,
    val tierSetImpact: TierSetScore,
    val roleMultiplier: RoleScore,
    val recentLootPenalty: DecayScore
)
```

#### Implementation Example
```kotlin
@Service
class ScoreCalculatorImpl(
    private val attendanceService: AttendanceService,
    private val performanceService: PerformanceService,
    private val simulationService: SimulationService,
    private val lootHistoryService: LootHistoryService
) : ScoreCalculationService {
    
    override suspend fun calculateFLPS(request: FLPSRequest): FLPSResult {
        val character = request.character
        val item = request.item
        
        // Calculate RMS components
        val attendanceScore = attendanceService.calculateScore(character)
        val performanceScore = performanceService.calculateScore(character, request.raidContext)
        val preparationScore = calculatePreparationScore(character)
        
        val rms = (attendanceScore * 0.4 + performanceScore * 0.4 + preparationScore * 0.2)
            .coerceIn(0.0, 1.0)
        
        // Calculate IPI components
        val upgradeValue = simulationService.calculateUpgradeValue(character, item)
        val tierSetImpact = calculateTierSetImpact(character, item)
        val roleMultiplier = calculateRoleMultiplier(character, item, request.raidContext)
        
        val ipi = (upgradeValue * 0.45 + tierSetImpact * 0.35 + roleMultiplier * 0.20)
            .coerceIn(0.0, 1.0)
        
        // Calculate RDF
        val recentLoot = lootHistoryService.getRecentLoot(character)
        val rdf = calculateRecencyDecayFactor(recentLoot, item)
        
        // Final calculation
        val totalScore = (rms * ipi) * rdf
        
        return FLPSResult(
            character = character.name,
            item = item.name,
            totalScore = totalScore,
            components = ScoreComponents(
                rms = rms,
                ipi = ipi,
                rdf = rdf,
                breakdown = buildComponentBreakdown(
                    attendanceScore, performanceScore, preparationScore,
                    upgradeValue, tierSetImpact, roleMultiplier, rdf
                )
            ),
            reasoning = buildReasoning(rms, ipi, rdf, character, item)
        )
    }
}
```

### Data Sync Service Interface

```kotlin
/**
 * Manages synchronization of external data sources.
 */
interface SyncService {
    
    /**
     * Performs full sync of all guild data.
     * Should be used sparingly due to API rate limits.
     */
    suspend fun performFullSync(): SyncResult
    
    /**
     * Performs incremental sync of recently changed data.
     * Preferred method for regular updates.
     */
    suspend fun performIncrementalSync(): SyncResult
    
    /**
     * Gets the status of the last sync operation.
     */
    suspend fun getLastSyncStatus(): SyncStatus
    
    /**
     * Manually triggers sync for specific data types.
     */
    suspend fun syncSpecificData(dataTypes: Set<DataType>): SyncResult
}

// Sync Models
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

data class SyncError(
    val dataType: DataType,
    val message: String,
    val exception: String?,
    val recoverable: Boolean
)
```

### Repository Interfaces

#### Character Repository
```kotlin
/**
 * Data access layer for character information.
 */
interface CharacterRepository : JpaRepository<Character, Long> {
    
    /**
     * Finds character by name and realm (unique identifier).
     */
    fun findByNameAndRealm(name: String, realm: String): Character?
    
    /**
     * Gets all active guild members with recent activity.
     */
    @Query("SELECT c FROM Character c WHERE c.lastUpdated > :since AND c.status = 'ACTIVE'")
    fun findActiveCharactersSince(@Param("since") since: LocalDateTime): List<Character>
    
    /**
     * Bulk update for sync efficiency.
     */
    @Modifying
    @Query("UPDATE Character c SET c.itemLevel = :itemLevel, c.lastUpdated = :timestamp WHERE c.id = :id")
    fun updateItemLevel(@Param("id") id: Long, @Param("itemLevel") itemLevel: Int, @Param("timestamp") timestamp: LocalDateTime)
    
    /**
     * Gets characters eligible for specific item type.
     */
    fun findByRoleAndStatus(role: Role, status: CharacterStatus): List<Character>
}
```

#### Loot History Repository
```kotlin
interface LootHistoryRepository : JpaRepository<LootHistory, Long> {
    
    /**
     * Gets recent loot for RDF calculations.
     */
    fun findByCharacterNameAndAwardDateAfter(
        characterName: String, 
        cutoffDate: LocalDateTime
    ): List<LootHistory>
    
    /**
     * Gets loot history for specific item across all characters.
     */
    fun findByItemNameOrderByAwardDateDesc(itemName: String): List<LootHistory>
    
    /**
     * Aggregates loot distribution statistics.
     */
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

## 📊 Health & Monitoring APIs

### Health Check Endpoints

```kotlin
@RestController
@RequestMapping("/actuator")
class CustomHealthController {
    
    /**
     * Detailed health check with dependency status.
     */
    @GetMapping("/health/detailed")
    suspend fun getDetailedHealth(): HealthResponse {
        return HealthResponse(
            status = determineOverallStatus(),
            components = mapOf(
                "database" to checkDatabaseHealth(),
                "wowaudit-api" to checkWoWAuditHealth(),
                "sync-status" to checkLastSyncHealth(),
                "score-calculator" to checkScoreCalculatorHealth()
            ),
            timestamp = Instant.now()
        )
    }
    
    private suspend fun checkWoWAuditHealth(): ComponentHealth {
        return try {
            val response = wowAuditClient.ping()
            ComponentHealth.up("API responding normally")
        } catch (exception: Exception) {
            ComponentHealth.down("API unavailable: ${exception.message}")
        }
    }
}

data class HealthResponse(
    val status: HealthStatus,
    val components: Map<String, ComponentHealth>,
    val timestamp: Instant
)

data class ComponentHealth(
    val status: HealthStatus,
    val details: Map<String, Any> = emptyMap()
) {
    companion object {
        fun up(message: String) = ComponentHealth(HealthStatus.UP, mapOf("message" to message))
        fun down(message: String) = ComponentHealth(HealthStatus.DOWN, mapOf("error" to message))
    }
}
```

### Metrics Endpoints

```kotlin
/**
 * Custom metrics for EdgeRush LootMan operations.
 */
@Component
class SyncMetrics(private val meterRegistry: MeterRegistry) {
    
    private val syncDuration = Timer.builder("sync.duration")
        .description("Time taken for sync operations")
        .register(meterRegistry)
    
    private val scoreCalculations = Counter.builder("score.calculations")
        .description("Number of FLPS calculations performed")
        .register(meterRegistry)
    
    private val apiErrors = Counter.builder("api.errors")
        .description("External API call failures")
        .register(meterRegistry)
    
    fun recordSyncDuration(duration: Duration, dataType: DataType) {
        Timer.Sample.start(meterRegistry)
            .stop(syncDuration.tag("type", dataType.name))
    }
    
    fun incrementScoreCalculations(character: String) {
        scoreCalculations.increment(Tags.of("character", character))
    }
}
```

---

## 🎯 Usage Examples

### Complete Score Calculation Flow
```kotlin
/**
 * Example: Calculate FLPS for loot distribution decision
 */
suspend fun demonstrateScoreCalculation() {
    // 1. Fetch current character data
    val character = characterRepository.findByNameAndRealm("PlayerName", "Stormrage")
        ?: throw CharacterNotFoundException("Player not found")
    
    // 2. Define the item being distributed
    val item = Item(
        name = "Neltharion's Call to Dominance",
        itemId = 204202,
        itemLevel = 441,
        slot = EquipmentSlot.TRINKET,
        stats = mapOf("intellect" to 1234, "haste" to 567),
        isTierPiece = false
    )
    
    // 3. Set raid context
    val raidContext = RaidContext(
        encounter = "Kazzara, the Hellforged",
        difficulty = Difficulty.MYTHIC,
        date = LocalDateTime.now(),
        attendees = listOf(/* raid roster */)
    )
    
    // 4. Calculate FLPS
    val request = FLPSRequest(character, item, raidContext)
    val result = scoreCalculator.calculateFLPS(request)
    
    // 5. Present results
    println("FLPS Result for ${character.name}:")
    println("Total Score: ${result.totalScore}")
    println("RMS: ${result.components.rms}")
    println("IPI: ${result.components.ipi}")  
    println("RDF: ${result.components.rdf}")
    println("Reasoning: ${result.reasoning.joinToString("; ")}")
}
```

### Bulk Score Calculation for Loot Council
```kotlin
suspend fun calculateScoresForAllCandidates(item: Item): List<FLPSResult> {
    // Get all characters eligible for this item
    val eligibleCharacters = characterRepository.findByRoleAndStatus(
        role = item.primaryRole,
        status = CharacterStatus.ACTIVE
    )
    
    // Calculate scores for all candidates
    val results = scoreCalculator.calculateBulkFLPS(
        candidates = eligibleCharacters,
        item = item,
        context = getCurrentRaidContext()
    )
    
    // Sort by score descending
    return results.sortedByDescending { it.totalScore }
}
```

This comprehensive API documentation provides AI agents with all the necessary interfaces, patterns, and examples to effectively work with the EdgeRush LootMan codebase.