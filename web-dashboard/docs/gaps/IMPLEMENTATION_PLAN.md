# Gap Remediation Implementation Plan

> **Created:** 2026-02-05  
> **Project:** EdgeRush LootMan Web Dashboard  
> **Total Gaps:** 12 | **Critical:** 6 | **High:** 4 | **Medium:** 2

---

## Overview

This plan addresses all 12 gaps identified in the Gap Analysis. Each gap includes:
- Detailed implementation steps
- File changes required
- Test requirements (aligned with existing patterns)
- Verification criteria

### Best Practices Applied (2025/2026 Standards)
- **TanStack Table** for data grids with pagination/sorting/filtering
- **Debounced autocomplete** with keyboard navigation
- **useMutation + onMutate** for optimistic updates
- **Teleport modals** for proper z-indexing
- Existing test patterns: Vitest unit tests + Playwright E2E

---

## Priority Order

```
Phase 1: API Layer (Required for all UI work)
├── GAP-005: loot.ts API mutations
└── GAP-008: sync.ts API trigger

Phase 2: Core Loot Workflow  
├── GAP-001: LootConsole / Award Modal
├── GAP-002: Edit/Revoke context actions
└── GAP-003: Search/Filter

Phase 3: Data Grid Enhancement
└── GAP-004: Pagination/Sorting

Phase 4: Sync Operations
├── GAP-006: Sync Now button
└── GAP-007: SyncLogViewer

Phase 5: Raider Management
├── GAP-009: RaiderDetail modal
└── GAP-010: Edit Rank/Status

Phase 6: Polish
├── GAP-011: Forgot Password
└── GAP-012: ErrorBoundary
```

---

## Phase 1: API Layer

### GAP-005: `loot.ts` API Mutations

**Problem:** API only has read operations.

**Files to Modify:**
- `src/api/loot.ts` — Add mutation methods
- `src/types/index.ts` — Add `AwardLootRequest` interface

**Implementation:**
```typescript
// src/api/loot.ts - ADD these methods

export interface AwardLootRequest {
  raiderId: number
  itemId: number
  itemName: string
  raidId?: number
  notes?: string
}

export interface UpdateLootRequest {
  itemName?: string
  notes?: string
}

export const lootApi = {
  // ... existing methods ...

  async awardLoot(guildId: string, data: AwardLootRequest): Promise<LootAward> {
    const response = await api.post<LootAward>(
      `/v1/loot/guilds/${guildId}/awards`,
      data
    )
    return response.data
  },

  async updateLoot(awardId: number, data: UpdateLootRequest): Promise<LootAward> {
    const response = await api.patch<LootAward>(
      `/v1/loot/awards/${awardId}`,
      data
    )
    return response.data
  },

  async revokeLoot(awardId: number): Promise<void> {
    await api.delete(`/v1/loot/awards/${awardId}`)
  },

  async searchItems(query: string, limit = 20): Promise<WowItem[]> {
    const response = await api.get<WowItem[]>('/v1/game-data/items/search', {
      params: { q: query, limit }
    })
    return response.data
  },
}
```

**Verification:**
- Unit test: `src/api/loot.test.ts` (add test cases for new methods)
- Run: `npm run test -- loot.test.ts`

---

### GAP-008: `sync.ts` API Trigger

**Problem:** No method to trigger sync operations.

**Files to Modify:**
- `src/api/sync.ts` — Add trigger and logs methods

**Implementation:**
```typescript
// src/api/sync.ts - ADD these methods

export interface SyncLog {
  timestamp: string
  level: 'INFO' | 'WARN' | 'ERROR'
  message: string
}

export const syncApi = {
  // ... existing methods ...

  async triggerSync(source: 'WoWAudit' | 'WarcraftLogs'): Promise<SyncRun> {
    const response = await api.post<SyncRun>(`/api/sync/trigger/${source}`)
    return response.data
  },

  async getSyncLogs(syncRunId: number): Promise<SyncLog[]> {
    const response = await api.get<SyncLog[]>(`/api/sync-runs/${syncRunId}/logs`)
    return response.data
  },
}
```

**Verification:**
- Unit test: Create `src/api/sync.test.ts`
- Run: `npm run test -- sync.test.ts`

---

## Phase 2: Core Loot Workflow

### GAP-001: LootConsole / Award Modal

**Problem:** No UI to award loot to raiders.

**Files to Create:**
- `src/components/loot/AwardLootModal.vue` — Modal form
- `src/components/loot/ItemAutocomplete.vue` — Debounced item search
- `src/composables/useLootAward.ts` — Mutation logic

**Files to Modify:**
- `src/pages/LootHistoryPage.vue` — Add "Award Loot" button

**Implementation Details:**

#### ItemAutocomplete.vue
```vue
<script setup lang="ts">
import { ref, watch } from 'vue'
import { lootApi } from '@/api/loot'
import { useDebounceFn } from '@vueuse/core'

const props = defineProps<{
  modelValue: WowItem | null
}>()
const emit = defineEmits<{
  'update:modelValue': [item: WowItem | null]
}>()

const query = ref('')
const results = ref<WowItem[]>([])
const isOpen = ref(false)
const isLoading = ref(false)
const selectedIndex = ref(0)

const searchItems = useDebounceFn(async () => {
  if (query.value.length < 2) {
    results.value = []
    return
  }
  isLoading.value = true
  try {
    results.value = await lootApi.searchItems(query.value)
  } finally {
    isLoading.value = false
  }
}, 300)

watch(query, () => {
  searchItems()
  isOpen.value = true
})

function selectItem(item: WowItem) {
  emit('update:modelValue', item)
  query.value = item.name
  isOpen.value = false
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'ArrowDown') {
    selectedIndex.value = Math.min(selectedIndex.value + 1, results.value.length - 1)
  } else if (e.key === 'ArrowUp') {
    selectedIndex.value = Math.max(selectedIndex.value - 1, 0)
  } else if (e.key === 'Enter' && results.value[selectedIndex.value]) {
    selectItem(results.value[selectedIndex.value])
  }
}
</script>
```

#### useLootAward.ts Composable
```typescript
import { useMutation, useQueryClient } from '@tanstack/vue-query'
import { lootApi, type AwardLootRequest } from '@/api/loot'
import { useToast } from '@/composables/useToast'

export function useLootAward(guildId: Ref<string | undefined>) {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: (data: AwardLootRequest) => 
      lootApi.awardLoot(guildId.value!, data),
    
    onMutate: async (newAward) => {
      // Cancel outgoing refetches
      await queryClient.cancelQueries({ queryKey: ['myLootHistory', guildId.value] })
      
      // Snapshot previous value
      const previousAwards = queryClient.getQueryData(['myLootHistory', guildId.value])
      
      // Optimistically update
      queryClient.setQueryData(['myLootHistory', guildId.value], (old: any) => ({
        ...old,
        awards: [{ ...newAward, id: -1, awardedAt: new Date().toISOString() }, ...old.awards]
      }))
      
      return { previousAwards }
    },
    
    onError: (_err, _variables, context) => {
      // Rollback on error
      queryClient.setQueryData(['myLootHistory', guildId.value], context?.previousAwards)
      toast({ type: 'error', title: 'Award Failed', message: 'Could not award loot. Please try again.' })
    },
    
    onSuccess: () => {
      toast({ type: 'success', title: 'Loot Awarded', message: 'Item has been assigned to the raider.' })
    },
    
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ['myLootHistory', guildId.value] })
    },
  })
}
```

**Verification:**
- Unit test: `src/components/loot/AwardLootModal.test.ts`
- Unit test: `src/composables/useLootAward.test.ts`
- E2E test: Add to `e2e/loot-history.spec.ts`
- Run: `npm run test` and `npx playwright test loot-history`

---

### GAP-002: Edit/Revoke Context Actions

**Problem:** No way to modify or delete existing loot awards.

**Files to Create:**
- `src/components/loot/LootContextMenu.vue` — Right-click menu
- `src/components/loot/EditLootModal.vue` — Edit form

**Files to Modify:**
- `src/pages/LootHistoryPage.vue` — Attach context menu to rows

**Implementation:**
- Add `@contextmenu.prevent="openMenu(award)"` to award rows
- Use teleport for menu positioning
- Confirm dialog before revoke

**Verification:**
- Unit test: `src/components/loot/LootContextMenu.test.ts`
- E2E test: Add context menu test to `e2e/loot-history.spec.ts`

---

### GAP-003: Search/Filter for Loot History

**Problem:** No way to search or filter loot history.

**Files to Modify:**
- `src/pages/LootHistoryPage.vue` — Add search input and filter logic

**Implementation:**
```vue
<script setup>
// Add to LootHistoryPage.vue
const searchQuery = ref('')
const debouncedSearch = useDebounceFn((val) => {
  // Filter logic or API call with search param
}, 300)

const filteredAwards = computed(() => {
  if (!searchQuery.value) return data.value?.awards ?? []
  const q = searchQuery.value.toLowerCase()
  return data.value?.awards.filter(a => 
    a.itemName.toLowerCase().includes(q)
  ) ?? []
})
</script>

<template>
  <!-- Add search bar in header -->
  <input 
    v-model="searchQuery"
    type="text"
    placeholder="Search items..."
    class="input w-64"
  />
</template>
```

**Verification:**
- E2E test: Add search test to `e2e/loot-history.spec.ts`
- Manual: Type in search box, verify list filters

---

### GAP-004: Pagination/Sorting

**Problem:** No pagination or column sorting.

**Files to Modify:**
- `src/pages/LootHistoryPage.vue` — Add table with TanStack Table or manual controls

**Implementation:**
```typescript
// Add pagination state
const page = ref(0)
const pageSize = 20
const sortColumn = ref<'awardedAt' | 'itemName' | 'flpsAtAward'>('awardedAt')
const sortDir = ref<'asc' | 'desc'>('desc')

// Update query to use pagination
const { data } = useQuery({
  queryKey: ['lootHistory', guildId, page, pageSize, sortColumn, sortDir],
  queryFn: () => lootApi.getGuildLootHistory(guildId.value!, {
    page: page.value,
    size: pageSize,
    sort: `${sortColumn.value},${sortDir.value}`
  }),
})
```

**Verification:**
- E2E test: Click pagination controls, verify data changes
- E2E test: Click column header, verify sort order

---

## Phase 3: Sync Operations

### GAP-006: Sync Now Button

**Problem:** No way to trigger sync from UI.

**Files to Modify:**
- `src/pages/SyncHistoryPage.vue` — Add trigger buttons
- `src/composables/useSyncTrigger.ts` — Mutation logic

**Implementation:**
```vue
<template>
  <div class="flex gap-2">
    <button 
      @click="triggerWowAudit.mutate()"
      :disabled="triggerWowAudit.isPending.value"
      class="btn-primary"
    >
      <span v-if="triggerWowAudit.isPending.value" class="animate-spin mr-2">⟳</span>
      Sync WoWAudit
    </button>
    <button 
      @click="triggerWarcraftLogs.mutate()"
      :disabled="triggerWarcraftLogs.isPending.value"
      class="btn-secondary"
    >
      <span v-if="triggerWarcraftLogs.isPending.value" class="animate-spin mr-2">⟳</span>
      Sync WarcraftLogs
    </button>
  </div>
</template>
```

**Verification:**
- E2E test: Create `e2e/sync.spec.ts` with trigger test
- Manual: Click button, verify spinner shows, new run appears in list

---

### GAP-007: SyncLogViewer

**Problem:** No way to view detailed logs for a sync run.

**Files to Create:**
- `src/components/SyncLogViewer.vue` — Log display modal/panel

**Files to Modify:**
- `src/pages/SyncHistoryPage.vue` — Add click handler to view logs

**Verification:**
- Unit test: `src/components/SyncLogViewer.test.ts`
- E2E test: Add to `e2e/sync.spec.ts`

---

## Phase 4: Raider Management

### GAP-009: RaiderDetail Modal

**Problem:** Clicking raider name does nothing.

**Files to Create:**
- `src/components/RaiderDetailModal.vue` — Profile modal
- `src/api/raiders.ts` — API for raider data

**Files to Modify:**
- `src/pages/LeaderboardPage.vue` — Add click handler

**Implementation:**
```vue
<!-- LeaderboardPage.vue -->
<tr 
  @click="openRaiderDetail(entry.raiderId)"
  class="cursor-pointer hover:bg-gray-700/30"
>
```

**Verification:**
- E2E test: Add to `e2e/leaderboard.spec.ts`
- Manual: Click raider name, verify modal opens with data

---

### GAP-010: Edit Rank/Status Forms

**Problem:** No admin forms to edit raider metadata.

**Files to Create:**
- `src/components/admin/RaiderEditForm.vue` — Form component

**Files to Modify:**
- `src/components/RaiderDetailModal.vue` — Include edit form for admins

**Verification:**
- Unit test: `src/components/admin/RaiderEditForm.test.ts`
- E2E test: Add to `e2e/leaderboard.spec.ts` (admin flow)

---

## Phase 5: Polish

### GAP-011: Forgot Password Flow

**Problem:** No password recovery option.

**Files to Create:**
- `src/pages/ForgotPasswordPage.vue` — Request form
- `src/pages/ResetPasswordPage.vue` — Reset form

**Files to Modify:**
- `src/pages/LoginPage.vue` — Add "Forgot Password?" link
- `src/router/index.ts` — Add routes
- `src/api/auth.ts` — Add forgot/reset methods

**Verification:**
- E2E test: Add to `e2e/auth.spec.ts`

---

### GAP-012: Vue ErrorBoundary

**Problem:** No global error catching for component crashes.

**Files to Create:**
- `src/components/ErrorBoundary.vue` — Wrapper component

**Files to Modify:**
- `src/App.vue` — Wrap RouterView in ErrorBoundary

**Implementation:**
```vue
<!-- ErrorBoundary.vue -->
<script setup lang="ts">
import { onErrorCaptured, ref } from 'vue'

const error = ref<Error | null>(null)

onErrorCaptured((err) => {
  error.value = err
  return false // Stop propagation
})

function retry() {
  error.value = null
}
</script>

<template>
  <div v-if="error" class="min-h-screen flex items-center justify-center">
    <div class="card text-center max-w-md">
      <h2 class="text-xl font-bold text-destructive mb-4">Something went wrong</h2>
      <p class="text-muted-foreground mb-4">{{ error.message }}</p>
      <button @click="retry" class="btn-primary">Try Again</button>
    </div>
  </div>
  <slot v-else />
</template>
```

**Verification:**
- Unit test: `src/components/ErrorBoundary.test.ts`
- Manual: Throw error in component, verify boundary catches it

---

## Test Commands Summary

```bash
# Unit tests
npm run test                          # All unit tests
npm run test -- loot                  # Loot-related tests
npm run test -- sync                  # Sync-related tests

# E2E tests  
npx playwright test                   # All E2E tests
npx playwright test loot-history      # Loot history E2E
npx playwright test auth              # Auth E2E
npx playwright test --ui              # Interactive mode
```

---

## Dependencies to Install

```bash
npm install @vueuse/core  # For useDebounceFn, if not already installed
```

---

## Estimated Effort

| Phase | Effort | Dependencies |
|-------|--------|--------------|
| Phase 1: API Layer | 2-3 hours | Backend endpoints must exist |
| Phase 2: Core Loot | 8-10 hours | Phase 1 |
| Phase 3: Data Grid | 3-4 hours | None |
| Phase 4: Sync Ops | 4-5 hours | Phase 1 |
| Phase 5: Raider Mgmt | 5-6 hours | API may need updates |
| Phase 6: Polish | 3-4 hours | None |

**Total: ~25-32 hours**
