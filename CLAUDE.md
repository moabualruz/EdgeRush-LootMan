# EdgeRush LootMan

A progression-first guild operations platform for World of Warcraft that automates fair loot distribution using the FLPS (Final Loot Priority Score) algorithm.

---

## Core Philosophy

- **Data-driven fairness** - Loot decisions backed by objective metrics, not politics
- **Transparency over black boxes** - Every score is explainable and auditable
- **Human approval at boundaries** - FLPS informs decisions, humans make them
- **Test-driven development** - Tests BEFORE implementation, always
- **Domain-driven design** - Code organized by business domain, not technical layers

---

## Seven Principles (Non-Negotiable)

1. **No mock data in production** - All calculations use real synced data
2. **Guild-specific configuration** - Every guild can tune weights and thresholds
3. **Scores must be auditable** - Full breakdown of every FLPS calculation
4. **Tests before features** - TDD enforced, no untested code to main
5. **Graceful degradation** - System works even when external APIs fail
6. **Single source of truth** - One authoritative document per topic
7. **Human approval for loot** - No automated distribution without confirmation

---

## TDD Development Workflow (MANDATORY)

**Every feature MUST follow this workflow:**

### Red-Green-Refactor Cycle

```
1. RED:    Write a failing test first
2. GREEN:  Write minimal code to make the test pass
3. REFACTOR: Improve code while keeping tests green
4. REPEAT: Continue until feature is complete
```

### TDD Rules

1. **NEVER write production code without a failing test first**
2. **Write only enough test to fail** (compilation failure counts)
3. **Write only enough code to pass the failing test**
4. **Refactor only when all tests are green**
5. **Each test should test ONE behavior**

### Example TDD Flow

```kotlin
// Step 1: Write failing test
@Test
fun `should calculate FLPS score for raider with item`() {
    val raider = createTestRaider()
    val item = createTestItem()

    val score = calculator.calculate(raider, item)

    score.value shouldBeGreaterThan 0.0
}

// Step 2: Write minimal implementation to pass
class FlpsCalculator {
    fun calculate(raider: Raider, item: Item): FlpsScore {
        return FlpsScore(0.5) // Minimal implementation
    }
}

// Step 3: Refactor and add more tests
```

### Test Categories

| Category        | Purpose                      | Dependencies               | Location                 |
|-----------------|------------------------------|----------------------------|--------------------------|
| **Unit**        | Test single class/function   | None (mocked)              | `test/.../domain/`       |
| **Integration** | Test component interactions  | Real DB (Testcontainers)   | `test/.../integration/`  |
| **API**         | Test REST endpoints          | MockMvc + mocked services  | `test/.../api/`          |
| **E2E**         | Test full workflows          | Full stack                 | `test/.../e2e/`          |

---

## Domain-Driven Design (DDD)

### Layer Architecture

```
┌─────────────────────────────────────────────────────────┐
│                      API Layer                          │
│  Controllers, DTOs, Request/Response mapping            │
├─────────────────────────────────────────────────────────┤
│                  Application Layer                      │
│  Use Cases, Orchestration, Transaction boundaries       │
├─────────────────────────────────────────────────────────┤
│                    Domain Layer                         │
│  Entities, Value Objects, Domain Services, Repositories │
├─────────────────────────────────────────────────────────┤
│                 Infrastructure Layer                    │
│  Database, External APIs, Messaging, Caching           │
└─────────────────────────────────────────────────────────┘
```

### DDD Rules

1. **Domain layer has NO dependencies** on other layers
2. **Application layer depends only on Domain**
3. **Infrastructure implements Domain interfaces**
4. **API layer coordinates Application services**

### Bounded Context Organization

```
lootman/
├── flps/                    # FLPS Calculation Context
│   ├── api/                 # REST controllers
│   ├── application/         # Use cases
│   ├── domain/              # Core business logic
│   └── infrastructure/      # Persistence, external
│
├── loot/                    # Loot Management Context
├── attendance/              # Attendance Context
├── raids/                   # Raid Management Context
└── shared/                  # Shared Kernel
```

### Value Objects vs Entities

```kotlin
// VALUE OBJECT: Immutable, identity by value
data class FlpsScore(val value: Double) {
    init { require(value in 0.0..1.0) }
}

// ENTITY: Has identity, mutable state
class Raider(
    val id: RaiderId,
    var name: String,
    var guildId: GuildId
)
```

---

## Documentation

### Quick Reference

| Path | Purpose |
|------|---------|
| `CLAUDE.md` | This file - project overview and principles |
| `PROJECT_PRIORITIES.md` | Current work priorities and status |
| `README.md` | Getting started and setup instructions |
| `API_REFERENCE.md` | REST API endpoint documentation |

### Planning Documents (in `.project/`)

| File | Purpose |
|------|---------|
| `.project/requirements.md` | Functional and non-functional requirements |
| `.project/decisions.md` | Architecture decisions and rationale |
| `.project/constraints.md` | Technical constraints and technology stack |
| `.project/glossary.md` | Domain terminology definitions |
| `.project/non_goals.md` | What this project explicitly does NOT do |
| `.project/risks.md` | Risk assessment and mitigation strategies |

### Feature Specifications (in `.kiro/specs/`)

| Spec | Status | Description |
|------|--------|-------------|
| `warcraft-logs-integration/` | Complete | Warcraft Logs API integration |
| `simulation-integration/` | Complete | Local SimulationCraft via Docker |
| `rest-api-layer/` | 40% | REST API for all entities |
| `web-dashboard/` | Planned | User-facing dashboard |
| `discord-bot/` | Planned | Notification bot |

### Domain Documentation (in `docs/`)

| File | Purpose |
|------|---------|
| `docs/score-model.md` | FLPS algorithm specification |
| `docs/system-overview.md` | Architecture overview |
| `docs/flps-walkthrough.md` | Step-by-step FLPS calculation |
| `docs/local-setup.md` | Development environment setup |

---

## Technology Stack

| Component | Technology | Rationale |
|-----------|------------|-----------|
| **Language** | Kotlin 1.9+ | Concise, null-safe, excellent Spring support |
| **Framework** | Spring Boot 3.x | Production-ready, comprehensive ecosystem |
| **Architecture** | Domain-Driven Design | Clear bounded contexts, business-focused |
| **Database** | PostgreSQL 15+ | Complex queries, reliability, JSONB |
| **Data Access** | Spring Data JDBC | Simple, type-safe, aggregate support |
| **Migrations** | Flyway | Version-controlled schema changes |
| **Testing** | JUnit 5 + MockK + Testcontainers | Modern, comprehensive testing |
| **API Docs** | OpenAPI/Swagger | Industry standard documentation |
| **Container** | Docker (JDK 21) | Consistent environments |
| **Build** | Gradle 8.10+ (Kotlin DSL) | Fast, incremental builds |

---

## Project Structure (Domain-Driven Design)

```
data-sync-service/
├── src/main/kotlin/com/edgerush/
│   ├── datasync/                    # Infrastructure & Configuration
│   │   ├── config/                  # Spring configuration
│   │   ├── scheduling/              # Scheduled tasks
│   │   └── security/                # Security configuration
│   │
│   └── lootman/                     # Business Logic (by domain)
│       ├── flps/                    # FLPS Calculation Domain
│       │   ├── api/                 # REST controllers
│       │   ├── application/         # Application services
│       │   ├── domain/              # Domain models & logic
│       │   └── infrastructure/      # External integrations
│       │
│       ├── loot/                    # Loot Management Domain
│       ├── attendance/              # Attendance Tracking Domain
│       ├── raids/                   # Raid Management Domain
│       ├── integrations/            # External API Clients
│       │   ├── wowaudit/           # WoWAudit API
│       │   └── warcraftlogs/       # Warcraft Logs API
│       │
│       └── shared/                  # Shared Components
│           ├── domain/              # Common domain models
│           └── infrastructure/      # Common infrastructure
│
├── src/main/resources/
│   ├── db/migration/               # Flyway migrations (V0001-V0019)
│   └── application.yaml            # Configuration
│
└── src/test/kotlin/                # Tests mirror src/main structure
```

---

## Bounded Contexts

| Context | Responsibility | Key Entities |
|---------|---------------|--------------|
| **FLPS** | Score calculation engine | FLPSReport, ScoreBreakdown |
| **Loot** | Loot awards and drops | LootAward, Drop, Item |
| **Attendance** | Raid attendance tracking | AttendanceStat, AttendanceRecord |
| **Raids** | Raid management | Raid, Encounter, Signup |
| **Integrations** | External API clients | WoWAuditClient, WarcraftLogsClient |
| **Shared** | Common models | Character, Guild, Configuration |

---

## Milestones

### Phase 1: Core System (Complete)

| Milestone | Status | Description |
|-----------|--------|-------------|
| FLPS Algorithm | Complete | Core calculation engine |
| WoWAudit Integration | Complete | Character, attendance, loot sync |
| Warcraft Logs Integration | Complete | Performance data, MAS scoring |
| Database Layer | Complete | 17 migrations, 45+ entities |
| Test Suite | Complete | 509 tests, 100% pass rate |

### Phase 2: Production Ready (In Progress)

| Milestone | Status | Priority | Description |
|-----------|--------|----------|-------------|
| Test Coverage 85% | In Progress | P0 | Currently 64%, need API controller tests |
| REST API Complete | 40% | P1 | CRUD for all 45+ entities |
| SimulationCraft Integration | Complete | P2 | Local Docker simulation for UV |
| GraphQL API | Planned | P3 | Flexible queries, subscriptions |

### Phase 3: User Facing (Planned)

| Milestone | Status | Priority | Description |
|-----------|--------|----------|-------------|
| Web Dashboard | Planned | P1 | FLPS transparency, admin panel |
| Discord Bot | Planned | P2 | Notifications, commands |
| RC Loot Council Addon | Planned | P3 | In-game integration |

---

## Ownership Rules

| Path | Owner | Automation |
|------|-------|------------|
| `.project/` | Human | NEVER - planning documents |
| `.kiro/specs/` | Human | NEVER - feature specifications |
| `docs/` | Human | Documentation only |
| `src/main/` | Mixed | Via approved PRs |
| `src/test/` | Mixed | Tests required for features |
| `db/migration/` | Human | Schema changes require review |

---

## Database Standards

### Spring Data JDBC Required

All database operations MUST use Spring Data repositories. Raw JDBC is PROHIBITED except for Flyway migrations.

```kotlin
// CORRECT: Spring Data repository
@Repository
interface CharacterRepository : CrudRepository<CharacterEntity, Long> {
    fun findByGuildId(guildId: String): List<CharacterEntity>
}

// INCORRECT: Raw JDBC - DO NOT USE
class JdbcCharacterRepository(private val jdbcTemplate: JdbcTemplate) {
    fun findByGuildId(guildId: String) = jdbcTemplate.query(...)  // NO!
}
```

### Migration Naming
```
V{NNNN}__{description}.sql
Example: V0018__add_warcraft_logs_config.sql
```

---

## Testing Standards

### Coverage Requirements (ACHIEVED)

- **Overall**: 98% instruction, 95% branch (target: 85%)
- **Domain Layer**: 87.7%
- **Application Layer**: 91.7%
- **API Layer**: Improving with new CRUD controllers
- **Infrastructure Layer**: 97%

### Test Organization

```
tests/
├── domain/          # Unit tests - no external deps
├── application/     # Service tests - mocked deps
├── api/             # Controller tests - MockMvc
├── integration/     # Full stack tests - Testcontainers
└── e2e/             # End-to-end workflow tests
```

### Test Naming Convention

```kotlin
// Pattern: should_ExpectedBehavior_When_StateUnderTest
@Test
fun `should return high score when raider has perfect attendance`()

@Test
fun `should throw exception when raider not found`()

@Test
fun `should create entity when request is valid`()
```

### Test Structure (AAA Pattern)

```kotlin
@Test
fun `should calculate FLPS score for raider with item`() {
    // Arrange (Given)
    val raider = createTestRaider(attendance = 0.95)
    val item = createTestItem(upgradeValue = 50.0)

    // Act (When)
    val result = calculator.calculateFLPS(raider, item)

    // Assert (Then)
    result.totalScore shouldBeGreaterThan 0.8
}
```

---

## Development Workflow

### Before Starting Work

1. Read `PROJECT_PRIORITIES.md` for current focus
2. Check `.kiro/specs/` for feature specifications
3. Review `.project/` for constraints and requirements

### When Implementing Features (TDD Required)

1. **Write failing test first** - Define expected behavior
2. **Write minimal code** - Just enough to pass the test
3. **Refactor** - Improve while keeping tests green
4. **Run full test suite**: `./gradlew test`
5. **Update documentation** if needed

### Commit Standards
```
feat(flps): add tier set completion bonus to IPI calculation
fix(wowaudit): handle rate limiting in sync service
test(api): add integration tests for FlpsController
docs(readme): update setup instructions
```

### Before Merging
1. All tests pass (509+)
2. Coverage meets target (85%)
3. No ktlint/detekt violations
4. Documentation updated

---

## Key Configuration

### Environment Variables

| Variable | Purpose | Required |
|----------|---------|----------|
| `POSTGRES_URL` | Database connection | Yes |
| `POSTGRES_USER` | Database username | Yes |
| `POSTGRES_PASSWORD` | Database password | Yes |
| `WOWAUDIT_GUILD_ID` | WoWAudit guild identifier | Yes |
| `WARCRAFTLOGS_CLIENT_ID` | WCL OAuth client ID | Yes |
| `WARCRAFTLOGS_CLIENT_SECRET` | WCL OAuth secret | Yes |
| `SIMULATION_DOCKER_IMAGE` | SimC Docker image | No (default: simulationcraftorg/simc) |
| `SIMULATION_DOCKER_TIMEOUT_MINUTES` | Simulation timeout | No (default: 30) |

### FLPS Configuration (per-guild)
```yaml
flps:
  modifiers:
    rms-weight: 0.4      # Raider Merit Score weight
    ipi-weight: 0.4      # Item Priority Index weight
    rdf-weight: 0.2      # Recency Decay Factor weight
  attendance:
    perfect-threshold: 0.95
    decay-weeks: 8
  performance:
    mas-weight: 0.3
    parse-weight: 0.7
```

---

## Current State Summary

### What's Working

- FLPS calculation engine (100%)
- WoWAudit data sync (100%)
- Warcraft Logs integration (100%)
- SimulationCraft integration (100%)
- 509 tests passing (100% pass rate)
- Test coverage: 98% instruction, 95% branch
- 17 database migrations applied
- REST API foundation with CRUD patterns

### What Needs Work

- REST API: 40% → 100% (30+ entities remaining)
- GraphQL API: not started
- Web dashboard: not started
- Discord bot: not started

### Current Priority

- **REST API Layer**: Implementing CRUD controllers for all 45+ entities using TDD

---

## Quick Commands

```bash
# Run all tests
./gradlew test

# Run with coverage
./gradlew test jacocoTestReport

# Start local environment
docker-compose up -d

# Run specific test class
./gradlew test --tests "*.FlpsCalculatorTest"

# Check code quality
./gradlew ktlintCheck detekt

# Generate API docs
./gradlew generateOpenApiDocs
```

---

## Documentation Cleanup Notice

**IMPORTANT**: This project underwent documentation cleanup. Old status files have been archived to `docs/archived/`.

**Authoritative Documents:**
- `CLAUDE.md` - Project overview (this file)
- `PROJECT_PRIORITIES.md` - Current priorities
- `.project/` - Planning documents
- `.kiro/specs/` - Feature specifications

Do NOT create new `*_COMPLETE.md` or `*_STATUS.md` files. Update `PROJECT_PRIORITIES.md` instead.
