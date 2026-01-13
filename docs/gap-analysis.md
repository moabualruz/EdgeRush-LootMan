# EdgeRush LootMan - Gap Analysis

**Date:** January 2026
**Purpose:** Systematic assessment of project state and remaining work

---

## Executive Summary

EdgeRush LootMan has achieved a solid foundation with core FLPS functionality complete, but significant work remains to reach production readiness. The primary blockers are:

1. **Test coverage** at 64% vs 85% target
2. **REST API** only 40% complete
3. **Raidbots integration** blocked on API key availability

---

## Current State by Feature

### Phase 1: Core System (100% Complete)

| Feature | Status | Coverage | Notes |
|---------|--------|----------|-------|
| FLPS Algorithm | Complete | 87.7% | Full RMS/IPI/RDF calculation |
| WoWAudit Integration | Complete | 91.7% | All endpoints syncing |
| Warcraft Logs Integration | Complete | 100% | MAS scoring enabled |
| Database Layer | Complete | N/A | 17 migrations, 45+ entities |
| Test Suite | Complete | 64% | 509 tests, 100% pass rate |

### Phase 2: Production Ready (In Progress)

| Feature | Status | Progress | Blocker |
|---------|--------|----------|---------|
| Test Coverage 85% | In Progress | 64% | None - active work |
| REST API Complete | In Progress | 40% | None |
| GraphQL API | Not Started | 0% | Awaiting test coverage |
| Raidbots Integration | Blocked | 40% | API key unavailable |
| Security Hardening | Partial | 60% | Rate limiting, secrets |

### Phase 3: User Facing (Not Started)

| Feature | Status | Progress | Blocker |
|---------|--------|----------|---------|
| Web Dashboard | Planned | 0% | Requires API completion |
| Discord Bot | Planned | 0% | Requires API completion |
| RC Loot Council Addon | Planned | 0% | Requires dashboard |

---

## Test Coverage Gap Analysis

### Current Coverage: 64%

| Layer | Current | Target | Gap | Priority |
|-------|---------|--------|-----|----------|
| Domain | 87.7% | 85% | Met | - |
| Application | 91.7% | 85% | Met | - |
| API | ~30% | 80% | -50% | CRITICAL |
| Infrastructure | ~40% | 70% | -30% | HIGH |
| Shared | ~50% | 80% | -30% | HIGH |

### Highest Impact Test Additions

1. **FlpsController integration tests** (~10% gain)
2. **LootController integration tests** (~8% gain)
3. **WoWAudit sync service tests** (~5% gain)
4. **Security configuration tests** (~3% gain)
5. **Domain shared model tests** (~2% gain)

**Estimated effort to reach 85%:** 2-3 weeks focused work

---

## REST API Gap Analysis

### Current: 40% Complete (37/90+ endpoints)

| Domain | Implemented | Remaining | Priority |
|--------|-------------|-----------|----------|
| FLPS | 5 endpoints | 0 | Complete |
| WCL Config | 4 endpoints | 0 | Complete |
| Guild Mgmt | 3 endpoints | 5 | High |
| Characters | 4 endpoints | 6 | High |
| Raids | 3 endpoints | 7 | Medium |
| Attendance | 2 endpoints | 8 | Medium |
| Loot | 3 endpoints | 10 | Medium |
| Applications | 2 endpoints | 8 | Low |
| Items/Drops | 2 endpoints | 12 | Low |
| Other | 9 endpoints | 20+ | Low |

### Recommendation

Focus on high-priority domains first:
1. Guild Management (admin functions)
2. Characters (core data)
3. Raids and Attendance (reporting)
4. Loot (core business logic)

---

## Raidbots Integration Gap Analysis

### Current: 40% Complete, BLOCKED

| Component | Status | Notes |
|-----------|--------|-------|
| Database Schema | Complete | V0017 migration |
| Configuration | Complete | Properties, guild config |
| Profile Generation | Complete | SimC profiles from WoWAudit |
| API Client | Not Started | Requires API key |
| Simulation Service | Not Started | Depends on API client |
| Upgrade Calculator | Not Started | Depends on simulation |

### Current Workaround

Using wishlist percentages from WoWAudit as proxy for upgrade value. This is less accurate than actual simulation data but functional.

### Resolution Path

1. **Option A**: Obtain Raidbots API key
   - Contact Raidbots developers
   - Check if developer program exists
   - Estimated: 1-2 days if available

2. **Option B**: Alternative upgrade calculation
   - Use item level delta
   - Use stat weights and item stats
   - Estimated: 2-3 weeks to implement

---

## Documentation Gap Analysis

### Completed (This Session)

- [x] Created CLAUDE.md (comprehensive project overview)
- [x] Created .project/ folder with planning documents
- [x] Archived 65 redundant status files
- [x] Established single source of truth structure

### Remaining Documentation Needs

| Document | Status | Priority |
|----------|--------|----------|
| API Reference | Complete | - |
| Code Architecture | Complete | - |
| Development Standards | Complete | - |
| Deployment Guide | Missing | Medium |
| Monitoring Guide | Missing | Low |
| User Guide | Missing | Low (no UI yet) |

---

## Technical Debt

| Item | Severity | Effort | Priority |
|------|----------|--------|----------|
| Test coverage below target | High | 2-3 weeks | P0 |
| Rate limiting disabled | Medium | 1 day | P1 |
| Secrets in .env.local | Medium | 2-3 days | P1 |
| No production monitoring | Medium | 1 week | P2 |
| Gradle wrapper missing | Low | 1 hour | P3 |
| Some large service classes | Low | 1 week | P3 |

---

## Recommended Next Steps

### Immediate (This Week)

1. **Add API controller integration tests**
   - Start with FlpsController
   - Then LootController
   - Target: +18% coverage

2. **Enable rate limiting**
   - Already configured, just disabled
   - Enable for production environment

### Short Term (2-4 Weeks)

3. **Complete REST API for high-priority domains**
   - Guild Management
   - Characters
   - Raids/Attendance

4. **Reach 85% test coverage**
   - Complete API layer tests
   - Add infrastructure tests

### Medium Term (1-2 Months)

5. **Resolve Raidbots situation**
   - Try to obtain API key
   - Or implement alternative calculation

6. **Start GraphQL implementation**
   - After test coverage achieved
   - Spec already complete

### Long Term (3+ Months)

7. **Build Web Dashboard**
   - React + TypeScript
   - Connect to GraphQL

8. **Build Discord Bot**
   - Kotlin + JDA
   - Connect to REST API

---

## Success Metrics

### Phase 2 Complete When:

- [ ] Test coverage >= 85%
- [ ] REST API 100% complete
- [ ] GraphQL API operational
- [ ] Security hardening complete
- [ ] Raidbots resolved (integrated or alternative)

### Phase 3 Complete When:

- [ ] Web dashboard deployed
- [ ] Discord bot operational
- [ ] User acceptance testing passed
- [ ] Production deployment complete
