# Requirements Document

## Introduction

This specification addresses the migration of raw JDBC operations to Spring Data repositories in the EdgeRush LootMan project. Currently, **only one repository** (`JdbcRaidRepository`) uses manual `JdbcTemplate` operations with raw SQL queries and row mappers. All other repositories in the codebase already use Spring Data repositories wrapped in adapter classes. This single inconsistency creates maintenance overhead and violates the project's architectural patterns.

### Current State Analysis

**Repositories Already Using Spring Data (9 repositories):**
- `JdbcGuildRepository` - wraps `GuildConfigurationRepository`
- `JdbcRaiderRepository` - wraps `RaiderRepository`
- `JdbcFlpsModifierRepository` - wraps `FlpsGuildModifierRepository`
- `JdbcApplicationRepository` - wraps `ApplicationRepository`, `ApplicationAltRepository`, etc.
- `JdbcSyncOperationRepository` - wraps `SyncRunRepository`
- `JdbcLootBanRepository` - wraps `LootBanRepository`
- `JdbcLootAwardRepository` - wraps `LootAwardRepository`
- `JdbcAttendanceRepository` - wraps `AttendanceStatRepository`

**Repository Using Raw JDBC (1 repository):**
- `JdbcRaidRepository` - uses `JdbcTemplate` with 300+ lines of manual SQL and row mappers

The migration scope is focused and well-defined: convert one repository to match the pattern used by all others.

## Glossary

- **System**: The EdgeRush LootMan data persistence layer
- **JdbcTemplate**: Spring's low-level JDBC abstraction requiring manual SQL and row mapping
- **Spring Data JDBC**: Spring's higher-level repository abstraction with automatic query generation
- **Raid Aggregate**: A domain aggregate containing Raid, RaidEncounters, and RaidSignups
- **Repository Adapter**: Infrastructure layer class that bridges domain repositories with Spring Data repositories
- **Row Mapper**: Manual code that maps database result sets to entity objects

## Requirements

### Requirement 1: Eliminate Raw JDBC Operations

**User Story:** As a developer, I want all database operations to use Spring Data repositories, so that the codebase follows consistent patterns and reduces manual SQL maintenance.

#### Acceptance Criteria

1. WHEN the System performs raid persistence operations, THE System SHALL use Spring Data JDBC repositories instead of JdbcTemplate
2. WHEN the System queries raid data, THE System SHALL leverage Spring Data query methods instead of manual SQL strings
3. WHEN the System maps database results to entities, THE System SHALL use Spring Data's automatic mapping instead of manual RowMappers
4. THE System SHALL maintain all existing functionality during the migration
5. THE System SHALL preserve all existing test coverage after migration

### Requirement 2: Maintain Aggregate Consistency

**User Story:** As a developer, I want raid aggregates (raids with encounters and signups) to be persisted atomically, so that data integrity is maintained.

#### Acceptance Criteria

1. WHEN the System saves a Raid aggregate, THE System SHALL persist the raid entity, encounter entities, and signup entities in a single transaction
2. WHEN the System deletes a Raid, THE System SHALL cascade delete all related encounters and signups
3. WHEN the System loads a Raid, THE System SHALL eagerly load all encounters and signups as part of the aggregate
4. THE System SHALL use Spring Data's relationship mapping features to manage aggregate relationships
5. THE System SHALL maintain transactional boundaries for all aggregate operations

### Requirement 3: Preserve Domain Layer Isolation

**User Story:** As a developer, I want the domain layer to remain independent of infrastructure concerns, so that business logic is not coupled to persistence technology.

#### Acceptance Criteria

1. THE System SHALL maintain the existing domain repository interface without changes
2. THE System SHALL keep the adapter pattern where infrastructure repositories implement domain repository interfaces
3. THE System SHALL continue using mappers to convert between domain models and persistence entities
4. THE System SHALL not expose Spring Data types to the domain layer
5. THE System SHALL maintain the existing package structure and separation of concerns

### Requirement 4: Ensure Test Compatibility

**User Story:** As a developer, I want all existing tests to pass after migration, so that I can verify functionality is preserved.

#### Acceptance Criteria

1. WHEN the migration is complete, THE System SHALL pass all existing unit tests for JdbcRaidRepository
2. WHEN the migration is complete, THE System SHALL pass all existing integration tests for raid operations
3. THE System SHALL maintain test isolation using the existing test database configuration
4. THE System SHALL preserve all test assertions and verification logic
5. THE System SHALL update test mocks and stubs to reflect Spring Data repository interfaces

### Requirement 5: Support Complex Queries

**User Story:** As a developer, I want to query raids by various criteria, so that the application can retrieve data efficiently.

#### Acceptance Criteria

1. THE System SHALL support finding raids by ID using Spring Data repository methods
2. THE System SHALL support finding raids by guild ID using custom query methods
3. THE System SHALL support finding raids by guild ID and date using derived query methods
4. THE System SHALL support ordering results by date and time
5. WHERE complex queries are needed, THE System SHALL use @Query annotations with JPQL or native SQL

### Requirement 6: Maintain Performance

**User Story:** As a developer, I want database operations to perform at least as well as the current implementation, so that migration does not degrade system performance.

#### Acceptance Criteria

1. THE System SHALL load raid aggregates with encounters and signups in a single database round-trip where possible
2. THE System SHALL use batch operations for inserting multiple encounters or signups
3. THE System SHALL leverage database indexes for query optimization
4. THE System SHALL avoid N+1 query problems when loading raid collections
5. THE System SHALL maintain or improve upon current query execution times

### Requirement 7: Simplify Codebase

**User Story:** As a developer, I want to reduce boilerplate code, so that the codebase is easier to maintain and understand.

#### Acceptance Criteria

1. THE System SHALL eliminate manual SQL string definitions from repository implementations
2. THE System SHALL remove manual RowMapper implementations
3. THE System SHALL reduce the total lines of code in the raid repository by at least 30%
4. THE System SHALL use Spring Data's automatic query generation where applicable
5. THE System SHALL maintain clear and readable code despite the reduction in lines
