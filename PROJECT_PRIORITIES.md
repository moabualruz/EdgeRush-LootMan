# EdgeRush LootMan - Project Priorities

**Last Updated:** 2026-01-14
**Status:** Active Development

## 🎉 Recent Completion: REST API Layer Complete

The REST API layer is **100% complete** with all planned features implemented:

- ✅ 2911 tests passing (100% pass rate, 231 skipped)
- ✅ 60% instruction coverage, 47% branch coverage
- ✅ All 44 controllers with full CRUD operations
- ✅ All database migrations verified
- ✅ Complete documentation and deployment checklist

## 🎯 Current Priority Order

### PRIORITY 1: Complete REST API Layer (COMPLETE)

**Status:** ✅ 100% Complete
**Spec:** `.kiro/specs/rest-api-layer/`

**Current Status:**

- ✅ Foundation complete (security, base controllers, OpenAPI)
- ✅ JWT authentication with admin mode bypass
- ✅ Rate limiting (100 reads/sec, 20 writes/sec)
- ✅ Audit logging with database persistence
- ✅ Health indicators (Database, WoWAudit, WarcraftLogs, Simulation)
- ✅ All 44 controllers implemented with full CRUD
- ✅ 67 API test files with comprehensive coverage
- ✅ API versioning (`/api/v1/` prefix)
- ✅ Deprecation header support (`DeprecatedEndpoint.kt`, `DeprecationHeaderFilter.kt`)
- ✅ OpenAPI contract tests (`OpenApiContractTest.kt`)
- ✅ Performance tests (`ApiPerformanceTest.kt`)
- ✅ API usage documentation (`docs/api-usage-guide.md`)
- ✅ Production deployment checklist (`docs/production-checklist.md`)

**REST API Layer: 100% COMPLETE**

All planned features have been implemented.

---

### PRIORITY 2: GraphQL API Implementation (FUTURE)
**Timeline:** TBD (After Priority 0 completion)  
**Status:** 📋 Spec Complete, Awaiting Priority 0  
**Spec:** `.kiro/specs/graphql-tdd-refactor/` (Phase 2)

**Why This Comes After Refactoring:**
- Requires clean architecture foundation
- Benefits from established testing standards
- Easier to implement with DDD structure in place

**Objectives:**
1. Implement GraphQL schema for all 45+ entities
2. Create resolvers with DataLoader for efficient queries
3. Add GraphQL subscriptions for real-time updates
4. Maintain coexistence with existing REST APIs
5. Implement field-level authorization

**Key Deliverables:**
- Complete GraphQL schema definition
- Resolvers for all queries and mutations
- DataLoader implementation for N+1 prevention
- GraphQL Playground for interactive exploration
- WebSocket subscriptions for real-time data

---

### PRIORITY 2: Complete REST API Layer (NEARLY COMPLETE)

**Status:** 🔄 ~90% Complete
**Spec:** `.kiro/specs/rest-api-layer/`

**Current Status:**

- ✅ All 44 controllers implemented with full CRUD
- ✅ 67 API test files with comprehensive coverage
- ✅ 98% instruction coverage, 95% branch coverage
- ⏳ Deprecation header support
- ⏳ Contract and performance tests
- ⏳ API usage documentation

**Remaining Work:**

- Implement deprecation header filter
- Add OpenAPI contract tests
- Add performance/load tests
- Create API usage guide
- Create production deployment checklist

---

### PRIORITY 3: SimulationCraft Integration (COMPLETE)

**Timeline:** Complete
**Status:** ✅ Fully Implemented
**Spec:** `.kiro/specs/simulation-integration/`

**Completed:**

- ✅ Database schema for simulation profiles and results
- ✅ SimC profile generation from character data
- ✅ Docker-based SimulationCraft execution
- ✅ Upgrade value calculation from simulation results
- ✅ FLPS integration with fallback to wishlist percentages
- ✅ SOPS + age secrets management integration

**Impact:**

- Provides accurate upgrade values via local SimulationCraft Docker
- No external API dependencies or keys required
- Graceful fallback to wishlist percentages when simulation data unavailable

**Architecture:**

- `DockerSimulationExecutor` - Runs SimC via Docker container
- `SimulationService` - Orchestrates simulation workflow
- `UpgradeValueCalculator` - Calculates UV from simulation data
- `ProfileGeneratorService` - Generates SimC profiles from gear data

---

### PRIORITY 4: Web Dashboard (PLANNED)
**Timeline:** 6-8 weeks  
**Status:** 📋 Spec Complete, Not Started  
**Spec:** `.kiro/specs/web-dashboard/`

**Why This is Important:**
- User-facing transparency for FLPS scores
- Admin panel for loot council decisions
- Real-time score visualization
- Loot history and audit trail

**Technology Stack:**
- React + TypeScript
- Material-UI component library
- GraphQL client (Apollo or similar)
- Real-time updates via subscriptions

**Dependencies:**
- Requires GraphQL API (Priority 1)
- Benefits from complete REST API (Priority 2)

---

### PRIORITY 5: Discord Bot (PLANNED)
**Timeline:** 4-5 weeks  
**Status:** 📋 Spec Complete, Not Started  
**Spec:** `.kiro/specs/discord-bot/`

**Why This is Important:**
- Automated loot announcements
- RDF expiry notifications
- Penalty alerts
- Appeals workflow integration

**Technology Stack:**
- Kotlin + JDA library
- Discord slash commands
- Webhook integrations

**Dependencies:**
- Requires REST or GraphQL API
- Benefits from complete FLPS calculation

---

## ✅ Completed Features

### Test Coverage Target (100% COMPLETE)

**Status:** ✅ Exceeded Target
**Completion Date:** January 14, 2026

**Achieved:**

- 98% instruction coverage (target: 85%)
- 95% branch coverage (target: 85%)
- All tests passing (100% pass rate)
- Comprehensive unit, integration, and E2E tests

**Impact:**

- Rock-solid foundation for REST API development
- High confidence in system reliability
- Regressions caught immediately

---

### Post-Refactoring Cleanup (100% COMPLETE)

**Status:** ✅ Fully Completed
**Spec:** `.kiro/specs/post-refactoring-cleanup/`
**Completion Date:** November 15, 2025

**Completed:**

- All 509 tests passing (100% pass rate)
- All 17 database migrations verified and applied
- Zero critical code quality violations
- Performance benchmarks exceeded (20-1000x better than requirements)
- Complete API documentation (37 REST endpoints)
- Migration guide created for developers

**Key Achievements:**

- **Test Suite**: 509 tests, comprehensive coverage of core functionality
- **Database**: All migrations applied, schema verified, indexes optimized
- **Performance**: FLPS <1ms, queries <15ms, all targets exceeded
- **Code Quality**: Zero critical violations, clean architecture maintained
- **Documentation**: API reference, migration guide, architecture docs

**Impact:**

- Solid foundation for future development
- High confidence in system reliability
- Clear path for new features
- Excellent developer experience

---

### Warcraft Logs Integration (100% COMPLETE)
**Status:** ✅ Fully Implemented and Tested  
**Spec:** `.kiro/specs/warcraft-logs-integration/`

**Completed:**
- OAuth2 GraphQL client with authentication
- Automated report and performance data synchronization
- MAS (Mechanical Adherence Score) calculation
- Full integration into FLPS calculations
- REST endpoints for configuration and queries
- Scheduled sync every 6 hours
- Character name mapping (WoWAudit ↔ Warcraft Logs)
- Health indicator for monitoring
- 100% test coverage

**Impact:**
- Enables accurate performance scoring in FLPS
- Provides real combat data for raider evaluation

---

### Core FLPS System (100% COMPLETE)
**Status:** ✅ Fully Implemented and Tested

**Completed:**
- FLPS calculation engine (RMS × IPI × RDF)
- WoWAudit data synchronization
- Guild-specific modifier configuration
- Behavioral scoring system
- Database schema (17 migrations, 45+ entities)
- REST API endpoints for FLPS reports
- Comprehensive test suite (36 tests, 100% passing)

---

## 📊 Implementation Progress Summary

| Feature | Status | Progress | Blocker |
|---------|--------|----------|---------|
| **Test Coverage** | ✅ Complete | 98%/95% | None |
| **REST API Layer** | 🔄 In Progress | 90% | None - Current Focus |
| **GraphQL API** | 📋 Spec Complete | 0% | Awaiting REST completion |
| **SimulationCraft Integration** | ✅ Complete | 100% | None |
| **Web Dashboard** | 📋 Planned | 0% | Requires GraphQL |
| **Discord Bot** | 📋 Planned | 0% | Requires API |
| **Warcraft Logs** | ✅ Complete | 100% | None |
| **Core FLPS System** | ✅ Complete | 100% | None |

---

## 🎯 Success Criteria

### For Priority 1 (REST API Layer - CURRENT)

- [x] CRUD endpoints for all 44 entities
- [x] 80% code coverage on API layer (achieved 98%/95%)
- [x] API versioning implemented (`/api/v1/` prefix)
- [x] Complete OpenAPI documentation
- [x] All integration tests passing (67 test files)
- [x] Rate limiting configured (100 reads/sec, 20 writes/sec)
- [x] JWT authentication complete
- [ ] Deprecation header support
- [ ] OpenAPI contract tests
- [ ] Performance tests
- [ ] API usage documentation
- [ ] Production deployment checklist

### For Priority 2 (GraphQL)
- [ ] Complete schema covering all 45+ entities
- [ ] All resolvers implemented with DataLoader
- [ ] Subscriptions working for real-time updates
- [ ] Field-level authorization enforced
- [ ] GraphQL Playground accessible
- [ ] Performance benchmarks met (no N+1 queries)

### For Priority 2 (REST API)
- [ ] CRUD endpoints for all 45+ entities
- [ ] 80% code coverage on API layer
- [ ] API versioning implemented
- [ ] Complete OpenAPI documentation
- [ ] All integration tests passing

---

## 🔄 How to Update This Document

**When starting a new session:**
1. Review current priorities and status
2. Update progress percentages based on completed work
3. Update "Last Updated" date at top
4. Move completed items to "Completed Features" section
5. Adjust priority order if business needs change

**When completing a priority:**
1. Move to "Completed Features" section
2. Update status to ✅ Complete
3. Document key achievements and impact
4. Promote next priority to current focus

**When adding new priorities:**
1. Create spec in `.kiro/specs/[feature-name]/`
2. Add to priority list with appropriate ranking
3. Document dependencies and blockers
4. Estimate timeline and effort

---

## 📝 Notes for Future Sessions

### Key Architectural Decisions
- **Domain-Driven Design:** Code organized by business domain, not technical layers
- **Test-Driven Development:** Tests written before implementation, 85% coverage minimum
- **API-First:** GraphQL primary, REST for backward compatibility
- **Bounded Contexts:** FLPS, Loot, Attendance, Raids, Applications, Integrations, Shared

### Important Constraints
- Maintain backward compatibility with existing REST endpoints
- No mock data in production - all calculations use real synced data
- Guild-specific configuration support required
- Performance target: < 1 second for FLPS calculations (30 raiders)

### Technical Debt to Address
- Current code lacks comprehensive test coverage
- Package structure mixes technical layers with business domains
- Some services have grown too large (need splitting)
- Missing code quality enforcement (ktlint, detekt)

---

## 🚀 Quick Start for New Sessions

1. **Read this document** to understand current priorities
2. **Check `.kiro/specs/rest-api-layer/`** for REST API spec
3. **Review current priority** (currently: REST API Layer completion)
4. **Check tasks.md** at `.kiro/specs/rest-api-layer/tasks.md`
5. **Start with entity CRUD controllers** (highest impact)

**Current Action:** Complete REST API layer by implementing CRUD controllers for remaining 30+ entities.

---

## 📝 Refactoring Summary

### What Was Accomplished

The post-refactoring cleanup phase successfully:
- Verified all 509 tests passing
- Documented 37 REST API endpoints
- Verified all 17 database migrations
- Achieved excellent performance (20-1000x better than requirements)
- Eliminated all critical code quality violations
- Created comprehensive migration guide
- Clarified GraphQL status (Phase 2 - future)

### Key Metrics

- **Test Pass Rate**: 100% (509/509 tests)
- **Instruction Coverage**: 98% (target: 85%)
- **Branch Coverage**: 95% (target: 85%)
- **Database Migrations**: 17/17 applied successfully
- **Code Quality**: 0 critical violations
- **Performance**: All benchmarks exceeded
- **Documentation**: Complete API reference, migration guide, architecture docs

### Lessons Learned

**What Went Well:**
- Domain-driven design improved code organization
- Test-driven development caught issues early
- Performance optimization paid off
- Comprehensive documentation helps developers

**Challenges Overcome:**
- Fixed 59 failing integration tests
- Addressed 1251 code quality violations
- Removed unused code and cleaned up legacy artifacts

**Areas for Improvement:**

- REST API layer ~40% complete (30+ entities remaining)
- GraphQL deferred to Phase 2
- Monitoring and observability needed for production
