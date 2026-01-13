# EdgeRush LootMan - Project Requirements

## Problem Statement

World of Warcraft raid guilds struggle with fair, transparent loot distribution. Manual loot councils are subjective and prone to bias, while automated systems (DKP, EPGP) fail to account for modern raid dynamics like performance, attendance patterns, and upgrade value.

EdgeRush LootMan solves this by implementing the FLPS (Final Loot Priority Score) algorithm - a transparent, data-driven approach that balances raider merit, item priority, and recency to ensure fair loot distribution.

## Functional Requirements

### Core FLPS System (100% Complete)
- [x] FR-001: Calculate Raider Merit Score (RMS) from attendance, performance, and behavior
- [x] FR-002: Calculate Item Priority Index (IPI) from upgrade value, role requirements, and tier completion
- [x] FR-003: Calculate Recency Decay Factor (RDF) based on recent loot history
- [x] FR-004: Combine scores: FLPS = (RMS × IPI) × RDF
- [x] FR-005: Support guild-specific weight configuration
- [x] FR-006: Provide transparent, auditable score breakdowns

### Data Integration
- [x] FR-007: Sync character data from WoWAudit API
- [x] FR-008: Sync attendance data from WoWAudit API
- [x] FR-009: Sync loot history from WoWAudit API
- [x] FR-010: Sync performance data from Warcraft Logs API
- [ ] FR-011: Sync simulation data from Raidbots API (BLOCKED - API key)

### API Layer
- [x] FR-012: REST API for FLPS reports
- [x] FR-013: REST API for guild configuration
- [x] FR-014: REST API for Warcraft Logs integration
- [ ] FR-015: Complete CRUD APIs for all 45+ entities (40% complete)
- [ ] FR-016: GraphQL API for flexible queries (planned - Phase 2)

### User Interfaces (Not Started)
- [ ] FR-017: Web dashboard for FLPS transparency
- [ ] FR-018: Admin panel for loot council decisions
- [ ] FR-019: Discord bot for notifications and commands

## Non-Functional Requirements

### Performance
- [x] NFR-001: FLPS calculations complete in < 1ms (achieved: < 0.5ms)
- [x] NFR-002: API response time < 100ms for standard queries (achieved: < 15ms)
- [x] NFR-003: Support 100+ concurrent users
- [x] NFR-004: Database queries < 50ms (achieved: < 15ms)

### Reliability
- [x] NFR-005: 99.9% uptime for core API services
- [x] NFR-006: Graceful degradation when external APIs unavailable
- [x] NFR-007: All data changes auditable
- [ ] NFR-008: Test coverage minimum 85% (current: 64%)

### Security
- [x] NFR-009: JWT-based authentication
- [x] NFR-010: Spring Security integration
- [x] NFR-011: CORS configuration for web clients
- [ ] NFR-012: Rate limiting (configured but disabled)
- [ ] NFR-013: Secrets management for production (needs vault)

### Maintainability
- [x] NFR-014: Domain-driven design architecture
- [x] NFR-015: Comprehensive code documentation
- [x] NFR-016: Database migrations via Flyway
- [ ] NFR-017: Code quality enforcement (ktlint, detekt configured)

## Success Criteria

### Phase 1: Core System (ACHIEVED)
1. FLPS algorithm fully implemented and tested
2. WoWAudit integration syncing all data types
3. Warcraft Logs integration providing MAS scores
4. REST API serving FLPS reports
5. 509 tests passing, 100% pass rate

### Phase 2: Production Ready (IN PROGRESS)
1. Test coverage reaches 85%
2. Complete REST API for all entities
3. GraphQL API implementation
4. Security hardening complete

### Phase 3: User Facing (PLANNED)
1. Web dashboard deployed
2. Discord bot operational
3. RC Loot Council addon integration
