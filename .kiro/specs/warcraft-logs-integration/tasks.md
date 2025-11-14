# Implementation Plan - Warcraft Logs Integration

## Overview

This implementation plan breaks down the Warcraft Logs integration into discrete, manageable coding tasks. Each task builds incrementally on previous work, ensuring the system remains functional throughout development.

---

## Task List

- [x] 1. Set up project structure and core interfaces



  - Create package structure for Warcraft Logs components
  - Define core interfaces and data models
  - Add required dependencies to build.gradle.kts
  - _Requirements: 1.1, 1.2_



- [ ] 1.1 Add Warcraft Logs dependencies
  - Add WebClient dependencies (already present)
  - Add OAuth2 client dependencies
  - Add resilience4j dependencies for circuit breaker
  - Add encryption dependencies (javax.crypto)


  - _Requirements: 1.1_

- [ ] 1.2 Create package structure
  - Create `com.edgerush.datasync.client.warcraftlogs` package
  - Create `com.edgerush.datasync.service.warcraftlogs` package
  - Create `com.edgerush.datasync.entity.warcraftlogs` package


  - Create `com.edgerush.datasync.repository.warcraftlogs` package
  - Create `com.edgerush.datasync.config.warcraftlogs` package
  - _Requirements: 1.1_



- [ ] 1.3 Define core data models
  - Create `WarcraftLogsReport` data class
  - Create `WarcraftLogsFight` data class
  - Create `CharacterPerformanceData` data class
  - Create `PerformanceMetrics` data class


  - Create `SpecAverages` data class
  - _Requirements: 2.1, 4.1, 4.2_

- [x] 2. Implement database schema and entities


  - Create Flyway migration for Warcraft Logs tables
  - Implement JPA entities for all tables
  - Create Spring Data repositories
  - _Requirements: 7.1, 7.2_

- [x] 2.1 Create database migration


  - Write V0016__add_warcraft_logs_tables.sql
  - Include all tables: config, reports, fights, performance, character_mappings
  - Add indexes for performance optimization
  - _Requirements: 7.1_

- [ ] 2.2 Implement entity classes
  - Create `WarcraftLogsConfigEntity`


  - Create `WarcraftLogsReportEntity`
  - Create `WarcraftLogsFightEntity`
  - Create `WarcraftLogsPerformanceEntity`
  - Create `WarcraftLogsCharacterMappingEntity`
  - _Requirements: 7.1_



- [ ] 2.3 Create repository interfaces
  - Create `WarcraftLogsConfigRepository`
  - Create `WarcraftLogsReportRepository`


  - Create `WarcraftLogsFightRepository`
  - Create `WarcraftLogsPerformanceRepository`
  - Create `WarcraftLogsCharacterMappingRepository`
  - Add custom query methods for common operations


  - _Requirements: 7.1, 7.2_


- [ ] 3. Implement configuration system
  - Create configuration properties classes


  - Implement credential encryption service
  - Create configuration service with guild-specific overrides
  - _Requirements: 1.1, 1.3, 9.1, 9.2, 11.1_




- [ ] 3.1 Create configuration properties
  - Create `WarcraftLogsProperties` with @ConfigurationProperties
  - Define all configurable parameters with defaults
  - Add validation annotations


  - _Requirements: 1.1, 9.1_

- [x] 3.2 Implement credential encryption


  - Create `CredentialEncryptionService`


  - Implement AES-256-GCM encryption/decryption
  - Add encryption key configuration

  - _Requirements: 11.1, 11.2_

- [x] 3.3 Create configuration service

  - Create `WarcraftLogsConfigService`

  - Implement getConfig() with guild-specific overrides
  - Implement updateConfig() with validation
  - Implement getEffectiveClientCredentials()
  - _Requirements: 1.3, 9.1, 9.2, 9.3_



- [ ] 3.4 Create character mapping service
  - Create `CharacterMappingService`
  - Implement character name resolution
  - Support manual mappings and automatic fallback


  - _Requirements: 2.1, 2.2, 2.3_

- [ ] 4. Implement Warcraft Logs API client
  - Create OAuth2 authentication handler


  - Implement API client with all required endpoints


  - Add error handling and retry logic
  - _Requirements: 1.1, 1.4, 3.1, 3.2, 3.3, 8.1, 8.2_


- [x] 4.1 Create OAuth2 authentication handler




  - Create `WarcraftLogsAuthService`
  - Implement OAuth2 client credentials flow
  - Add token caching and refresh logic
  - _Requirements: 1.1, 1.4_




- [ ] 4.2 Implement base API client
  - Create `WarcraftLogsClient` interface
  - Create `WarcraftLogsClientImpl` with WebClient

  - Add request/response logging

  - Configure timeouts and connection pooling
  - _Requirements: 3.1, 3.2, 8.1, 12.4_

- [ ] 4.3 Implement report fetching
  - Implement fetchReportsForGuild() method

  - Add pagination support

  - Add date range filtering
  - _Requirements: 3.1, 3.3_

- [ ] 4.4 Implement fight data fetching
  - Implement fetchFightData() method



  - Parse fight metadata
  - Extract encounter information
  - _Requirements: 4.1, 4.2, 4.3_

- [x] 4.5 Implement performance data fetching


  - Implement fetchCharacterPerformance() method
  - Parse death counts and damage metrics

  - Extract avoidable damage data
  - _Requirements: 4.2, 5.1, 5.2_

- [ ] 4.6 Add error handling
  - Create custom exception hierarchy



  - Implement retry logic with exponential backoff
  - Add rate limit handling with Retry-After support


  - _Requirements: 8.1, 8.2, 8.3_



- [ ] 5. Implement sync service
  - Create sync orchestration service


  - Implement report discovery and processing
  - Add scheduled sync execution
  - _Requirements: 3.1, 3.2, 3.3, 4.1, 4.2, 4.3, 4.4, 7.1, 12.1_



- [x] 5.1 Create sync service foundation

  - Create `WarcraftLogsSyncService`
  - Inject required dependencies (client, repositories, config)
  - Add structured logging
  - _Requirements: 3.1, 7.1, 10.1_




- [ ] 5.2 Implement report sync logic
  - Implement syncReportsForGuild() method
  - Fetch reports from API

  - Store report metadata in database
  - Handle duplicate reports
  - _Requirements: 3.1, 3.2, 3.3, 7.1_


- [ ] 5.3 Implement fight data processing
  - Implement processFightData() method
  - Extract fight metadata

  - Filter by difficulty configuration

  - Store fight data in database
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 7.1_

- [ ] 5.4 Implement performance metric extraction
  - Implement extractPerformanceMetrics() method

  - Fetch character performance for each fight
  - Map character names using CharacterMappingService
  - Calculate avoidable damage percentages
  - Store performance data in database
  - _Requirements: 4.2, 5.1, 5.2, 7.1_

- [ ] 5.5 Implement spec average calculation
  - Implement calculateSpecAverages() method

  - Query performance data by spec
  - Calculate configurable percentile (default 50th)
  - Handle minimum sample size requirement
  - Cache results
  - _Requirements: 5.3, 5.4, 7.3_



- [ ] 5.6 Add scheduled sync execution
  - Add @Scheduled annotation with configurable cron


  - Iterate through all enabled guilds
  - Execute sync with error handling per guild

  - Log sync results
  - _Requirements: 3.1, 3.2, 8.1, 10.1_

- [ ] 5.7 Add async processing
  - Configure async executor for Warcraft Logs
  - Make sync methods async with CompletableFuture

  - Add timeout handling
  - _Requirements: 12.1, 12.5_


- [ ] 6. Implement performance service for MAS calculation
  - Create service to calculate MAS from Warcraft Logs data

  - Implement caching for MAS scores
  - Integrate with ScoreCalculator
  - _Requirements: 5.1, 5.2, 5.3, 6.1, 6.2, 6.3, 6.4, 7.3_


- [ ] 6.1 Create performance service foundation
  - Create `WarcraftLogsPerformanceService`

  - Inject repositories and config service

  - Add cache manager
  - _Requirements: 6.1, 7.3_





- [ ] 6.2 Implement performance metric retrieval
  - Implement getPerformanceMetrics() method
  - Query database for character performance
  - Apply time window filtering


  - Apply time-based weighting for recent performance
  - _Requirements: 5.3, 5.4, 5.5_




- [ ] 6.3 Implement DPA and ADT ratio calculation
  - Implement calculateDPARatio() method
  - Implement calculateADTRatio() method
  - Handle division by zero cases



  - Use spec averages for normalization
  - _Requirements: 5.1, 5.2, 5.3_

- [ ] 6.4 Implement MAS calculation
  - Implement getMASForCharacter() method

  - Calculate DPA and ADT ratios

  - Apply guild-specific weights
  - Check critical threshold and set MAS to 0.0 if exceeded
  - Clamp MAS to 0.0-1.0 range
  - Use fallback value if no data available
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_


- [ ] 6.5 Add MAS caching
  - Implement cache key generation
  - Add @Cacheable annotation to getMASForCharacter()

  - Configure cache TTL
  - Implement cache invalidation on new data sync
  - _Requirements: 7.3, 7.4_

- [ ] 6.6 Integrate with ScoreCalculator
  - Modify ScoreCalculator.calculateMechanicalAdherenceFromActivity()

  - Check if Warcraft Logs integration is enabled
  - Call WarcraftLogsPerformanceService.getMASForCharacter()
  - Fall back to existing logic if Warcraft Logs unavailable
  - _Requirements: 6.1, 6.4_

- [x] 7. Implement resilience patterns

  - Add circuit breaker configuration
  - Implement retry logic
  - Add fallback mechanisms
  - _Requirements: 8.1, 8.2, 8.3, 8.4_


- [ ] 7.1 Configure circuit breaker
  - Create `WarcraftLogsResilienceConfig`
  - Configure CircuitBreaker bean
  - Set failure rate threshold and wait duration
  - _Requirements: 8.1, 8.2_


- [ ] 7.2 Configure retry policy
  - Configure Retry bean
  - Set max attempts and wait duration
  - Define retryable exceptions
  - Define non-retryable exceptions (auth failures)

  - _Requirements: 8.1, 8.2_

- [ ] 7.3 Apply resilience to client methods
  - Add @CircuitBreaker annotation to API calls
  - Add @Retry annotation to API calls
  - Implement fallback methods
  - _Requirements: 8.1, 8.2, 8.3_

- [x] 8. Implement REST API endpoints

  - Create controller for configuration management
  - Add sync management endpoints
  - Add performance data query endpoints
  - _Requirements: 9.3, 10.5_

- [x] 8.1 Create configuration controller

  - Create `WarcraftLogsConfigController`
  - Implement GET /api/warcraft-logs/config/{guildId}
  - Implement PUT /api/warcraft-logs/config/{guildId}
  - Add validation and error handling
  - _Requirements: 9.3, 9.4_

- [ ] 8.2 Create character mapping endpoints
  - Implement POST /api/warcraft-logs/config/{guildId}/character-mapping
  - Implement DELETE /api/warcraft-logs/config/{guildId}/character-mapping/{id}

  - Add validation
  - _Requirements: 2.3, 9.3_


- [ ] 8.3 Create sync management endpoints
  - Implement POST /api/warcraft-logs/sync/{guildId} for manual sync
  - Implement GET /api/warcraft-logs/sync/{guildId}/status
  - Implement GET /api/warcraft-logs/sync/{guildId}/history
  - _Requirements: 10.5_





- [ ] 8.4 Create performance data endpoints
  - Implement GET /api/warcraft-logs/performance/{guildId}/{characterName}
  - Implement GET /api/warcraft-logs/reports/{guildId}
  - Implement GET /api/warcraft-logs/reports/{reportCode}/fights
  - _Requirements: 10.5_


- [x] 9. Implement monitoring and observability

  - Create metrics collection
  - Add health check indicator
  - Enhance logging
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_



- [ ] 9.1 Create metrics component
  - Create `WarcraftLogsMetrics` component
  - Add counters for sync success/failure
  - Add timer for API latency

  - Add counters for cache hit/miss
  - _Requirements: 10.2_


- [ ] 9.2 Integrate metrics into services
  - Add metric recording to sync service

  - Add metric recording to client
  - Add metric recording to performance service
  - _Requirements: 10.2_


- [ ] 9.3 Create health check indicator
  - Create `WarcraftLogsHealthIndicator`
  - Check last sync time per guild
  - Check next sync time per guild
  - Report integration status
  - _Requirements: 8.4, 10.5_


- [ ] 9.4 Enhance logging
  - Add structured logging with context
  - Log all API calls with timing
  - Log sync results


  - Log errors with actionable guidance
  - _Requirements: 10.1, 10.4_

- [ ] 10. Write unit tests
  - Test configuration service
  - Test credential encryption
  - Test client with mocked responses

  - Test sync service logic
  - Test performance service calculations
  - Test MAS integration
  - _Requirements: All_

- [x] 10.1 Test configuration service

  - Test guild-specific config retrieval
  - Test default value fallback
  - Test config validation
  - Test credential encryption/decryption
  - _Requirements: 9.1, 9.2, 11.1_


- [ ] 10.2 Test API client
  - Mock WebClient responses
  - Test OAuth2 authentication flow
  - Test report fetching with pagination
  - Test fight data parsing
  - Test performance data parsing

  - Test error handling for various HTTP status codes
  - Test rate limit handling
  - _Requirements: 1.4, 3.1, 3.2, 3.3, 4.1, 4.2, 8.1, 8.2_

- [ ] 10.3 Test sync service
  - Mock client responses
  - Test report sync logic
  - Test fight data processing
  - Test performance metric extraction
  - Test spec average calculation
  - Test error handling
  - _Requirements: 3.1, 3.2, 3.3, 4.1, 4.2, 4.3, 4.4, 8.1_


- [ ] 10.4 Test performance service
  - Test MAS calculation with various inputs
  - Test DPA/ADT ratio calculations
  - Test critical threshold handling
  - Test fallback value usage
  - Test time-based weighting
  - Test caching behavior
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 6.1, 6.2, 6.3, 6.4, 7.3_

- [ ] 10.5 Test ScoreCalculator integration
  - Test MAS calculation with Warcraft Logs enabled
  - Test fallback when Warcraft Logs disabled
  - Test fallback when no data available
  - Test integration with existing FLPS calculation
  - _Requirements: 6.1, 6.4_

- [ ] 11. Write integration tests
  - Test database operations
  - Test API client with MockWebServer
  - Test end-to-end sync flow
  - _Requirements: All_

- [ ] 11.1 Test database operations
  - Test entity persistence and retrieval
  - Test custom repository queries
  - Test transaction handling
  - Test index performance
  - _Requirements: 7.1, 7.2_

- [ ] 11.2 Test API client integration
  - Use MockWebServer to simulate Warcraft Logs API
  - Test complete authentication flow
  - Test report fetching with pagination
  - Test fight data retrieval
  - Test error scenarios
  - _Requirements: 1.4, 3.1, 3.2, 3.3, 4.1, 4.2_

- [ ] 11.3 Test end-to-end sync flow
  - Test complete sync from API to database
  - Test performance metric calculation
  - Test MAS integration with ScoreCalculator
  - Test with real-like data volumes
  - _Requirements: All_

- [ ] 12. Create documentation and configuration examples
  - Document configuration properties
  - Create setup guide
  - Add API documentation
  - Create troubleshooting guide
  - _Requirements: 9.5, 10.5_

- [ ] 12.1 Document configuration
  - Document all application.yaml properties
  - Provide guild-specific config examples
  - Document character mapping format
  - Document encryption key setup
  - _Requirements: 1.1, 1.3, 9.1, 9.2, 11.1_

- [ ] 12.2 Create setup guide
  - Document Warcraft Logs API credential setup
  - Document initial guild configuration
  - Document character mapping setup
  - Document testing the integration
  - _Requirements: 1.1, 1.2, 2.1, 2.2, 2.3_

- [ ] 12.3 Add API documentation
  - Document all REST endpoints
  - Add request/response examples
  - Document error responses
  - Consider adding OpenAPI/Swagger spec
  - _Requirements: 9.3, 10.5_

- [ ] 12.4 Create troubleshooting guide
  - Document common issues and solutions
  - Document authentication failures
  - Document rate limiting handling
  - Document data availability issues
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 10.4_

---

## Implementation Notes

### Testing Strategy
- Unit tests are marked as optional (*) to focus on core functionality first
- Integration tests should be run against TestContainers PostgreSQL
- Use MockWebServer for API client testing
- Create comprehensive test data fixtures

### Configuration Priority
All configuration should be:
1. Guild-specific when provided
2. Fall back to system defaults
3. Validated on update
4. Applied without restart

### Error Handling Priority
1. Authentication failures → Disable integration, alert admin
2. Rate limiting → Exponential backoff, respect Retry-After
3. API errors → Log and continue with fallback
4. Data errors → Skip problematic data, continue processing

### Performance Considerations
- Async processing for all sync operations
- Caching for MAS scores and spec averages
- Database indexing for common queries
- Connection pooling for API client
- Batch processing where possible

### Security Considerations
- Encrypt credentials at rest
- Never log credentials
- Use HTTPS for all API calls
- Validate all configuration inputs
- Restrict admin endpoints

---

## Success Criteria

- [ ] Warcraft Logs API authentication working
- [ ] Reports syncing automatically on schedule
- [ ] Performance data extracted and stored
- [ ] MAS calculation using real Warcraft Logs data
- [ ] MAS no longer returns 0.0 for characters with data
- [ ] FLPS scores reflect actual combat performance
- [ ] Configuration manageable via REST API
- [ ] Health checks showing integration status
- [ ] Metrics available for monitoring
- [ ] Documentation complete and accurate
