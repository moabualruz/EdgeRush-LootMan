# Design Document: Spring Data Migration for JdbcRaidRepository

## Overview

This design document outlines the migration of `JdbcRaidRepository` from raw JDBC operations to Spring Data JDBC, aligning it with the existing pattern used by all other repositories in the codebase. The migration will eliminate 300+ lines of manual SQL, row mappers, and boilerplate code while maintaining all existing functionality and test coverage.

### Design Goals

1. **Consistency**: Match the adapter pattern used by all 9 existing repositories
2. **Simplicity**: Leverage Spring Data's automatic query generation
3. **Maintainability**: Reduce code complexity and manual SQL maintenance
4. **Performance**: Maintain or improve query performance
5. **Safety**: Preserve all existing functionality and test coverage

## Architecture

### Current Architecture (Raw JDBC)

```
Domain Layer
    ↓
RaidRepository (interface)
    ↓
JdbcRaidRepository (implementation)
    ├── JdbcTemplate (manual SQL)
    ├── RowMappers (manual mapping)
    └── RaidMapper (domain ↔ entity)
```

**Problems:**
- 300+ lines of manual SQL strings
- 3 custom RowMapper implementations
- Manual transaction management
- Inconsistent with other repositories
- High maintenance overhead

### Target Architecture (Spring Data)

```
Domain Layer
    ↓
RaidRepository (domain interface)
    ↓
JdbcRaidRepository (adapter)
    ├── SpringRaidRepository (Spring Data)
    ├── RaidEncounterRepository (Spring Data)
    ├── RaidSignupRepository (Spring Data)
    └── RaidMapper (domain ↔ entity)
```

**Benefits:**
- Consistent with 9 existing repositories
- Automatic query generation
- Automatic entity mapping
- Declarative transaction management
- ~70% code reduction

## Components and Interfaces

### 1. Spring Data Repository Interfaces

These already exist in `com.edgerush.datasync.repository` package:

```kotlin
// Already exists - no changes needed
@Repository
interface RaidRepository : CrudRepository<RaidEntity, Long> {
    // Add custom query methods
    fun findByDate(date: LocalDate): List<RaidEntity>
}

// Already exists - needs query methods
@Repository
interface RaidEncounterRepository : CrudRepository<RaidEncounterEntity, Long> {
    fun findByRaidId(raidId: Long): List<RaidEncounterEntity>
    fun deleteByRaidId(raidId: Long)
}

// Already exists - needs query methods
@Repository
interface RaidSignupRepository : CrudRepository<RaidSignupEntity, Long> {
    fun findByRaidId(raidId: Long): List<RaidSignupEntity>
    fun deleteByRaidId(raidId: Long)
}
```

### 2. Adapter Repository Implementation

The new `JdbcRaidRepository` will follow the established pattern:

```kotlin
@Repository
class JdbcRaidRepository(
    private val springRaidRepository: SpringRaidRepository,
    private val encounterRepository: RaidEncounterRepository,
    private val signupRepository: RaidSignupRepository,
    private val mapper: RaidMapper
) : RaidRepository {
    
    override fun findById(id: RaidId): Raid? {
        val entity = springRaidRepository.findById(id.value).orElse(null) ?: return null
        val encounters = encounterRepository.findByRaidId(id.value)
        val signups = signupRepository.findByRaidId(id.value)
        return mapper.toDomain(entity, "unknown-guild", encounters, signups)
    }
    
    override fun findByGuildId(guildId: GuildId): List<Raid> {
        // Note: Schema doesn't have guild_id column yet
        return springRaidRepository.findAll()
            .map { entity ->
                val encounters = encounterRepository.findByRaidId(entity.raidId)
                val signups = signupRepository.findByRaidId(entity.raidId)
                mapper.toDomain(entity, guildId.value, encounters, signups)
            }
    }
    
    override fun findByGuildIdAndDate(guildId: GuildId, date: LocalDate): List<Raid> {
        return springRaidRepository.findByDate(date)
            .map { entity ->
                val encounters = encounterRepository.findByRaidId(entity.raidId)
                val signups = signupRepository.findByRaidId(entity.raidId)
                mapper.toDomain(entity, guildId.value, encounters, signups)
            }
    }
    
    @Transactional
    override fun save(raid: Raid): Raid {
        val entity = mapper.toEntity(raid)
        springRaidRepository.save(entity)
        
        // Delete and re-insert child entities
        encounterRepository.deleteByRaidId(entity.raidId)
        signupRepository.deleteByRaidId(entity.raidId)
        
        val encounterEntities = mapper.encounterMapper.toEntities(raid.getEncounters(), entity.raidId)
        val signupEntities = mapper.signupMapper.toEntities(raid.getSignups(), entity.raidId)
        
        encounterRepository.saveAll(encounterEntities)
        signupRepository.saveAll(signupEntities)
        
        return raid
    }
    
    override fun delete(id: RaidId) {
        // Cascading deletes handled by database foreign keys
        springRaidRepository.deleteById(id.value)
    }
}
```

### 3. Mapper Components

The existing `RaidMapper`, `RaidEncounterMapper`, and `RaidSignupMapper` remain unchanged. They already handle domain ↔ entity conversion.

## Data Models

### Entity Relationships

```
RaidEntity (1) ──┬── (N) RaidEncounterEntity
                 └── (N) RaidSignupEntity
```

**Database Schema:**
- `raids` table: Primary aggregate root
- `raid_encounters` table: Child entities with `raid_id` foreign key
- `raid_signups` table: Child entities with `raid_id` foreign key
- Foreign keys configured with `ON DELETE CASCADE`

### Aggregate Loading Strategy

**Current Approach (Manual):**
- Load raid entity with SQL query
- Load encounters with separate SQL query
- Load signups with separate SQL query
- Manually assemble aggregate

**New Approach (Spring Data):**
- Load raid entity using `findById()`
- Load encounters using `findByRaidId()`
- Load signups using `findByRaidId()`
- Assemble aggregate in adapter

**Performance Consideration:**
- Both approaches use 3 queries (N+1 is unavoidable for this schema)
- Spring Data adds minimal overhead
- Consider adding `@Query` with JOIN FETCH if performance issues arise

## Error Handling

### Transaction Management

**Current (Manual):**
```kotlin
@Transactional
override fun save(raid: Raid): Raid {
    // Manual transaction boundary
}
```

**New (Declarative):**
```kotlin
@Transactional
override fun save(raid: Raid): Raid {
    // Spring manages transaction automatically
}
```

### Error Scenarios

| Scenario | Current Behavior | New Behavior | Notes |
|----------|------------------|--------------|-------|
| Raid not found | Returns `null` | Returns `null` | No change |
| Database connection failure | Throws `DataAccessException` | Throws `DataAccessException` | No change |
| Constraint violation | Throws `DataIntegrityViolationException` | Throws `DataIntegrityViolationException` | No change |
| Transaction rollback | Manual rollback | Automatic rollback | Improved |

## Testing Strategy

### Unit Tests

**Test File:** `JdbcRaidRepositoryTest.kt`

**Current Tests (11 tests):**
1. `findById returns raid when exists`
2. `findById returns null when not exists`
3. `findByGuildId returns all raids`
4. `findByGuildIdAndDate returns raids for specific date`
5. `save creates new raid with encounters and signups`
6. `save updates existing raid`
7. `save handles raid without encounters`
8. `save handles raid without signups`
9. `delete removes raid`
10. `delete cascades to encounters and signups`
11. `save is transactional`

**Migration Strategy:**
- Keep all 11 existing tests
- Update test setup to use Spring Data repositories
- Replace `JdbcTemplate` cleanup with repository cleanup
- Verify all assertions remain valid

### Integration Tests

**Test File:** `JdbcRaidRepositoryIntegrationTest.kt` (if exists)

**Approach:**
- Use `@DataJdbcTest` for Spring Data JDBC testing
- Use `@Transactional` for test isolation
- Verify database state after operations
- Test cascade deletes

### Test Coverage Goals

- Maintain 100% coverage on `JdbcRaidRepository`
- Maintain 100% coverage on mapper classes
- Add coverage for new Spring Data repository query methods

## Migration Plan

### Phase 1: Extend Spring Data Repositories

**Files to Modify:**
- `data-sync-service/bin/main/com/edgerush/datasync/repository/RaidRepository.kt`
- `data-sync-service/bin/main/com/edgerush/datasync/repository/RaidEncounterRepository.kt`
- `data-sync-service/bin/main/com/edgerush/datasync/repository/RaidSignupRepository.kt`

**Changes:**
```kotlin
// Add to RaidRepository
fun findByDate(date: LocalDate): List<RaidEntity>

// Add to RaidEncounterRepository
fun findByRaidId(raidId: Long): List<RaidEncounterEntity>
fun deleteByRaidId(raidId: Long)

// Add to RaidSignupRepository
fun findByRaidId(raidId: Long): List<RaidSignupEntity>
fun deleteByRaidId(raidId: Long)
```

### Phase 2: Rewrite JdbcRaidRepository

**File to Modify:**
- `data-sync-service/src/main/kotlin/com/edgerush/datasync/infrastructure/persistence/repository/JdbcRaidRepository.kt`

**Changes:**
1. Remove `JdbcTemplate` dependency
2. Add Spring Data repository dependencies
3. Remove all manual SQL strings
4. Remove all RowMapper implementations
5. Implement methods using Spring Data repositories
6. Keep `@Transactional` annotations

**Code Reduction:**
- Before: ~310 lines
- After: ~80 lines
- Reduction: ~74%

### Phase 3: Update Tests

**File to Modify:**
- `data-sync-service/src/test/kotlin/com/edgerush/datasync/infrastructure/persistence/repository/JdbcRaidRepositoryTest.kt`

**Changes:**
1. Replace `JdbcTemplate` with Spring Data repositories in test setup
2. Update cleanup methods to use `deleteAll()`
3. Update verification queries to use repository methods
4. Ensure all 11 tests still pass

### Phase 4: Verification

**Checklist:**
- [ ] All unit tests pass
- [ ] All integration tests pass
- [ ] Code coverage maintained at 100%
- [ ] No manual SQL strings remain
- [ ] No RowMapper implementations remain
- [ ] Consistent with other 9 repositories
- [ ] Performance benchmarks meet requirements

## Performance Considerations

### Query Optimization

**Current Performance:**
- `findById`: 3 queries (raid + encounters + signups)
- `findByGuildId`: N×3 queries (N raids × 3 queries each)
- `save`: 1 upsert + 2 deletes + M inserts + P inserts

**Expected Performance:**
- Same query count (no degradation)
- Slightly faster due to Spring Data optimizations
- Better connection pooling
- Better prepared statement caching

### Potential Optimizations (Future)

If performance becomes an issue, consider:

1. **Batch Loading:**
```kotlin
@Query("SELECT * FROM raid_encounters WHERE raid_id IN (:raidIds)")
fun findByRaidIdIn(raidIds: List<Long>): List<RaidEncounterEntity>
```

2. **Native Query with JOIN:**
```kotlin
@Query("""
    SELECT r.*, e.*, s.*
    FROM raids r
    LEFT JOIN raid_encounters e ON r.raid_id = e.raid_id
    LEFT JOIN raid_signups s ON r.raid_id = s.raid_id
    WHERE r.raid_id = :raidId
""", nativeQuery = true)
fun findByIdWithChildren(raidId: Long): RaidAggregateProjection
```

3. **Caching:**
```kotlin
@Cacheable("raids")
override fun findById(id: RaidId): Raid?
```

## Risks and Mitigation

### Risk 1: Behavioral Differences

**Risk:** Spring Data might handle edge cases differently than manual JDBC

**Mitigation:**
- Comprehensive test coverage
- Side-by-side comparison during development
- Gradual rollout with feature flag

### Risk 2: Performance Regression

**Risk:** Spring Data overhead might slow down operations

**Mitigation:**
- Performance benchmarks before/after
- Load testing with realistic data volumes
- Profiling to identify bottlenecks

### Risk 3: Transaction Boundary Changes

**Risk:** Declarative transactions might behave differently

**Mitigation:**
- Explicit `@Transactional` annotations
- Transaction propagation testing
- Rollback scenario testing

## Success Criteria

1. **Functionality:** All 11 existing tests pass without modification to assertions
2. **Code Quality:** Code reduced by at least 70% (from ~310 to ~80 lines)
3. **Consistency:** Implementation matches pattern of 9 existing repositories
4. **Performance:** No degradation in query execution time
5. **Maintainability:** No manual SQL strings or RowMappers remain
6. **Coverage:** Test coverage maintained at 100%

## Documentation Updates

### Code Standards

Update `AI_DEVELOPMENT_STANDARDS.md` and `docs/code-standards.md` to include:

**New Standard: Database Access Pattern**

```markdown
### Database Access

- **REQUIRED:** All database operations MUST use Spring Data repositories
- **PROHIBITED:** Raw JDBC operations (JdbcTemplate, NamedParameterJdbcTemplate) are NOT allowed
- **PROHIBITED:** Manual SQL strings and RowMapper implementations are NOT allowed
- **EXCEPTION:** Database migrations (Flyway) may use raw SQL

**Rationale:**
- Consistency across codebase
- Reduced boilerplate and maintenance overhead
- Automatic query generation and optimization
- Better transaction management
- Type-safe query methods

**Pattern to Follow:**
```kotlin
// ✅ CORRECT: Spring Data repository
@Repository
interface MyEntityRepository : CrudRepository<MyEntity, Long> {
    fun findByName(name: String): List<MyEntity>
}

// ❌ INCORRECT: Raw JDBC
@Repository
class MyRepository(private val jdbcTemplate: JdbcTemplate) {
    fun findByName(name: String): List<MyEntity> {
        return jdbcTemplate.query("SELECT * FROM my_table WHERE name = ?", ...)
    }
}
```
```

### Testing Standards

Update `docs/testing-standards.md` to include:

**Repository Testing Guidelines**

```markdown
### Repository Tests

- Use `@DataJdbcTest` for Spring Data JDBC repository tests
- Use `@Transactional` for test isolation
- Test custom query methods with real database
- Verify cascade operations and constraints
- NO manual JDBC operations in tests (except for verification queries if absolutely necessary)

**Example:**
```kotlin
@DataJdbcTest
@Transactional
class MyRepositoryTest {
    @Autowired
    private lateinit var repository: MyRepository
    
    @Test
    fun `should find entities by name`() {
        // Given
        val entity = MyEntity(name = "test")
        repository.save(entity)
        
        // When
        val results = repository.findByName("test")
        
        // Then
        assertThat(results).hasSize(1)
        assertThat(results[0].name).isEqualTo("test")
    }
}
```
```

## Future Enhancements

### Schema Improvements

Add `guild_id` column to `raids` table to enable proper guild filtering:

```sql
ALTER TABLE raids ADD COLUMN guild_id VARCHAR(255);
CREATE INDEX idx_raids_guild_id ON raids(guild_id);
```

Then update repository:
```kotlin
fun findByGuildId(guildId: String): List<RaidEntity>
fun findByGuildIdAndDate(guildId: String, date: LocalDate): List<RaidEntity>
```

### Aggregate Root Pattern

Consider using Spring Data JDBC's aggregate root pattern:

```kotlin
@Table("raids")
data class RaidEntity(
    @Id val raidId: Long,
    // ... other fields ...
    @MappedCollection(idColumn = "raid_id")
    val encounters: Set<RaidEncounterEntity> = emptySet(),
    @MappedCollection(idColumn = "raid_id")
    val signups: Set<RaidSignupEntity> = emptySet()
)
```

This would eliminate the need for separate repository calls and simplify the adapter.

## References

- [Spring Data JDBC Documentation](https://spring.io/projects/spring-data-jdbc)
- [Spring Data JDBC Reference Guide](https://docs.spring.io/spring-data/jdbc/docs/current/reference/html/)
- Existing repository implementations in `com.edgerush.datasync.infrastructure.persistence.repository`
- Project testing standards in `docs/testing-standards.md`
