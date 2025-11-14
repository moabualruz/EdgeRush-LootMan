# TDD Refactoring Implementation Guide

## ✅ Confirmed Approach

**Project Status:** Not online yet - pragmatic refactoring allowed
**Strategy:** Clean migration to DDD with full test coverage
**Timeline:** Flexible - quality over speed (estimates provided for tracking)

## Confirmed Decisions

| Decision | Approach | Rationale |
|----------|----------|-----------|
| **Package Migration** | Migrate to `com.edgerush.lootman` | Clean slate, matches design spec |
| **Testing Library** | MockK for ALL tests | Kotlin-idiomatic, no mixed libraries |
| **Coverage Threshold** | Start 70%, reach 85% by Phase 3 | Gradual, achievable improvement |
| **Database Testing** | Testcontainers | Self-contained, better isolation |
| **Migration Strategy** | Parallel structure, ignore old code | Safe rollback, copy logic as needed |
| **Test Coverage** | Write tests for EVERYTHING | No exceptions, same standards throughout |
| **Documentation** | In `docs/` directory | Project-wide standards |
| **Old Tests** | Rewrite with MockK | Consistent testing approach |

## Current State Analysis

### Existing Testing Infrastructure

**Already Configured:**
- ✅ JUnit 5 (via spring-boot-starter-test)
- ✅ Mockito (will be replaced with MockK)
- ✅ Spring Boot Test support
- ✅ MockWebServer for API client testing
- ✅ Test configuration (application-test.properties)

**Needs Adding:**
- ❌ MockK (Kotlin-friendly mocking library)
- ❌ Testcontainers (for PostgreSQL integration tests)
- ❌ JaCoCo (code coverage reporting with 70% → 85% threshold)
- ❌ ktlint (Kotlin code formatting)
- ❌ detekt (static code analysis)
- ❌ Kotest (optional, for better Kotlin assertions)

### Current Package Structure

**Existing Structure:**
```
com.edgerush.datasync/
├── api/                    # Controllers and DTOs
│   ├── dto/
│   ├── exception/
│   ├── v1/                # REST API v1
│   ├── warcraftlogs/
│   └── wowaudit/
├── client/                # External API clients
├── config/                # Configuration classes
├── entity/                # Database entities (45+ entities)
├── model/                 # Domain models
├── repository/            # Spring Data repositories
├── security/              # Security configuration
└── service/               # Business logic services
    ├── crud/
    ├── mapper/
    ├── raidbots/
    └── warcraftlogs/
```

**Target DDD Structure (from design):**
```
com.edgerush.lootman/
├── api/                   # API Layer
│   ├── rest/v1/
│   ├── graphql/          # Phase 2
│   └── dto/
├── application/          # Use Cases
│   ├── flps/
│   ├── loot/
│   ├── attendance/
│   └── raids/
├── domain/               # Domain Models
│   ├── flps/
│   ├── loot/
│   ├── attendance/
│   └── raids/
├── infrastructure/       # Infrastructure
│   ├── persistence/
│   ├── external/
│   └── config/
└── shared/              # Shared utilities
```

### Current Test Organization

**Existing Tests (36 tests, all passing):**
- Unit tests: BehavioralScoreServiceTest, ScoreCalculatorTest, etc.
- Integration tests: SimpleFlpsIntegrationTest, ComprehensiveFlpsIntegrationTest
- Client tests: WoWAuditClientTest
- Security tests: JwtServiceTest, AuthenticatedUserTest

**Test Patterns Observed:**
- Using Mockito for mocking (not MockK)
- Using JUnit 5 with `@Test` annotations
- Integration tests use `@SpringBootTest` with PostgreSQL (requires Docker)
- Test naming: backtick style with descriptive names
- AAA pattern (Arrange-Act-Assert) is used

## Implementation Approach

### Package Migration Strategy

**Target:** `com.edgerush.lootman` (clean migration from `datasync`)

**Approach:**
1. Create new `lootman` package structure
2. Implement new DDD architecture in parallel
3. Copy and refactor logic from old `datasync` code as needed
4. Old `datasync` package can be ignored/deleted after migration
5. No need to maintain backward compatibility

**Benefits:**
- Clean architecture from day one
- No technical debt from old structure
- Matches design specification exactly
- Easy to understand for new developers

### Testing Strategy

**Library:** MockK for ALL tests (no Mockito)

**Approach:**
1. Add MockK to dependencies
2. Write all new tests using MockK
3. Rewrite existing 36 tests to use MockK
4. Consistent testing patterns throughout codebase

**Coverage Requirements:**
- Phase 1-2: 70% minimum coverage
- Phase 3+: 85% minimum coverage
- All new code must have tests
- No exceptions - everything gets tested

### Database Testing

**Tool:** Testcontainers for all integration tests

**Approach:**
1. Add Testcontainers dependencies
2. Configure PostgreSQL container for tests
3. Tests automatically start/stop database
4. No manual Docker Compose setup required

**Benefits:**
- Self-contained tests
- Consistent test environment
- No manual setup required
- Better CI/CD integration

## Implementation Strategy

### Phase 1: Foundation (Estimated: 3-4 days)

**Task 1: Set up testing infrastructure** (Estimated: 4-6 hours)
- Add MockK, Testcontainers, JaCoCo to build.gradle.kts
- Configure JaCoCo with 70% threshold (increase to 85% by Phase 3)
- Add ktlint and detekt plugins
- Create detekt.yml configuration
- Update test configuration for Testcontainers

**Task 2: Create base test classes** (Estimated: 3-4 hours)
- Create `UnitTest` base class using MockK
- Create `IntegrationTest` base class using Testcontainers
- Create test data builders (TestDataFactory)
- Create test utilities and helpers

**Task 3: Write testing standards documentation** (Estimated: 2-3 hours)
- Document TDD workflow (Red-Green-Refactor)
- Document test naming conventions
- Document AAA pattern (Arrange-Act-Assert)
- Provide MockK examples for common scenarios
- Document integration test patterns

**Task 4: Write code standards documentation** (Estimated: 2-3 hours)
- Document Kotlin conventions
- Document DDD patterns (Value Objects, Entities, Aggregates)
- Document package organization rules
- Document error handling patterns
- Document naming conventions

**Task 5: Create target package structure** (Estimated: 2-3 hours)
- Create new `com.edgerush.lootman` package
- Create bounded context packages: `domain/`, `application/`, `infrastructure/`
- Create sub-packages for each bounded context (flps, loot, attendance, raids)
- Add package-info.kt files with documentation
- Add README.md files for major packages

### Phase 2-7: Bounded Context Migration (Estimated per context: 1-2 weeks)

**For each bounded context:**
1. **Domain Layer** (Estimated: 2-3 days)
   - Create value objects with validation
   - Write unit tests for value objects (100% coverage)
   - Create entities and aggregates
   - Write unit tests for entities
   - Create domain services
   - Write unit tests for domain logic

2. **Application Layer** (Estimated: 2-3 days)
   - Create use case classes
   - Write unit tests for use cases
   - Implement orchestration logic
   - Test error handling and edge cases

3. **Infrastructure Layer** (Estimated: 2-3 days)
   - Create repository implementations
   - Write integration tests with Testcontainers
   - Create entity mappers
   - Test database operations

4. **API Layer** (Estimated: 1-2 days)
   - Create/update REST controllers
   - Write API integration tests
   - Update DTOs
   - Test request/response handling

5. **Verification** (Estimated: 1 day)
   - Run full test suite
   - Verify coverage meets threshold
   - Run code quality checks
   - Performance testing

### Rollback Strategy

**Parallel Structure Benefits:**
- New `lootman` package is completely separate
- Old `datasync` code remains untouched
- Can switch between implementations easily
- No risk to existing functionality

**Emergency Rollback:**
- Simply don't use new `lootman` package
- Old `datasync` code continues working
- Database migrations are additive (no rollback needed)

## Time Estimates by Phase

| Phase | Bounded Context | Estimated Time | Cumulative |
|-------|----------------|----------------|------------|
| 1 | Foundation & Docs | 3-4 days | 4 days |
| 2 | FLPS | 8-10 days | 14 days |
| 3 | Loot | 8-10 days | 24 days |
| 4 | Attendance | 6-8 days | 32 days |
| 5 | Raids | 8-10 days | 42 days |
| 6 | Applications & Integrations | 10-12 days | 54 days |
| 7 | Cleanup & Optimization | 3-5 days | 59 days |

**Total Estimated Time:** ~12 weeks (59 working days)

**Note:** Estimates assume:
- Full-time focus on refactoring
- No major blockers or dependencies
- Existing logic is well-understood
- Database schema doesn't need major changes

## Success Metrics

**Per-Phase:**
- ✅ All tests pass (100% pass rate)
- ✅ Code coverage meets threshold (70% → 85%)
- ✅ Zero ktlint violations
- ✅ Zero detekt violations
- ✅ Performance equal or better than before
- ✅ All requirements from spec met

**Overall:**
- ✅ 85%+ code coverage across all bounded contexts
- ✅ All 6 bounded contexts refactored to DDD
- ✅ Complete documentation (testing + code standards)
- ✅ Zero regression in functionality
- ✅ Clean architecture ready for GraphQL (Phase 2)

## Next Steps

1. ✅ **Review and confirm approach** - CONFIRMED
2. **Create documentation files** - testing-standards.md, code-standards.md
3. **Begin Task 1** - Set up testing infrastructure
4. **Proceed through phases** - One bounded context at a time
5. **Track progress** - Update estimates based on actual time
