# REST API Layer Implementation Plan

**Created:** 2026-01-14
**Status:** Ready for Implementation
**Priority:** P1 (Current Focus)

## Executive Summary

This plan outlines the systematic implementation of REST API endpoints for all 45+ entities in the EdgeRush LootMan project. The foundation is already in place (8 controllers exist), and this plan focuses on completing the remaining work.

---

## Current State Analysis

### Existing Controllers (8 total)

| Controller | Path | Status |
|-----------|------|--------|
| RaiderController | `/api/raiders` | Implemented |
| LootController | `/api/loot` | Implemented |
| FlpsController | `/api/flps` | Implemented |
| AttendanceController | `/api/attendance` | Implemented |
| GuildController | `/api/guilds` | Implemented |
| GearController | `/api/gear` | Implemented |
| WishlistController | `/api/wishlists` | Implemented |
| SimulationController | `/api/simulations` | Implemented |

### Existing Infrastructure

- GlobalExceptionHandler
- PagedResponse, PageRequest
- PaginationProperties
- DTOs for major domains (FlpsDto, LootDto, RaiderDto, GuildDto, AttendanceDto, GearDto, WishlistDto, SimulationDtos)

---

## Implementation Phases

### Phase 1: Foundation Enhancement (Tasks 1.1-1.5)

**Goal:** Establish reusable patterns for rapid controller implementation

#### 1.1 Create BaseCrudController Pattern

```kotlin
abstract class BaseCrudController<T, ID, CreateReq, UpdateReq, Resp>(
    protected val service: CrudService<T, ID, CreateReq, UpdateReq, Resp>
) {
    @GetMapping
    fun findAll(pageable: Pageable): PagedResponse<Resp>

    @GetMapping("/{id}")
    fun findById(@PathVariable id: ID): Resp

    @PostMapping
    fun create(@Valid @RequestBody request: CreateReq): ResponseEntity<Resp>

    @PutMapping("/{id}")
    fun update(@PathVariable id: ID, @Valid @RequestBody request: UpdateReq): Resp

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: ID): ResponseEntity<Unit>
}
```

**Estimated Effort:** 2-3 hours

#### 1.2 JWT Authentication Setup

- Create JwtAuthenticationFilter
- Configure AdminModeConfig with env variable
- Add startup warnings for admin mode
- Create AuthenticatedUser model

**Estimated Effort:** 3-4 hours

#### 1.3 Security Configuration

- Role-based access: PUBLIC_USER, GUILD_ADMIN, SYSTEM_ADMIN
- CORS configuration
- Security filter chain

**Estimated Effort:** 2-3 hours

#### 1.4 Rate Limiting

- RateLimitFilter with configurable limits
- Rate limit headers in responses
- 429 Too Many Requests handling

**Estimated Effort:** 2-3 hours

---

### Phase 2: Core Entity APIs (Tasks 3-4)

**Goal:** Implement CRUD for the most-used entities

#### Priority Entities (need full CRUD controllers)

| Entity | Existing Controller | Needs Work |
|--------|--------------------|----|
| Raider | Yes | Add CRUD methods |
| LootAward | Yes (LootController) | Add CRUD methods |
| AttendanceStat | Yes | Add CRUD methods |
| Raid | No | New controller |
| RaidEncounter | No | New controller |
| RaidSignup | No | New controller |

#### Implementation for Each Entity

1. Create/Update Request DTOs with validation
2. Response DTO
3. Mapper (Entity <-> DTO)
4. CrudService implementation
5. Controller extending BaseCrudController
6. Integration tests

**Estimated Effort per Entity:** 2-4 hours

---

### Phase 3: FLPS & Guild Management (Task 5)

| Entity | Controller Status |
|--------|------------------|
| FlpsDefaultModifier | New controller needed |
| FlpsGuildModifier | New controller needed |
| BehavioralAction | New controller needed |
| LootBan | New controller needed |
| GuildConfiguration | New controller needed |

**Estimated Effort:** 8-12 hours total

---

### Phase 4: Character Data APIs (Task 6)

| Entity | Controller Status |
|--------|------------------|
| RaiderGearItem | GearController exists - enhance |
| RaiderVaultSlot | New controller needed |
| RaiderCrestCount | New controller needed |
| RaiderRaidProgress | New controller needed |
| RaiderStatistics | New controller needed |
| RaiderWarcraftLog | New controller needed |
| RaiderTrackItem | New controller needed |
| RaiderPvpBracket | New controller needed |
| RaiderRenown | New controller needed |

**Estimated Effort:** 12-18 hours total

---

### Phase 5: Application & Wishlist APIs (Task 7)

| Entity | Controller Status |
|--------|------------------|
| Application | New controller needed |
| ApplicationAlt | New controller needed |
| ApplicationQuestion | New controller needed |
| ApplicationQuestionFile | New controller needed |
| WishlistSnapshot | WishlistController exists - enhance |
| WishlistItem | New controller needed |

**Estimated Effort:** 10-14 hours total

---

### Phase 6: Integration APIs (Task 8)

| Entity | Controller Status |
|--------|------------------|
| WarcraftLogsConfig | New controller needed |
| WarcraftLogsReport | New controller needed |
| WarcraftLogsFight | New controller needed |
| WarcraftLogsPerformance | New controller needed |

**Estimated Effort:** 8-12 hours total

---

### Phase 7: System & Metadata APIs (Task 9)

| Entity | Controller Status |
|--------|------------------|
| SyncRun | New controller needed |
| PeriodSnapshot | New controller needed |
| WoWAuditSnapshot | New controller needed |
| TeamMetadata | New controller needed |
| TeamRaidDay | New controller needed |
| CharacterHistory | New controller needed |
| AuditLog | New controller needed |

**Estimated Effort:** 10-14 hours total

---

### Phase 8: Remaining Entities (Task 10)

| Entity | Controller Status |
|--------|------------------|
| Guest | New controller needed |
| HistoricalActivity | New controller needed |
| LootAwardBonusId | Part of LootAward - nested |
| LootAwardOldItem | Part of LootAward - nested |
| LootAwardWishData | Part of LootAward - nested |

**Estimated Effort:** 6-8 hours total

---

## Implementation Order (Recommended)

### Sprint 1: Foundation (1-2 days)

1. [ ] BaseCrudController abstract class
2. [ ] CrudService interface
3. [ ] EntityMapper interface
4. [ ] Enhanced GlobalExceptionHandler
5. [ ] Unit tests for foundation

### Sprint 2: Core CRUD (2-3 days)

1. [ ] RaiderController enhancements (full CRUD)
2. [ ] RaidController (new)
3. [ ] RaidEncounterController (new)
4. [ ] RaidSignupController (new)
5. [ ] Integration tests

### Sprint 3: FLPS & Behavioral (1-2 days)

1. [ ] FlpsModifierController (combines default + guild)
2. [ ] BehavioralActionController (new)
3. [ ] LootBanController (new)
4. [ ] GuildConfigurationController (new)
5. [ ] Integration tests

### Sprint 4: Character Data (2-3 days)

1. [ ] RaiderVaultSlotController
2. [ ] RaiderCrestCountController
3. [ ] RaiderRaidProgressController
4. [ ] RaiderStatisticsController
5. [ ] RaiderWarcraftLogController
6. [ ] RaiderTrackItemController
7. [ ] RaiderPvpBracketController
8. [ ] RaiderRenownController
9. [ ] Integration tests

### Sprint 5: Applications & Wishlists (1-2 days)

1. [ ] ApplicationController
2. [ ] ApplicationQuestionController
3. [ ] WishlistItemController
4. [ ] Integration tests

### Sprint 6: Integrations (1-2 days)

1. [ ] WarcraftLogsConfigController
2. [ ] WarcraftLogsReportController
3. [ ] WarcraftLogsFightController
4. [ ] WarcraftLogsPerformanceController
5. [ ] Integration tests

### Sprint 7: System & Cleanup (1-2 days)

1. [ ] SyncRunController
2. [ ] SnapshotController (combined)
3. [ ] TeamMetadataController
4. [ ] CharacterHistoryController
5. [ ] AuditLogController
6. [ ] GuestController
7. [ ] HistoricalActivityController
8. [ ] Final integration tests

### Sprint 8: Documentation & Verification (1 day)

1. [ ] Complete OpenAPI annotations
2. [ ] Generate API documentation
3. [ ] Verify all endpoints in Swagger UI
4. [ ] Performance testing
5. [ ] Final coverage verification

---

## Entity Count Summary

| Category | Existing | New Needed | Total |
|----------|----------|------------|-------|
| Controllers | 8 | ~25 | ~33 |
| DTOs (sets) | 8 | ~25 | ~33 |
| Services | 8 | ~25 | ~33 |
| Integration Tests | Some | Many | All |

---

## Technical Standards

### Controller Pattern

```kotlin
@RestController
@RequestMapping("/api/v1/entity-name")
@Tag(name = "Entity Name", description = "CRUD operations for Entity")
class EntityController(
    private val service: EntityCrudService
) : BaseCrudController<Entity, Long, CreateEntityRequest, UpdateEntityRequest, EntityResponse>(service) {

    // Additional specialized endpoints
    @GetMapping("/guild/{guildId}")
    fun findByGuild(@PathVariable guildId: String): List<EntityResponse>
}
```

### DTO Pattern

```kotlin
data class CreateEntityRequest(
    @field:NotBlank
    val name: String,

    @field:Size(max = 255)
    val description: String?
)

data class UpdateEntityRequest(
    @field:NotBlank
    val name: String
)

data class EntityResponse(
    val id: Long,
    val name: String,
    val createdAt: Instant
)
```

### Test Pattern

```kotlin
@SpringBootTest
@AutoConfigureMockMvc
class EntityControllerTest : IntegrationTest() {

    @Test
    fun `should create entity with valid request`() {
        // Given
        val request = CreateEntityRequest(name = "Test")

        // When/Then
        mockMvc.post("/api/v1/entities") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.name") { value("Test") }
        }
    }
}
```

---

## Success Criteria

- [ ] All 45+ entities have REST API endpoints
- [ ] 80% code coverage on API layer
- [ ] All endpoints documented in OpenAPI
- [ ] All integration tests passing
- [ ] Rate limiting configured
- [ ] JWT authentication working
- [ ] CORS properly configured
- [ ] No critical security issues

---

## Next Action

Start with **Sprint 1: Foundation** - create the BaseCrudController pattern to enable rapid implementation of remaining controllers.
