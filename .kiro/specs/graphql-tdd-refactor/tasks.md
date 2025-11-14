# Implementation Plan

## Phase 1: Foundation and Testing Infrastructure

- [x] 1. Set up testing infrastructure and tools





  - Configure JUnit 5, MockK, and Testcontainers in build.gradle.kts
  - Configure JaCoCo with 85% coverage threshold and fail build on violation
  - Set up ktlint plugin with formatting rules
  - Set up detekt plugin with code quality rules
  - Create test database configuration for Testcontainers



  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 10.1, 10.2, 10.3, 10.4, 10.5_

- [x] 2. Create base test classes and utilities


  - Create UnitTest abstract base class with MockK setup
  - Create IntegrationTest abstract base class with Testcontainers
  - Create test data builders for common entities (Raider, LootAward, Raid)
  - Create test utilities for database cleanup and setup
  - Write examples demonstrating test patterns
  - _Requirements: 2.1, 2.2, 2.3, 3.2, 3.3, 3.4_

- [x] 3. Write testing standards documentation

  - Create docs/testing-standards.md with TDD workflow
  - Document test naming conventions (should_ExpectedBehavior_When_StateUnderTest)
  - Document test organization patterns (AAA: Arrange-Act-Assert)
  - Document mocking guidelines and best practices
  - Provide examples for unit, integration, and E2E tests
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [x] 4. Write code standards documentation

  - Create docs/code-standards.md with Kotlin conventions
  - Document package organization rules (domain, application, infrastructure)
  - Document naming conventions for classes, functions, variables
  - Document error handling patterns and domain exceptions
  - Document DDD patterns (Value Objects, Entities, Aggregates)
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

- [x] 5. Create target package structure



  - Create new package structure: api/, application/, domain/, infrastructure/
  - Create bounded context packages: flps/, loot/, attendance/, raids/
  - Create package-info.kt files documenting each package purpose
  - Create README.md files for major packages
  - Set up folder structure for test organization (unit/, integration/, e2e/)
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 7.1, 7.2, 7.3, 7.4, 7.5_

## Phase 2: FLPS Bounded Context Refactoring



- [x] 6. Create FLPS domain layer with TDD


  - Write tests for FlpsScore value object (validation, operations)
  - Implement FlpsScore value object with immutability
  - Write tests for RaiderMeritScore, ItemPriorityIndex, RecencyDecayFactor
  - Implement remaining value objects
  - Write tests for FlpsCalculationService domain logic
  - Implement FlpsCalculationService with FLPS algorithm
  - _Requirements: 1.1, 1.2, 1.3, 5.1, 5.2, 5.3, 5.4, 5.5_

- [x] 7. Create FLPS application layer with TDD



  - Write tests for CalculateFlpsScoreUseCase
  - Implement CalculateFlpsScoreUseCase orchestrating domain services
  - Write tests for UpdateModifiersUseCase
  - Implement UpdateModifiersUseCase with validation
  - Write tests for GetFlpsReportUseCase
  - Implement GetFlpsReportUseCase with data aggregation
  - _Requirements: 1.1, 1.2, 1.3, 5.1, 5.2_

- [x] 8. Create FLPS infrastructure layer with TDD



  - Write tests for FlpsModifierRepository implementation
  - Implement JdbcFlpsModifierRepository with CRUD operations
  - Write tests for entity-to-domain mappers
  - Implement FlpsModifierMapper for entity conversion
  - Write integration tests for repository with Testcontainers
  - Verify database queries and transactions work correctly
  - _Requirements: 2.2, 5.4, 5.5_


- [x] 9. Update FLPS API layer with TDD






  - Write integration tests for FlpsController endpoints
  - Refactor FlpsController to use new use cases
  - Update DTOs to match new domain models
  - Write API contract tests verifying request/response formats
  - Verify backward compatibility with existing endpoints
  - _Requirements: 1.1, 1.2, 1.3, 6.5, 8.1, 8.2, 8.3, 8.4_

- [x] 10. Verify FLPS bounded context completion







  - Run full test suite and verify all tests pass
  - Generate coverage report and verify ≥85% coverage
  - Run ktlint and detekt, fix any violations
  - Run performance tests comparing old vs new implementation
  - Update documentation with FLPS architecture
  - _Requirements: 2.4, 2.5, 8.5, 9.1, 9.2, 9.3, 9.4, 9.5, 10.3, 10.4_

## Phase 3: Loot Bounded Context Refactoring

- [x] 11. Create Loot domain layer with TDD








  - Write tests for LootAward aggregate root (creation, state transitions)
  - Implement LootAward aggregate with business rules
  - Write tests for LootBan entity (validation, expiration logic)
  - Implement LootBan entity with lifecycle methods
  - Write tests for LootDistributionService domain logic
  - Implement LootDistributionService with loot award rules
  - _Requirements: 1.1, 1.2, 1.3, 5.1, 5.2, 5.3, 5.4, 5.5_

- [x] 12. Create Loot application layer with TDD






  - Write tests for AwardLootUseCase (happy path and error cases)
  - Implement AwardLootUseCase with validation and orchestration
  - Write tests for ManageLootBansUseCase
  - Implement ManageLootBansUseCase with ban creation/expiration
  - Write tests for GetLootHistoryUseCase
  - Implement GetLootHistoryUseCase with filtering and pagination
  - _Requirements: 1.1, 1.2, 1.3, 5.1, 5.2_

- [x] 13. Create Loot infrastructure layer with TDD





  - Write tests for LootAwardRepository implementation
  - Implement JdbcLootAwardRepository with optimized queries
  - Write tests for LootBanRepository implementation
  - Implement JdbcLootBanRepository with active ban queries
  - Write tests for entity mappers
  - Implement mappers for LootAward and LootBan entities
  - _Requirements: 2.2, 5.4, 5.5_

- [x] 14. Update Loot API layer with TDD





  - Write integration tests for LootController endpoints
  - Refactor LootController to use new use cases
  - Write integration tests for LootAwardController
  - Refactor LootAwardController with new domain models
  - Verify backward compatibility with existing endpoints
  - _Requirements: 1.1, 1.2, 1.3, 6.5, 8.1, 8.2, 8.3, 8.4_

- [x] 15. Verify Loot bounded context completion






  - Run full test suite and verify all tests pass
  - Generate coverage report and verify ≥85% coverage
  - Run code quality checks (ktlint, detekt)
  - Run performance tests for loot queries
  - Update documentation with Loot architecture
  - _Requirements: 2.4, 2.5, 8.5, 9.1, 9.2, 9.3, 9.4, 9.5, 10.3, 10.4_

## Phase 4: Attendance Bounded Context Refactoring

- [ ] 16. Create Attendance domain layer with TDD


  - Write tests for AttendanceRecord entity
  - Implement AttendanceRecord with validation
  - Write tests for AttendanceStats value object
  - Implement AttendanceStats with calculation methods
  - Write tests for AttendanceCalculationService
  - Implement AttendanceCalculationService with attendance logic
  - _Requirements: 1.1, 1.2, 1.3, 5.1, 5.2, 5.3, 5.4, 5.5_

- [ ] 17. Create Attendance application layer with TDD


  - Write tests for TrackAttendanceUseCase
  - Implement TrackAttendanceUseCase with raid attendance tracking
  - Write tests for GetAttendanceReportUseCase
  - Implement GetAttendanceReportUseCase with aggregation
  - _Requirements: 1.1, 1.2, 1.3, 5.1, 5.2_


- [x] 18. Create Attendance infrastructure layer with TDD




  - Write tests for AttendanceRepository implementation
  - Implement JdbcAttendanceRepository with date range queries
  - Write tests for entity mappers
  - Implement AttendanceMapper for entity conversion
  - _Requirements: 2.2, 5.4, 5.5_


- [ ] 19. Update Attendance API layer with TDD
  - Write integration tests for AttendanceController
  - Refactor AttendanceController to use new use cases
  - Verify backward compatibility
  - _Requirements: 1.1, 1.2, 1.3, 6.5, 8.1, 8.2, 8.3, 8.4_

- [ ] 20. Verify Attendance bounded context completion


  - Run full test suite and verify all tests pass
  - Generate coverage report and verify ≥85% coverage
  - Run code quality checks
  - Update documentation
  - _Requirements: 2.4, 2.5, 8.5, 9.1, 9.2, 9.3, 9.4, 9.5, 10.3, 10.4_

## Phase 5: Raids Bounded Context Refactoring

- [ ] 21. Create Raids domain layer with TDD


  - Write tests for Raid aggregate root (scheduling, state transitions)
  - Implement Raid aggregate with business rules
  - Write tests for RaidEncounter entity
  - Implement RaidEncounter with encounter logic
  - Write tests for RaidSignup entity
  - Implement RaidSignup with signup validation
  - Write tests for RaidSchedulingService
  - Implement RaidSchedulingService with scheduling logic
  - _Requirements: 1.1, 1.2, 1.3, 5.1, 5.2, 5.3, 5.4, 5.5_


- [ ] 22. Create Raids application layer with TDD
  - Write tests for ScheduleRaidUseCase
  - Implement ScheduleRaidUseCase with validation
  - Write tests for ManageSignupsUseCase
  - Implement ManageSignupsUseCase with signup logic
  - Write tests for RecordRaidResultsUseCase
  - Implement RecordRaidResultsUseCase with result recording
  - _Requirements: 1.1, 1.2, 1.3, 5.1, 5.2_

- [ ] 23. Create Raids infrastructure layer with TDD


  - Write tests for RaidRepository implementation
  - Implement JdbcRaidRepository with complex queries
  - Write tests for entity mappers
  - Implement mappers for Raid, RaidEncounter, RaidSignup
  - _Requirements: 2.2, 5.4, 5.5_


- [ ] 24. Update Raids API layer with TDD
  - Write integration tests for RaidController
  - Refactor RaidController to use new use cases
  - Verify backward compatibility
  - _Requirements: 1.1, 1.2, 1.3, 6.5, 8.1, 8.2, 8.3, 8.4_


- [ ] 25. Verify Raids bounded context completion
  - Run full test suite and verify all tests pass
  - Generate coverage report and verify ≥85% coverage
  - Run code quality checks
  - Update documentation
  - _Requirements: 2.4, 2.5, 8.5, 9.1, 9.2, 9.3, 9.4, 9.5, 10.3, 10.4_

## Phase 6: Remaining Bounded Contexts


- [ ] 26. Refactor Applications bounded context
  - Create domain layer for guild applications
  - Create application layer with use cases
  - Create infrastructure layer with repositories
  - Update API layer
  - Write comprehensive tests
  - _Requirements: 1.1, 1.2, 1.3, 5.1, 5.2, 5.3, 5.4, 5.5_


- [ ] 27. Refactor Integrations bounded context
  - Create domain layer for external integrations
  - Create application layer for sync use cases
  - Create infrastructure layer for API clients
  - Update API layer
  - Write comprehensive tests
  - _Requirements: 1.1, 1.2, 1.3, 5.1, 5.2, 5.3, 5.4, 5.5_


- [ ] 28. Refactor Shared context
  - Create domain layer for Raider and Guild entities
  - Create application layer for common use cases
  - Create infrastructure layer for shared repositories
  - Update API layer
  - Write comprehensive tests
  - _Requirements: 1.1, 1.2, 1.3, 5.1, 5.2, 5.3, 5.4, 5.5_

## Phase 7: Cleanup and Optimization

- [ ] 29. Remove deprecated code


  - Identify and mark old service layer as deprecated
  - Remove unused entity classes
  - Clean up old mapper implementations
  - Remove duplicate code
  - Update imports across codebase
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_


- [ ] 30. Optimize database queries
  - Add database indexes for frequently queried fields
  - Optimize N+1 query problems with joins
  - Add query result caching where appropriate
  - Write performance tests for critical queries
  - Verify query performance meets benchmarks
  - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_

- [ ] 31. Final verification and documentation


  - Run complete test suite across all bounded contexts
  - Generate final coverage report (verify ≥85%)
  - Run all code quality checks (ktlint, detekt)
  - Run performance test suite
  - Update all documentation (architecture, API, testing)
  - Create Architecture Decision Records (ADRs)
  - _Requirements: 2.4, 2.5, 3.5, 4.5, 8.5, 9.5, 10.3, 10.4, 10.5_


- [ ] 32. Team training and handoff


  - Conduct training session on new architecture
  - Review testing standards with team
  - Review code standards with team
  - Demonstrate TDD workflow
  - Answer questions and provide support
  - _Requirements: 3.1, 3.5, 4.1, 4.5_

