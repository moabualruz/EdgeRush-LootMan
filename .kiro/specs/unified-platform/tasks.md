# Implementation Tasks - EdgeRush LootMan Unified Platform

## Overview

This document provides a detailed breakdown of all implementation phases and tasks for building the complete EdgeRush LootMan platform with feature parity to RaidPlan.io, Raidbots, WoWAudit, and RCLootCouncil.

---

## Phase 0: Foundation (Current State)

**Status: 85% Complete**

These tasks represent the current system state that serves as the foundation for new features.

### Completed ✅
- [x] FLPS algorithm implementation
- [x] WoWAudit integration (characters, attendance, loot)
- [x] Warcraft Logs integration (performance, MAS)
- [x] SimulationCraft integration (local Docker)
- [x] PostgreSQL database with 20 migrations
- [x] Spring Boot REST API foundation
- [x] JWT authentication
- [x] Vue 3 frontend scaffolding
- [x] Docker Compose orchestration
- [x] 3000+ tests with 98% coverage

### Remaining
- [ ] Complete REST API for all 45+ entities (40% done)
- [ ] Reach 85% test coverage target

---

## Phase 1: Complete REST API & GraphQL

**Dependencies**: None
**Estimated Effort**: 2-3 weeks

### Task 1.1: Complete Entity CRUD Controllers
```
For each remaining entity, implement:
1. Controller with standard CRUD endpoints
2. DTO request/response classes
3. Service layer with business logic
4. Repository methods
5. Unit tests (controller, service)
6. Integration tests
```

**Entities to Complete**:
- [ ] RaidSignup controller and tests
- [ ] GuildApplicationQuestion controller and tests
- [ ] GuildApplicationAnswer controller and tests
- [ ] RaidEncounterProgress controller and tests
- [ ] Remaining entity controllers per REST API spec

### Task 1.2: Implement GraphQL API
```
1. Add graphql-kotlin-spring-server dependency
2. Create GraphQL config with custom scalars
3. Implement Query resolvers for all entities
4. Implement Mutation resolvers for CRUD
5. Add DataLoaders to prevent N+1
6. Implement Subscription for real-time updates
7. Add field-level authorization
8. Write tests for all resolvers
```

**Files to Create**:
- `api/graphql/config/GraphQLConfig.kt`
- `api/graphql/query/*QueryResolver.kt`
- `api/graphql/mutation/*MutationResolver.kt`
- `api/graphql/subscription/*SubscriptionResolver.kt`
- `api/graphql/dataloader/*.kt`

### Task 1.3: Real-Time WebSocket Support
```
1. Configure Spring WebSocket with STOMP
2. Implement message broker for subscriptions
3. Create WebSocket authentication filter
4. Add FLPS update broadcasts
5. Add loot award broadcasts
6. Add raid signup broadcasts
7. Test real-time functionality
```

---

## Phase 2: Web Dashboard Core

**Dependencies**: Phase 1 (API complete)
**Estimated Effort**: 3-4 weeks

### Task 2.1: Authentication UI
```
1. Create login page with Discord/Battle.net OAuth buttons
2. Implement OAuth redirect handlers
3. Create authentication store (Pinia)
4. Add JWT token management
5. Implement logout functionality
6. Add session persistence
7. Create protected route guards
```

**Files to Create**:
- `views/LoginView.vue`
- `stores/auth.ts`
- `composables/useAuth.ts`
- `router/guards/auth.ts`

### Task 2.2: Personal Dashboard
```
1. Create dashboard layout component
2. Implement FLPS score display card
3. Create RMS breakdown component
4. Create IPI breakdown component
5. Create RDF status component
6. Add eligibility status display
7. Create recent loot widget
8. Create upcoming events widget
9. Implement FLPS trend chart (Chart.js)
10. Add real-time updates via WebSocket
```

**Files to Create**:
- `views/DashboardView.vue`
- `components/dashboard/FlpsScoreCard.vue`
- `components/dashboard/RmsBreakdown.vue`
- `components/dashboard/IpiBreakdown.vue`
- `components/dashboard/RdfStatus.vue`
- `components/dashboard/RecentLoot.vue`
- `components/dashboard/FlpsTrendChart.vue`

### Task 2.3: Wowhead Tooltip Integration
```
1. Add Wowhead tooltip script to index.html
2. Create WowheadLink component
3. Create WowheadTooltip composable
4. Implement item linking with bonus IDs
5. Add spell tooltip support
6. Test tooltip rendering
```

**Files to Create**:
- `components/common/WowheadLink.vue`
- `composables/useWowheadTooltip.ts`

### Task 2.4: Loot History View
```
1. Create loot history page
2. Implement data table with sorting
3. Add Wowhead item tooltips
4. Create date range filter
5. Create difficulty filter
6. Create slot filter
7. Create RDF status filter
8. Add RDF countdown timers
9. Implement CSV export
10. Add pagination/infinite scroll
```

**Files to Create**:
- `views/LootHistoryView.vue`
- `components/loot/LootHistoryTable.vue`
- `components/loot/LootHistoryFilters.vue`

### Task 2.5: Guild Leaderboard
```
1. Create leaderboard page
2. Implement sortable data table
3. Add class icons and colors
4. Create role filter
5. Create class filter
6. Create eligibility filter
7. Highlight current user row
8. Add tie-breaker explanation tooltips
9. Implement real-time updates
10. Add CSV export
```

**Files to Create**:
- `views/LeaderboardView.vue`
- `components/leaderboard/LeaderboardTable.vue`
- `components/leaderboard/LeaderboardFilters.vue`

### Task 2.6: Performance Metrics View
```
1. Create performance page
2. Display Warcraft Logs parse data
3. Create parse percentile chart
4. Display DPA/ADT metrics
5. Create performance trend chart
6. Highlight critical issues
7. Add links to Warcraft Logs reports
8. Calculate MAS impact display
```

**Files to Create**:
- `views/PerformanceView.vue`
- `components/performance/ParseChart.vue`
- `components/performance/DpaAdtMetrics.vue`
- `components/performance/PerformanceTrend.vue`

### Task 2.7: Attendance View
```
1. Create attendance page
2. Display attendance statistics
3. Create calendar view (vue-cal)
4. Show ACS breakdown
5. Add status type differentiation
6. Create date range filter
7. Add tier filter
```

**Files to Create**:
- `views/AttendanceView.vue`
- `components/attendance/AttendanceStats.vue`
- `components/attendance/AttendanceCalendar.vue`

### Task 2.8: Wishlist View
```
1. Create wishlist page
2. Display wishlist items with tooltips
3. Show upgrade value percentages
4. Add simulation source indicators
5. Create staleness warnings
6. Group by drop source
7. Add sorting options
8. Calculate expected FLPS per item
```

**Files to Create**:
- `views/WishlistView.vue`
- `components/wishlist/WishlistTable.vue`

---

## Phase 3: Admin Panel

**Dependencies**: Phase 2
**Estimated Effort**: 2-3 weeks

### Task 3.1: Configuration Panel
```
1. Create admin configuration page
2. Build FLPS weight editor
3. Build RMS component weight editor
4. Build IPI component weight editor
5. Build RDF decay parameter editor
6. Add validation for all fields
7. Implement save with audit logging
8. Add preview of impact on scores
9. Add export/import functionality
10. Add reset to defaults
```

**Files to Create**:
- `views/admin/ConfigurationView.vue`
- `components/admin/FlpsWeightEditor.vue`
- `components/admin/ConfigPreview.vue`

### Task 3.2: Behavioral Action Management
```
1. Create behavioral actions page
2. Build action creation form
3. Add raider selection dropdown
4. Implement duration picker
5. Display active actions list
6. Show FLPS impact
7. Add edit/remove functionality
8. Display historical actions
9. Implement audit logging
```

**Files to Create**:
- `views/admin/BehavioralActionsView.vue`
- `components/admin/ActionForm.vue`
- `components/admin/ActionsList.vue`

### Task 3.3: Loot Ban Management
```
1. Create loot bans page
2. Build ban creation form
3. Add ban scope options (item, slot, all)
4. Implement duration picker
5. Display active bans
6. Show eligibility impact
7. Add edit/remove functionality
8. Display historical bans
```

**Files to Create**:
- `views/admin/LootBansView.vue`
- `components/admin/BanForm.vue`
- `components/admin/BansList.vue`

### Task 3.4: User Management
```
1. Create user management page
2. Display user list with roles
3. Add character linking display
4. Implement role change
5. Add account disable/enable
6. Display activity log
7. Add bulk actions
```

**Files to Create**:
- `views/admin/UsersView.vue`
- `components/admin/UsersList.vue`
- `components/admin/UserDetail.vue`

### Task 3.5: Audit Log View
```
1. Create audit log page
2. Implement log table with pagination
3. Add date range filter
4. Add action type filter
5. Add actor filter
6. Add target filter
7. Implement CSV export
```

**Files to Create**:
- `views/admin/AuditLogView.vue`
- `components/admin/AuditLogTable.vue`

---

## Phase 4: Raid Planning (RaidPlan.io Parity)

**Dependencies**: Phase 2
**Estimated Effort**: 4-5 weeks

### Task 4.1: Plan Editor Foundation
```
1. Create canvas-based plan editor (Konva.js or Fabric.js)
2. Implement zoom and pan controls
3. Add grid snapping
4. Implement undo/redo stack
5. Create save/load functionality
6. Add export as image
```

**Files to Create**:
- `views/RaidPlanView.vue`
- `components/raidplan/PlanCanvas.vue`
- `composables/useCanvasEditor.ts`
- `stores/planEditor.ts`

### Task 4.2: Encounter Maps
```
1. Fetch encounter data from Blizzard API
2. Create encounter map backgrounds
3. Add boss position markers
4. Add hazard zone overlays
5. Support multiple phases
6. Cache map assets
```

**Files to Create**:
- `components/raidplan/EncounterMap.vue`
- `services/encounterMapService.ts`

### Task 4.3: Markers and Shapes
```
1. Implement raid marker icons (skull, X, etc.)
2. Create role marker icons (tank, healer, DPS)
3. Add player name markers
4. Create class-colored markers
5. Implement shape drawing (circles, arrows, lines)
6. Add text annotations
7. Implement color palette selector
```

**Files to Create**:
- `components/raidplan/MarkerPalette.vue`
- `components/raidplan/ShapeTools.vue`

### Task 4.4: Multi-Step Plans
```
1. Implement step timeline UI
2. Add step navigation (prev/next)
3. Implement step rearrangement (drag)
4. Add step copy/delete
5. Create step transition indicators
6. Implement step-based marker visibility
```

**Files to Create**:
- `components/raidplan/StepTimeline.vue`
- `components/raidplan/StepControls.vue`

### Task 4.5: Plan Sharing
```
1. Implement plan save to database
2. Generate shareable links
3. Add public/guild-only visibility
4. Create plan embed view
5. Add image export
6. Implement plan duplication
```

**Files to Create**:
- `views/PlanShareView.vue`
- `services/planSharingService.ts`

### Task 4.6: Roster Integration
```
1. Load raid roster data
2. Auto-populate player markers
3. Show player availability
4. Highlight missing roles
5. Add roster composition summary
```

**Files to Create**:
- `components/raidplan/RosterPanel.vue`

### Task 4.7: Cooldown Assignments
```
1. Create cooldown assignment grid
2. Load available cooldowns per class
3. Add timeline-based assignment
4. Implement cooldown recovery validation
5. Add overlap warnings
6. Export to MRT note format
7. Export to WeakAura string
```

**Files to Create**:
- `views/CooldownsView.vue`
- `components/cooldowns/CooldownGrid.vue`
- `components/cooldowns/AssignmentTimeline.vue`
- `services/cooldownExportService.ts`

---

## Phase 5: Simulation Features (Raidbots Parity)

**Dependencies**: Phase 2, SimC Docker integration
**Estimated Effort**: 3-4 weeks

### Task 5.1: Character Import Enhancement
```
1. Enhance Blizzard API integration for gear
2. Parse SimC addon strings
3. Display imported gear visually
4. Show import comparison (old vs new)
5. Detect outdated imports
6. Store import history
```

**Files to Create**:
- `views/SimulationView.vue`
- `components/simulation/CharacterImport.vue`
- `services/simcParserService.ts`

### Task 5.2: Droptimizer Feature
```
1. Create droptimizer UI
2. Add source selection (raid, M+, vault)
3. Fetch available items from Blizzard API
4. Queue simulation jobs
5. Display DPS gain per item
6. Calculate expected value per boss
7. Show priority ranking
8. Integrate with wishlist
```

**Files to Create**:
- `views/DroptimizerView.vue`
- `components/simulation/DroptimizerResults.vue`
- `services/droptimizerService.ts`

### Task 5.3: Top Gear Feature
```
1. Create top gear UI
2. Add gear selection (bags, bank, vault)
3. Support gem/enchant optimization
4. Queue combination simulations
5. Display best setup
6. Show alternatives
7. Handle tier set optimization
8. Handle embellishment optimization
```

**Files to Create**:
- `views/TopGearView.vue`
- `components/simulation/TopGearResults.vue`
- `services/topGearService.ts`

### Task 5.4: Gear Compare Feature
```
1. Create gear compare UI
2. Support item ID entry
3. Display DPS comparison
4. Show stat changes
5. Support multiple item comparison
6. Add "what if" scenarios
```

**Files to Create**:
- `views/GearCompareView.vue`
- `components/simulation/GearCompareResults.vue`

### Task 5.5: Simulation Job Queue
```
1. Create simulation job entity
2. Implement job queue system
3. Add job status tracking
4. Implement priority queuing
5. Add batch processing (overnight)
6. Create job history view
7. Add job cancellation
```

**Files to Create**:
- `domain/simulation/SimulationJobEntity.kt`
- `application/simulation/SimulationJobQueueService.kt`
- `views/SimJobsView.vue`

---

## Phase 6: Desktop Client

**Dependencies**: Phase 1 (API)
**Estimated Effort**: 3-4 weeks

### Task 6.1: Tauri Project Setup
```
1. Initialize Tauri project
2. Configure build settings
3. Set up auto-update
4. Create installer packaging
5. Add code signing (Windows/Mac)
```

### Task 6.2: File Watcher
```
1. Implement SavedVariables path detection
2. Create file change watcher
3. Add debouncing for rapid changes
4. Handle WoW installation detection
5. Support multiple WoW installations
```

**Files to Create (Rust)**:
- `src-tauri/src/watcher.rs`
- `src-tauri/src/wow_detector.rs`

### Task 6.3: Lua Parser
```
1. Implement Lua SavedVariables parser
2. Parse gear data structure
3. Parse bag data structure
4. Parse loot history structure
5. Handle parsing errors gracefully
```

**Files to Create (Rust)**:
- `src-tauri/src/parser.rs`
- `src-tauri/src/lua_parser.rs`

### Task 6.4: API Client
```
1. Create authenticated API client
2. Implement sync endpoint calls
3. Handle token refresh
4. Implement offline queue
5. Add retry logic
```

**Files to Create (Rust)**:
- `src-tauri/src/api_client.rs`
- `src-tauri/src/auth.rs`

### Task 6.5: Sync Service
```
1. Implement bidirectional sync
2. Push character data to server
3. Pull FLPS data from server
4. Write FLPS data to SavedVariables
5. Handle conflict resolution
```

**Files to Create (Rust)**:
- `src-tauri/src/sync.rs`

### Task 6.6: System Tray & UI
```
1. Create system tray icon
2. Add tray menu (sync, settings, quit)
3. Create settings window
4. Display sync status
5. Show sync history
6. Add notification support
```

**Files to Create**:
- `src-tauri/src/tray.rs`
- `src/components/StatusPanel.vue`
- `src/components/Settings.vue`

---

## Phase 7: WoW Addon

**Dependencies**: Phase 6 (Desktop Client)
**Estimated Effort**: 4-5 weeks

### Task 7.1: Addon Foundation
```
1. Create addon project structure
2. Set up Ace3 libraries
3. Create TOC file
4. Implement slash commands
5. Create settings panel
6. Add minimap button
```

**Files to Create (Lua)**:
- `EdgeRushLootMan.toc`
- `Core/Core.lua`
- `Core/Constants.lua`
- `Core/Utils.lua`

### Task 7.2: Gear Export
```
1. Implement equipment slot reading
2. Export gear to SavedVariables
3. Implement bag scanning
4. Export bag items
5. Implement talent export
6. Export on logout/reload
```

**Files to Create (Lua)**:
- `Modules/GearSync/GearExport.lua`
- `Modules/GearSync/BagExport.lua`
- `Modules/GearSync/TalentExport.lua`

### Task 7.3: FLPS Display
```
1. Create FLPS frame UI
2. Display score breakdown
3. Implement tooltip integration
4. Create guild leaderboard frame
5. Add minibar display option
```

**Files to Create (Lua)**:
- `Modules/FLPS/Display.lua`
- `Modules/FLPS/Leaderboard.lua`
- `UI/FlpsFrame.lua`

### Task 7.4: Loot Council Interface
```
1. Create loot frame UI
2. Display eligible raiders
3. Show FLPS and upgrade values
4. Implement voting system
5. Record council decisions
6. Announce to raid chat
7. Export to SavedVariables
```

**Files to Create (Lua)**:
- `Modules/LootCouncil/LootFrame.lua`
- `Modules/LootCouncil/VotingFrame.lua`
- `Modules/LootCouncil/AwardHistory.lua`

### Task 7.5: RCLootCouncil Compatibility
```
1. Detect RCLootCouncil presence
2. Hook into RCLC voting frame
3. Add FLPS column to RCLC
4. Export RCLC awards to our system
5. Import RCLC history
```

**Files to Create (Lua)**:
- `Modules/LootCouncil/RCLCCompat.lua`

### Task 7.6: Wishlist Display
```
1. Create wishlist frame
2. Display items with upgrade values
3. Show during loot council
4. Import from desktop client
```

**Files to Create (Lua)**:
- `Modules/Wishlist/Display.lua`
- `Modules/Wishlist/Import.lua`

---

## Phase 8: Discord Bot

**Dependencies**: Phase 1 (API)
**Estimated Effort**: 2-3 weeks

### Task 8.1: Bot Foundation
```
1. Create bot project (Kotlin + Discord4J or JDA)
2. Configure bot token
3. Implement slash command registration
4. Add permission checking
5. Create embed utilities
```

**Files to Create**:
- `discord-bot/src/main/kotlin/Main.kt`
- `discord-bot/src/main/kotlin/commands/CommandRegistry.kt`
- `discord-bot/src/main/kotlin/utils/EmbedBuilder.kt`

### Task 8.2: FLPS Commands
```
1. Implement /flps command
2. Implement /leaderboard command
3. Implement /loot history command
4. Implement /wishlist command
5. Implement /attendance command
```

**Files to Create**:
- `discord-bot/src/main/kotlin/commands/FlpsCommand.kt`
- `discord-bot/src/main/kotlin/commands/LeaderboardCommand.kt`
- `discord-bot/src/main/kotlin/commands/LootHistoryCommand.kt`

### Task 8.3: Admin Commands
```
1. Implement /admin action create
2. Implement /admin action remove
3. Implement /admin ban create
4. Implement /admin ban remove
5. Implement /admin sync
6. Add permission validation
```

**Files to Create**:
- `discord-bot/src/main/kotlin/commands/AdminCommands.kt`

### Task 8.4: Character Linking
```
1. Implement /link command
2. Implement /unlink command
3. Implement /link list command
4. Validate character ownership
5. Handle conflicts
```

**Files to Create**:
- `discord-bot/src/main/kotlin/commands/LinkCommand.kt`

### Task 8.5: Notifications
```
1. Implement loot award notifications
2. Implement RDF expiry notifications
3. Implement behavioral action notifications
4. Implement raid reminder notifications
5. Add notification preferences
```

**Files to Create**:
- `discord-bot/src/main/kotlin/notifications/LootNotifier.kt`
- `discord-bot/src/main/kotlin/notifications/RdfNotifier.kt`
- `discord-bot/src/main/kotlin/notifications/RaidNotifier.kt`

---

## Phase 9: Application Portal

**Dependencies**: Phase 2, Phase 8
**Estimated Effort**: 3-4 weeks

### Task 9.1: Application Form
```
1. Create application flow
2. Implement OAuth connection step
3. Create character selection step
4. Implement data fetching from APIs
5. Create form questions steps
6. Create review and submit step
7. Add form validation
```

**Files to Create**:
- `views/ApplyView.vue`
- `components/apply/OAuthStep.vue`
- `components/apply/CharacterStep.vue`
- `components/apply/PerformanceStep.vue`
- `components/apply/QuestionsStep.vue`
- `components/apply/ReviewStep.vue`

### Task 9.2: Data Enrichment
```
1. Fetch Warcraft Logs data for applicant
2. Fetch Raider.IO data
3. Calculate performance summary
4. Detect red flags
5. Cache external data
```

**Files to Create**:
- `services/applicationEnrichmentService.ts`

### Task 9.3: Officer Review Interface
```
1. Create application list view
2. Create application detail view
3. Implement approve/decline workflow
4. Add note system
5. Add quick stats display
6. Add officer discussion section
```

**Files to Create**:
- `views/admin/ApplicationsView.vue`
- `components/admin/ApplicationList.vue`
- `components/admin/ApplicationDetail.vue`
- `components/admin/ApplicationNotes.vue`

### Task 9.4: Trial System
```
1. Create trial dashboard
2. Implement trial creation on approval
3. Track trial metrics
4. Implement promote/extend/end
5. Add trial milestone notifications
```

**Files to Create**:
- `views/admin/TrialsView.vue`
- `components/admin/TrialDashboard.vue`
- `components/admin/TrialDetail.vue`

### Task 9.5: Discord Integration
```
1. Post new applications to channel
2. Send decision notifications
3. Create applicant channels (optional)
4. Implement application commands
```

**Files to Create**:
- `discord-bot/src/main/kotlin/notifications/ApplicationNotifier.kt`
- `discord-bot/src/main/kotlin/commands/ApplicationCommands.kt`

---

## Phase 10: External Data Integration

**Dependencies**: Phase 1
**Estimated Effort**: 2-3 weeks

### Task 10.1: Blizzard API Client
```
1. Create BlizzardApiClient service
2. Implement OAuth2 client credentials flow
3. Add item data fetching
4. Add spell data fetching
5. Add instance/encounter data fetching
6. Add character profile fetching
7. Implement caching layer
8. Add rate limit handling
```

**Files to Create**:
- `infrastructure/blizzard/BlizzardApiClient.kt`
- `infrastructure/blizzard/BlizzardOAuthService.kt`
- `infrastructure/blizzard/BlizzardCacheService.kt`

### Task 10.2: Raider.IO Integration
```
1. Create RaiderIOClient service
2. Implement character profile fetching
3. Add M+ score data
4. Add raid progression data
5. Implement caching
```

**Files to Create**:
- `infrastructure/raiderio/RaiderIOClient.kt`

### Task 10.3: Enhanced WCL Integration
```
1. Add character rankings queries
2. Add death analysis queries
3. Add damage taken analysis
4. Calculate MAS metrics
5. Add report event fetching
```

**Files to Create**:
- `infrastructure/warcraftlogs/WclAnalysisService.kt`

### Task 10.4: Cache Strategy
```
1. Configure Caffeine for hot cache
2. Add Redis for shared cache (optional)
3. Implement cache warming
4. Add cache statistics endpoint
5. Implement stale-while-revalidate
```

**Files to Create**:
- `config/CacheConfig.kt`
- `infrastructure/cache/CacheWarmingService.kt`

---

## Phase 11: Polish & Performance

**Dependencies**: All previous phases
**Estimated Effort**: 2-3 weeks

### Task 11.1: Performance Optimization
```
1. Add database query optimization
2. Implement response caching
3. Add CDN for static assets
4. Optimize bundle size
5. Add lazy loading for routes
6. Implement virtual scrolling for large lists
```

### Task 11.2: Mobile Optimization
```
1. Test all views on mobile
2. Add responsive breakpoints
3. Optimize touch interactions
4. Add mobile-specific navigation
5. Test on various devices
```

### Task 11.3: Accessibility
```
1. Add ARIA labels
2. Implement keyboard navigation
3. Test with screen readers
4. Ensure color contrast
5. Add skip links
```

### Task 11.4: Analytics & Monitoring
```
1. Add application metrics
2. Implement error tracking
3. Add user analytics (privacy-respecting)
4. Create admin analytics dashboard
5. Add health check endpoints
```

### Task 11.5: Documentation
```
1. Create user documentation
2. Create admin documentation
3. Create API documentation (OpenAPI)
4. Create addon usage guide
5. Create desktop client guide
```

---

## Summary Timeline

| Phase | Focus | Est. Duration |
|-------|-------|---------------|
| Phase 1 | Complete REST/GraphQL API | 2-3 weeks |
| Phase 2 | Web Dashboard Core | 3-4 weeks |
| Phase 3 | Admin Panel | 2-3 weeks |
| Phase 4 | Raid Planning | 4-5 weeks |
| Phase 5 | Simulation Features | 3-4 weeks |
| Phase 6 | Desktop Client | 3-4 weeks |
| Phase 7 | WoW Addon | 4-5 weeks |
| Phase 8 | Discord Bot | 2-3 weeks |
| Phase 9 | Application Portal | 3-4 weeks |
| Phase 10 | External Data Integration | 2-3 weeks |
| Phase 11 | Polish & Performance | 2-3 weeks |
| **Total** | | **30-41 weeks** |

---

## Parallelization Opportunities

Some phases can be worked on in parallel by different developers:

```
                    Phase 1 (Foundation)
                           │
           ┌───────────────┼───────────────┐
           ▼               ▼               ▼
       Phase 2         Phase 6         Phase 8
    (Web Dashboard)  (Desktop Client)  (Discord Bot)
           │               │               │
           ▼               ▼               │
       Phase 3         Phase 7            │
     (Admin Panel)    (WoW Addon)         │
           │               │               │
           ├───────────────┴───────────────┘
           ▼
       Phase 4          Phase 5
   (Raid Planning)   (Simulation)
           │               │
           └───────┬───────┘
                   ▼
              Phase 9
         (Applications)
                   │
                   ▼
              Phase 10
        (External Data)
                   │
                   ▼
              Phase 11
              (Polish)
```

With 2-3 developers working in parallel, estimated timeline: **15-20 weeks**

---

## Success Criteria Checklist

### Feature Parity: RaidPlan.io
- [ ] Visual encounter maps
- [ ] Drag-drop markers
- [ ] Multi-step plans
- [ ] Shareable links
- [ ] Cooldown assignments
- [ ] MRT export

### Feature Parity: Raidbots
- [ ] Character import (SimC string, Armory)
- [ ] Droptimizer analysis
- [ ] Top Gear optimization
- [ ] Gear Compare
- [ ] Local SimC execution

### Feature Parity: WoWAudit
- [ ] Attendance tracking
- [ ] Loot history
- [ ] Wishlist management
- [ ] Raid calendar
- [ ] Desktop client sync

### Feature Parity: RCLootCouncil
- [ ] In-game loot council UI
- [ ] Voting system
- [ ] FLPS integration
- [ ] Award history
- [ ] RCLootCouncil compatibility

### Platform Unique Features
- [ ] FLPS algorithm
- [ ] Transparent score breakdowns
- [ ] Real-time synchronization
- [ ] Unified platform experience
- [ ] Guild application portal
