# REST API Implementation Guide

## Phase 1: ✅ COMPLETE

**Foundation implemented and tested:**
- Security (JWT + Admin Mode)
- Base CRUD patterns
- OpenAPI documentation
- Rate limiting
- Audit logging
- Example: Raiders API (fully functional)

**Files**: 25 new files created  
**Build**: ✅ Successful  
**Tests**: ✅ Passing  

---

## Phase 2: Core Entities (In Progress)

### Pattern to Follow

For each entity, create these 5 files following the Raider example:

#### 1. Request DTOs
**File**: `api/dto/request/{Entity}Request.kt`

```kotlin
data class Create{Entity}Request(
    @field:NotBlank(message = "Field is required")
    val field: String,
    // ... other fields with validation
)

data class Update{Entity}Request(
    val field: String? = null,  // All optional
    // ... other fields
)
```

#### 2. Response DTO
**File**: `api/dto/response/{Entity}Response.kt`

```kotlin
data class {Entity}Response(
    val id: Long,
    val field: String,
    // ... all entity fields
)
```

#### 3. Mapper
**File**: `service/mapper/{Entity}Mapper.kt`

```kotlin
@Component
class {Entity}Mapper {
    fun toEntity(request: Create{Entity}Request): {Entity}Entity {
        return {Entity}Entity(
            id = null,
            field = request.field,
            // ... map all fields
        )
    }
    
    fun updateEntity(entity: {Entity}Entity, request: Update{Entity}Request): {Entity}Entity {
        return entity.copy(
            field = request.field ?: entity.field,
            // ... update fields
        )
    }
    
    fun toResponse(entity: {Entity}Entity): {Entity}Response {
        return {Entity}Response(
            id = entity.id!!,
            field = entity.field,
            // ... map all fields
        )
    }
}
```

#### 4. CRUD Service
**File**: `service/crud/{Entity}CrudService.kt`

```kotlin
@Service
class {Entity}CrudService(
    private val repository: {Entity}Repository,
    private val mapper: {Entity}Mapper,
    private val auditLogger: AuditLogger
) : CrudService<{Entity}Entity, Long, Create{Entity}Request, Update{Entity}Request, {Entity}Response> {
    
    private val logger = LoggerFactory.getLogger(javaClass)
    
    override fun findAll(pageable: Pageable): Page<{Entity}Response> {
        return repository.findAll(pageable).map { mapper.toResponse(it) }
    }
    
    override fun findById(id: Long): {Entity}Response {
        val entity = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("{Entity} not found with id: $id") }
        return mapper.toResponse(entity)
    }
    
    override fun create(request: Create{Entity}Request, user: AuthenticatedUser): {Entity}Response {
        logger.info("Creating {entity} by user: ${user.username}")
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)
        auditLogger.logCreate("{Entity}", saved.id!!, user)
        return mapper.toResponse(saved)
    }
    
    override fun update(id: Long, request: Update{Entity}Request, user: AuthenticatedUser): {Entity}Response {
        val existing = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("{Entity} not found with id: $id") }
        validateAccess(existing, user)
        
        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)
        auditLogger.logUpdate("{Entity}", id, user)
        return mapper.toResponse(saved)
    }
    
    override fun delete(id: Long, user: AuthenticatedUser) {
        val entity = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("{Entity} not found with id: $id") }
        validateAccess(entity, user)
        
        repository.delete(entity)
        auditLogger.logDelete("{Entity}", id, user)
    }
    
    override fun validateAccess(entity: {Entity}Entity, user: AuthenticatedUser) {
        if (!user.isGuildAdmin()) {
            throw AccessDeniedException("Insufficient permissions")
        }
    }
}
```

#### 5. Controller
**File**: `api/v1/{Entity}Controller.kt`

```kotlin
@RestController
@RequestMapping("/api/v1/{entities}")
@Tag(name = "{Entities}", description = "Manage {entity} data")
class {Entity}Controller(
    {entity}Service: {Entity}CrudService
) : BaseCrudController<{Entity}Entity, Long, Create{Entity}Request, Update{Entity}Request, {Entity}Response>({entity}Service)
```

#### 6. Update Repository
**File**: `repository/{Entity}Repository.kt`

```kotlin
@Repository
interface {Entity}Repository : 
    CrudRepository<{Entity}Entity, Long>, 
    PagingAndSortingRepository<{Entity}Entity, Long> {
    // Custom query methods
}
```

---

## Priority Entities to Implement

### High Priority (Core FLPS functionality)

1. **LootAwards** ✅ Started
   - Critical for loot history
   - Used in RDF calculations
   - Endpoints: `/api/v1/loot-awards`

2. **Raids**
   - Raid scheduling and tracking
   - Endpoints: `/api/v1/raids`

3. **AttendanceStats**
   - Attendance tracking for ACS
   - Endpoints: `/api/v1/attendance`

4. **BehavioralActions**
   - Behavioral score adjustments
   - Endpoints: `/api/v1/behavioral-actions`

5. **LootBans**
   - Loot restriction management
   - Endpoints: `/api/v1/loot-bans`

### Medium Priority (Guild Management)

6. **FlpsGuildModifiers**
7. **FlpsDefaultModifiers**
8. **GuildConfigurations**
9. **Applications**
10. **Wishlists**

### Lower Priority (Character Data)

11. **RaiderGearItems**
12. **RaiderVaultSlots**
13. **RaiderCrestCounts**
14. **RaiderRaidProgress**
15. **RaiderStatistics**

---

## Quick Implementation Steps

### For Each Entity:

1. **Check the entity file** in `entity/{Entity}Entity.kt`
2. **Copy the Raider pattern** from these files:
   - `api/dto/request/RaiderRequest.kt`
   - `api/dto/response/RaiderResponse.kt`
   - `service/mapper/RaiderMapper.kt`
   - `service/crud/RaiderCrudService.kt`
   - `api/v1/RaiderController.kt`
3. **Replace "Raider" with your entity name**
4. **Update fields** to match the entity
5. **Update repository** to extend `PagingAndSortingRepository`
6. **Build and test**: `cmd /c "gradlew.bat build"`

---

## Automated Approach (Recommended)

Since all entities follow the same pattern, you can:

1. **Use an IDE refactoring tool** to duplicate and rename
2. **Use find/replace** across files
3. **Create a code generation script** (template provided in `generate-crud-api.ps1`)

---

## Testing Each Entity

After implementing an entity:

1. **Build**: `cmd /c "gradlew.bat build"`
2. **Start server**: `cmd /c "gradlew.bat bootRun"`
3. **Open Swagger**: `http://localhost:8080/swagger-ui.html`
4. **Test endpoints** in Swagger UI
5. **Check audit logs** in database

---

## Estimated Time

- **Per entity** (following pattern): 10-15 minutes
- **5 high-priority entities**: 1-1.5 hours
- **All 45+ entities**: 8-10 hours

---

## Current Status

✅ **Phase 1 Complete**: Foundation + Raiders example  
🔄 **Phase 2 In Progress**: Core entities  
⏳ **Phase 3 Pending**: Remaining entities  
⏳ **Phase 4 Pending**: Comprehensive testing  

---

## Next Immediate Steps

1. Complete LootAwards (started)
2. Implement Raids
3. Implement AttendanceStats
4. Implement BehavioralActions
5. Implement LootBans

Then test all 5 together before proceeding to remaining entities.

---

## Support Files Created

- `REST_API_PHASE1_COMPLETE.md` - Phase 1 documentation
- `generate-crud-api.ps1` - Code generation template
- `test-rest-api.ps1` - API testing script
- This file - Implementation guide

---

## Questions?

The pattern is established and working. Follow the Raider example exactly, and you'll have a consistent, well-documented API for all entities.

**Key principle**: Copy, rename, update fields. Don't reinvent the pattern.
