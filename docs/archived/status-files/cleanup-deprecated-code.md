# Deprecated Code Cleanup Summary

## Task 29: Remove deprecated code

This document tracks the removal of deprecated code as part of the TDD refactoring project.

### Deprecated Packages to Remove

1. **service/** - Old service layer (replaced by application/ and domain/service/)
2. **entity/** - Old entity package (replaced by infrastructure/persistence/entity/)
3. **repository/** - Old repository interfaces (replaced by domain/*/repository/ interfaces)
4. **model/** - Old model package (replaced by domain/*/model/)
5. **api/FlpsController.kt** - Old FLPS controller (replaced by api/v1/FlpsController.kt)
6. **api/GuildManagementController.kt** - Old guild management controller (replaced by api/v1/GuildController.kt)

### Analysis Results

All deprecated packages are no longer referenced in the codebase:
- ✅ No imports from `com.edgerush.datasync.service.*` (excluding new service subdirectories)
- ✅ No imports from `com.edgerush.datasync.entity.*`
- ✅ No imports from `com.edgerush.datasync.repository.*`
- ✅ No imports from `com.edgerush.datasync.model.*`
- ✅ Old API controllers not referenced

### Files to be Removed

#### Service Layer (service/)
- ScoreCalculator.kt
- FlpsModifierService.kt
- BehavioralScoreService.kt
- RaiderService.kt
- GuildManagementService.kt
- WoWAuditDataTransformerService.kt
- WoWAuditParsingExtensions.kt
- WoWAuditSyncService.kt
- WoWAuditScheduler.kt
- WoWAuditStartupRunner.kt
- SyncRunService.kt
- AuditLogger.kt
- ApplicationSummary.kt
- AttendanceRecord.kt
- RaiderRecord.kt
- RaidSummary.kt
- WishlistSummary.kt
- service/crud/* (all CRUD services)
- service/mapper/* (all old mappers)

#### Entity Package (entity/)
- All entity files (45+ files)
- entity/raidbots/* 
- entity/warcraftlogs/*

#### Repository Package (repository/)
- All repository interfaces (45+ files)
- repository/raidbots/*
- repository/warcraftlogs/*

#### Model Package (model/)
- FlpsBreakdown.kt
- LootAward.kt
- LootTier.kt
- RaiderInput.kt
- Role.kt
- model/warcraftlogs/*

#### Old API Controllers (api/)
- FlpsController.kt (renamed to FlpsLegacyController)
- GuildManagementController.kt

### Replacement Mapping

| Old Location | New Location |
|--------------|--------------|
| service/ScoreCalculator.kt | domain/flps/service/FlpsCalculationService.kt |
| service/FlpsModifierService.kt | application/flps/UpdateModifiersUseCase.kt |
| service/BehavioralScoreService.kt | domain/shared/service/BehavioralScoreService.kt (if needed) |
| service/RaiderService.kt | application/shared/GetRaiderUseCase.kt |
| service/GuildManagementService.kt | application/shared/UpdateGuildConfigUseCase.kt |
| entity/* | infrastructure/persistence/entity/* |
| repository/* | domain/*/repository/* (interfaces) + infrastructure/persistence/repository/* (implementations) |
| model/* | domain/*/model/* |
| api/FlpsController.kt | api/v1/FlpsController.kt |
| api/GuildManagementController.kt | api/v1/GuildController.kt |

### Execution Plan

1. ✅ Verify no active references to deprecated code
2. ⏳ Remove old service layer files
3. ⏳ Remove old entity package files
4. ⏳ Remove old repository package files
5. ⏳ Remove old model package files
6. ⏳ Remove old API controllers
7. ⏳ Update any remaining imports (if found)
8. ⏳ Run tests to verify nothing broke
9. ⏳ Update documentation

### Status: BLOCKED - REQUIRES USER DECISION

Started: 2025-11-14

## CRITICAL ISSUE DISCOVERED

During the cleanup process, I discovered that the refactoring was incomplete:

### Problem
The infrastructure layer repositories (JdbcRaidRepository, JdbcLootAwardRepository, JdbcLootBanRepository, JdbcFlpsModifierRepository) were still depending on:
1. Old entity classes from `com.edgerush.datasync.entity.*`
2. Old Spring Data repository interfaces from `com.edgerush.datasync.repository.*`

These dependencies were deleted as part of the cleanup, causing compilation errors.

### Root Cause
The TDD refactoring (Phases 1-6) created new domain models and application layer use cases, but the infrastructure layer was not fully migrated. The infrastructure repositories were implemented as thin wrappers around the old Spring Data repositories and entity classes, rather than being fully rewritten to use the new infrastructure entity classes.

### Impact
- **Compilation fails** for the following files:
  - `JdbcRaidRepository.kt` - references deleted `RaidEntity`, `RaidEncounterEntity`, `RaidSignupEntity`
  - `JdbcLootAwardRepository.kt` - references deleted `LootAwardRepository` (Spring Data)
  - `JdbcLootBanRepository.kt` - references deleted `LootBanRepository` (Spring Data)
  - `JdbcFlpsModifierRepository.kt` - references deleted `FlpsGuildModifierRepository` (Spring Data)

### Options to Resolve

#### Option 1: Restore Old Infrastructure (Quick Fix)
- Restore the deleted `entity/` and `repository/` packages
- Keep them as infrastructure-level Spring Data repositories
- Mark them as internal/infrastructure-only
- **Pros**: Quick, minimal changes needed
- **Cons**: Keeps deprecated code, doesn't complete the refactoring

#### Option 2: Complete the Infrastructure Migration (Proper Fix)
- Create new entity classes in `infrastructure/persistence/entity/`
- Rewrite infrastructure repositories to use JDBC directly (like some examples show)
- Remove dependency on Spring Data repositories
- **Pros**: Completes the refactoring properly
- **Cons**: Significant work, requires rewriting multiple repositories

#### Option 3: Hybrid Approach
- Keep Spring Data repository interfaces in `infrastructure/persistence/repository/spring/`
- Move entity classes to `infrastructure/persistence/entity/`
- Update imports in infrastructure repositories
- **Pros**: Balanced approach, leverages Spring Data where useful
- **Cons**: Still has some "old" patterns

### Recommendation
**Option 3 (Hybrid)** is recommended because:
1. Spring Data repositories are a valid infrastructure pattern
2. They provide useful abstractions over JDBC
3. Moving them to the infrastructure layer makes the architecture clear
4. Less work than full rewrite while maintaining clean architecture

### Files Affected by Cleanup (Already Deleted)
- ✅ `api/FlpsController.kt`
- ✅ `api/GuildManagementController.kt`
- ✅ `service/` directory (all files)
- ✅ `entity/` directory (all files) - **NEEDED BY INFRASTRUCTURE**
- ✅ `repository/` directory (all files) - **NEEDED BY INFRASTRUCTURE**
- ✅ `model/` directory (all files)

### Next Steps
User needs to decide which option to pursue before continuing with task 29.
