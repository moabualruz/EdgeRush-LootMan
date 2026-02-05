# Gap Analysis Report: EdgeRush LootMan Web Dashboard

> **Audit Date:** 2026-02-05  
> **Target:** `web-dashboard`  
> **Verdict:** ⚠️ **NOT RELEASE READY** — 12 Critical Gaps Identified

---

## Executive Summary

The web-dashboard codebase demonstrates **strong architectural foundations** with mature patterns (TanStack Query, Pinia stores, Vue Router guards) and excellent visual polish. However, **core business workflows are incomplete or missing entirely**, preventing a Release Candidate declaration.

---

## 1. FULL COVERAGE CHECKLIST

### A. Authentication & Access ✅ PASS (with minor gap)

| Requirement | Status | Evidence |
|-------------|--------|----------|
| Login Form with Validation | ✅ | `LoginPage.vue` - `isFormValid` computed with length/email checks |
| Token Persistence | ✅ | `stores/auth.ts` - `setTokens()` with localStorage |
| OAuth (Discord/Battle.net) | ✅ | `LoginPage.vue` - Functional buttons with OAuth flow |
| 401 Interceptor | ✅ | `api/client.ts` - Token refresh with fallback redirect |
| Intended Destination Storage | ✅ | `router/index.ts` - `localStorage.setItem('redirectAfterLogin', ...)` |
| Public vs Logged-In State | ✅ | Router guards with `meta.requiresAuth` |

> **Minor Gap:** No "Forgot Password" or "Help" flow exists.

---

### B. Loot Management ❌ CRITICAL FAILURES

| Requirement | Status | Evidence |
|-------------|--------|----------|
| View Loot History | ✅ | `LootHistoryPage.vue` - Renders awards list |
| **Pagination** | ❌ MISSING | No `currentPage`/`totalPages` state or controls |
| **Sorting** | ❌ MISSING | No column headers with sort handlers |
| **Search Bar** | ❌ MISSING | No filter/search input anywhere |
| **Context Actions (Edit/Revoke)** | ❌ MISSING | No action menu, right-click, or edit buttons |
| **LootConsole / Add Award** | ❌ MISSING | No `LootConsole.vue` or award form exists |
| Item Autocomplete | ❌ MISSING | Dependent on LootConsole |

**API Gap:** `api/loot.ts` is **READ-ONLY** — no `awardLoot()`, `revokeLoot()`, or mutation endpoints.

---

### C. Raider Roster ⚠️ PARTIAL

| Requirement | Status | Evidence |
|-------------|--------|----------|
| Leaderboard View | ✅ | `LeaderboardPage.vue` - Table with role filter |
| FLPS Visualization | ✅ | `FlpsVisualization.vue` - DonutChart, ProgressBar, DecayProjectionChart |
| **RaiderDetail Modal** | ❌ MISSING | No click handler on leaderboard rows, no `RaiderDetail.vue` component |
| **Edit Rank/Status Forms** | ❌ MISSING | No raider edit functionality anywhere |
| Profile Page | ✅ | `ProfilePage.vue` - User's own profile with OAuth linking |

---

### D. Sync Operations ❌ CRITICAL FAILURE

| Requirement | Status | Evidence |
|-------------|--------|----------|
| View Sync History | ✅ | `SyncHistoryPage.vue` - Paginated list with filters |
| **"Sync Now" Trigger Button** | ❌ MISSING | Only "Refresh" button exists (refetches history, doesn't trigger sync) |
| Loading State on Sync | ❌ N/A | No trigger means no loading state |
| **SyncLogViewer / Console** | ❌ MISSING | No detailed log view per sync run |

**API Gap:** `api/sync.ts` is **READ-ONLY** — no `triggerSync()` method.

---

## 2. CRITICAL GAPS SUMMARY

| # | ID | Domain | Gap | Severity | Status |
|---|-----|--------|-----|----------|--------|
| 1 | GAP-001 | Loot | No LootConsole / Award Modal | 🔴 CRITICAL | ⬜ TODO |
| 2 | GAP-002 | Loot | No Edit/Revoke context actions | 🔴 CRITICAL | ⬜ TODO |
| 3 | GAP-003 | Loot | No Search/Filter | 🟠 HIGH | ⬜ TODO |
| 4 | GAP-004 | Loot | No Pagination/Sorting on table | 🟠 HIGH | ⬜ TODO |
| 5 | GAP-005 | Loot | `loot.ts` API is read-only | 🔴 CRITICAL | ⬜ TODO |
| 6 | GAP-006 | Sync | No "Sync Now" trigger button | 🔴 CRITICAL | ⬜ TODO |
| 7 | GAP-007 | Sync | No SyncLogViewer | 🟠 HIGH | ⬜ TODO |
| 8 | GAP-008 | Sync | `sync.ts` API is read-only | 🔴 CRITICAL | ⬜ TODO |
| 9 | GAP-009 | Raider | No RaiderDetail modal on click | 🟠 HIGH | ⬜ TODO |
| 10 | GAP-010 | Raider | No Edit Rank/Status forms | 🟠 HIGH | ⬜ TODO |
| 11 | GAP-011 | Auth | No "Forgot Password" flow | 🟡 MEDIUM | ⬜ TODO |
| 12 | GAP-012 | Global | No Vue ErrorBoundary wrapper | 🟡 MEDIUM | ⬜ TODO |

**Status Legend:**
- ⬜ TODO - Not started
- 🔄 IN PROGRESS - Currently being worked on  
- ✅ DONE - Completed and verified

---

## 3. TOP 3 FILES HANDLING COMPLEX LOGIC

1. **`RaidDetailPage.vue`** (473 lines) - Full signup CRUD with `useMutation`
2. **`api/client.ts`** (85 lines) - 401 interceptor with automatic token refresh
3. **`FlpsVisualization.vue`** (223 lines) - Formula visualization with charts

---

## Verdict

> **NOT RELEASE CANDIDATE READY**

The dashboard requires implementation of **core loot awarding workflows** and **sync trigger functionality** before production deployment.
