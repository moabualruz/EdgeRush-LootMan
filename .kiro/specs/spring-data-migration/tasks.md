# Implementation Plan: Spring Data Migration

## Overview

This implementation plan converts the `JdbcRaidRepository` from raw JDBC operations to Spring Data repositories, and establishes project-wide standards prohibiting raw JDBC usage.

---

## Tasks

- [x] 1. Extend Spring Data repository interfaces





  - Add custom query methods to existing Spring Data repositories for raid-related operations
  - Create `findByDate()` method in `RaidRepository`
  - Create `findByRaidId()` and `deleteByRaidId()` methods in `RaidEncounterRepository`
  - Create `findByRaidId()` and `deleteByRaidId()` methods in `RaidSignupRepository`
  - _Requirements: 1.1, 1.2, 2.1, 5.1, 5.2, 5.3_

- [x] 2. Rewrite JdbcRaidRepository to use Spring Data





  - [x] 2.1 Replace JdbcTemplate dependency with Spring Data repositories


    - Remove `JdbcTemplate` constructor parameter
    - Add `SpringRaidRepository`, `RaidEncounterRepository`, and `RaidSignupRepository` dependencies
    - Keep `RaidMapper` dependency
    - _Requirements: 1.1, 3.1, 3.2, 7.1_

  - [x] 2.2 Implement findById using Spring Data


    - Replace manual SQL query with `springRaidRepository.findById()`
    - Use `encounterRepository.findByRaidId()` to load encounters
    - Use `signupRepository.findByRaidId()` to load signups
    - Assemble aggregate using mapper
    - _Requirements: 1.2, 2.3, 5.1_

  - [x] 2.3 Implement findByGuildId using Spring Data


    - Replace manual SQL query with `springRaidRepository.findAll()`
    - Load encounters and signups for each raid
    - Map to domain aggregates
    - _Requirements: 1.2, 5.2_

  - [x] 2.4 Implement findByGuildIdAndDate using Spring Data


    - Replace manual SQL query with `springRaidRepository.findByDate()`
    - Load encounters and signups for each raid
    - Map to domain aggregates
    - _Requirements: 1.2, 5.3_

  - [x] 2.5 Implement save using Spring Data


    - Use `springRaidRepository.save()` for raid entity
    - Use `encounterRepository.deleteByRaidId()` and `saveAll()` for encounters
    - Use `signupRepository.deleteByRaidId()` and `saveAll()` for signups
    - Maintain `@Transactional` annotation
    - _Requirements: 1.1, 2.1, 2.2, 7.2_

  - [x] 2.6 Implement delete using Spring Data


    - Replace manual SQL with `springRaidRepository.deleteById()`
    - Verify cascade deletes work via foreign keys
    - _Requirements: 1.1, 2.2_

  - [x] 2.7 Remove all manual SQL and RowMappers


    - Delete all SQL string constants
    - Delete `raidEntityRowMapper`, `encounterEntityRowMapper`, `signupEntityRowMapper`
    - Delete helper methods: `insertRaid()`, `updateRaid()`, `insertEncounter()`, `insertSignup()`, etc.
    - _Requirements: 1.1, 1.3, 7.1, 7.2, 7.3_

- [x] 3. Update repository tests





  - [x] 3.1 Update test setup to use Spring Data repositories


    - Replace `JdbcTemplate` injection with Spring Data repositories
    - Update cleanup methods to use `deleteAll()` instead of manual SQL
    - _Requirements: 4.1, 4.3_

  - [x] 3.2 Update test verification to use Spring Data


    - Replace `jdbcTemplate.queryForObject()` with repository methods
    - Update assertions to work with Spring Data results
    - _Requirements: 4.1, 4.4_

  - [x] 3.3 Verify all 11 existing tests pass


    - Run test suite and ensure 100% pass rate
    - Verify test coverage remains at 100%
    - _Requirements: 4.1, 4.2, 4.5_

- [x] 4. Update project documentation standards




  - [x] 4.1 Add database access standards to AI_DEVELOPMENT_STANDARDS.md


    - Add "Database Access Pattern" section
    - Document requirement to use Spring Data repositories
    - Document prohibition of raw JDBC operations
    - Add code examples showing correct and incorrect patterns
    - Note exception for database migrations
    - _Requirements: 1.1, 7.4_


  - [x] 4.2 Add database access standards to docs/code-standards.md

    - Add "Database Access" section
    - Document Spring Data repository requirement
    - Document raw JDBC prohibition
    - Add rationale and examples
    - _Requirements: 1.1, 7.4_


  - [x] 4.3 Update docs/testing-standards.md with repository testing guidelines

    - Add "Repository Tests" section
    - Document use of `@DataJdbcTest` annotation
    - Document test isolation with `@Transactional`
    - Add example repository test
    - Document prohibition of raw JDBC in tests
    - _Requirements: 4.1, 4.3_

- [ ] 5. Verification and cleanup
  - [ ] 5.1 Run full test suite
    - Execute `./gradlew test` to run all tests
    - Verify 100% pass rate
    - Check test coverage reports
    - _Requirements: 4.1, 4.2_

  - [ ] 5.2 Verify code metrics
    - Confirm JdbcRaidRepository reduced from ~310 to ~80 lines (74% reduction)
    - Verify no manual SQL strings remain in repository code
    - Verify no RowMapper implementations remain
    - _Requirements: 7.3, 7.4, 7.5_

  - [ ] 5.3 Verify consistency with other repositories
    - Compare JdbcRaidRepository structure with other 9 repositories
    - Ensure adapter pattern is consistent
    - Verify naming conventions match
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

  - [ ] 5.4 Performance benchmarking
    - Create benchmark tests for findById, findByGuildId, and save operations
    - Compare performance before and after migration
    - Verify no performance degradation
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

  - [ ] 5.5 Update project status documentation
    - Update relevant project status files to reflect migration completion
    - Document the new standard in project README or quick reference
    - _Requirements: 7.5_

---

## Notes

- Tasks should be executed in order as they have dependencies
- All tests must pass before moving to the next major task
- The migration is focused on a single repository, making it low-risk
- Performance benchmarking (5.4) is required to ensure no degradation
- Documentation updates (task 4) can be done in parallel with code changes
