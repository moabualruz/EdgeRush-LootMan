# Package Consolidation Audit

## Executive Summary

This audit identifies deprecated code in the `datasync` package that should be removed or consolidated with the new `lootman` package structure following the TDD refactoring.

## Status: ✅ READY FOR CLEANUP

Most of the refactoring has been completed successfully. The following items are safe to remove as they are no longer used.

---

## 1. Deprecated Entity Classes (SAFE TO REMOVE)

### Location: `com.edgerush.datasync.entity`

These old Spring Data entity classes are **NOT USED** anywhere in the codebase:

- ❌ `RaidEntity.kt` - Old Spring Data entity
- ❌ `RaidEncounterEntity.kt` - Old Spring Data entity  
- ❌ `RaidSignupEntity.kt` - Old Spring Data entity

**Status**: No imports found. Safe to delete.

**Replacement**: New domain models exist in:
- `com.edgerush.datasync.domain.raids.model.Raid`
- `com.edgerush.datasync.domain.raids.model.RaidEncounter`
- `com.edgerush.datasync.domain.raids.model.RaidSignup`

---

## 2. Deprecated Repository Interfaces (SAFE TO REMOVE)

### Location: `com.edgerush.datasync.repository`

These old Spring Data CrudRepository interfaces are **NOT USED**:

- ❌ `RaidRepository.kt` - Old Spring Data repository
- ❌ `RaidEncounterRepository.kt` - Old Spring Data repository
- ❌ `RaidSignupRepository.kt` - Old Spring Data repository

**Status**: No imports found. Safe to delete.

**Replacement**: New repository implementations exist in:
- `com.edgerush.datasync.infrastructure.persistence.repository.JdbcRaidRepository`
- Domain repository interfaces in `com.edgerush.datasync.domain.raids.repository`

---

## 3. Empty Service Directories (SAFE TO REMOVE)

### Location: `com.edgerush.datasync.service`

All subdirectories are **EMPTY** and can be deleted:

- ❌ `crud/` - Empty directory
- ❌ `mapper/` - Empty directory
- ❌ `raidbots/` - Empty directory
- ❌ `warcraftlogs/` - Empty directory

**Status**: Confirmed empty. Safe to delete.

**Reason**: All service logic has been migrated to:
- Application layer: `com.edgerush.datasync.application.*`
- Domain services: `com.edgerush.datasync.domain.*.service`
- Infrastructure: `com.edgerush.datasync.infrastructure.*`

---

## 4. Package Structure Analysis

### Current State

```
com.edgerush/
├── datasync/                          # Legacy package (partially migrated)
│   ├── api/                          # ✅ KEEP - REST controllers
│   ├── application/                  # ✅ KEEP - Use cases (Applications, FLPS, Integrations, Raids, Shared)
│   ├── client/                       # ✅ KEEP - External API clients (WoWAudit, WarcraftLogs, Raidbots)
│   ├── config/                       # ✅ KEEP - Configuration classes
│   ├── domain/                       # ✅ KEEP - Domain models (Applications, Attendance, FLPS, Integrations, Raids, Shared)
│   ├── entity/                       # ❌ DELETE - Old Spring Data entities (3 files)
│   ├── infrastructure/               # ✅ KEEP - Persistence implementations
│   │   ├── external/                # ✅ KEEP - External API implementations
│   │   └── persistence/             # ✅ KEEP - Repository implementations & mappers
│   ├── model/                        # ✅ KEEP - WarcraftLogs models
│   ├── repository/                   # ❌ DELETE - Old Spring Data repositories (3 files)
│   ├── security/                     # ✅ KEEP - Security configuration
│   └── service/                      # ❌ DELETE - Empty directories (4 subdirs)
│
└── lootman/                          # New DDD package structure
    ├── api/                          # ✅ KEEP - REST controllers (Attendance, FLPS, Loot)
    ├── application/                  # ✅ KEEP - Use cases (Attendance, FLPS, Loot)
    ├── config/                       # ✅ KEEP - Configuration
    ├── domain/                       # ✅ KEEP - Domain models (Attendance, FLPS, Loot, Raids, Shared)
    └── infrastructure/               # ✅ KEEP - Persistence implementations
```

### Migration Status by Bounded Context

| Bounded Context | Domain | Application | Infrastructure | API | Status |
|----------------|--------|-------------|----------------|-----|--------|
| **FLPS** | datasync | datasync | datasync | datasync | ✅ Complete |
| **Loot** | lootman | lootman | lootman | lootman | ✅ Complete |
| **Attendance** | datasync | lootman | lootman | lootman | ✅ Complete |
| **Raids** | datasync | datasync | datasync | datasync | ✅ Complete |
| **Applications** | datasync | datasync | datasync | datasync | ✅ Complete |
| **Integrations** | datasync | datasync | datasync | datasync | ✅ Complete |
| **Shared** | datasync | datasync | datasync | datasync | ✅ Complete |

---

## 5. Configuration and Client Code Analysis

### Configuration Files (KEEP ALL)

**Location**: `com.edgerush.datasync.config`

All configuration files are **ACTIVELY USED**:

- ✅ `FlpsConfigProperties.kt` - FLPS configuration
- ✅ `OpenApiConfig.kt` - API documentation
- ✅ `RateLimitConfig.kt` - Rate limiting
- ✅ `SyncProperties.kt` - Sync configuration
- ✅ `WebClientConfig.kt` - HTTP client configuration
- ✅ `WoWAuditProperties.kt` - WoWAudit API configuration
- ✅ `raidbots/` - Raidbots configuration
- ✅ `warcraftlogs/` - WarcraftLogs configuration

**Decision**: Keep in `datasync.config` - these are application-wide configurations.

### Client Code (KEEP ALL)

**Location**: `com.edgerush.datasync.client`

All client implementations are **ACTIVELY USED**:

- ✅ `WoWAuditClient.kt` - WoWAudit API client
- ✅ `WoWAuditClient*Exception.kt` - Exception classes
- ✅ `warcraftlogs/` - WarcraftLogs client
- ✅ `raidbots/` - Raidbots client

**Decision**: Keep in `datasync.client` - these are infrastructure concerns for external APIs.

### Security Code (KEEP ALL)

**Location**: `com.edgerush.datasync.security`

All security files are **ACTIVELY USED**:

- ✅ `AdminModeConfig.kt` - Admin mode configuration
- ✅ `AuthenticatedUser.kt` - User authentication
- ✅ `JwtAuthenticationFilter.kt` - JWT filter
- ✅ `JwtService.kt` - JWT service
- ✅ `SecurityConfig.kt` - Security configuration

**Decision**: Keep in `datasync.security` - application-wide security configuration.

---

## 6. Migration Plan

### Phase 1: Remove Deprecated Code (SAFE - NO DEPENDENCIES)

1. **Delete old entity classes**:
   ```
   data-sync-service/src/main/kotlin/com/edgerush/datasync/entity/RaidEntity.kt
   data-sync-service/src/main/kotlin/com/edgerush/datasync/entity/RaidEncounterEntity.kt
   data-sync-service/src/main/kotlin/com/edgerush/datasync/entity/RaidSignupEntity.kt
   ```

2. **Delete old repository interfaces**:
   ```
   data-sync-service/src/main/kotlin/com/edgerush/datasync/repository/RaidRepository.kt
   data-sync-service/src/main/kotlin/com/edgerush/datasync/repository/RaidEncounterRepository.kt
   data-sync-service/src/main/kotlin/com/edgerush/datasync/repository/RaidSignupRepository.kt
   ```

3. **Delete empty service directories**:
   ```
   data-sync-service/src/main/kotlin/com/edgerush/datasync/service/crud/
   data-sync-service/src/main/kotlin/com/edgerush/datasync/service/mapper/
   data-sync-service/src/main/kotlin/com/edgerush/datasync/service/raidbots/
   data-sync-service/src/main/kotlin/com/edgerush/datasync/service/warcraftlogs/
   ```

4. **Delete parent service directory if empty**:
   ```
   data-sync-service/src/main/kotlin/com/edgerush/datasync/service/
   ```

5. **Delete empty entity subdirectories**:
   ```
   data-sync-service/src/main/kotlin/com/edgerush/datasync/entity/raidbots/
   data-sync-service/src/main/kotlin/com/edgerush/datasync/entity/warcraftlogs/
   ```

6. **Delete parent entity directory if only subdirs remain**:
   ```
   data-sync-service/src/main/kotlin/com/edgerush/datasync/entity/
   ```

7. **Delete empty repository subdirectories**:
   ```
   data-sync-service/src/main/kotlin/com/edgerush/datasync/repository/raidbots/
   data-sync-service/src/main/kotlin/com/edgerush/datasync/repository/warcraftlogs/
   ```

8. **Delete parent repository directory if only subdirs remain**:
   ```
   data-sync-service/src/main/kotlin/com/edgerush/datasync/repository/
   ```

### Phase 2: Verify Build (NO CHANGES EXPECTED)

1. Run full build: `./gradlew clean build`
2. Run all tests: `./gradlew test`
3. Verify no compilation errors
4. Verify all tests pass

---

## 7. Risk Assessment

### Risk Level: **LOW** ✅

**Justification**:
- All files to be deleted have **zero references** in the codebase
- No imports found for any deprecated classes
- Empty directories have no impact
- All functionality has been migrated to new structure

### Rollback Plan

If issues arise:
1. Git revert the deletion commit
2. Investigate any missed dependencies
3. Update audit document with findings

---

## 8. Recommendations

### DO NOT MOVE

The following should **remain in datasync package**:

1. **Configuration** (`datasync.config`) - Application-wide settings
2. **Security** (`datasync.security`) - Application-wide security
3. **Clients** (`datasync.client`) - External API integrations
4. **API Controllers** (`datasync.api`) - REST endpoints
5. **Domain Models** (`datasync.domain`) - Core business logic
6. **Application Layer** (`datasync.application`) - Use cases
7. **Infrastructure** (`datasync.infrastructure`) - Technical implementations

### Rationale

The `datasync` package represents the **application root** and contains:
- Application-wide concerns (config, security)
- External integrations (clients)
- Domain-driven design structure (domain, application, infrastructure, api)

The `lootman` package is a **bounded context** within datasync for:
- Loot-specific functionality
- Attendance-specific functionality
- FLPS-specific functionality (partially - some in datasync)

This is a **valid DDD structure** where:
- `datasync` = Application/System
- `lootman` = Bounded Context within the system

---

## 9. Conclusion

**Status**: ✅ Ready for cleanup

**Action Items**:
1. Delete 3 old entity files
2. Delete 3 old repository files
3. Delete 4 empty service subdirectories
4. Delete empty parent directories
5. Run build verification
6. Run test suite

**Expected Impact**: None - all deleted code is unused

**Estimated Time**: 15 minutes

**Confidence Level**: Very High (100%)
