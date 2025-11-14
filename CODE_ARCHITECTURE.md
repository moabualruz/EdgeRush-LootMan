# Code Architecture & Navigation Guide

## 🏗️ System Architecture Overview

EdgeRush LootMan follows a **Domain-Driven Design (DDD) architecture** with clear bounded contexts and layered separation:

```
┌─────────────────────────────────────────────────────────────┐
│                     API Layer (REST)                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ REST         │  │ DTOs         │  │ Exception    │     │
│  │ Controllers  │  │ (Request/    │  │ Handlers     │     │
│  │              │  │  Response)   │  │              │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│              Application Layer (Use Cases)                   │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Bounded Context Services                            │  │
│  │  - FLPS Calculation Use Cases                        │  │
│  │  - Loot Distribution Use Cases                       │  │
│  │  - Attendance Tracking Use Cases                     │  │
│  │  - Raid Management Use Cases                         │  │
│  │  - Application Review Use Cases                      │  │
│  │  - Integration Sync Use Cases                        │  │
│  └──────────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│                   Domain Layer                               │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Domain Models & Business Logic                      │  │
│  │  - Entities, Value Objects, Aggregates               │  │
│  │  - Domain Services                                    │  │
│  │  - Repository Interfaces                             │  │
│  │  - Domain Events                                      │  │
│  └──────────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│              Infrastructure Layer                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Repositories, External APIs, Database               │  │
│  │  - Repository Implementations                        │  │
│  │  - WoWAudit Client, Warcraft Logs Client            │  │
│  │  - PostgreSQL Database                               │  │
│  │  - Entity Mappers                                    │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

## 🎯 Bounded Contexts

The system is organized into the following bounded contexts:

1. **FLPS** - Final Loot Priority Score calculation and modifiers
2. **Loot** - Loot distribution, awards, and bans
3. **Attendance** - Raid attendance tracking and reporting
4. **Raids** - Raid scheduling, signups, and management
5. **Applications** - Guild application review and processing
6. **Integrations** - External API synchronization (WoWAudit, Warcraft Logs)
7. **Shared** - Cross-cutting concerns (Raider, Guild entities)

## 📦 Package Structure (Domain-Driven Design)

### Layered Architecture

Each bounded context follows a consistent layered structure:

```
com.edgerush.{datasync|lootman}/
├── api/                           # API Layer (REST endpoints)
│   ├── v1/                        # Versioned REST controllers
│   ├── dto/                       # Request/Response DTOs
│   └── exception/                 # API exception handlers
│
├── application/                   # Application Layer (Use Cases)
│   ├── {context}/                 # Per bounded context
│   │   ├── *UseCase.kt           # Use case implementations
│   │   └── Commands.kt            # Command objects
│   └── shared/                    # Cross-cutting use cases
│
├── domain/                        # Domain Layer (Business Logic)
│   ├── {context}/                 # Per bounded context
│   │   ├── model/                 # Entities, Value Objects, Aggregates
│   │   ├── service/               # Domain services
│   │   └── repository/            # Repository interfaces
│   └── shared/                    # Shared domain concepts
│       └── model/                 # Raider, Guild, etc.
│
└── infrastructure/                # Infrastructure Layer
    ├── persistence/               # Database implementations
    │   ├── entity/                # JPA/JDBC entities
    │   ├── repository/            # Repository implementations
    │   └── mapper/                # Entity ↔ Domain mappers
    ├── external/                  # External API clients
    │   ├── wowaudit/
    │   ├── warcraftlogs/
    │   └── raidbots/
    └── config/                    # Configuration classes
```

### Bounded Context Examples

**FLPS Context:**
```
com.edgerush.datasync/
├── api/v1/FlpsController.kt
├── application/flps/
│   ├── CalculateFlpsScoreUseCase.kt
│   ├── UpdateModifiersUseCase.kt
│   └── GetFlpsReportUseCase.kt
├── domain/flps/
│   ├── model/
│   │   ├── FlpsScore.kt (Value Object)
│   │   ├── RaiderMeritScore.kt (Value Object)
│   │   └── ItemPriorityIndex.kt (Value Object)
│   ├── service/FlpsCalculationService.kt
│   └── repository/FlpsModifierRepository.kt (interface)
└── infrastructure/persistence/
    └── repository/JdbcFlpsModifierRepository.kt
```

**Loot Context:**
```
com.edgerush.lootman/
├── api/loot/LootController.kt
├── application/loot/
│   ├── AwardLootUseCase.kt
│   ├── ManageLootBansUseCase.kt
│   └── GetLootHistoryUseCase.kt
├── domain/loot/
│   ├── model/
│   │   ├── LootAward.kt (Aggregate Root)
│   │   ├── LootBan.kt (Entity)
│   │   └── LootTier.kt (Value Object)
│   ├── service/LootDistributionService.kt
│   └── repository/
│       ├── LootAwardRepository.kt (interface)
│       └── LootBanRepository.kt (interface)
└── infrastructure/loot/
    ├── JdbcLootAwardRepository.kt
    └── InMemoryLootBanRepository.kt
```

## 📁 Detailed File Structure Guide

### Core Application (`data-sync-service/src/main/kotlin/com/edgerush/datasync/`)

```
com.edgerush.datasync/
├── DataSyncApplication.kt          # 🚀 Spring Boot entry point
├── config/                         # ⚙️ Configuration classes
│   ├── SyncProperties.kt           # Environment-based config
│   └── WebClientConfig.kt          # HTTP client setup
├── client/                         # 🔌 External API integrations
│   ├── WoWAuditClient.kt          # Primary data source
│   ├── WarcraftLogsClient.kt      # Combat log analysis
│   └── RaidbotsClient.kt          # Gear simulation
├── service/                        # 🧠 Business logic
│   ├── ScoreCalculator.kt         # ⭐ FLPS algorithm core
│   ├── WoWAuditScheduler.kt       # Scheduled sync jobs
│   ├── WoWAuditStartupRunner.kt   # Optional startup sync
│   └── ValidationService.kt       # Data integrity checks
├── entity/                         # 📊 Database models
│   ├── Character.kt               # Player data
│   ├── LootHistory.kt             # Past distributions
│   ├── WishlistEntry.kt           # Player preferences
│   └── RaidSnapshot.kt            # Historical data
├── repository/                     # 💾 Data access layer
│   ├── CharacterRepository.kt
│   ├── LootHistoryRepository.kt
│   └── WishlistRepository.kt
├── dto/                           # 📦 Data transfer objects
│   ├── wowaudit/                  # API response models
│   ├── internal/                  # Internal data structures
│   └── score/                     # FLPS calculation DTOs
└── exception/                     # ⚠️ Error handling
    ├── SyncException.kt
    ├── ValidationException.kt
    └── GlobalExceptionHandler.kt
```

### Configuration & Resources (`data-sync-service/src/main/resources/`)

```
resources/
├── application.yaml               # 🔧 Main configuration
├── application-sqlite.yaml       # 🧪 Testing configuration
└── db/migration/postgres/         # 🗃️ Database schema evolution
    ├── V0001__init.sql           # Initial schema
    ├── V0002__wishlist_snapshots.sql
    ├── V0003__wowaudit_snapshots.sql
    ├── V0004__wowaudit_normalized.sql
    ├── V0005__expand_roster.sql
    ├── V0006__expand_applications.sql
    ├── V0007__expand_loot_history.sql
    ├── V0008__expand_raids_attendance_wishlists.sql
    ├── V0009__team_period_metadata.sql
    └── V0010__expand_team_metadata.sql
```

### Testing Structure (`data-sync-service/src/test/kotlin/`)

```
test/kotlin/com/edgerush/datasync/
├── AcceptanceSmokeTest.kt         # 🔍 End-to-end validation
├── api/wowaudit/
│   └── CharacterDeserializerTest.kt
├── client/
│   └── WoWAuditClientTest.kt      # External API mocking
├── config/
│   └── SyncPropertiesTest.kt      # Configuration validation
├── schema/
│   └── WoWAuditSchemaTest.kt      # Data format validation
└── service/
    └── ScoreCalculatorTest.kt     # ⭐ FLPS algorithm tests
```

## 🎯 Key Components Deep Dive

### 1. ScoreCalculator.kt - FLPS Algorithm Core

**Location**: `service/ScoreCalculator.kt`
**Purpose**: Implements the Final Loot Priority Score calculation

**Key Methods**:
```kotlin
fun calculateFLPS(character: Character, item: Item): FLPSResult
fun calculateRMS(attendance: AttendanceData): Double
fun calculateIPI(upgrade: UpgradeData): Double
fun calculateRDF(recentLoot: List<LootAward>): Double
```

**Critical Business Rules**:
- RMS caps at 0.0 for critical mechanical failures
- IPI tier set bonuses override individual upgrades
- RDF applies time-based decay factors

### 2. WoWAuditClient.kt - Primary Data Source

**Location**: `client/WoWAuditClient.kt`
**Purpose**: Fetches all guild data from WoWAudit API

**Key Endpoints**:
```kotlin
suspend fun fetchCharacters(): List<CharacterData>
suspend fun fetchTeamData(): TeamData
suspend fun fetchLootHistory(season: String): LootHistoryData
suspend fun fetchWishlists(): WishlistData
suspend fun fetchAttendance(): AttendanceData
```

**Error Handling**:
- Retry with exponential backoff
- Circuit breaker for API failures
- Graceful degradation strategies

### 3. Database Entities - Data Model

**Character.kt**: Core player information
```kotlin
@Entity
data class Character(
    val name: String,
    val realm: String,
    val characterClass: String,
    val spec: String,
    val role: Role,
    val itemLevel: Int
)
```

**LootHistory.kt**: Historical loot awards
```kotlin
@Entity
data class LootHistory(
    val characterName: String,
    val itemName: String,
    val awardDate: LocalDateTime,
    val flpsScore: Double,
    val tier: LootTier
)
```

## 🔄 Data Flow Architecture

### 1. Sync Process Flow
```
WoWAudit API → WoWAuditClient → Validation → Database Entities → Repository Save
     ↓
Scheduled Job → Data Fetch → Transform → Calculate Scores → Store Results
     ↓
Health Check → Verify Data → Update Status → Expose Metrics
```

### 2. Score Calculation Flow
```
Character Data + Item Data → ScoreCalculator → FLPS Components
     ↓
RMS Calculation (Attendance + Performance + Preparation)
     ↓
IPI Calculation (Upgrade Value + Tier Sets + Role Multiplier)
     ↓
RDF Application (Recent Loot Decay)
     ↓
Final Score + Reasoning → Database Storage
```

## 🧩 Module Dependencies

### Core Dependencies
```kotlin
// Spring Boot ecosystem
implementation("org.springframework.boot:spring-boot-starter-webflux")
implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
implementation("org.springframework.boot:spring-boot-starter-actuator")

// Database & migrations
implementation("org.flywaydb:flyway-core")
runtimeOnly("org.postgresql:postgresql")

// Kotlin & coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

// Resilience & monitoring
implementation("io.github.resilience4j:resilience4j-spring-boot3")
```

### Development Dependencies
```kotlin
// Testing framework
testImplementation("org.springframework.boot:spring-boot-starter-test")
testImplementation("io.projectreactor:reactor-test")
testImplementation("com.squareup.okhttp3:mockwebserver")
```

## 🎛️ Configuration Management

### Environment Variables
```yaml
# Database connection
POSTGRES_HOST: postgres
POSTGRES_PORT: 5432
POSTGRES_DB: edgerush
POSTGRES_USER: edgerush
POSTGRES_PASSWORD: edgerush

# External APIs
WOWAUDIT_API_KEY: ${api_key}
WOWAUDIT_GUILD_URI: ${guild_url}
WARCRAFT_LOGS_CLIENT_ID: ${client_id}
RAIDBOTS_API_KEY: ${api_key}

# Application behavior
SYNC_RUN_ON_STARTUP: true
SPRING_PROFILES_ACTIVE: default
```

### Spring Profiles
- **default**: PostgreSQL with full external API integration
- **sqlite**: Local testing with file-based database
- **test**: Mock external services for unit testing

## 🚀 Deployment Architecture

### Docker Compose Stack
```yaml
services:
  postgres:    # PostgreSQL 18 database
  data-sync:   # Spring Boot app (custom JDK 24 image)
  nginx:       # Reverse proxy for API access
```

### Container Communication
- **Internal network**: Services communicate via container names
- **Volume mounting**: Source code for development
- **Port mapping**: 80 (nginx) → 8080 (app) → 5432 (db)

## 📊 Monitoring & Observability

### Health Checks
- **Application**: `/actuator/health`
- **Database**: Connection pool metrics
- **External APIs**: Circuit breaker status

### Logging Strategy
- **Structured logging**: JSON format for parsing
- **Log levels**: DEBUG for development, INFO for production
- **Correlation IDs**: Track requests across services

---

## 🎯 AI Agent Navigation Tips

### Starting Points for Common Tasks

**Algorithm Changes**: Start with `ScoreCalculatorTest.kt` → `ScoreCalculator.kt`
**API Integration**: Start with `WoWAuditClient.kt` → add tests
**Database Schema**: Start with latest migration → create new migration
**Configuration**: Start with `application.yaml` → `SyncProperties.kt`
**New Features**: Start with `docs/project-plan.md` → design → implement

### Code Quality Indicators
- **Test Coverage**: Aim for >80% on business logic
- **Documentation**: Every public method should have KDoc
- **Error Handling**: All external calls must handle failures
- **Performance**: Database queries should be optimized
- **Security**: API keys and sensitive data properly managed