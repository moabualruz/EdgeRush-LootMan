# Implementation Plan - Frontend Application

## Overview

This document provides a comprehensive task list for implementing the full EdgeRush LootMan frontend application. Tasks are organized into phases, with backend prerequisites clearly identified.

---

## Phase 0: Backend Prerequisites (Required Before Frontend)

These tasks must be completed in the backend before frontend development can proceed.

### 0.1 Discord User Linking Infrastructure
- [ ] Create migration V0021 for `discord_user_links` table
- [ ] Implement `DiscordUserLink` domain entity
- [ ] Implement `DiscordUserLinkRepository` interface
- [ ] Implement `JdbcDiscordUserLinkRepository`
- [ ] Implement `InMemoryDiscordUserLinkRepository` for tests
- [ ] Create `DiscordLinkingService`
- [ ] Create `DiscordUserLinkController` with CRUD endpoints
- [ ] Write unit tests for service
- [ ] Write integration tests for controller
- _Requirements: R16.1-16.6_
- _Effort: 2-3 days_

### 0.2 User Management Tables
- [ ] Create migration V0022 for `users` table
- [ ] Create migration for `user_refresh_tokens` table
- [ ] Implement `User` domain entity
- [ ] Implement `UserRepository` interface
- [ ] Implement `JdbcUserRepository`
- [ ] Write repository tests
- _Requirements: R1.1-1.7_
- _Effort: 1-2 days_

### 0.3 OAuth2 Authentication Endpoints
- [ ] Configure Discord OAuth2 properties
- [ ] Configure Battle.net OAuth2 properties
- [ ] Implement `OAuth2Service` for token exchange
- [ ] Implement `AuthenticationService` for user management
- [ ] Create `AuthController` with endpoints:
  - GET `/api/v1/auth/discord/url`
  - POST `/api/v1/auth/discord/callback`
  - GET `/api/v1/auth/battlenet/url`
  - POST `/api/v1/auth/battlenet/callback`
  - GET `/api/v1/auth/me`
  - POST `/api/v1/auth/refresh`
  - POST `/api/v1/auth/logout`
- [ ] Write unit tests for services
- [ ] Write integration tests for controller
- _Requirements: R1.1-1.7_
- _Effort: 3-4 days_

### 0.4 User-Character Mapping
- [ ] Create migration V0023 for `user_character_mappings` table
- [ ] Implement `UserCharacterMapping` entity
- [ ] Implement repository
- [ ] Add endpoints to get/set character mappings
- [ ] Write tests
- _Requirements: R16.1-16.6_
- _Effort: 1-2 days_

### 0.5 Enhanced Leaderboard API
- [ ] Add filtering parameters to leaderboard endpoint:
  - `role` (tank, healer, dps)
  - `class` (all WoW classes)
  - `eligible` (true/false)
  - `limit`, `offset` for pagination
- [ ] Update service layer
- [ ] Write tests
- _Requirements: R4.2, R4.3_
- _Effort: 1 day_

---

## Phase 1: Frontend Foundation (Week 1-2)

### 1.1 Project Setup
- [ ] Initialize React + TypeScript + Vite project
- [ ] Configure TypeScript (strict mode)
- [ ] Set up ESLint + Prettier
- [ ] Configure Tailwind CSS
- [ ] Install shadcn/ui components
- [ ] Set up folder structure per design.md
- [ ] Configure path aliases
- _Requirements: R18.1-18.5_
- _Effort: 1 day_

### 1.2 Base Configuration
- [ ] Create environment configuration
- [ ] Set up API client (Axios)
- [ ] Set up GraphQL client (Apollo)
- [ ] Configure TanStack Query
- [ ] Set up Zustand stores
- [ ] Create theme configuration
- _Effort: 1 day_

### 1.3 Layout Components
- [ ] Create AppShell component
- [ ] Create Sidebar navigation component
- [ ] Create Header component
- [ ] Create Footer component
- [ ] Implement responsive breakpoints
- [ ] Create loading and error states
- _Requirements: R18.1-18.5_
- _Effort: 2 days_

### 1.4 Authentication Implementation
- [ ] Create LoginPage with OAuth buttons
- [ ] Create CallbackPage for OAuth redirect
- [ ] Implement AuthProvider context
- [ ] Create useAuth hook
- [ ] Implement token storage
- [ ] Create ProtectedRoute wrapper
- [ ] Implement logout functionality
- [ ] Handle auth errors gracefully
- _Requirements: R1.1-1.7_
- _Effort: 2-3 days_

### 1.5 Routing Setup
- [ ] Configure React Router
- [ ] Create route definitions
- [ ] Implement route guards (auth, admin)
- [ ] Set up 404 page
- [ ] Implement navigation guards
- _Effort: 1 day_

---

## Phase 2: Core Features (Week 3-4)

### 2.1 Personal Dashboard Page
- [ ] Create DashboardPage layout
- [ ] Implement FlpsScoreCard component
- [ ] Implement FlpsBreakdown component
- [ ] Create eligibility status display
- [ ] Display active penalties/bonuses
- [ ] Show recent loot awards (last 5)
- [ ] Display upcoming RDF expirations
- [ ] Add FLPS trend chart
- _Requirements: R2.1-2.8_
- _Effort: 3 days_

### 2.2 Loot History Page
- [ ] Create LootHistoryPage layout
- [ ] Implement LootHistoryTable component
- [ ] Create LootAwardCard component
- [ ] Implement RdfCountdown component
- [ ] Add date range filter
- [ ] Add tier/difficulty filter
- [ ] Add item type filter
- [ ] Implement sorting
- [ ] Add pagination
- _Requirements: R3.1-3.6_
- _Effort: 2-3 days_

### 2.3 Guild Leaderboard Page
- [ ] Create LeaderboardPage layout
- [ ] Implement LeaderboardTable with sorting
- [ ] Create LeaderboardFilters component
- [ ] Add role filter
- [ ] Add class filter
- [ ] Add eligibility filter
- [ ] Highlight current user
- [ ] Show user rank if not visible
- [ ] Add CSV export
- _Requirements: R4.1-4.7_
- _Effort: 2-3 days_

### 2.4 Wishlist Page
- [ ] Create WishlistPage layout
- [ ] Implement WishlistTable component
- [ ] Create ItemCard component
- [ ] Display upgrade values
- [ ] Show simulation source
- [ ] Add stale data warnings
- [ ] Implement sorting
- [ ] Add item detail modal
- _Requirements: R5.1-5.6_
- _Effort: 2 days_

---

## Phase 3: Performance & Attendance (Week 5)

### 3.1 Performance Metrics Page
- [ ] Create PerformancePage layout
- [ ] Implement PerformanceChart component
- [ ] Create MetricCard components (DPA, ADT)
- [ ] Display spec averages
- [ ] Show performance trend
- [ ] Highlight critical issues
- [ ] Link to Warcraft Logs
- [ ] Handle missing data gracefully
- _Requirements: R6.1-6.6_
- _Effort: 2-3 days_

### 3.2 Attendance Page
- [ ] Create AttendancePage layout
- [ ] Implement attendance statistics display
- [ ] Create attendance calendar view
- [ ] Show ACS component impact
- [ ] Differentiate attendance types
- [ ] Add date range filter
- [ ] Add tier filter
- _Requirements: R7.1-7.5_
- _Effort: 2 days_

---

## Phase 4: Admin Features (Week 6-7)

### 4.1 Admin Dashboard
- [ ] Create AdminPage layout
- [ ] Display admin overview cards
- [ ] Show pending actions count
- [ ] Display recent admin activity
- _Requirements: R8.1-8.7_
- _Effort: 1 day_

### 4.2 Configuration Panel
- [ ] Create ConfigPage layout
- [ ] Implement ConfigForm component
- [ ] Display all FLPS parameters
- [ ] Add validation for inputs
- [ ] Implement preview mode
- [ ] Show score change preview
- [ ] Add reset to defaults
- [ ] Implement save with audit log
- _Requirements: R8.1-8.7_
- _Effort: 3 days_

### 4.3 Behavioral Action Management
- [ ] Create BehavioralActionsPage layout
- [ ] Implement ActionForm component
- [ ] Create actions table
- [ ] Support create/edit/delete
- [ ] Show FLPS impact
- [ ] Display action history
- [ ] Implement audit logging
- _Requirements: R9.1-9.5_
- _Effort: 2 days_

### 4.4 Loot Ban Management
- [ ] Create LootBansPage layout
- [ ] Implement BanForm component
- [ ] Create bans table
- [ ] Support create/edit/delete
- [ ] Show eligibility impact
- [ ] Display ban history
- [ ] Implement audit logging
- _Requirements: R10.1-10.5_
- _Effort: 2 days_

---

## Phase 5: Raid & Team Management (Week 8)

### 5.1 Raids Page
- [ ] Create RaidsPage layout
- [ ] Display upcoming raids
- [ ] Implement raid calendar view
- [ ] Show past raids
- [ ] Support signup/withdrawal
- [ ] Create RaidDetailPage
- [ ] Display encounter progression
- _Requirements: R12.1-12.6_
- _Effort: 3 days_

### 5.2 Team Management
- [ ] Create team roster view
- [ ] Display role assignments
- [ ] Show attendance statistics
- [ ] Display FLPS distribution
- [ ] Support roster modifications
- _Requirements: R13.1-13.5_
- _Effort: 2 days_

### 5.3 Loot Council Interface
- [ ] Create LootCouncilPage layout
- [ ] Display eligible raiders for item
- [ ] Show FLPS scores and breakdowns
- [ ] Implement role/eligibility filters
- [ ] Show recommendation with reasoning
- [ ] Highlight tie-breakers
- [ ] Support quick award action
- [ ] Display runner-up comparison
- _Requirements: R11.1-11.8_
- _Effort: 3 days_

---

## Phase 6: Additional Features (Week 9)

### 6.1 Application Management
- [ ] Create applications list view
- [ ] Display application details
- [ ] Show character info
- [ ] Display WCL profile link
- [ ] Support approve/reject
- [ ] Add application notes
- _Requirements: R14.1-14.5_
- _Effort: 2 days_

### 6.2 Analytics & Reporting
- [ ] Create analytics dashboard
- [ ] Implement FLPS distribution chart
- [ ] Create loot distribution analytics
- [ ] Add attendance trends
- [ ] Add performance trends
- [ ] Support date range filtering
- [ ] Implement PDF/CSV export
- _Requirements: R15.1-15.7_
- _Effort: 3 days_

### 6.3 Character Linking
- [ ] Create character management in settings
- [ ] Display linked characters
- [ ] Support adding alts
- [ ] Support unlinking
- [ ] Set primary character
- [ ] Handle conflict scenarios
- _Requirements: R16.1-16.6_
- _Effort: 1-2 days_

### 6.4 Notification Preferences
- [ ] Create notification settings page
- [ ] Support in-app notification config
- [ ] Support email preferences
- [ ] Support Discord DM settings
- [ ] Allow disabling by type
- _Requirements: R17.1-17.3_
- _Effort: 1 day_

---

## Phase 7: Real-time & Polish (Week 10)

### 7.1 Real-time Updates (Requires Backend GAP 5)
- [ ] Implement WebSocket service
- [ ] Connect on app load
- [ ] Handle FLPS update events
- [ ] Handle loot award events
- [ ] Display update notifications
- [ ] Implement fallback polling
- [ ] Show connection status
- _Requirements: R19.1-19.6_
- _Effort: 2-3 days_

### 7.2 Search & Navigation
- [ ] Implement global search
- [ ] Search raiders by name
- [ ] Search items by name
- [ ] Add keyboard shortcuts
- [ ] Implement breadcrumbs
- [ ] Remember user preferences
- _Requirements: R20.1-20.5_
- _Effort: 2 days_

### 7.3 Error Handling
- [ ] Create error boundary
- [ ] Implement user-friendly error messages
- [ ] Add retry functionality
- [ ] Implement graceful degradation
- [ ] Add error logging
- _Requirements: R22.1-22.6_
- _Effort: 1-2 days_

---

## Phase 8: Testing & Documentation (Week 11-12)

### 8.1 Unit Tests
- [ ] Test all custom hooks
- [ ] Test utility functions
- [ ] Test form validation
- [ ] Test state management
- [ ] Achieve 80%+ coverage
- _Effort: 3 days_

### 8.2 Component Tests
- [ ] Test FlpsScoreCard
- [ ] Test LeaderboardTable
- [ ] Test LootHistoryTable
- [ ] Test ConfigForm
- [ ] Test ActionForm
- [ ] Test authentication flow
- _Effort: 3 days_

### 8.3 Integration Tests (Playwright)
- [ ] Test login flow
- [ ] Test dashboard data loading
- [ ] Test leaderboard filtering
- [ ] Test admin configuration
- [ ] Test real-time updates
- _Effort: 3 days_

### 8.4 Accessibility Audit
- [ ] Run automated accessibility tests
- [ ] Test keyboard navigation
- [ ] Test screen reader compatibility
- [ ] Verify color contrast
- [ ] Fix identified issues
- _Requirements: R21.1-21.6_
- _Effort: 2 days_

### 8.5 Documentation
- [ ] Create user guide
- [ ] Create admin guide
- [ ] Document deployment process
- [ ] Create troubleshooting guide
- [ ] Update API documentation
- _Effort: 2 days_

---

## Phase 9: Deployment (Week 12)

### 9.1 Build & Deploy Setup
- [ ] Create production build configuration
- [ ] Set up Docker container
- [ ] Configure nginx
- [ ] Set up CI/CD pipeline
- [ ] Configure environment variables
- [ ] Set up monitoring
- _Effort: 2 days_

### 9.2 Performance Optimization
- [ ] Implement code splitting
- [ ] Optimize bundle size
- [ ] Add image optimization
- [ ] Configure caching headers
- [ ] Run Lighthouse audit
- [ ] Address performance issues
- _Effort: 2 days_

---

## Summary

### Backend Prerequisites
| Task | Effort | Priority |
|------|--------|----------|
| 0.1 Discord User Linking | 2-3 days | P0 |
| 0.2 User Management Tables | 1-2 days | P0 |
| 0.3 OAuth2 Authentication | 3-4 days | P0 |
| 0.4 User-Character Mapping | 1-2 days | P1 |
| 0.5 Enhanced Leaderboard API | 1 day | P1 |
| **Backend Total** | **9-12 days** | |

### Frontend Implementation
| Phase | Effort |
|-------|--------|
| Phase 1: Foundation | 7-8 days |
| Phase 2: Core Features | 9-11 days |
| Phase 3: Performance & Attendance | 4-5 days |
| Phase 4: Admin Features | 8 days |
| Phase 5: Raid & Team | 8 days |
| Phase 6: Additional Features | 7-8 days |
| Phase 7: Real-time & Polish | 5-7 days |
| Phase 8: Testing & Docs | 13 days |
| Phase 9: Deployment | 4 days |
| **Frontend Total** | **65-72 days** |

### Total Project Effort
- **Backend Prerequisites**: 2-3 weeks
- **Frontend Application**: 12-14 weeks
- **Total**: 14-17 weeks

---

## Dependencies

```
Phase 0 (Backend)
    │
    └──▶ Phase 1 (Foundation)
              │
              └──▶ Phase 2 (Core Features)
                        │
                        ├──▶ Phase 3 (Performance)
                        │
                        ├──▶ Phase 4 (Admin)
                        │
                        └──▶ Phase 5 (Raid/Team)
                                  │
                                  └──▶ Phase 6 (Additional)
                                            │
                                            └──▶ Phase 7 (Real-time)
                                                      │
                                                      └──▶ Phase 8 (Testing)
                                                                │
                                                                └──▶ Phase 9 (Deploy)
```

---

## Success Criteria

- [ ] All 22 requirements implemented
- [ ] 80%+ test coverage on frontend code
- [ ] Lighthouse score > 90 for performance
- [ ] WCAG 2.1 AA compliance
- [ ] Real-time updates working
- [ ] Mobile-responsive design
- [ ] All admin features functional
- [ ] OAuth2 authentication working
- [ ] Production deployment complete
