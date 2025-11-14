# Implementation Plan

- [x] 1. Set up API foundation and security infrastructure


  - Create base controller, service, and DTO patterns
  - Implement JWT authentication filter with admin mode bypass
  - Configure Spring Security with role-based access control
  - Set up global exception handler with standardized error responses
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 3.1, 3.2, 3.3, 3.4, 8.1, 8.2, 8.3, 8.4, 8.5_



- [ ] 1.1 Create base CRUD controller pattern
  - Write `BaseCrudController` abstract class with standard CRUD operations
  - Add OpenAPI annotations for automatic documentation
  - Implement pagination support with PagedResponse DTO

  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 7.1, 7.2_

- [ ] 1.2 Implement JWT authentication and admin mode
  - Create `JwtAuthenticationFilter` for token validation
  - Implement `AdminModeConfig` with environment variable support
  - Add startup warning logging when admin mode is enabled

  - Create `AuthenticatedUser` model with role and guild context
  - _Requirements: 2.1, 2.5, 3.1, 3.2, 3.3, 3.4_

- [ ] 1.3 Configure Spring Security
  - Create `SecurityConfig` with HTTP security rules
  - Define role-based endpoint access (PUBLIC_USER, GUILD_ADMIN, SYSTEM_ADMIN)

  - Configure CORS with environment-based origins
  - Add security filter chain with JWT filter
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 15.1, 15.2, 15.3, 15.4, 15.5_

- [ ] 1.4 Implement global exception handling
  - Create `GlobalExceptionHandler` with @RestControllerAdvice

  - Add handlers for ResourceNotFoundException, ValidationException, AccessDeniedException
  - Implement standardized ErrorResponse and ValidationErrorResponse DTOs
  - Add database constraint violation handling
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_



- [ ] 1.5 Write unit tests for security components
  - Test JWT filter with valid/invalid tokens
  - Test admin mode bypass behavior
  - Test role-based access control rules
  - Test exception handler responses
  - _Requirements: 9.2, 9.3_

- [ ] 2. Implement OpenAPI documentation and API infrastructure
  - Configure Springdoc OpenAPI with security schemes
  - Set up Swagger UI with custom branding

  - Implement rate limiting filter
  - Create audit logging service
  - Add health check and metrics endpoints
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 12.1, 12.2, 12.3, 12.4, 12.5, 13.1, 13.2, 13.3, 13.4, 13.5, 14.1, 14.2, 14.3, 14.4, 14.5_

- [x] 2.1 Configure OpenAPI documentation


  - Create `OpenApiConfig` with API info and security schemes
  - Add admin mode banner to API description
  - Configure Swagger UI paths and settings
  - Add server URLs for local and production
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 3.5_



- [ ] 2.2 Implement rate limiting
  - Create `RateLimitFilter` with separate limits for read/write operations
  - Add rate limit headers to responses
  - Implement 429 Too Many Requests response
  - Add admin mode bypass for rate limiting

  - _Requirements: 12.1, 12.2, 12.3, 12.4, 12.5_

- [-] 2.3 Create audit logging service

  - Implement `AuditLogger` service for tracking operations
  - Create `AuditLogEntity` and repository
  - Add database migration for audit_logs table
  - Log CREATE, UPDATE, DELETE, and sensitive ACCESS operations
  - Include admin mode indicator in logs
  - _Requirements: 13.1, 13.2, 13.3, 13.4, 13.5_


- [ ] 2.4 Add health check and metrics endpoints
  - Configure Spring Actuator health endpoint
  - Add database connectivity check
  - Expose Prometheus metrics for request counts and response times
  - Add custom metrics for API operations

  - _Requirements: 14.1, 14.2, 14.3, 14.4, 14.5_


- [ ] 3. Implement core entity CRUD services and DTOs
  - Create service layer pattern with CrudService interface
  - Implement mapper pattern for entity-DTO conversion


  - Add input validation with Jakarta Bean Validation
  - Create DTOs for Raiders, Raids, LootAwards, and AttendanceStats
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 6.1, 6.2, 6.3, 6.4, 6.5_

- [x] 3.1 Create CRUD service interface and base implementation


  - Define `CrudService<T, ID, CreateReq, UpdateReq, Resp>` interface
  - Implement common CRUD operations (findAll, findById, create, update, delete)
  - Add guild-based access validation
  - Integrate audit logging for all write operations
  - _Requirements: 5.1, 5.2, 2.2, 13.1, 13.2_




- [ ] 3.2 Implement mapper pattern
  - Create `EntityMapper<T, CreateReq, UpdateReq, Resp>` interface
  - Implement toEntity, updateEntity, and toResponse methods

  - Handle nested entity relationships with DTOs
  - Prevent circular references in serialization
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_


- [ ] 3.3 Create Raider CRUD service and DTOs
  - Implement `RaiderCrudService` with guild filtering
  - Create `CreateRaiderRequest`, `UpdateRaiderRequest`, `RaiderResponse` DTOs
  - Add validation annotations (@NotBlank, @Size, etc.)
  - Implement `RaiderMapper` for entity-DTO conversion
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 6.1, 6.2, 6.3_

- [ ] 3.4 Create Raid CRUD service and DTOs
  - Implement `RaidCrudService` with date filtering
  - Create `CreateRaidRequest`, `UpdateRaidRequest`, `RaidResponse` DTOs
  - Add validation for raid dates and encounter references
  - Implement `RaidMapper` for entity-DTO conversion
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 6.1, 6.2, 6.3, 6.4_

- [ ] 3.5 Create LootAward CRUD service and DTOs
  - Implement `LootAwardCrudService` with raider and item filtering
  - Create `CreateLootAwardRequest`, `UpdateLootAwardRequest`, `LootAwardResponse` DTOs
  - Add nested DTOs for bonus IDs and wish data
  - Implement `LootAwardMapper` with nested entity handling
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 6.1, 6.2, 6.3_

- [ ] 3.6 Create AttendanceStat CRUD service and DTOs
  - Implement `AttendanceStatCrudService` with raider filtering
  - Create `CreateAttendanceStatRequest`, `UpdateAttendanceStatRequest`, `AttendanceStatResponse` DTOs
  - Add validation for attendance percentages (0-100)
  - Implement `AttendanceStatMapper` for entity-DTO conversion
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 6.1, 6.2, 6.3_

- [ ] 3.7 Write unit tests for core services
  - Test CRUD operations for each service
  - Test guild access validation
  - Test mapper conversions
  - Test validation error handling
  - _Requirements: 9.1, 9.2, 9.3_

- [ ] 4. Implement core entity REST controllers
  - Create controllers for Raiders, Raids, LootAwards, and AttendanceStats
  - Add OpenAPI annotations for documentation
  - Implement pagination and filtering
  - Add specialized endpoints for common queries
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 4.3, 4.4, 7.1, 7.2, 7.3, 7.4, 7.5, 10.1, 10.2, 10.3_

- [ ] 4.1 Create RaiderController
  - Extend `BaseCrudController` with Raider types
  - Add `/api/v1/raiders/guild/{guildId}` endpoint for guild roster
  - Add `/api/v1/raiders/search` endpoint for name/realm lookup
  - Add OpenAPI tags and operation descriptions
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 4.3, 4.4, 10.1_

- [ ] 4.2 Create RaidController
  - Extend `BaseCrudController` with Raid types
  - Add `/api/v1/raids/guild/{guildId}` endpoint for guild raids
  - Add `/api/v1/raids/upcoming` endpoint for scheduled raids
  - Add date range filtering support
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 4.3, 4.4, 7.3_



- [ ] 4.3 Create LootAwardController
  - Extend `BaseCrudController` with LootAward types
  - Add `/api/v1/loot-awards/guild/{guildId}` endpoint for guild loot history
  - Add `/api/v1/loot-awards/raider/{raiderId}` endpoint for raider loot
  - Add item filtering support
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 4.3, 4.4, 10.2_



- [ ] 4.4 Create AttendanceStatController
  - Extend `BaseCrudController` with AttendanceStat types
  - Add `/api/v1/attendance/guild/{guildId}/summary` endpoint for aggregated stats
  - Add `/api/v1/attendance/raider/{raiderId}` endpoint for raider attendance
  - Add date range filtering support


  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 4.3, 4.4, 10.3_

- [ ] 4.5 Write integration tests for core controllers
  - Test all CRUD endpoints with TestContainers
  - Test pagination and filtering
  - Test authentication and authorization
  - Test validation error responses
  - _Requirements: 9.1, 9.2, 9.3, 9.4_

- [ ] 5. Implement FLPS and guild management entity APIs
  - Create controllers and services for FLPS modifiers, behavioral actions, and loot bans
  - Add guild configuration endpoints
  - Implement specialized FLPS calculation endpoints
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 2.2, 10.4, 10.5_

- [ ] 5.1 Create FlpsModifier CRUD services and controllers
  - Implement services for FlpsDefaultModifier and FlpsGuildModifier
  - Create DTOs with validation for modifier values (0.0-1.0)
  - Add controllers with guild-specific filtering
  - Add OpenAPI documentation
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 6.3_

- [ ] 5.2 Create BehavioralAction CRUD service and controller
  - Implement `BehavioralActionCrudService` with active/expired filtering
  - Create DTOs with validation for deduction amounts
  - Add `/api/v1/behavioral-actions/guild/{guildId}/active` endpoint
  - Require GUILD_ADMIN role for write operations
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 2.2, 10.5_

- [ ] 5.3 Create LootBan CRUD service and controller
  - Implement `LootBanCrudService` with active ban filtering
  - Create DTOs with expiration date validation
  - Add `/api/v1/loot-bans/guild/{guildId}/active` endpoint
  - Require GUILD_ADMIN role for write operations
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 2.2_

- [ ] 5.4 Create GuildConfiguration CRUD service and controller
  - Implement `GuildConfigurationCrudService`
  - Create DTOs for guild settings
  - Add guild-specific access control
  - Require GUILD_ADMIN role for modifications
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 2.2_

- [ ] 5.5 Write tests for FLPS and guild management APIs
  - Test CRUD operations for all entities
  - Test guild-specific access control
  - Test validation for modifier values
  - Test active/expired filtering
  - _Requirements: 9.1, 9.2, 9.3_

- [ ] 6. Implement character data entity APIs
  - Create controllers and services for raider gear, vault slots, crests, and progression
  - Add nested entity handling for complex relationships
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 5.5_

- [ ] 6.1 Create RaiderGearItem CRUD service and controller
  - Implement service with raider filtering
  - Create DTOs with item validation
  - Add `/api/v1/raider-gear/raider/{raiderId}` endpoint
  - Handle nested bonus IDs
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 5.5_

- [ ] 6.2 Create RaiderVaultSlot CRUD service and controller
  - Implement service with raider and week filtering
  - Create DTOs for vault rewards
  - Add `/api/v1/vault-slots/raider/{raiderId}/current` endpoint
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [ ] 6.3 Create RaiderCrestCount CRUD service and controller
  - Implement service with raider filtering
  - Create DTOs with crest type validation
  - Add `/api/v1/crest-counts/raider/{raiderId}` endpoint
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [ ] 6.4 Create RaiderRaidProgress CRUD service and controller
  - Implement service with raider and raid filtering
  - Create DTOs for progression tracking
  - Add `/api/v1/raid-progress/raider/{raiderId}` endpoint
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [ ] 6.5 Create remaining character data controllers
  - Implement RaiderTrackItem, RaiderPvpBracket, RaiderRenown controllers
  - Create corresponding services and DTOs
  - Add raider-specific filtering endpoints
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [ ] 6.6 Write tests for character data APIs
  - Test CRUD operations for all character entities
  - Test nested entity handling
  - Test raider-specific filtering
  - _Requirements: 9.1, 9.3_


- [ ] 7. Implement application and wishlist entity APIs
  - Create controllers and services for guild applications and wishlists
  - Add file upload handling for application attachments
  - Implement wishlist snapshot endpoints
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 10.4_

- [ ] 7.1 Create Application CRUD service and controller
  - Implement `ApplicationCrudService` with status filtering
  - Create DTOs for application data
  - Add `/api/v1/applications/guild/{guildId}` endpoint
  - Add status transition validation (pending → approved/rejected)
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [ ] 7.2 Create ApplicationQuestion CRUD service and controller
  - Implement service with application filtering
  - Create DTOs for questions and answers
  - Handle file attachments with ApplicationQuestionFile
  - Add nested DTOs for file metadata
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 5.5_

- [ ] 7.3 Create WishlistSnapshot CRUD service and controller
  - Implement service with guild and period filtering
  - Create DTOs for wishlist data
  - Add `/api/v1/wishlists/guild/{guildId}/current` endpoint
  - Add item priority grouping endpoint
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 10.4_

- [ ] 7.4 Write tests for application and wishlist APIs
  - Test CRUD operations
  - Test status transitions
  - Test file attachment handling
  - Test wishlist grouping
  - _Requirements: 9.1, 9.3_

- [ ] 8. Implement integration entity APIs (Warcraft Logs and Raidbots)
  - Create controllers and services for Warcraft Logs and Raidbots data
  - Add configuration management endpoints
  - Implement performance metrics endpoints
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 2.2_

- [ ] 8.1 Create WarcraftLogsConfig CRUD service and controller
  - Implement service with guild filtering
  - Create DTOs with credential encryption
  - Add `/api/v1/warcraft-logs/config/guild/{guildId}` endpoint
  - Require GUILD_ADMIN role for modifications
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 2.2_

- [ ] 8.2 Create WarcraftLogsPerformance CRUD service and controller
  - Implement service with raider and fight filtering
  - Create DTOs for performance metrics
  - Add `/api/v1/warcraft-logs/performance/raider/{raiderId}` endpoint
  - Add aggregation endpoint for average performance
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [ ] 8.3 Create WarcraftLogsFight and Report controllers
  - Implement services for fights and reports
  - Create DTOs with encounter details
  - Add `/api/v1/warcraft-logs/reports/guild/{guildId}` endpoint
  - Add fight filtering by encounter and difficulty
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [ ] 8.4 Create RaidbotsConfig and Result controllers
  - Implement services for Raidbots configuration and results
  - Create DTOs for simulation data
  - Add `/api/v1/raidbots/results/raider/{raiderId}` endpoint
  - Require GUILD_ADMIN role for config modifications
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 2.2_

- [ ] 8.5 Write tests for integration entity APIs
  - Test CRUD operations
  - Test guild-specific access control
  - Test credential encryption
  - Test performance metric aggregation
  - _Requirements: 9.1, 9.2, 9.3_

- [ ] 9. Implement system and metadata entity APIs
  - Create controllers and services for sync runs, snapshots, and team metadata
  - Add system-wide endpoints for monitoring
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [ ] 9.1 Create SyncRun CRUD service and controller
  - Implement service with status and date filtering
  - Create DTOs for sync execution data
  - Add `/api/v1/sync-runs/recent` endpoint for latest syncs
  - Add `/api/v1/sync-runs/failed` endpoint for error tracking
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [ ] 9.2 Create snapshot entity controllers
  - Implement services for PeriodSnapshot and WoWAuditSnapshot
  - Create DTOs for snapshot data
  - Add date range filtering
  - Add comparison endpoints for snapshot diffs
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [ ] 9.3 Create TeamMetadata and TeamRaidDay controllers
  - Implement services for team configuration
  - Create DTOs for team settings
  - Add `/api/v1/teams/guild/{guildId}` endpoint
  - Add raid schedule endpoints
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [ ] 9.4 Write tests for system entity APIs
  - Test CRUD operations
  - Test filtering and date ranges
  - Test snapshot comparison logic
  - _Requirements: 9.1, 9.3_

- [ ] 10. Implement remaining entity APIs
  - Create controllers and services for all remaining entities
  - Ensure complete API coverage for all 45+ entities
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [ ] 10.1 Create RaidEncounter and RaidSignup controllers
  - Implement services for encounters and signups
  - Create DTOs with encounter details
  - Add raid-specific filtering
  - Add signup status endpoints
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [ ] 10.2 Create Guest and HistoricalActivity controllers
  - Implement services for guest tracking and activity history
  - Create DTOs for guest and activity data
  - Add date range filtering for historical data
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [ ] 10.3 Create CharacterHistory controller
  - Implement service for character change tracking
  - Create DTOs for history records
  - Add `/api/v1/character-history/raider/{raiderId}` endpoint
  - Add change type filtering
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [ ] 10.4 Create RaiderStatistics and RaiderWarcraftLog controllers
  - Implement services for statistics and log references
  - Create DTOs for stat data
  - Add raider-specific endpoints
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [ ] 10.5 Write tests for remaining entity APIs
  - Test CRUD operations for all remaining entities
  - Test filtering and specialized endpoints
  - Verify complete API coverage
  - _Requirements: 9.1, 9.3_

- [ ] 11. Implement API versioning and deprecation support
  - Add version prefix to all endpoints
  - Create version negotiation mechanism
  - Add deprecation warning headers
  - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5_

- [ ] 11.1 Configure API versioning
  - Add `/api/v1/` prefix to all controllers
  - Create version configuration properties
  - Update OpenAPI documentation with version info
  - _Requirements: 11.1, 11.2_

- [ ] 11.2 Implement deprecation support
  - Create `@Deprecated` annotation handler
  - Add deprecation warning to response headers
  - Update OpenAPI to show deprecated endpoints
  - _Requirements: 11.4, 11.5_

- [ ] 11.3 Write tests for versioning
  - Test version prefix routing
  - Test deprecation headers
  - Test OpenAPI version documentation
  - _Requirements: 9.4_

- [ ] 12. Complete comprehensive testing suite
  - Write integration tests for all controllers
  - Add contract tests for OpenAPI compliance
  - Implement security tests
  - Add performance tests
  - Achieve 80% code coverage
  - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_

- [ ] 12.1 Write integration tests for all controllers
  - Create TestContainers setup for PostgreSQL
  - Test all CRUD operations with real database
  - Test pagination, filtering, and sorting
  - Test error responses
  - _Requirements: 9.1_

- [ ] 12.2 Implement OpenAPI contract tests
  - Generate OpenAPI spec from running application
  - Compare with expected specification
  - Validate request/response schemas
  - Test example payloads
  - _Requirements: 9.4_

- [ ] 12.3 Create security test suite
  - Test authentication requirements for all endpoints
  - Test role-based access control
  - Test admin mode bypass
  - Test CORS configuration
  - _Requirements: 9.2_

- [ ] 12.4 Add performance tests
  - Test response times under load
  - Test pagination performance with large datasets
  - Test rate limiting behavior
  - Test database connection pool under stress
  - _Requirements: 9.1_

- [ ] 12.5 Verify code coverage
  - Run coverage reports for all packages
  - Ensure 80% minimum coverage on controllers and services
  - Add tests for uncovered branches
  - _Requirements: 9.5_

- [ ] 13. Finalize documentation and deployment preparation
  - Complete OpenAPI documentation for all endpoints
  - Create API usage guide
  - Add deployment configuration
  - Create production checklist
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

- [ ] 13.1 Complete OpenAPI documentation
  - Add detailed descriptions for all endpoints
  - Add request/response examples for all operations
  - Document all error responses
  - Add authentication requirements to all endpoints
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

- [ ] 13.2 Create API usage documentation
  - Write getting started guide
  - Document authentication flow
  - Add common usage examples
  - Document rate limiting and pagination
  - Create troubleshooting guide
  - _Requirements: 4.2_

- [ ] 13.3 Add deployment configuration
  - Create production application.yaml
  - Add Docker configuration with health checks
  - Configure environment variables
  - Add database migration verification
  - _Requirements: 3.3_

- [ ] 13.4 Create production deployment checklist
  - Verify admin mode is disabled
  - Verify JWT secret is configured
  - Verify CORS origins are restricted
  - Verify rate limiting is enabled
  - Verify HTTPS enforcement
  - Document monitoring setup
  - _Requirements: 3.3, 3.4_
