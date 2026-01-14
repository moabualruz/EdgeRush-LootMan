# Web Dashboard Implementation Plan

## Current Status

### Fully Implemented Pages
1. **LoginPage** - OAuth login with Discord/Battle.net
2. **DashboardPage** - FLPS score card, breakdown, recent loot
3. **LeaderboardPage** - Guild rankings with role filtering
4. **LootHistoryPage** - Personal loot history with RDF status
5. **AdminPage** - Config editor, behavioral actions, loot bans

### Placeholder Pages (Need Implementation)
1. **WishlistPage** - Currently just a placeholder message
2. **PerformancePage** - Currently just a placeholder message

---

## WishlistPage Implementation Plan

### Features Required
1. Display wishlist items sorted by upgrade value
2. Show simulation source (Raidbots vs wishlist percentage)
3. Display stale data warnings
4. Trigger new simulations
5. Show simulation progress/status

### API Endpoints Needed
```typescript
GET  /api/v1/wishlist/guilds/{guildId}/me
GET  /api/v1/simulations/guilds/{guildId}/raiders/{raiderId}/status
POST /api/v1/simulations/guilds/{guildId}/raiders/{raiderId}/run
```

### Components
- Wishlist item table with upgrade values
- Simulation status card with progress bar
- Stale data warning banner
- Run simulation button

### Data Types
```typescript
interface WishlistItem {
  itemId: number
  itemName: string
  slot: string
  upgradeValue: number
  simulationSource: 'RAIDBOTS' | 'WISHLIST_PERCENTAGE'
  lastSimulatedAt?: string
  isStale: boolean
}

interface SimulationStatus {
  raiderId: number
  status: 'IDLE' | 'QUEUED' | 'RUNNING' | 'COMPLETED' | 'FAILED'
  progress?: number
  lastRunAt?: string
  error?: string
}
```

---

## PerformancePage Implementation Plan

### Features Required
1. Display MAS (Mechanical Adherence Score) breakdown
2. Show DPA (Damage Per Active time) metrics
3. Show ADT (Active Damage Time) metrics
4. Display performance trend chart (last 30 days)
5. Show recent Warcraft Logs reports
6. Compare against spec averages

### API Endpoints Needed
```typescript
GET /api/v1/performance/guilds/{guildId}/me
GET /api/v1/warcraftlogs/guilds/{guildId}/raiders/{raiderId}/reports
```

### Components
- MAS score card with breakdown
- Performance metrics cards (DPA, ADT)
- Line chart for performance trend
- Recent reports table with percentiles
- Spec comparison visualization

### Data Types
```typescript
interface PerformanceMetrics {
  raiderId: number
  characterName: string
  dpa: number
  adt: number
  specAverage: number
  performanceTrend: PerformanceDataPoint[]
  lastUpdated: string
}

interface PerformanceDataPoint {
  date: string
  dpa: number
  adt: number
}

interface WarcraftLogsEntry {
  reportId: string
  encounterId: number
  encounterName: string
  difficulty: string
  date: string
  dps?: number
  hps?: number
  ilvl: number
  spec: string
  percentile: number
  deaths: number
}
```

---

## Implementation Order

1. **WishlistPage** (Priority: High)
   - Create `src/api/wishlist.ts`
   - Update `src/types/index.ts` with wishlist types
   - Implement full `WishlistPage.vue`

2. **PerformancePage** (Priority: High)
   - Create `src/api/performance.ts`
   - Update `src/types/index.ts` with performance types
   - Implement full `PerformancePage.vue`
   - Add chart component for trends

3. **Tests** (Priority: Medium)
   - Unit tests for API clients
   - Component tests for pages

---

## Files to Create/Update

### New Files
- `src/api/wishlist.ts` - Wishlist API client
- `src/api/performance.ts` - Performance API client
- `src/components/PerformanceChart.vue` - Trend chart component

### Files to Update
- `src/types/index.ts` - Add WishlistItem, SimulationStatus, PerformanceMetrics types
- `src/pages/WishlistPage.vue` - Full implementation
- `src/pages/PerformancePage.vue` - Full implementation
