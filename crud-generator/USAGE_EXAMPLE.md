# CRUD Generator Usage Example

## For EdgeRush LootMan Project

### Step 1: Build the Generator

```bash
cd crud-generator
./gradlew build
```

### Step 2: Generate APIs for All Remaining Entities

```bash
# Generate for all entities in the entity directory
./gradlew run --args="generate-batch \
  --entities-dir=../data-sync-service/src/main/kotlin/com/edgerush/datasync/entity \
  --output=../data-sync-service/src/main/kotlin \
  --package=com.edgerush.datasync"
```

### Step 3: Generate for a Single Entity

```bash
# Generate for LootBan entity
./gradlew run --args="generate \
  --entity=../data-sync-service/src/main/kotlin/com/edgerush/datasync/entity/LootBanEntity.kt \
  --output=../data-sync-service/src/main/kotlin \
  --package=com.edgerush.datasync"
```

### Step 4: Review Generated Files

The generator will create:
```
data-sync-service/src/main/kotlin/com/edgerush/datasync/
├── api/
│   ├── dto/
│   │   ├── request/
│   │   │   └── LootBanRequest.kt
│   │   └── response/
│   │       └── LootBanResponse.kt
│   └── v1/
│       └── LootBanController.kt
└── service/
    ├── crud/
    │   └── LootBanCrudService.kt
    └── mapper/
        └── LootBanMapper.kt
```

### Step 5: Update Repositories Manually

The generator outputs repository code, but you need to manually update existing repositories:

```kotlin
// Before
interface LootBanRepository : CrudRepository<LootBanEntity, Long>

// After (add PagingAndSortingRepository and custom methods)
interface LootBanRepository : 
    CrudRepository<LootBanEntity, Long>, 
    PagingAndSortingRepository<LootBanEntity, Long> {
    fun findByGuildId(guildId: String, pageable: Pageable): Page<LootBanEntity>
    fun findByGuildIdAndIsActive(guildId: String, isActive: Boolean, pageable: Pageable): Page<LootBanEntity>
}
```

### Step 6: Build and Test

```bash
cd ../data-sync-service
./gradlew build
./gradlew bootRun
```

Visit: http://localhost:8080/swagger-ui.html

## Batch Generation for All 38 Entities

```bash
# This will generate APIs for all entities at once
cd crud-generator
./gradlew run --args="generate-batch \
  --entities-dir=../data-sync-service/src/main/kotlin/com/edgerush/datasync/entity \
  --output=../data-sync-service/src/main/kotlin \
  --package=com.edgerush.datasync"
```

Expected output:
```
Found 38 entity files
Processing: LootBanEntity.kt
  ✓ Generated successfully
Processing: FlpsGuildModifierEntity.kt
  ✓ Generated successfully
...
==================================================
Generation complete!
  Success: 38
  Failed: 0
  Total: 38
```

## Time Savings

- **Manual**: 38 entities × 28 minutes = 17.7 hours
- **With Generator**: 38 entities × 2 minutes (review) = 1.3 hours
- **Savings**: 16.4 hours (92% faster)

## Customization

### Add Custom Methods

Edit the generated service to add custom query methods:

```kotlin
// In LootBanCrudService.kt
fun findActiveByGuildId(guildId: String, pageable: Pageable): Page<LootBanResponse> {
    return repository.findByGuildIdAndIsActive(guildId, true, pageable)
        .map(mapper::toResponse)
}
```

Then add the endpoint in the controller:

```kotlin
// In LootBanController.kt
@GetMapping("/guild/{guildId}/active")
fun getActiveByGuildId(
    @PathVariable guildId: String,
    pageable: Pageable
): Page<LootBanResponse> {
    return service.findActiveByGuildId(guildId, pageable)
}
```

## Verification

After generation, verify:

1. **Build succeeds**: `./gradlew build`
2. **No compilation errors**
3. **Swagger UI shows new endpoints**: http://localhost:8080/swagger-ui.html
4. **Test basic CRUD operations**

## Next Steps

1. Generate all 38 entities
2. Review generated code
3. Add custom methods as needed
4. Update repositories with custom queries
5. Run tests
6. Update OpenAPI documentation
7. Deploy

## Tips

- Review generated validation annotations and adjust as needed
- Add custom business logic to services
- Customize OpenAPI descriptions
- Add integration tests for new endpoints
