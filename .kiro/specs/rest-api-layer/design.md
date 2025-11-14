# Design Document

## Overview

The REST API Layer provides comprehensive HTTP endpoints for all EdgeRush LootMan database entities, implementing a clean architecture with DTOs, role-based authentication, OpenAPI documentation, and extensive testing. The design follows Spring Boot best practices with clear separation between controllers, services, and repositories.

## Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     API Clients                              │
│              (Web Dashboard, Discord Bot, CLI)               │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTP/REST
┌────────────────────────▼────────────────────────────────────┐
│                   API Gateway Layer                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ CORS Filter  │  │ Auth Filter  │  │ Rate Limiter │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│                  Controller Layer                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Entity Controllers (45+ controllers)                 │  │
│  │  - RaiderController, RaidController, etc.            │  │
│  │  - Request validation                                 │  │
│  │  - DTO conversion                                     │  │
│  │  - OpenAPI annotations                                │  │
│  └──────────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│                   Service Layer                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  CRUD Services (45+ services)                         │  │
│  │  - Business logic                                     │  │
│  │  - Authorization checks                               │  │
│  │  - Entity ↔ DTO mapping                              │  │
│  └──────────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│                 Repository Layer                             │
│  (Existing 45+ Spring Data repositories)                    │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│                  PostgreSQL Database                         │
└─────────────────────────────────────────────────────────────┘
```

### Package Structure

```
com.edgerush.datasync
├── api/
│   ├── v1/                          # Version 1 controllers
│   │   ├── RaiderController.kt
│   │   ├── RaidController.kt
│   │   ├── LootAwardController.kt
│   │   └── ... (45+ controllers)
│   ├── dto/                         # Data Transfer Objects
│   │   ├── request/
│   │   │   ├── CreateRaiderRequest.kt
│   │   │   ├── UpdateRaiderRequest.kt
│   │   │   └── ...
│   │   └── response/
│   │       ├── RaiderResponse.kt
│   │       ├── PagedResponse.kt
│   │       └── ...
│   └── exception/                   # API exception handlers
│       ├── GlobalExceptionHandler.kt
│       └── ApiException.kt
├── service/
│   ├── crud/                        # CRUD service layer
│   │   ├── RaiderCrudService.kt
│   │   ├── RaidCrudService.kt
│   │   └── ...
│   └── mapper/                      # Entity ↔ DTO mappers
│       ├── RaiderMapper.kt
│       └── ...
├── security/                        # Authentication & Authorization
│   ├── SecurityConfig.kt
│   ├── JwtAuthenticationFilter.kt
│   ├── AdminModeConfig.kt
│   └── RoleBasedAccessControl.kt
├── config/                          # Configuration
│   ├── OpenApiConfig.kt
│   ├── CorsConfig.kt
│   ├── RateLimitConfig.kt
│   └── AuditConfig.kt
└── validation/                      # Custom validators
    ├── EntityExistsValidator.kt
    └── GuildContextValidator.kt
```


## Components and Interfaces

### 1. Controller Layer

#### Base Controller Pattern

All entity controllers extend a base controller providing common CRUD operations:

```kotlin
@RestController
@RequestMapping("/api/v1/{entityPath}")
@Tag(name = "Entity Name", description = "CRUD operations for Entity")
abstract class BaseCrudController<T : Entity, ID, CreateReq, UpdateReq, Resp>(
    protected val service: CrudService<T, ID, CreateReq, UpdateReq, Resp>
) {
    @GetMapping
    @Operation(summary = "List all entities with pagination")
    fun findAll(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) sort: String?
    ): PagedResponse<Resp>
    
    @GetMapping("/{id}")
    @Operation(summary = "Get entity by ID")
    fun findById(@PathVariable id: ID): Resp
    
    @PostMapping
    @PreAuthorize("hasAnyRole('GUILD_ADMIN', 'SYSTEM_ADMIN')")
    @Operation(summary = "Create new entity")
    fun create(@Valid @RequestBody request: CreateReq): Resp
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('GUILD_ADMIN', 'SYSTEM_ADMIN')")
    @Operation(summary = "Update existing entity")
    fun update(@PathVariable id: ID, @Valid @RequestBody request: UpdateReq): Resp
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('GUILD_ADMIN', 'SYSTEM_ADMIN')")
    @Operation(summary = "Delete entity")
    fun delete(@PathVariable id: ID): ResponseEntity<Void>
}
```

#### Example: RaiderController

```kotlin
@RestController
@RequestMapping("/api/v1/raiders")
@Tag(name = "Raiders", description = "Manage guild raiders and their data")
class RaiderController(
    private val raiderService: RaiderCrudService
) : BaseCrudController<RaiderEntity, Long, CreateRaiderRequest, UpdateRaiderRequest, RaiderResponse>(raiderService) {
    
    @GetMapping("/guild/{guildId}")
    @Operation(summary = "Get all raiders for a guild")
    fun findByGuild(@PathVariable guildId: String): List<RaiderResponse> {
        return raiderService.findByGuild(guildId)
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search raiders by name and realm")
    fun search(
        @RequestParam name: String,
        @RequestParam realm: String
    ): RaiderResponse? {
        return raiderService.findByNameAndRealm(name, realm)
    }
}
```

### 2. Service Layer

#### CRUD Service Interface

```kotlin
interface CrudService<T : Entity, ID, CreateReq, UpdateReq, Resp> {
    fun findAll(pageable: Pageable): Page<Resp>
    fun findById(id: ID): Resp
    fun create(request: CreateReq, user: AuthenticatedUser): Resp
    fun update(id: ID, request: UpdateReq, user: AuthenticatedUser): Resp
    fun delete(id: ID, user: AuthenticatedUser)
    fun validateAccess(entity: T, user: AuthenticatedUser)
}
```

#### Example: RaiderCrudService

```kotlin
@Service
class RaiderCrudService(
    private val repository: RaiderRepository,
    private val mapper: RaiderMapper,
    private val auditLogger: AuditLogger
) : CrudService<RaiderEntity, Long, CreateRaiderRequest, UpdateRaiderRequest, RaiderResponse> {
    
    override fun findAll(pageable: Pageable): Page<RaiderResponse> {
        return repository.findAll(pageable).map { mapper.toResponse(it) }
    }
    
    override fun findById(id: Long): RaiderResponse {
        val entity = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("Raider not found: $id") }
        return mapper.toResponse(entity)
    }
    
    override fun create(request: CreateRaiderRequest, user: AuthenticatedUser): RaiderResponse {
        validateGuildAccess(request.guildId, user)
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)
        auditLogger.logCreate("Raider", saved.id, user)
        return mapper.toResponse(saved)
    }
    
    override fun update(id: Long, request: UpdateRaiderRequest, user: AuthenticatedUser): RaiderResponse {
        val existing = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("Raider not found: $id") }
        validateAccess(existing, user)
        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)
        auditLogger.logUpdate("Raider", saved.id, user)
        return mapper.toResponse(saved)
    }
    
    override fun delete(id: Long, user: AuthenticatedUser) {
        val entity = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("Raider not found: $id") }
        validateAccess(entity, user)
        repository.delete(entity)
        auditLogger.logDelete("Raider", id, user)
    }
    
    override fun validateAccess(entity: RaiderEntity, user: AuthenticatedUser) {
        if (!user.isSystemAdmin() && !user.hasGuildAccess(entity.guildId)) {
            throw AccessDeniedException("No access to this raider")
        }
    }
    
    fun findByGuild(guildId: String): List<RaiderResponse> {
        return repository.findByGuildId(guildId).map { mapper.toResponse(it) }
    }
}
```

### 3. DTO Layer

#### Request DTOs

```kotlin
data class CreateRaiderRequest(
    @field:NotBlank(message = "Character name is required")
    @field:Size(min = 2, max = 50)
    val characterName: String,
    
    @field:NotBlank(message = "Realm is required")
    val realm: String,
    
    @field:NotBlank(message = "Guild ID is required")
    val guildId: String,
    
    @field:NotNull(message = "Class is required")
    val characterClass: String,
    
    val spec: String?,
    val role: String?,
    val itemLevel: Int?
)

data class UpdateRaiderRequest(
    val characterClass: String?,
    val spec: String?,
    val role: String?,
    val itemLevel: Int?,
    val isActive: Boolean?
)
```

#### Response DTOs

```kotlin
data class RaiderResponse(
    val id: Long,
    val characterName: String,
    val realm: String,
    val guildId: String,
    val characterClass: String,
    val spec: String?,
    val role: String?,
    val itemLevel: Int?,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class PagedResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val isFirst: Boolean,
    val isLast: Boolean
)
```

### 4. Security Layer

#### JWT Authentication Filter

```kotlin
@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val adminModeConfig: AdminModeConfig
) : OncePerRequestFilter() {
    
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (adminModeConfig.isEnabled()) {
            // Admin mode: simulate system admin
            val adminAuth = createAdminAuthentication()
            SecurityContextHolder.getContext().authentication = adminAuth
            filterChain.doFilter(request, response)
            return
        }
        
        val token = extractToken(request)
        if (token != null && jwtService.validateToken(token)) {
            val authentication = jwtService.getAuthentication(token)
            SecurityContextHolder.getContext().authentication = authentication
        }
        
        filterChain.doFilter(request, response)
    }
}
```

#### Admin Mode Configuration

```kotlin
@Configuration
@ConfigurationProperties(prefix = "api.admin-mode")
data class AdminModeConfig(
    var enabled: Boolean = false
) {
    @PostConstruct
    fun logWarning() {
        if (enabled) {
            logger.warn("⚠️  ADMIN MODE ENABLED - Authentication bypassed for all requests!")
            logger.warn("⚠️  This should ONLY be used in development/testing environments")
        }
    }
}
```

#### Security Configuration

```kotlin
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val jwtAuthFilter: JwtAuthenticationFilter,
    private val adminModeConfig: AdminModeConfig
) {
    
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/v1/health", "/api/v1/metrics").permitAll()
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    .requestMatchers("/api/v1/flps/**").hasAnyRole("PUBLIC_USER", "GUILD_ADMIN", "SYSTEM_ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/v1/**").hasAnyRole("PUBLIC_USER", "GUILD_ADMIN", "SYSTEM_ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/v1/**").hasAnyRole("GUILD_ADMIN", "SYSTEM_ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/**").hasAnyRole("GUILD_ADMIN", "SYSTEM_ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/**").hasAnyRole("GUILD_ADMIN", "SYSTEM_ADMIN")
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
        
        return http.build()
    }
}
```


### 5. OpenAPI Configuration

```kotlin
@Configuration
class OpenApiConfig(
    private val adminModeConfig: AdminModeConfig
) {
    
    @Bean
    fun customOpenAPI(): OpenAPI {
        val securityScheme = SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .`in`(SecurityScheme.In.HEADER)
            .name("Authorization")
        
        val securityRequirement = SecurityRequirement().addList("bearerAuth")
        
        val info = Info()
            .title("EdgeRush LootMan API")
            .version("1.0.0")
            .description(buildDescription())
            .contact(Contact().name("EdgeRush Team"))
        
        return OpenAPI()
            .info(info)
            .addSecurityItem(securityRequirement)
            .components(Components().addSecuritySchemes("bearerAuth", securityScheme))
            .servers(listOf(
                Server().url("http://localhost:8080").description("Local Development"),
                Server().url("https://api.edgerush.com").description("Production")
            ))
    }
    
    private fun buildDescription(): String {
        val baseDesc = """
            Comprehensive REST API for EdgeRush LootMan guild management system.
            
            ## Features
            - Full CRUD operations for all entities
            - Role-based access control
            - Pagination and filtering
            - OpenAPI 3.0 documentation
            
            ## Authentication
            Most endpoints require JWT bearer token authentication.
        """.trimIndent()
        
        return if (adminModeConfig.isEnabled()) {
            """
            ⚠️ **ADMIN MODE ACTIVE** - Authentication is bypassed!
            
            $baseDesc
            """.trimIndent()
        } else {
            baseDesc
        }
    }
}
```

### 6. Exception Handling

```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(ex: ResourceNotFoundException, request: WebRequest): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.NOT_FOUND.value(),
            error = "Not Found",
            message = ex.message ?: "Resource not found",
            path = request.getDescription(false)
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
    }
    
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException, request: WebRequest): ResponseEntity<ValidationErrorResponse> {
        val fieldErrors = ex.bindingResult.fieldErrors.map {
            FieldError(it.field, it.defaultMessage ?: "Invalid value")
        }
        
        val error = ValidationErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validation Failed",
            message = "Input validation failed",
            path = request.getDescription(false),
            fieldErrors = fieldErrors
        )
        return ResponseEntity.badRequest().body(error)
    }
    
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: AccessDeniedException, request: WebRequest): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.FORBIDDEN.value(),
            error = "Forbidden",
            message = ex.message ?: "Access denied",
            path = request.getDescription(false)
        )
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error)
    }
    
    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrity(ex: DataIntegrityViolationException, request: WebRequest): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.CONFLICT.value(),
            error = "Conflict",
            message = "Database constraint violation",
            path = request.getDescription(false)
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error)
    }
}

data class ErrorResponse(
    val timestamp: LocalDateTime,
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)

data class ValidationErrorResponse(
    val timestamp: LocalDateTime,
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val fieldErrors: List<FieldError>
)

data class FieldError(
    val field: String,
    val message: String
)
```

### 7. Rate Limiting

```kotlin
@Component
class RateLimitFilter : OncePerRequestFilter() {
    
    private val readLimiter = RateLimiter.create(100.0) // 100 requests per second
    private val writeLimiter = RateLimiter.create(20.0) // 20 requests per second
    
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val limiter = if (isWriteOperation(request)) writeLimiter else readLimiter
        
        if (!limiter.tryAcquire()) {
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.addHeader("Retry-After", "1")
            response.writer.write("""{"error": "Rate limit exceeded"}""")
            return
        }
        
        filterChain.doFilter(request, response)
    }
    
    private fun isWriteOperation(request: HttpServletRequest): Boolean {
        return request.method in listOf("POST", "PUT", "DELETE", "PATCH")
    }
}
```

### 8. Audit Logging

```kotlin
@Service
class AuditLogger(
    private val auditRepository: AuditLogRepository
) {
    
    fun logCreate(entityType: String, entityId: Any, user: AuthenticatedUser) {
        log("CREATE", entityType, entityId, user)
    }
    
    fun logUpdate(entityType: String, entityId: Any, user: AuthenticatedUser) {
        log("UPDATE", entityType, entityId, user)
    }
    
    fun logDelete(entityType: String, entityId: Any, user: AuthenticatedUser) {
        log("DELETE", entityType, entityId, user)
    }
    
    fun logAccess(entityType: String, entityId: Any, user: AuthenticatedUser) {
        log("ACCESS", entityType, entityId, user)
    }
    
    private fun log(operation: String, entityType: String, entityId: Any, user: AuthenticatedUser) {
        val entry = AuditLogEntity(
            timestamp = LocalDateTime.now(),
            operation = operation,
            entityType = entityType,
            entityId = entityId.toString(),
            userId = user.id,
            username = user.username,
            isAdminMode = user.isAdminMode,
            requestId = MDC.get("requestId")
        )
        auditRepository.save(entry)
    }
}
```

### 9. Mapper Pattern

```kotlin
@Component
class RaiderMapper {
    
    fun toEntity(request: CreateRaiderRequest): RaiderEntity {
        return RaiderEntity(
            characterName = request.characterName,
            realm = request.realm,
            guildId = request.guildId,
            characterClass = request.characterClass,
            spec = request.spec,
            role = request.role,
            itemLevel = request.itemLevel,
            isActive = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
    
    fun updateEntity(entity: RaiderEntity, request: UpdateRaiderRequest): RaiderEntity {
        return entity.copy(
            characterClass = request.characterClass ?: entity.characterClass,
            spec = request.spec ?: entity.spec,
            role = request.role ?: entity.role,
            itemLevel = request.itemLevel ?: entity.itemLevel,
            isActive = request.isActive ?: entity.isActive,
            updatedAt = LocalDateTime.now()
        )
    }
    
    fun toResponse(entity: RaiderEntity): RaiderResponse {
        return RaiderResponse(
            id = entity.id!!,
            characterName = entity.characterName,
            realm = entity.realm,
            guildId = entity.guildId,
            characterClass = entity.characterClass,
            spec = entity.spec,
            role = entity.role,
            itemLevel = entity.itemLevel,
            isActive = entity.isActive,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}
```

## Data Models

### Entity Categories

The API will expose 45+ entities grouped into logical categories:

#### Core Guild Data
- **Raiders**: Guild member characters
- **Raids**: Scheduled raid events
- **RaidEncounters**: Boss encounters within raids
- **RaidSignups**: Raider signups for raids
- **Guests**: Non-guild raid participants
- **TeamMetadata**: Guild team information
- **TeamRaidDays**: Scheduled raid days

#### Loot Management
- **LootAwards**: Loot distribution records
- **LootAwardBonusIds**: Item bonus IDs
- **LootAwardOldItems**: Replaced items
- **LootAwardWishData**: Wishlist data for awards
- **LootBans**: Temporary loot restrictions
- **WishlistSnapshots**: Historical wishlist data

#### Attendance & Performance
- **AttendanceStats**: Raider attendance records
- **HistoricalActivity**: Long-term activity tracking
- **CharacterHistory**: Character change history
- **RaiderStatistics**: Performance statistics

#### FLPS Configuration
- **FlpsDefaultModifiers**: System-wide FLPS settings
- **FlpsGuildModifiers**: Guild-specific FLPS overrides
- **BehavioralActions**: Behavioral score adjustments

#### Character Data
- **RaiderGearItems**: Equipped gear
- **RaiderTrackItems**: Tracked items
- **RaiderVaultSlots**: Great Vault rewards
- **RaiderCrestCounts**: Upgrade currency counts
- **RaiderRaidProgress**: Raid progression
- **RaiderPvpBrackets**: PvP ratings
- **RaiderRenown**: Faction renown levels
- **RaiderWarcraftLogs**: Warcraft Logs references

#### Applications
- **Applications**: Guild applications
- **ApplicationAlts**: Applicant alternate characters
- **ApplicationQuestions**: Application questions
- **ApplicationQuestionFiles**: Attached files

#### Integration Data
- **WarcraftLogsConfigs**: Warcraft Logs settings
- **WarcraftLogsReports**: Synced reports
- **WarcraftLogsFights**: Individual boss fights
- **WarcraftLogsPerformance**: Performance metrics
- **WarcraftLogsCharacterMappings**: Name mappings
- **RaidbotsConfigs**: Raidbots settings
- **RaidbotsSimulations**: Simulation requests
- **RaidbotsResults**: Simulation results

#### System Data
- **SyncRuns**: Data synchronization history
- **PeriodSnapshots**: Point-in-time snapshots
- **WoWAuditSnapshots**: WoWAudit sync snapshots
- **GuildConfigurations**: Guild settings


## Error Handling

### Error Response Format

All errors follow a consistent structure:

```json
{
  "timestamp": "2025-11-13T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/raiders",
  "fieldErrors": [
    {
      "field": "characterName",
      "message": "Character name is required"
    }
  ]
}
```

### HTTP Status Codes

| Status | Usage |
|--------|-------|
| 200 OK | Successful GET, PUT operations |
| 201 Created | Successful POST operation |
| 204 No Content | Successful DELETE operation |
| 400 Bad Request | Validation errors, malformed requests |
| 401 Unauthorized | Missing or invalid authentication |
| 403 Forbidden | Insufficient permissions |
| 404 Not Found | Resource doesn't exist |
| 409 Conflict | Database constraint violation |
| 429 Too Many Requests | Rate limit exceeded |
| 500 Internal Server Error | Unexpected server errors |

### Error Handling Strategy

1. **Validation Errors**: Return field-level details for client-side display
2. **Authorization Errors**: Generic messages to avoid information leakage
3. **Database Errors**: User-friendly messages, full details in logs
4. **External API Errors**: Graceful degradation with fallback responses
5. **Unexpected Errors**: Generic message, correlation ID for support

## Testing Strategy

### Test Pyramid

```
                    ┌─────────────┐
                    │   E2E Tests │  (10%)
                    │  Contract   │
                    └─────────────┘
                  ┌─────────────────┐
                  │ Integration Tests│ (30%)
                  │  API + Database  │
                  └─────────────────┘
              ┌───────────────────────┐
              │     Unit Tests         │ (60%)
              │ Services, Mappers, etc │
              └───────────────────────┘
```

### Unit Tests

Test individual components in isolation:

```kotlin
@ExtendWith(MockitoExtension::class)
class RaiderCrudServiceTest {
    
    @Mock
    private lateinit var repository: RaiderRepository
    
    @Mock
    private lateinit var mapper: RaiderMapper
    
    @Mock
    private lateinit var auditLogger: AuditLogger
    
    @InjectMocks
    private lateinit var service: RaiderCrudService
    
    @Test
    fun `findById should return raider when exists`() {
        // Given
        val raiderId = 1L
        val entity = createTestRaider(raiderId)
        val response = createTestRaiderResponse(raiderId)
        
        `when`(repository.findById(raiderId)).thenReturn(Optional.of(entity))
        `when`(mapper.toResponse(entity)).thenReturn(response)
        
        // When
        val result = service.findById(raiderId)
        
        // Then
        assertEquals(response, result)
        verify(repository).findById(raiderId)
        verify(mapper).toResponse(entity)
    }
    
    @Test
    fun `findById should throw exception when not found`() {
        // Given
        val raiderId = 999L
        `when`(repository.findById(raiderId)).thenReturn(Optional.empty())
        
        // When/Then
        assertThrows<ResourceNotFoundException> {
            service.findById(raiderId)
        }
    }
    
    @Test
    fun `create should validate guild access`() {
        // Given
        val request = CreateRaiderRequest(
            characterName = "TestChar",
            realm = "TestRealm",
            guildId = "guild-123",
            characterClass = "Warrior"
        )
        val user = createTestUser(guildId = "guild-456") // Different guild
        
        // When/Then
        assertThrows<AccessDeniedException> {
            service.create(request, user)
        }
    }
}
```

### Integration Tests

Test API endpoints with real database:

```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class RaiderControllerIntegrationTest {
    
    @Container
    private val postgres = PostgreSQLContainer<Nothing>("postgres:18")
    
    @Autowired
    private lateinit var restTemplate: TestRestTemplate
    
    @Autowired
    private lateinit var raiderRepository: RaiderRepository
    
    @BeforeEach
    fun setup() {
        raiderRepository.deleteAll()
    }
    
    @Test
    fun `GET raiders should return paginated list`() {
        // Given
        createTestRaiders(25)
        
        // When
        val response = restTemplate.getForEntity(
            "/api/v1/raiders?page=0&size=10",
            PagedResponse::class.java
        )
        
        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        val body = response.body!!
        assertEquals(10, body.content.size)
        assertEquals(25, body.totalElements)
        assertEquals(3, body.totalPages)
    }
    
    @Test
    fun `POST raider should create new raider`() {
        // Given
        val request = CreateRaiderRequest(
            characterName = "NewChar",
            realm = "TestRealm",
            guildId = "guild-123",
            characterClass = "Mage"
        )
        
        // When
        val response = restTemplate.postForEntity(
            "/api/v1/raiders",
            request,
            RaiderResponse::class.java
        )
        
        // Then
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertNotNull(response.body?.id)
        assertEquals("NewChar", response.body?.characterName)
        
        // Verify in database
        val saved = raiderRepository.findById(response.body!!.id)
        assertTrue(saved.isPresent)
    }
    
    @Test
    fun `POST raider with invalid data should return 400`() {
        // Given
        val request = mapOf("characterName" to "") // Invalid: empty name
        
        // When
        val response = restTemplate.postForEntity(
            "/api/v1/raiders",
            request,
            ValidationErrorResponse::class.java
        )
        
        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertTrue(response.body?.fieldErrors?.any { it.field == "characterName" } == true)
    }
}
```

### Contract Tests (OpenAPI Validation)

Verify API matches OpenAPI specification:

```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenApiContractTest {
    
    @LocalServerPort
    private var port: Int = 0
    
    @Test
    fun `API should match OpenAPI specification`() {
        // Generate OpenAPI spec from running application
        val actualSpec = RestAssured.given()
            .port(port)
            .get("/v3/api-docs")
            .then()
            .extract()
            .asString()
        
        // Compare with expected spec
        val expectedSpec = File("src/main/resources/openapi.yaml").readText()
        
        // Use OpenAPI diff tool
        val diff = OpenApiCompare.fromContents(expectedSpec, actualSpec)
        assertTrue(diff.isUnchanged, "API does not match specification: ${diff.newEndpoints}")
    }
}
```

### Security Tests

Verify authentication and authorization:

```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityIntegrationTest {
    
    @Autowired
    private lateinit var restTemplate: TestRestTemplate
    
    @Test
    fun `GET without auth should return 401`() {
        val response = restTemplate.getForEntity(
            "/api/v1/raiders",
            ErrorResponse::class.java
        )
        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
    }
    
    @Test
    fun `POST with public user role should return 403`() {
        val token = generateToken(roles = listOf("PUBLIC_USER"))
        val headers = HttpHeaders().apply {
            setBearerAuth(token)
        }
        
        val request = HttpEntity(CreateRaiderRequest(...), headers)
        val response = restTemplate.postForEntity(
            "/api/v1/raiders",
            request,
            ErrorResponse::class.java
        )
        
        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }
    
    @Test
    fun `admin mode should bypass authentication`() {
        // Set admin mode via environment
        System.setProperty("api.admin-mode.enabled", "true")
        
        // Request without auth should succeed
        val response = restTemplate.getForEntity(
            "/api/v1/raiders",
            PagedResponse::class.java
        )
        
        assertEquals(HttpStatus.OK, response.statusCode)
    }
}
```

### Performance Tests

Verify API performance under load:

```kotlin
@SpringBootTest
class PerformanceTest {
    
    @Test
    fun `list raiders should complete within 1 second`() {
        // Given: 1000 raiders in database
        createTestRaiders(1000)
        
        // When
        val startTime = System.currentTimeMillis()
        val response = restTemplate.getForEntity(
            "/api/v1/raiders?page=0&size=20",
            PagedResponse::class.java
        )
        val duration = System.currentTimeMillis() - startTime
        
        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertTrue(duration < 1000, "Request took ${duration}ms, expected < 1000ms")
    }
}
```

## Configuration

### Application Properties

```yaml
# API Configuration
api:
  version: v1
  base-path: /api
  
  # Admin Mode (DEVELOPMENT ONLY)
  admin-mode:
    enabled: ${API_ADMIN_MODE:false}
  
  # Rate Limiting
  rate-limit:
    read-requests-per-second: 100
    write-requests-per-second: 20
    enabled: ${API_RATE_LIMIT_ENABLED:true}
  
  # Pagination
  pagination:
    default-page-size: 20
    max-page-size: 100
  
  # CORS
  cors:
    allowed-origins: ${API_CORS_ORIGINS:http://localhost:3000}
    allowed-methods: GET,POST,PUT,DELETE,OPTIONS
    allowed-headers: "*"
    allow-credentials: true
    max-age: 3600

# JWT Configuration
jwt:
  secret: ${JWT_SECRET:your-secret-key-change-in-production}
  expiration-ms: 86400000  # 24 hours
  issuer: edgerush-lootman

# OpenAPI Configuration
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operations-sorter: alpha
    tags-sorter: alpha
  show-actuator: true

# Actuator Configuration
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,info
  endpoint:
    health:
      show-details: when-authorized
  metrics:
    export:
      prometheus:
        enabled: true
```

### Environment Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `API_ADMIN_MODE` | Enable admin mode (bypass auth) | `false` | No |
| `JWT_SECRET` | JWT signing secret | - | Yes (prod) |
| `API_CORS_ORIGINS` | Allowed CORS origins | `http://localhost:3000` | No |
| `API_RATE_LIMIT_ENABLED` | Enable rate limiting | `true` | No |

## Deployment Considerations

### Docker Configuration

```dockerfile
FROM eclipse-temurin:24-jre-alpine

# Add non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy application
COPY --chown=spring:spring build/libs/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/v1/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Production Checklist

- [ ] `API_ADMIN_MODE` is set to `false`
- [ ] `JWT_SECRET` is set to a strong random value
- [ ] CORS origins are restricted to production domains
- [ ] Rate limiting is enabled
- [ ] HTTPS is enforced
- [ ] Database connection pool is tuned
- [ ] Actuator endpoints are secured
- [ ] Logging is configured for production
- [ ] Metrics are exported to monitoring system

## API Documentation Examples

### Example: List Raiders

**Request:**
```http
GET /api/v1/raiders?page=0&size=20&sort=characterName,asc
Authorization: Bearer <jwt-token>
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "characterName": "Arthas",
      "realm": "Stormrage",
      "guildId": "guild-123",
      "characterClass": "Death Knight",
      "spec": "Frost",
      "role": "DPS",
      "itemLevel": 489,
      "isActive": true,
      "createdAt": "2025-01-01T10:00:00",
      "updatedAt": "2025-11-13T10:30:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 45,
  "totalPages": 3,
  "isFirst": true,
  "isLast": false
}
```

### Example: Create Raider

**Request:**
```http
POST /api/v1/raiders
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "characterName": "Thrall",
  "realm": "Stormrage",
  "guildId": "guild-123",
  "characterClass": "Shaman",
  "spec": "Enhancement",
  "role": "DPS",
  "itemLevel": 485
}
```

**Response:**
```json
{
  "id": 46,
  "characterName": "Thrall",
  "realm": "Stormrage",
  "guildId": "guild-123",
  "characterClass": "Shaman",
  "spec": "Enhancement",
  "role": "DPS",
  "itemLevel": 485,
  "isActive": true,
  "createdAt": "2025-11-13T10:35:00",
  "updatedAt": "2025-11-13T10:35:00"
}
```

### Example: Validation Error

**Request:**
```http
POST /api/v1/raiders
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "characterName": "",
  "realm": "Stormrage"
}
```

**Response:**
```json
{
  "timestamp": "2025-11-13T10:40:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/raiders",
  "fieldErrors": [
    {
      "field": "characterName",
      "message": "Character name is required"
    },
    {
      "field": "guildId",
      "message": "Guild ID is required"
    },
    {
      "field": "characterClass",
      "message": "Class is required"
    }
  ]
}
```

## Migration Strategy

### Phase 1: Foundation (Week 1)
- Set up base controller, service, and DTO patterns
- Implement security configuration with admin mode
- Configure OpenAPI documentation
- Create exception handling framework

### Phase 2: Core Entities (Week 2-3)
- Implement CRUD for core entities (Raiders, Raids, Loot)
- Add comprehensive unit tests
- Add integration tests with TestContainers

### Phase 3: Extended Entities (Week 4-5)
- Implement CRUD for remaining entities
- Add specialized endpoints for complex queries
- Implement audit logging

### Phase 4: Polish & Documentation (Week 6)
- Complete OpenAPI documentation
- Add performance tests
- Security audit
- Production deployment preparation
