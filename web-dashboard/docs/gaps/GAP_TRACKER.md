# Gap Tracker

> **Last Updated:** 2026-02-05  
> **Progress:** 3/12 Complete

---

## Status Overview

| Status | Count |
|--------|-------|
| ⬜ TODO | 9 |
| 🔄 IN PROGRESS | 0 |
| ✅ DONE | 3 |

---

## All Gaps

### Phase 1: API Layer 🔴 CRITICAL

| ID | Gap | Status | Started | Completed | Notes |
|----|-----|--------|---------|-----------|-------|
| GAP-005 | `loot.ts` API mutations | ✅ DONE | 2026-02-05 | 2026-02-05 | TDD: 6 tests passing |
| GAP-008 | `sync.ts` API trigger | ✅ DONE | 2026-02-05 | 2026-02-05 | TDD: 4 tests passing |

### Phase 2: Core Loot Workflow

| ID | Gap | Status | Started | Completed | Notes |
|----|-----|--------|---------|-----------|-------|
| GAP-001 | LootConsole / Award Modal | ✅ DONE | 2026-02-05 | 2026-02-05 | useLootAward + AwardLootModal + ItemAutocomplete |
| GAP-002 | Edit/Revoke context actions | ⬜ TODO | - | - | Requires GAP-005 |
| GAP-003 | Search/Filter | ⬜ TODO | - | - | |

### Phase 3: Data Grid Enhancement

| ID | Gap | Status | Started | Completed | Notes |
|----|-----|--------|---------|-----------|-------|
| GAP-004 | Pagination/Sorting | ⬜ TODO | - | - | |

### Phase 4: Sync Operations

| ID | Gap | Status | Started | Completed | Notes |
|----|-----|--------|---------|-----------|-------|
| GAP-006 | Sync Now button | ⬜ TODO | - | - | Requires GAP-008 |
| GAP-007 | SyncLogViewer | ⬜ TODO | - | - | |

### Phase 5: Raider Management

| ID | Gap | Status | Started | Completed | Notes |
|----|-----|--------|---------|-----------|-------|
| GAP-009 | RaiderDetail modal | ⬜ TODO | - | - | |
| GAP-010 | Edit Rank/Status forms | ⬜ TODO | - | - | Admin only |

### Phase 6: Polish

| ID | Gap | Status | Started | Completed | Notes |
|----|-----|--------|---------|-----------|-------|
| GAP-011 | Forgot Password flow | ⬜ TODO | - | - | |
| GAP-012 | Vue ErrorBoundary | ⬜ TODO | - | - | |

---

## Work Log

| Date | Gap ID | Action | Details |
|------|--------|--------|---------|
| 2026-02-05 | GAP-001 | Complete | useLootAward composable, ItemAutocomplete, AwardLootModal, LootHistoryPage integration |
| 2026-02-05 | GAP-008 | Complete | Added triggerSync, getSyncLogs to sync.ts. 4 tests passing. |
| 2026-02-05 | GAP-005 | Complete | Added awardLoot, updateLoot, revokeLoot, searchItems to loot.ts. 6 tests passing. |
| 2026-02-05 | - | Audit | Initial gap analysis complete |

---

## Next Up

**Recommended next gap to work on:** `GAP-002` (Edit/Revoke context actions)  
**Reason:** Complete core loot workflow (Phase 2)

---

## How to Update This File

When starting work on a gap:
1. Change status from `⬜ TODO` to `🔄 IN PROGRESS`
2. Add start date
3. Add entry to Work Log

When completing a gap:
1. Change status to `✅ DONE`
2. Add completion date
3. Update Status Overview counts
4. Add entry to Work Log with verification results
