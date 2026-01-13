# EdgeRush LootMan - Project Priorities

**Last Updated:** 2025-11-15  
**Status:** Active Development

## 🎉 Recent Completion: Post-Refactoring Cleanup

The post-refactoring cleanup phase has been **successfully completed**, establishing a solid foundation with:
- ✅ 509 tests passing (100% pass rate)
- ✅ All database migrations verified
- ✅ Performance benchmarks exceeded
- ✅ Zero critical code quality violations
- ✅ Complete documentation

## 🎯 Current Priority Order

### PRIORITY 0: Achieve 100% Test Coverage (BLOCKING)
**Timeline:** Until complete
**Status:** 🔄 In Progress
**Current Coverage:** 64% (Target: 100%)

**Why This is Priority 0 and BLOCKING:**
- No new features until 100% test coverage achieved
- Foundation must be rock-solid before proceeding
- Every line of code must be verified
- Prevents regressions and ensures reliability

**Objectives:**
1. Achieve 100% coverage on Domain layer (current: 87.7%)
2. Achieve 100% coverage on Application layer (current: 91.7%)
3. Achieve 100% coverage on API layer (current: ~30%)
4. Achieve 100% coverage on Infrastructure layer
5. Achieve 100% overall test coverage

**Coverage Gap Analysis:**
- Domain layer: 87.7% → 100% (12.3% gap)
- Application layer: 91.7% → 100% (8.3% gap)
- API layer: ~30% → 100% (70% gap)
- Infrastructure: ~40% → 100% (60% gap)

**Next Steps:**
1. Run coverage report to identify all uncovered lines
2. Systematically add tests for each uncovered class/method
3. Prioritize by layer: Domain → Application → API → Infrastructure
4. Re-run coverage after each batch of tests
5. Do not proceed to any other priority until 100% achieved

---

### PRIORITY 1: GraphQL API Implementation (FUTURE)
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

### PRIORITY 2: Complete REST API Layer (PARTIALLY COMPLETE)
**Timeline:** 6-8 weeks  
**Status:** 🔄 ~40% Complete  
**Spec:** `.kiro/specs/rest-api-layer/`

**Current Status:**
- ✅ Foundation complete (security, base controllers, OpenAPI)
- ✅ Core CRUD services (Raider, Raid, LootAward, AttendanceStat)
- ⏳ Remaining entity APIs (30+ entities)
- ⏳ Comprehensive testing suite
- ⏳ API versioning and deprecation support

**Why This is Lower Priority:**
- Basic REST endpoints already functional
- Can be completed incrementally alongside Priority 0
- GraphQL will eventually become primary API

**Remaining Work:**
- Complete CRUD APIs for remaining 30+ entities
- Implement comprehensive testing (integration, contract, security tests)
- Add API versioning support
- Complete OpenAPI documentation
- Achieve 80% code coverage on API layer

---

### PRIORITY 3: Raidbots Integration (BLOCKED)
**Timeline:** 3-4 weeks  
**Status:** ⚠️ 40% Complete, Blocked by API Key Availability  
**Spec:** `.kiro/specs/raidbots-integration/`

**Current Status:**
- ✅ Database schema and entities
- ✅ Configuration system
- ✅ SimC profile generation
- ❌ API client (requires API key)
- ❌ Simulation service
- ❌ Upgrade value calculation

**Blocker:**
- Raidbots API key availability uncertain
- Need to verify developer access to Raidbots API

**Impact:**
- Currently using wishlist percentages as proxy for upgrade values
- Achieving 100% accurate FLPS scores requires this integration

**Next Steps:**
1. Verify Raidbots API key availability
2. If available: Complete API client implementation
3. If not available: Explore alternative upgrade value calculation methods

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

### Post-Refactoring Cleanup (100% COMPLETE - NEW)
**Status:** ✅ Fully Completed  
**Spec:** `.kiro/specs/post-refactoring-cleanup/`  
**Completion Date:** November 15, 2025

**Completed:**
- All 509 tests passing (100% pass rate)
- Test coverage: 64% overall (domain: 87.7%, application: 91.7%)
- All 17 database migrations verified and applied
- Zero critical code quality violations
- Performance benchmarks exceeded (20-1000x better than requirements)
- Complete API documentation (37 REST endpoints)
- Migration guide created for developers
- GraphQL status clarified (Phase 2 - not yet implemented)

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
| **Post-Refactoring Cleanup** | ✅ Complete | 100% | None |
| **Test Coverage Improvement** | 🔄 In Progress | 64% | None - Active work |
| **GraphQL API** | 📋 Spec Complete | 0% | Awaiting test coverage |
| **REST API Layer** | 🔄 In Progress | 40% | None |
| **Raidbots Integration** | ⚠️ Blocked | 40% | API Key Availability |
| **Web Dashboard** | 📋 Planned | 0% | Requires GraphQL |
| **Discord Bot** | 📋 Planned | 0% | Requires API |
| **Warcraft Logs** | ✅ Complete | 100% | None |
| **Core FLPS System** | ✅ Complete | 100% | None |

---

## 🎯 Success Criteria

### For Priority 0 (100% Test Coverage - BLOCKING)

- [ ] Overall test coverage = 100%
- [ ] Domain layer coverage = 100%
- [ ] Application layer coverage = 100%
- [ ] API layer coverage = 100%
- [ ] Infrastructure layer coverage = 100%
- [ ] All tests passing
- [ ] Coverage report verified

### For Priority 1 (GraphQL)
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
2. **Check `.kiro/specs/`** for detailed specs on each feature
3. **Review current priority** (currently: Test Coverage Improvement)
4. **Check test coverage report** at `.kiro/specs/post-refactoring-cleanup/task-20-coverage-report.md`
5. **Start with highest impact tests** (API controllers)

**Current Action:** Improve test coverage by adding API controller integration tests to reach 85% threshold.

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
- **Test Coverage**: 64% overall, 87.7% domain, 91.7% application
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
- Test coverage below 85% target (need API controller tests)
- GraphQL deferred to Phase 2
- Monitoring and observability needed for production
