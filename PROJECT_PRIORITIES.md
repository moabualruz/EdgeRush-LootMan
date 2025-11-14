# EdgeRush LootMan - Project Priorities

**Last Updated:** 2025-11-14  
**Status:** Active Development

## 🎯 Current Priority Order

### PRIORITY 0: TDD Standards & Project Refactoring (CRITICAL - IN PROGRESS)
**Timeline:** 13 weeks  
**Status:** ⏳ Spec Complete, Implementation Not Started  
**Spec:** `.kiro/specs/graphql-tdd-refactor/`

**Why This is Priority 0:**
- Establishes foundation for all future development
- Improves code quality, testability, and maintainability
- Prevents technical debt accumulation
- Required before adding new features

**Objectives:**
1. Establish TDD standards with 85% code coverage requirement
2. Refactor project structure using domain-driven design (DDD)
3. Organize code into bounded contexts (FLPS, Loot, Attendance, Raids, etc.)
4. Implement code quality tools (ktlint, detekt)
5. Create comprehensive testing and code standards documentation

**Key Deliverables:**
- Testing infrastructure (JUnit 5, MockK, Testcontainers, JaCoCo)
- DDD-based package structure (API → Application → Domain → Infrastructure)
- Refactored bounded contexts with 85%+ test coverage
- Testing standards guide and code standards guide
- Migration complete with zero regression

**Next Steps:**
1. Open `.kiro/specs/graphql-tdd-refactor/tasks.md`
2. Start with Task 1: Set up testing infrastructure
3. Follow TDD workflow: Write tests first, then implementation

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
| **TDD Standards & Refactoring** | 📋 Spec Complete | 0% | None - Ready to start |
| **GraphQL API** | 📋 Spec Complete | 0% | Awaiting Priority 0 |
| **REST API Layer** | 🔄 In Progress | 40% | None |
| **Raidbots Integration** | ⚠️ Blocked | 40% | API Key Availability |
| **Web Dashboard** | 📋 Planned | 0% | Requires GraphQL |
| **Discord Bot** | 📋 Planned | 0% | Requires API |
| **Warcraft Logs** | ✅ Complete | 100% | None |
| **Core FLPS System** | ✅ Complete | 100% | None |

---

## 🎯 Success Criteria

### For Priority 0 (TDD Refactoring)
- [ ] All tests pass (unit + integration)
- [ ] Code coverage ≥ 85% across all bounded contexts
- [ ] Zero ktlint or detekt violations
- [ ] All existing REST endpoints still functional
- [ ] Performance equal or better than before
- [ ] Complete documentation (testing standards, code standards, ADRs)
- [ ] Team trained on new architecture

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
3. **Review current priority spec** (currently: `graphql-tdd-refactor`)
4. **Open tasks.md** in current priority spec to see implementation tasks
5. **Start with Task 1** and follow TDD workflow

**Current Action:** Start Priority 0 by opening `.kiro/specs/graphql-tdd-refactor/tasks.md` and beginning Task 1.
