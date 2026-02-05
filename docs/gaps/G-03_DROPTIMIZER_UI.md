# G-03: Droptimizer UI Implementation Plan

**Requirement:** R12-R14 (Droptimizer, Top Gear, Gear Compare)  
**Priority:** P1  
**Estimated Effort:** 8-12 hours  
**Status:** ⬜ TODO

---

## Current State

- **Backend:** ✅ `DockerSimulationExecutor.kt` (193 LOC) working
- **Backend:** ✅ `SimulationService.kt`, repositories implemented
- **Frontend:** ❌ No Droptimizer/TopGear pages exist
- **Frontend:** 🟡 `GearPage.vue` exists but incomplete

---

## Requirements Checklist

### R12: Droptimizer
- [ ] Drop analysis for current raid (all difficulties)
- [ ] Drop analysis for M+ dungeons
- [ ] Drop analysis for weekly vault
- [ ] Display DPS gain % per item
- [ ] Display expected value per boss
- [ ] Priority ranking (which boss to prioritize)
- [ ] Filter by source, slot, minimum upgrade value
- [ ] Cache results with staleness indicator

### R13: Top Gear
- [ ] Analyze equipped gear combinations
- [ ] Include bag items, bank, vault options
- [ ] Optimize for ST/AoE/Custom profile
- [ ] Display best setup + top 5 alternatives
- [ ] Gem/enchant recommendations
- [ ] Tier set optimization

### R14: Gear Compare
- [ ] Compare 2-3 items side by side
- [ ] Display DPS difference (absolute + %)
- [ ] Show stat changes
- [ ] "What if" scenarios (e.g., upgrade with crest)

---

## Implementation Steps

### Phase 1: API Layer (2h)
1. **Simulation API Client**
   - File: `web-dashboard/src/api/simulation.ts`
   - Methods: `runDroptimizer()`, `getSimulationResults()`, `cancelSimulation()`

2. **API Types**
   - File: `web-dashboard/src/api/simulation.ts`
   - Types: `SimulationRequest`, `SimulationResult`, `DroptimizerResult`

### Phase 2: Droptimizer Page (4h)
3. **Create DroptimizerPage.vue**
   - File: `web-dashboard/src/pages/DroptimizerPage.vue`
   - Character selector
   - Source filter (Raid/M+/Vault)
   - Results table with DPS gain %, upgrade priority
   - Progress indicator for running sims

4. **Results Visualization**
   - Bar chart of upgrade values by boss
   - Color-coded by upgrade significance

### Phase 3: Top Gear Page (3h)
5. **Create TopGearPage.vue**
   - File: `web-dashboard/src/pages/TopGearPage.vue`
   - Profile selector (ST/AoE/Custom)
   - Display optimal gear setup
   - Side-by-side comparison with current

### Phase 4: Gear Compare Enhancement (2h)
6. **Enhance GearPage.vue**
   - Add multi-item compare mode
   - Visual stat comparison
   - DPS delta display

---

## Files to Create/Modify

| File | Action | Description |
|------|--------|-------------|
| `src/api/simulation.ts` | Create | Simulation API client |
| `src/api/simulation.test.ts` | Create | API tests |
| `src/pages/DroptimizerPage.vue` | Create | Main droptimizer UI |
| `src/pages/DroptimizerPage.test.ts` | Create | Page tests |
| `src/pages/TopGearPage.vue` | Create | Top gear optimizer |
| `src/pages/TopGearPage.test.ts` | Create | Page tests |
| `src/pages/GearPage.vue` | Modify | Add compare functionality |
| `src/router/index.ts` | Modify | Add new routes |
| `src/components/SimulationProgress.vue` | Create | Progress indicator |
| `src/components/UpgradeChart.vue` | Create | DPS gain visualization |

---

## Route Configuration

```typescript
// src/router/index.ts
{
  path: '/droptimizer',
  name: 'Droptimizer',
  component: () => import('@/pages/DroptimizerPage.vue'),
  meta: { requiresAuth: true }
},
{
  path: '/top-gear',
  name: 'TopGear',
  component: () => import('@/pages/TopGearPage.vue'),
  meta: { requiresAuth: true }
}
```

---

## Testing Strategy

1. **API Tests:** Mock Axios, verify request/response handling
2. **Component Tests:** Mount with mock data, test interactions
3. **E2E Test:** Full flow simulation request → results display

---

## Verification Commands

```bash
cd web-dashboard

# Run tests
npm run test -- src/api/simulation.test.ts
npm run test -- src/pages/DroptimizerPage.test.ts

# Dev server
npm run dev
```
