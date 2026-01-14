# Web Dashboard Implementation Plan

## Backend API Coverage Analysis

Based on the complete backend API analysis (48 controllers), here's what the frontend needs to support:

---

## Current Frontend Status

### Fully Implemented Pages
1. **LoginPage** - OAuth login with Discord/Battle.net ✓
2. **DashboardPage** - FLPS score card, breakdown, recent loot ✓
3. **LeaderboardPage** - Guild rankings with role filtering ✓
4. **LootHistoryPage** - Personal loot history with RDF status ✓
5. **AdminPage** - Config editor, behavioral actions, loot bans ✓

### Pages Needing Full Implementation
1. **WishlistPage** - Item priorities with upgrade values
2. **PerformancePage** - Warcraft Logs metrics and MAS breakdown
3. **SimulationPage** - SimulationCraft integration (NEW)
4. **AttendancePage** - Attendance tracking and reports (NEW)
5. **RaidsPage** - Raid management and signups (NEW)
6. **GearPage** - Character gear inspection (NEW)
7. **ApplicationsPage** - Guild applications (NEW - Admin)

---

## Complete Implementation Plan by Domain

### 1. Wishlist & Simulation Domain

#### API Endpoints (Backend)
```
GET  /api/v1/wishlists/raider/{raiderId}
POST /api/v1/wishlists
PUT  /api/v1/wishlists/raider/{raiderId}
DELETE /api/v1/wishlists/raider/{raiderId}

POST /api/v1/simulation/guilds/{guildId}/characters/{characterName}
GET  /api/v1/simulation/requests/{requestId}
GET  /api/v1/simulation/guilds/{guildId}/characters/{characterName}/realms/{realm}/results
GET  /api/v1/simulation/status
```

#### Frontend Components Needed
- `WishlistPage.vue` - Full wishlist display with upgrade values
- `SimulationStatusCard.vue` - Show simulation progress
- `WishlistItemTable.vue` - Sortable item list
- `src/api/wishlist.ts` - API client
- `src/api/simulation.ts` - API client

#### Features
- Display wishlist items sorted by upgrade value
- Show simulation source (Raidbots vs wishlist percentage)
- Trigger new simulations
- Real-time simulation progress
- Stale data warnings

---

### 2. Performance Domain (Warcraft Logs)

#### API Endpoints (Backend)
```
GET /api/raider-warcraft-logs/raider/{raiderId}
GET /api/raider-warcraft-logs/raider/{raiderId}/count
```

#### Frontend Components Needed
- `PerformancePage.vue` - Full performance metrics
- `PerformanceChart.vue` - Line chart for trends
- `WarcraftLogsTable.vue` - Recent reports table
- `MASBreakdown.vue` - MAS score breakdown
- `src/api/performance.ts` - API client

#### Features
- MAS (Mechanical Adherence Score) breakdown
- DPA/ADT metrics with spec comparison
- Performance trend chart (last 30 days)
- Recent Warcraft Logs reports
- Percentile visualization

---

### 3. Attendance Domain

#### API Endpoints (Backend)
```
POST /api/v1/attendance/track
GET  /api/v1/attendance/raiders/{raiderId}/report
GET  /api/v1/attendance/{recordId}
PUT  /api/v1/attendance/{recordId}
DELETE /api/v1/attendance/{recordId}
GET  /api/v1/attendance/raider/{raiderId}
GET  /api/v1/attendance/guild/{guildId}/summary
```

#### Frontend Components Needed
- `AttendancePage.vue` - Personal attendance view
- `AttendanceChart.vue` - Attendance trend visualization
- `AttendanceCalendar.vue` - Calendar view of attendance
- `src/api/attendance.ts` - API client

#### Features
- Personal attendance history
- Guild attendance summary
- Attendance percentage over time
- Calendar view of raid attendance
- ACS (Attendance Commitment Score) breakdown

---

### 4. Raids Domain

#### API Endpoints (Backend)
```
GET  /api/v1/raids
GET  /api/v1/raids/{id}
POST /api/v1/raids
PUT  /api/v1/raids/{id}
DELETE /api/v1/raids/{id}
GET  /api/v1/raids/team/{teamId}
GET  /api/v1/raids/date-range

# Raid Encounters
GET  /api/raid-encounters/raid/{raidId}

# Raid Signups
GET  /api/raid-signups/raid/{raidId}
POST /api/raid-signups
```

#### Frontend Components Needed
- `RaidsPage.vue` - Raid list and schedule
- `RaidDetailPage.vue` - Single raid details
- `RaidSignupForm.vue` - Sign up for raids
- `RaidEncounterList.vue` - Encounter progress
- `src/api/raids.ts` - API client

#### Features
- View upcoming raids
- Sign up for raids
- View raid history
- Encounter progress tracking
- Raid composition view

---

### 5. Gear Domain

#### API Endpoints (Backend)
```
GET /api/gear
GET /api/gear/{id}
GET /api/raider-gear-items/raider/{raiderId}
GET /api/raider-vault-slots/raider/{raiderId}
```

#### Frontend Components Needed
- `GearPage.vue` - Character gear inspection
- `GearSlotCard.vue` - Individual gear slot
- `VaultDisplay.vue` - Great Vault options
- `src/api/gear.ts` - API client

#### Features
- View current equipped gear
- Item level summary
- Great Vault options
- Upgrade recommendations
- Missing enchants/gems warnings

---

### 6. Applications Domain (Admin)

#### API Endpoints (Backend)
```
GET  /api/applications
GET  /api/applications/{id}
POST /api/applications
PUT  /api/applications/{id}
DELETE /api/applications/{id}
GET  /api/applications/status/{status}

# Application Questions
GET  /api/application-questions
POST /api/application-questions

# Application Files
GET  /api/application-question-files/question/{questionId}
```

#### Frontend Components Needed
- `ApplicationsPage.vue` - Application management (Admin)
- `ApplicationDetailPage.vue` - View single application
- `ApplicationForm.vue` - Apply to guild
- `src/api/applications.ts` - API client

#### Features
- View pending applications (Admin)
- Review and approve/reject
- Application form for applicants
- File uploads for questions
- Application status tracking

---

## Updated Router Configuration

```typescript
// New routes to add
{ path: 'wishlist', component: WishlistPage },
{ path: 'performance', component: PerformancePage },
{ path: 'attendance', component: AttendancePage },
{ path: 'raids', component: RaidsPage },
{ path: 'raids/:id', component: RaidDetailPage },
{ path: 'gear', component: GearPage },
{ path: 'admin/applications', component: ApplicationsPage },
{ path: 'admin/applications/:id', component: ApplicationDetailPage },
{ path: 'apply', component: ApplicationForm },
```

---

## Implementation Priority

### Phase 1 - Core Features (Complete)
1. ✅ WishlistPage - Full implementation with simulation integration
2. ✅ PerformancePage - Full implementation with Warcraft Logs data
3. ✅ SimulationPage integration into Wishlist (polling, progress, trigger)

### Phase 2 - Engagement Features (Complete)
4. ✅ AttendancePage - List view, calendar view, ACS breakdown
5. ✅ RaidsPage + RaidDetailPage - Raid list, signups, encounters
6. ✅ GearPage - Gear inspection, vault options, warnings

### Phase 3 - Admin Features
7. ApplicationsPage (Admin)
8. ApplicationForm (Public)

---

## Files to Create

### API Clients
- `src/api/wishlist.ts` ✅
- `src/api/simulation.ts`
- `src/api/performance.ts` ✅
- `src/api/attendance.ts`
- `src/api/raids.ts`
- `src/api/gear.ts`
- `src/api/applications.ts`

### Pages
- `src/pages/WishlistPage.vue` (update)
- `src/pages/PerformancePage.vue` (update)
- `src/pages/AttendancePage.vue` (new)
- `src/pages/RaidsPage.vue` (new)
- `src/pages/RaidDetailPage.vue` (new)
- `src/pages/GearPage.vue` (new)
- `src/pages/ApplicationsPage.vue` (new)
- `src/pages/ApplicationDetailPage.vue` (new)
- `src/pages/ApplicationForm.vue` (new)

### Components
- `src/components/SimulationStatusCard.vue`
- `src/components/WishlistItemTable.vue`
- `src/components/PerformanceChart.vue`
- `src/components/MASBreakdown.vue`
- `src/components/AttendanceChart.vue`
- `src/components/AttendanceCalendar.vue`
- `src/components/RaidSignupForm.vue`
- `src/components/GearSlotCard.vue`
- `src/components/VaultDisplay.vue`
