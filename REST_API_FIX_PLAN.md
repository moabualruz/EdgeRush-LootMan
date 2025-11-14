# REST API Fix Plan

## Current Situation

- 44/45 entities have API files created
- ~500 compilation errors in generated code
- Build fails

## Root Causes

1. **Missing imports** - java.time.Instant, LocalTime, LocalDate, OffsetDateTime
2. **Wrong AuditLogger API** - Using old signature with `details` parameter
3. **Missing constructor parameters** - Mappers missing required entity fields
4. **Empty data classes** - Some request DTOs have no fields
5. **Repository issues** - Not extending PagingAndSortingRepository

## Fix Strategy

### Option A: Fix Generator + Regenerate (Recommended)
**Time**: 2-3 hours  
**Risk**: Low  
**Benefit**: Clean, consistent code

**Steps**:
1. Fix DtoGenerator - Add missing imports
2. Fix ServiceGenerator - Update AuditLogger calls
3. Fix MapperGenerator - Include all entity fields
4. Delete all generated files
5. Regenerate all 42 entities
6. Verify build succeeds

### Option B: Manual Fixes
**Time**: 8-10 hours  
**Risk**: High (error-prone)  
**Benefit**: None

**Not recommended** - Too time consuming and error-prone

## Recommended Approach

**Fix the generator, then regenerate everything.**

This ensures:
- Consistent code quality
- All entities work the same way
- Future entities can be generated correctly
- Saves time in the long run

## Implementation Steps

### Step 1: Backup Current Work
```bash
git add .
git commit -m "WIP: Generated REST APIs with errors"
```

### Step 2: Fix Generator Issues

**File: DtoGenerator.kt**
- Add import detection for java.time classes
- Add import for Instant, LocalTime, LocalDate when needed

**File: ServiceGenerator.kt**
- Update AuditLogger calls to use correct signature:
  ```kotlin
  auditLogger.logCreate("EntityType", id.toString(), user)
  // NOT: auditLogger.logCreate(..., details = "...")
  ```

**File: MapperGenerator.kt**
- Parse ALL entity fields, not just some
- Include all fields in toEntity() methods

**File: ControllerGenerator.kt**
- Ensure correct type parameters for BaseCrudController

### Step 3: Clean Generated Files
```bash
# Delete all generated CRUD files
rm -rf data-sync-service/src/main/kotlin/com/edgerush/datasync/api/dto/request/*Request.kt
rm -rf data-sync-service/src/main/kotlin/com/edgerush/datasync/api/dto/response/*Response.kt
rm -rf data-sync-service/src/main/kotlin/com/edgerush/datasync/service/mapper/*Mapper.kt
rm -rf data-sync-service/src/main/kotlin/com/edgerush/datasync/service/crud/*CrudService.kt
rm -rf data-sync-service/src/main/kotlin/com/edgerush/datasync/api/v1/*Controller.kt
```

### Step 4: Regenerate All Entities
```bash
cd crud-generator
./gradlew run
```

### Step 5: Verify Build
```bash
cd ..
./gradlew :data-sync-service:build -x test
```

### Step 6: Test in Swagger
```bash
./gradlew :data-sync-service:bootRun
# Visit: http://localhost:8080/swagger-ui.html
```

## Timeline

- Fix generator: 1-2 hours
- Regenerate + verify: 30 minutes
- Test: 30 minutes
- **Total**: 2-3 hours

## Alternative: Minimal Fix

If full regeneration is too risky, we can:

1. Keep the 2 manually created entities (Application, Guest)
2. Delete all 42 generated entities
3. Manually create APIs for only the 10 most critical entities
4. Skip the rest for now

**Critical entities**:
1. Raider
2. Raid  
3. LootAward
4. AttendanceStat
5. BehavioralAction
6. FlpsGuildModifier
7. FlpsDefaultModifier
8. GuildConfiguration
9. LootBan
10. RaidEncounter

This would take 3-4 hours but give us working APIs for core functionality.

## Decision Required

Which approach do you prefer?

**A)** Fix generator + regenerate all (2-3 hours, clean solution)  
**B)** Manually create 10 critical entities only (3-4 hours, partial solution)  
**C)** Something else?

