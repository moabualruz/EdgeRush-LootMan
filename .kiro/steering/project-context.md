# EdgeRush LootMan Project Context

## Project Overview

EdgeRush LootMan (ELM) is a progression-first guild operations platform for World of Warcraft raid teams that automates loot distribution using the FLPS (Final Loot Priority Score) algorithm.

## Current Implementation Status (Verified from Code)

### ✅ FULLY IMPLEMENTED

#### Data Sync Infrastructure
- **WoWAudit API Integration**: Complete client with all endpoints
  - Characters/Roster sync
  - Attendance tracking
  - Raid management
  - Loot history
  - Wishlists
  - Applications
  - Historical activity
  - Team/Period metadata

#### Database Layer
- **PostgreSQL Schema**: 17 migrations covering all data models
- **Entities**: 45+ entity classes for complete data model
- **Repositories**: Spring Data JDBC repositories for all entities
- **Test Database**: Separate `edgerush_test` database for isolated testing
- **Migration Status**: All migrations applied to both dev and test databases

#### FLPS Calculation Engine
- **ScoreCalculator**: Core algorithm implementation
- **WoWAuditDataTransformerService**: Transforms synced data for FLPS
- **FlpsModifierService**: Guild-specific configuration support
- **BehavioralScoreService**: Behavioral scoring system
- **Real Data Integration**: FLPS calculations use actual WoWAudit data

#### Warcraft Logs Integration (100% Complete)
- **OAuth2 Client**: GraphQL-based API client with authentication
- **Sync Service**: Automated report and performance data synchronization
- **Performance Service**: MAS calculation using real combat data
- **ScoreCalculator Integration**: MAS fully integrated into FLPS calculations
- **REST Endpoints**: Configuration, sync management, performance queries
- **Scheduled Execution**: Automatic sync every 6 hours
- **Character Mapping**: WoWAudit ↔ Warcraft Logs name resolution
- **Health Indicator**: Custom health check for WCL integration status
- **Database Tables**: 5 tables for reports, fights, performance, config, mappings

#### API Endpoints
- **FlpsController**: `/api/flps/{guildId}` - Comprehensive FLPS reports
- **GuildManagementController**: Guild configuration management
- **WarcraftLogsConfigController**: Warcraft Logs configuration and mappings
- **WarcraftLogsSyncController**: Manual sync, status, performance data
- **Health Checks**: Spring Actuator endpoints

#### Configuration System
- **Guild-specific modifiers**: Customizable weights and thresholds
- **Behavioral actions**: Track and score player behavior
- **Loot bans**: Time-limited loot restrictions
- **Perfect score benchmarks**: Theoretical/top performer/custom
- **Warcraft Logs Config**: Per-guild credentials, sync settings, MAS weights

#### Test Suite (100% Passing)
- **36 Tests**: All passing with 0 failures
- **Unit Tests**: 31 tests covering core business logic
- **Integration Tests**: 5 tests covering REST endpoints and workflows
- **Test Configuration**: Separate database, disabled security, servlet stack
- **Coverage**: FLPS calculations, behavioral scoring, data transformation, API endpoints

### ⚠️ PARTIALLY IMPLEMENTED

#### Raidbots Integration (40% Complete)
- **Database Schema**: V0017 migration with entities and repositories
- **Configuration System**: Properties and guild-specific config
- **Profile Generation**: SimC profile generation from WoWAudit data
- **ScoreCalculator Integration**: Fallback to wishlist percentages
- **Missing**: API client (requires API key), simulation service, upgrade value calculation
- **Blocker**: Raidbots API key availability uncertain

### ❌ NOT IMPLEMENTED (Missing Features)

#### External API Integrations
- **Raidbots API**: Partial client implementation
  - Needed for: Upgrade Value calculation - Droptimizer simulation data
  - Impact: Using wishlist percentages as proxy (less accurate)
  - Blocker: API key availability on developer documentation unclear

### ❌ NOT IMPLEMENTED (Missing Features)

#### External API Integrations
- **Warcraft Logs API**: No client implementation
  - Needed for: Mechanical Adherence Score (MAS) - death rates, avoidable damage
  - Impact: MAS currently returns 0.0 (placeholder)
  
- **Raidbots API**: No client implementation
  - Needed for: Upgrade Value calculation - Droptimizer simulation data
  - Impact: Using wishlist percentages as proxy (less accurate)

#### User Interfaces
- **Web Dashboard**: No frontend implementation
  - Player-facing FLPS transparency
  - Admin panel for loot council
  - Real-time score visualization
  - Loot history and audit trail

- **Discord Bot**: No bot implementation
  - Automated loot announcements
  - RDF expiry notifications
  - Penalty alerts
  - Appeals workflow integration

#### In-Game Integration
- **RC Loot Council Addon**: No integration
  - FLPS display in voting frames
  - Automated decision recording

#### Advanced Features
- **Analytics Dashboard**: No implementation
  - Loot equity charts
  - Progression correlation analysis
  - Attendance trend reports
  - Performance dashboards

## Architecture Patterns to Follow

### Service Layer Pattern
```kotlin
@Service
class MyService(
    private val repository: MyRepository,
    private val otherService: OtherService
) {
    fun doSomething(): Result<Data> = runCatching {
        // Implementation
    }
}
```

### Client Pattern (for external APIs)
```kotlin
@Component
class ExternalApiClient(
    private val webClient: WebClient,
    private val properties: ApiProperties
) {
    suspend fun fetchData(): ApiResponse {
        return webClient.get()
            .uri(properties.baseUrl + "/endpoint")
            .retrieve()
            .awaitBody()
    }
}
```

### Controller Pattern
```kotlin
@RestController
@RequestMapping("/api/resource")
class ResourceController(
    private val service: ResourceService
) {
    @GetMapping("/{id}")
    fun getResource(@PathVariable id: String): ResourceDto {
        return service.getResource(id)
    }
}
```

## Data Flow

```
External APIs → Clients → Sync Services → Database Entities → Repositories
                                                                    ↓
                                                          Transformer Services
                                                                    ↓
                                                          Business Logic Services
                                                                    ↓
                                                          REST Controllers → Clients
```

## Testing Standards

- Unit tests for business logic
- Integration tests for repositories
- Client tests with MockWebServer
- End-to-end tests for critical workflows
- Minimum 80% coverage on service layer

## Key Constraints

1. **No Mock Data in Production**: All calculations must use real synced data
2. **Guild-Specific Configuration**: Support customizable weights and thresholds
3. **Transparency**: All scores must be auditable and explainable
4. **Performance**: Calculations should complete in < 1 second for 30 raiders
5. **Error Handling**: Graceful degradation when external APIs fail

## Priority Features for Specs

1. **Warcraft Logs Integration** (Critical) - Enables accurate MAS scoring
2. **Raidbots Integration** (Critical) - Enables accurate upgrade value calculation
3. **Web Dashboard** (High) - Transparency requirement
4. **Discord Bot** (High) - Operational efficiency
5. **RC Loot Council Integration** (Medium) - In-game convenience
6. **Advanced Analytics** (Low) - Enhancement feature
