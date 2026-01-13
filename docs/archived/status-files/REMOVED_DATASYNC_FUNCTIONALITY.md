# Removed DataSync Functionality - Reimplementation Required

**Date:** 2025-11-14  
**Reason for Removal:** Missing infrastructure layer (repository implementations)  
**Status:** Needs reimplementation following new DDD architecture standards

---

## Overview

During the post-refactoring cleanup (Task 5), the entire `datasync` domain layer was removed because it lacked infrastructure implementations (repositories). The domain models, use cases, controllers, and tests were all deleted to resolve Spring context loading issues.

**Key Issue:** The refactoring created domain interfaces but never implemented the infrastructure layer that would connect to the database.

---

## Removed Components

### 1. Applications Module

**Location:** `com.edgerush.datasync.domain.applications`

**Removed Files:**
- Domain Models:
  - `Application.kt` - Guild application aggregate
  - `ApplicationId.kt` - Value object
  - `ApplicationStatus.kt` - Enum (PENDING, APPROVED, REJECTED, WITHDRAWN)
  - `ApplicantInfo.kt` - Value object
  - `CharacterInfo.kt` - Value object
  - `ApplicationFile.kt` - Value object for file attachments
  
- Repository Interface:
  - `ApplicationRepository.kt` - Interface (no implementation existed)
  
- Domain Services:
  - `ApplicationReviewService.kt` - Business logic for reviewing applications
  
- Use Cases:
  - `GetApplicationsUseCase.kt` - Query applications
  - `ReviewApplicationUseCase.kt` - Approve/reject applications
  - `WithdrawApplicationUseCase.kt` - Withdraw application
  
- API Layer:
  - `ApplicationController.kt` - REST endpoints
  - `ApplicationDto.kt` - Response DTOs
  
- Tests:
  - `ApplicationControllerIntegrationTest.kt` (6 tests)
  - `ApplicationReviewServiceTest.kt`
  - `ReviewApplicationUseCaseTest.kt`

**Functionality:**
- Guild application submission and tracking
- Application review workflow (approve/reject)
- Application withdrawal
- File attachment support
- Status tracking (pending → approved/rejected/withdrawn)

**Alternative:** ❌ No alternative exists in lootman

**Reimplementation Required:** ✅ Yes - Following new DDD standards in `com.edgerush.lootman.domain.applications`

---

### 2. FLPS Module (DataSync Version)

**Location:** `com.edgerush.datasync.domain.flps`

**Removed Files:**
- Domain Models:
  - `FlpsScore.kt`
  - `RaiderMeritScore.kt`
  - `ItemPriorityIndex.kt`
  - `RecencyDecayFactor.kt`
  - `FlpsConfiguration.kt`
  - `FlpsModifier.kt`
  
- Repository Interface:
  - `FlpsModifierRepository.kt` - Interface (no implementation existed)
  
- Use Cases:
  - `CalculateFlpsScoreUseCase.kt`
  - `GetFlpsReportUseCase.kt`
  - `UpdateModifiersUseCase.kt`
  
- API Layer:
  - `FlpsController.kt` - REST endpoints
  
- Tests:
  - `FlpsControllerIntegrationTest.kt` (8 tests)

**Functionality:**
- FLPS score calculation
- Guild-specific modifier management
- FLPS report generation

**Alternative:** ✅ **Lootman has complete FLPS implementation**
- Location: `com.edgerush.lootman.domain.flps`
- Status: Fully functional with in-memory repositories
- Controllers: `com.edgerush.lootman.api.flps.FlpsController`

**Reimplementation Required:** ❌ No - Use lootman version

---

### 3. Guild Management Module

**Location:** `com.edgerush.datasync.domain.shared`

**Removed Files:**
- Domain Models:
  - `Guild.kt` - Guild aggregate
  - `GuildId.kt` - Value object
  - `BenchmarkMode.kt` - Enum
  
- Repository Interface:
  - `GuildRepository.kt` - Interface (no implementation existed)
  
- Use Cases:
  - `GetGuildConfigUseCase.kt`
  - `UpdateGuildConfigUseCase.kt`
  
- API Layer:
  - `GuildController.kt` - REST endpoints
  
- Tests:
  - `GuildControllerIntegrationTest.kt` (4 tests)

**Functionality:**
- Guild configuration management
- Benchmark mode settings
- Guild metadata

**Alternative:** ⚠️ **Partial alternative in lootman**
- Lootman has `GuildId` value object in shared domain
- No full Guild aggregate or configuration management

**Reimplementation Required:** ✅ Yes - Guild configuration management needed

---

### 4. Raider Management Module

**Location:** `com.edgerush.datasync.domain.shared`

**Removed Files:**
- Domain Models:
  - `Raider.kt` - Raider aggregate
  - `RaiderId.kt` - Value object
  - `RaiderStatus.kt` - Enum
  
- Repository Interface:
  - `RaiderRepository.kt` - Interface (no implementation existed)
  
- Use Cases:
  - `GetRaiderUseCase.kt`
  - `UpdateRaiderUseCase.kt`
  
- API Layer:
  - `RaiderController.kt` - REST endpoints
  
- Tests:
  - `RaiderControllerIntegrationTest.kt` (6 tests)

**Functionality:**
- Raider profile management
- Raider status tracking
- Character information

**Alternative:** ⚠️ **Partial alternative in lootman**
- Lootman has `RaiderId` value object in shared domain
- No full Raider aggregate or management

**Reimplementation Required:** ✅ Yes - Raider management needed

---

### 5. Integration/Sync Module

**Location:** `com.edgerush.datasync.domain.integrations`

**Removed Files:**
- Domain Models:
  - `SyncOperation.kt` - Sync operation aggregate
  - `ExternalDataSource.kt` - Enum (WOWAUDIT, WARCRAFT_LOGS)
  - `SyncStatus.kt` - Enum
  
- Repository Interface:
  - `SyncOperationRepository.kt` - Interface (no implementation existed)
  
- Use Cases:
  - `SyncWoWAuditDataUseCase.kt`
  - `SyncWarcraftLogsDataUseCase.kt`
  - `GetSyncStatusUseCase.kt`
  
- API Layer:
  - `IntegrationController.kt` - REST endpoints
  
- Tests:
  - `IntegrationControllerIntegrationTest.kt` (6 tests)

**Functionality:**
- External data source synchronization
- Sync operation tracking
- Sync status monitoring
- WoWAudit integration
- Warcraft Logs integration

**Alternative:** ⚠️ **Infrastructure exists but no domain layer**
- WoWAudit client exists: `com.edgerush.datasync.infrastructure.external.wowaudit`
- Warcraft Logs client exists: `com.edgerush.datasync.infrastructure.external.warcraftlogs`
- No domain layer to orchestrate syncs

**Reimplementation Required:** ✅ Yes - Sync orchestration and tracking needed

---

### 6. Raids Module

**Location:** `com.edgerush.datasync.domain.raids`

**Removed Files:**
- Domain Models:
  - `Raid.kt` - Raid aggregate
  - `RaidId.kt` - Value object
  - `RaidEncounter.kt` - Value object
  - `RaidSignup.kt` - Value object
  
- Repository Interface:
  - `RaidRepository.kt` - Interface (no implementation existed)
  
- Use Cases:
  - (None were created yet)
  
- API Layer:
  - (None were created yet)

**Functionality:**
- Raid scheduling
- Raid roster management
- Encounter tracking
- Signup management

**Alternative:** ❌ No alternative exists in lootman

**Reimplementation Required:** ✅ Yes - Raid management needed

---

### 7. Attendance Module (DataSync Version)

**Location:** `com.edgerush.datasync.domain.attendance`

**Removed Files:**
- Domain Models:
  - `AttendanceRecord.kt`
  - `AttendanceStats.kt`
  
- Repository Interface:
  - `AttendanceRecordRepository.kt` - Interface (no implementation existed)
  
- Use Cases:
  - (None were created yet)

**Functionality:**
- Attendance tracking
- Attendance statistics

**Alternative:** ✅ **Lootman has complete attendance implementation**
- Location: `com.edgerush.lootman.domain.attendance`
- Status: Fully functional with in-memory repositories
- Controllers: `com.edgerush.lootman.api.attendance.AttendanceController`

**Reimplementation Required:** ❌ No - Use lootman version

---

### 8. Loot Awards Module (DataSync Version)

**Location:** `com.edgerush.datasync.api.v1`

**Removed Files:**
- API Layer:
  - `LootAwardController.kt` - REST endpoints (legacy CRUD)
  
- Tests:
  - `LootAwardControllerIntegrationTest.kt` (3 tests)

**Functionality:**
- Legacy CRUD endpoints for loot awards
- Backward compatibility layer

**Alternative:** ✅ **Lootman has complete loot implementation**
- Location: `com.edgerush.lootman.domain.loot`
- Status: Fully functional with in-memory repositories
- Controllers: `com.edgerush.lootman.api.loot.LootController`

**Reimplementation Required:** ❌ No - Use lootman version

---

## Infrastructure That Was Removed

### DTOs and API Layer
- `com.edgerush.datasync.api.dto` - All DTOs deleted
- `com.edgerush.datasync.api.v1` - All controllers deleted
- `com.edgerush.datasync.api.exception.GlobalExceptionHandler` - Deleted (duplicate existed)

### External API Clients
- `com.edgerush.datasync.infrastructure.external` - Deleted
  - WoWAudit client
  - Warcraft Logs client
  - **Note:** These need to be preserved/restored as they contain working API integrations

---

## Reimplementation Plan

### Phase 1: Restore Critical Infrastructure (IMMEDIATE)
**Priority:** P0 - Required for basic functionality

1. **Restore External API Clients**
   - Location: `com.edgerush.datasync.infrastructure.external`
   - Files needed:
     - `wowaudit/WoWAuditDataClient.kt`
     - `warcraftlogs/WarcraftLogsClient.kt`
   - These contain working OAuth2 and API integration code
   - **Action:** Restore from git history

2. **Create Sync Orchestration Domain**
   - Location: `com.edgerush.lootman.domain.integrations`
   - Components:
     - `SyncOperation` aggregate
     - `SyncOperationRepository` interface
     - `InMemorySyncOperationRepository` implementation
     - `SyncOrchestrationService` domain service
   - Use cases:
     - `SyncWoWAuditDataUseCase`
     - `SyncWarcraftLogsDataUseCase`
     - `GetSyncStatusUseCase`
   - API:
     - `IntegrationController` in `com.edgerush.lootman.api.integrations`

### Phase 2: Guild & Raider Management (HIGH PRIORITY)
**Priority:** P1 - Required for multi-guild support

1. **Guild Management**
   - Location: `com.edgerush.lootman.domain.guild`
   - Components:
     - `Guild` aggregate
     - `GuildRepository` interface
     - `InMemoryGuildRepository` implementation
   - Use cases:
     - `GetGuildConfigUseCase`
     - `UpdateGuildConfigUseCase`
   - API:
     - `GuildController` in `com.edgerush.lootman.api.guild`

2. **Raider Management**
   - Location: `com.edgerush.lootman.domain.raider`
   - Components:
     - `Raider` aggregate
     - `RaiderRepository` interface
     - `InMemoryRaiderRepository` implementation
   - Use cases:
     - `GetRaiderUseCase`
     - `UpdateRaiderUseCase`
     - `GetRaiderStatsUseCase`
   - API:
     - `RaiderController` in `com.edgerush.lootman.api.raider`

### Phase 3: Application System (MEDIUM PRIORITY)
**Priority:** P2 - Nice to have for guild recruitment

1. **Application Domain**
   - Location: `com.edgerush.lootman.domain.applications`
   - Components:
     - `Application` aggregate
     - `ApplicationRepository` interface
     - `InMemoryApplicationRepository` implementation
     - `ApplicationReviewService` domain service
   - Use cases:
     - `SubmitApplicationUseCase`
     - `GetApplicationsUseCase`
     - `ReviewApplicationUseCase`
     - `WithdrawApplicationUseCase`
   - API:
     - `ApplicationController` in `com.edgerush.lootman.api.applications`

### Phase 4: Raid Management (MEDIUM PRIORITY)
**Priority:** P2 - Useful for raid scheduling

1. **Raid Domain**
   - Location: `com.edgerush.lootman.domain.raids`
   - Components:
     - `Raid` aggregate
     - `RaidRepository` interface
     - `InMemoryRaidRepository` implementation
   - Use cases:
     - `CreateRaidUseCase`
     - `GetRaidScheduleUseCase`
     - `ManageRaidRosterUseCase`
   - API:
     - `RaidController` in `com.edgerush.lootman.api.raids`

---

## Architecture Standards for Reimplementation

All reimplemented functionality MUST follow the new DDD architecture:

### Package Structure
```
com.edgerush.lootman/
├── api/                          # REST Controllers
│   ├── [domain]/
│   │   ├── [Domain]Controller.kt
│   │   ├── [Domain]Request.kt
│   │   └── [Domain]Response.kt
├── application/                  # Use Cases (Application Services)
│   └── [domain]/
│       ├── [Action]UseCase.kt
│       ├── [Action]Command.kt
│       └── [Action]Query.kt
├── domain/                       # Domain Layer
│   ├── [domain]/
│   │   ├── model/               # Aggregates, Entities, Value Objects
│   │   ├── repository/          # Repository Interfaces
│   │   └── service/             # Domain Services
└── infrastructure/               # Infrastructure Layer
    └── [domain]/
        └── InMemory[Domain]Repository.kt
```

### Testing Requirements
- **Unit Tests:** All domain logic (models, services)
- **Integration Tests:** All use cases and controllers
- **Coverage Target:** 85% minimum
- **Test Location:** Mirror source structure in `src/test/kotlin`

### Naming Conventions
- **Aggregates:** Singular noun (e.g., `Application`, `Guild`)
- **Value Objects:** Descriptive noun (e.g., `ApplicationId`, `GuildId`)
- **Repositories:** `[Aggregate]Repository` interface, `InMemory[Aggregate]Repository` implementation
- **Use Cases:** `[Verb][Aggregate]UseCase` (e.g., `GetApplicationsUseCase`)
- **Controllers:** `[Aggregate]Controller` (e.g., `ApplicationController`)
- **DTOs:** `[Aggregate]Request`, `[Aggregate]Response`

### Required Annotations
- **Domain Services:** `@Service`
- **Use Cases:** `@Service`
- **Repositories:** `@Repository`
- **Controllers:** `@RestController`
- **Request Mapping:** `@RequestMapping("/api/v1/[domain]")`

---

## Migration Notes

### Database Migrations
The database schema for removed functionality still exists:
- Tables: `applications`, `guilds`, `raiders`, `sync_operations`, `raids`
- Migrations: Located in `data-sync-service/src/main/resources/db/migration/postgres`
- **Action:** Keep migrations, they're still valid for future reimplementation

### Configuration
Some configuration was removed:
- `SyncProperties.kt` - Still exists in `com.edgerush.datasync.config`
- Application properties for sync scheduling - Still in `application.yaml`
- **Action:** No changes needed

### External Dependencies
All external API integrations were removed:
- WoWAudit client code
- Warcraft Logs client code
- **Action:** Restore from git history before reimplementing domain layer

---

## Success Criteria for Reimplementation

Each reimplemented module must meet:

1. ✅ **Architecture Compliance**
   - Follows DDD package structure
   - Clear separation of concerns (API → Application → Domain → Infrastructure)
   - No circular dependencies

2. ✅ **Testing Standards**
   - 85% code coverage minimum
   - Unit tests for all domain logic
   - Integration tests for all use cases
   - Integration tests for all API endpoints

3. ✅ **Code Quality**
   - Zero ktlint violations
   - Zero detekt violations
   - All classes have KDoc comments
   - All public methods documented

4. ✅ **Functionality**
   - All original features restored
   - API endpoints match original contracts
   - Backward compatibility maintained

5. ✅ **Performance**
   - Response times ≤ original implementation
   - No N+1 query problems
   - Efficient data access patterns

---

## References

- **Original Code:** Check git history for deleted files
- **Architecture Guide:** `CODE_ARCHITECTURE.md`
- **Testing Standards:** `.kiro/specs/graphql-tdd-refactor/design.md`
- **API Standards:** `API_REFERENCE.md`

---

## Questions for Future Implementation

1. **Should we restore external API clients first or reimplement domain layer first?**
   - Recommendation: Restore clients first (they're working code)

2. **Should we use in-memory repositories or implement database repositories?**
   - Recommendation: Start with in-memory (like lootman), migrate to database later

3. **Should we maintain separate datasync and lootman packages or merge?**
   - Recommendation: Merge into lootman following DDD bounded contexts

4. **What's the priority order for reimplementation?**
   - Recommendation: Follow Phase 1-4 order above (Sync → Guild/Raider → Applications → Raids)

---

**Last Updated:** 2025-11-14  
**Document Owner:** Development Team  
**Status:** Living Document - Update as reimplementation progresses
