# Requirements Document

## Introduction

This specification defines the post-refactoring cleanup and verification work needed after completing the TDD standards and domain-driven design refactoring of the EdgeRush LootMan codebase. The refactoring successfully reorganized the codebase into bounded contexts with clean architecture, but left some integration tests failing and potential unused code. This spec addresses three critical concerns:

1. **API Status Verification**: Confirm REST API functionality and clarify GraphQL implementation status
2. **Functionality Gap Analysis**: Verify no features were lost during refactoring
3. **Cleanup and Optimization**: Remove unused code, fix failing tests, and address code quality issues

## Glossary

- **REST_API**: The HTTP REST endpoints providing CRUD operations and business logic access
- **GraphQL_API**: The planned GraphQL layer (Phase 2 of original spec, not yet implemented)
- **Integration_Tests**: Tests that verify API endpoints with database and external dependencies
- **Unit_Tests**: Tests that verify business logic in isolation with mocked dependencies
- **Bounded_Context**: A logical domain boundary (FLPS, Loot, Attendance, Raids, Applications, Integrations, Shared)
- **Code_Quality**: Automated checks including detekt (static analysis) and ktlint (formatting)
- **Unused_Code**: Files, classes, or packages no longer referenced after refactoring
- **Test_Coverage**: Percentage of code executed by automated tests (target: 85%)
- **Build_System**: Gradle build configuration and compilation process

## Requirements

### Requirement 1: REST API Verification

**User Story:** As a product manager, I want to verify all REST API endpoints are functional, so that I can confirm the refactoring maintained backward compatibility.

#### Acceptance Criteria

1. WHEN THE REST_API is audited, THE REST_API SHALL document all available endpoints across both datasync and lootman packages
2. WHEN THE REST_API endpoints are tested, THE REST_API SHALL verify all CRUD operations work correctly
3. WHEN THE REST_API is compared to pre-refactoring, THE REST_API SHALL confirm no endpoints were removed or broken
4. WHEN THE REST_API documentation is updated, THE REST_API SHALL include endpoint paths, HTTP methods, request/response formats
5. WHEN THE REST_API is verified, THE REST_API SHALL confirm OpenAPI/Swagger documentation is accurate

### Requirement 2: GraphQL Implementation Status

**User Story:** As a technical lead, I want to understand GraphQL implementation status, so that I can plan future development work.

#### Acceptance Criteria

1. WHEN THE GraphQL_API status is checked, THE GraphQL_API SHALL confirm whether any GraphQL code exists in the codebase
2. WHEN THE GraphQL_API is documented, THE GraphQL_API SHALL clarify that Phase 2 (GraphQL) was not implemented
3. WHEN THE GraphQL_API plan is reviewed, THE GraphQL_API SHALL reference the original spec requirements for future implementation
4. WHEN THE GraphQL_API dependencies are checked, THE GraphQL_API SHALL verify no GraphQL libraries are included in build.gradle.kts
5. WHEN THE GraphQL_API is discussed, THE GraphQL_API SHALL confirm REST API is the only current implementation

### Requirement 3: Functionality Gap Analysis

**User Story:** As a QA engineer, I want to verify no functionality was lost during refactoring, so that I can ensure feature parity.

#### Acceptance Criteria

1. WHEN THE functionality is audited, THE Build_System SHALL compare pre-refactoring and post-refactoring feature lists
2. WHEN THE functionality is verified, THE Build_System SHALL confirm all FLPS calculation features work correctly
3. WHEN THE functionality is verified, THE Build_System SHALL confirm all loot distribution features work correctly
4. WHEN THE functionality is verified, THE Build_System SHALL confirm all attendance tracking features work correctly
5. WHEN THE functionality is verified, THE Build_System SHALL confirm all raid management features work correctly
6. WHEN THE functionality is verified, THE Build_System SHALL confirm all guild application features work correctly
7. WHEN THE functionality is verified, THE Build_System SHALL confirm all external integration features (WoWAudit, WarcraftLogs) work correctly

### Requirement 4: Fix Failing Integration Tests

**User Story:** As a developer, I want all integration tests passing, so that I can trust the test suite for future development.

#### Acceptance Criteria

1. WHEN THE Integration_Tests are analyzed, THE Integration_Tests SHALL identify root causes for all 59 failing tests
2. WHEN THE Integration_Tests are fixed, THE Integration_Tests SHALL resolve datasync API controller failures (33 tests)
3. WHEN THE Integration_Tests are fixed, THE Integration_Tests SHALL resolve lootman API controller failures (26 tests)
4. WHEN THE Integration_Tests are fixed, THE Integration_Tests SHALL verify database schema matches entity expectations
5. WHEN THE Integration_Tests are fixed, THE Integration_Tests SHALL verify repository implementations work correctly
6. WHEN THE Integration_Tests are fixed, THE Integration_Tests SHALL verify use case orchestration works end-to-end
7. WHEN THE Integration_Tests run, THE Integration_Tests SHALL achieve 100% pass rate

### Requirement 5: Address Code Quality Issues

**User Story:** As a code reviewer, I want code quality violations fixed, so that the codebase meets established standards.

#### Acceptance Criteria

1. WHEN THE Code_Quality checks run, THE Code_Quality SHALL identify all detekt violations (currently 1251 issues)
2. WHEN THE Code_Quality issues are fixed, THE Code_Quality SHALL remove trailing whitespace violations
3. WHEN THE Code_Quality issues are fixed, THE Code_Quality SHALL replace wildcard imports with explicit imports
4. WHEN THE Code_Quality issues are fixed, THE Code_Quality SHALL extract magic numbers to named constants
5. WHEN THE Code_Quality checks run, THE Code_Quality SHALL achieve zero critical violations
6. WHEN THE Code_Quality checks run, THE Code_Quality SHALL pass ktlint formatting checks
7. WHEN THE Code_Quality checks run, THE Code_Quality SHALL pass detekt static analysis checks

### Requirement 6: Remove Unused Code

**User Story:** As a maintainer, I want unused code removed, so that the codebase is clean and maintainable.

#### Acceptance Criteria

1. WHEN THE Unused_Code is identified, THE Unused_Code SHALL find all unreferenced classes and files
2. WHEN THE Unused_Code is identified, THE Unused_Code SHALL find all empty packages and directories
3. WHEN THE Unused_Code is removed, THE Unused_Code SHALL delete unreferenced entity classes
4. WHEN THE Unused_Code is removed, THE Unused_Code SHALL delete unreferenced repository interfaces
5. WHEN THE Unused_Code is removed, THE Unused_Code SHALL delete unreferenced mapper classes
6. WHEN THE Unused_Code is removed, THE Unused_Code SHALL delete empty configuration files
7. WHEN THE Unused_Code is removed, THE Build_System SHALL verify compilation still succeeds

### Requirement 7: Verify Test Coverage

**User Story:** As a quality engineer, I want to verify test coverage meets standards, so that I can ensure code quality.

#### Acceptance Criteria

1. WHEN THE Test_Coverage is measured, THE Test_Coverage SHALL generate JaCoCo coverage reports
2. WHEN THE Test_Coverage is analyzed, THE Test_Coverage SHALL verify overall coverage is ≥85%
3. WHEN THE Test_Coverage is analyzed, THE Test_Coverage SHALL verify domain layer coverage is ≥90%
4. WHEN THE Test_Coverage is analyzed, THE Test_Coverage SHALL verify application layer coverage is ≥85%
5. WHEN THE Test_Coverage is analyzed, THE Test_Coverage SHALL identify any uncovered critical paths
6. WHEN THE Test_Coverage is insufficient, THE Test_Coverage SHALL add tests to reach threshold
7. WHEN THE Test_Coverage meets standards, THE Build_System SHALL pass JaCoCo verification

### Requirement 8: Update Documentation

**User Story:** As a new developer, I want accurate documentation, so that I can understand the current system state.

#### Acceptance Criteria

1. WHEN THE documentation is updated, THE documentation SHALL reflect the new domain-driven architecture
2. WHEN THE documentation is updated, THE documentation SHALL document all REST API endpoints
3. WHEN THE documentation is updated, THE documentation SHALL clarify GraphQL is not yet implemented
4. WHEN THE documentation is updated, THE documentation SHALL update CODE_ARCHITECTURE.md with bounded context structure
5. WHEN THE documentation is updated, THE documentation SHALL update API_REFERENCE.md with current endpoints
6. WHEN THE documentation is updated, THE documentation SHALL update PROJECT_STATUS.md with refactoring completion
7. WHEN THE documentation is updated, THE documentation SHALL create migration guide for developers

### Requirement 9: Verify Database Migrations

**User Story:** As a database administrator, I want to verify database schema is correct, so that the application works with the database.

#### Acceptance Criteria

1. WHEN THE database migrations are verified, THE database migrations SHALL confirm all Flyway migrations applied successfully
2. WHEN THE database schema is checked, THE database schema SHALL verify all entity tables exist
3. WHEN THE database schema is checked, THE database schema SHALL verify all foreign key relationships are correct
4. WHEN THE database schema is checked, THE database schema SHALL verify all indexes are in place
5. WHEN THE database schema is checked, THE database schema SHALL verify no orphaned tables exist from old CRUD system
6. WHEN THE database migrations are tested, THE database migrations SHALL verify clean database initialization works
7. WHEN THE database migrations are tested, THE database migrations SHALL verify migration rollback works if needed

### Requirement 10: Performance Verification

**User Story:** As a performance engineer, I want to verify the refactored code performs well, so that users have a good experience.

#### Acceptance Criteria

1. WHEN THE performance is tested, THE performance SHALL verify FLPS calculations complete in <1 second for 30 raiders
2. WHEN THE performance is tested, THE performance SHALL verify loot history queries complete in <500ms for 1000 records
3. WHEN THE performance is tested, THE performance SHALL verify attendance reports complete in <500ms for 90-day range
4. WHEN THE performance is tested, THE performance SHALL verify raid scheduling operations complete in <200ms
5. WHEN THE performance is tested, THE performance SHALL compare pre-refactoring and post-refactoring performance
6. WHEN THE performance degrades, THE performance SHALL identify and optimize slow queries
7. WHEN THE performance meets standards, THE performance SHALL document baseline metrics
