# EdgeRush LootMan — Full Browser Verification & Fix Prompt

> **Copy-paste this entire prompt into a new session.** It will systematically test every feature via the browser, fix what's broken, and iterate until 100%.

---

## PROMPT START

You are performing a **full manual browser verification** of the EdgeRush LootMan web application. Your goal is to visit every page, test every feature and action, verify all sync data is present, and **fix any bugs you find** — iterating until everything works at 100%.

### Environment

- **Project root**: `f:\work\dod\looter`
- **Stack**: Docker Compose (Postgres 18 + Spring Boot backend + Vue 3 frontend + Nginx proxy)
- **Access URL**: `http://localhost` (via Nginx on port 80)
- **Direct Backend**: `http://localhost:8080`
- **Admin Mode**: Backend has `api.admin-mode.enabled=true` — auth is bypassed, all endpoints return 200 OK.
- **Guild ID**: `dod`
- **Sync on startup**: `SYNC_RUN_ON_STARTUP=true` — backend auto-syncs WoWAudit data during boot.

### Phase 0: Environment Readiness

1. Run `docker ps` to verify these containers are **Up**:
   - `edgerush-postgres`
   - `edgerush-data-sync`
   - `edgerush-web-dashboard`
   - `edgerush-nginx`
2. If any are down, run `docker-compose up -d` from `f:\work\dod\looter` and wait.
3. Hit `http://localhost:8080/actuator/health` — wait until status is `UP` (backend takes 2-5 min to compile on first run). If you see 502/503, keep retrying every 30 seconds.
4. Hit `http://localhost` — confirm the Vue app loads (login page or dashboard).

### Phase 1: API Health & Sync Data Verification

Before touching the browser UI, verify the backend has data:

```
GET http://localhost:8080/api/v1/raiders/guild/dod/all
GET http://localhost:8080/api/v1/loot/guilds/dod/history
GET http://localhost:8080/api/v1/flps/guilds/dod/report
GET http://localhost:8080/api/sync-runs/?page=0&size=10
GET http://localhost:8080/api/v1/raids/?page=0&size=10
GET http://localhost:8080/api/v1/attendance/guild/dod/summary
GET http://localhost:8080/api/guild-configurations/guild/dod
GET http://localhost:8080/api/v1/guilds/
```

**For each endpoint:**
- If it returns data → ✅ record it.
- If it returns an empty array or `404` → ⚠️ flag as "No sync data for X" but continue.
- If it returns `500` or crashes → 🔴 investigate the backend logs (`docker logs edgerush-data-sync --tail=100`), find the root cause in the Kotlin code under `data-sync-service/`, fix it, rebuild, and re-verify.

### Phase 2: Full Page-by-Page Browser Testing

Open the browser to `http://localhost`. For EVERY page below, navigate to it, verify it loads without errors, verify it shows data (or a proper empty state), and test every interactive action.

#### 2.1 Login Page (`/login`)
- [ ] Page renders with Discord and Battle.net OAuth buttons
- [ ] Battle.net logo/icon renders correctly
- [ ] "Forgot Password?" link navigates to `/forgot-password`
- [ ] Local login form (if present) accepts input

#### 2.2 Dashboard (`/dashboard`)
- [ ] Redirects here after login (or loads directly in admin mode)
- [ ] Shows personal FLPS score, rank, eligibility status
- [ ] Score breakdown visualization renders (RMS, IPI, RDF)
- [ ] Recent loot widget shows items (or proper empty state)
- [ ] CharacterSelector dropdown works (if multiple characters)
- [ ] No console errors

#### 2.3 Leaderboard (`/leaderboard`)
- [ ] Table loads with raider data (names, scores, classes)
- [ ] Rows are color-coded by role
- [ ] Sorting works (click column headers)
- [ ] No console errors

#### 2.4 Loot History (`/history`)
- [ ] Table loads with loot award records
- [ ] Search/filter works (type a character name)
- [ ] RDF status breakdown chart renders (donut chart)
- [ ] Monthly distribution chart renders (bar chart)
- [ ] FLPS benchmarking chart renders
- [ ] Right-click context menu works (Edit / Revoke actions)
- [ ] "Edit" opens EditLootModal — save works
- [ ] "Revoke" triggers confirmation and deletes the award
- [ ] Item hover shows enhanced tooltip / ghosting preview

#### 2.5 Wishlist (`/wishlist`)
- [ ] Wishlist items load for the current character
- [ ] Add/remove items works
- [ ] No console errors

#### 2.6 Performance (`/performance`)
- [ ] Performance metrics load (MAS, deaths, avoidable damage)
- [ ] Charts/visualizations render
- [ ] No console errors

#### 2.7 Attendance (`/attendance`)
- [ ] Attendance records load
- [ ] Date range filter works
- [ ] Summary statistics display
- [ ] No console errors

#### 2.8 Raids (`/raids`)
- [ ] Raid list loads with raids from sync data
- [ ] Click a raid → navigates to `/raids/:id` (Raid Detail)

#### 2.9 Raid Detail (`/raids/:id`)
- [ ] Encounter list renders
- [ ] Signups display
- [ ] Loot for this raid shows
- [ ] No console errors

#### 2.10 Raid Plans (`/raid-plans`)
- [ ] Plan list loads
- [ ] "Create Plan" button works
- [ ] Click a plan → navigates to `/raid-plans/:id` (Editor)

#### 2.11 Raid Plan Editor (`/raid-plans/:id`)
- [ ] SVG canvas renders with zoom/pan
- [ ] Grid overlay is visible
- [ ] Markers snap to grid when placed/moved
- [ ] Save plan works
- [ ] No console errors

#### 2.12 Gear (`/gear`)
- [ ] Current equipped gear loads for the selected character
- [ ] Item tooltips render (Wowhead integration)
- [ ] Gear set toggle works (EQUIPPED vs BEST)
- [ ] No console errors

#### 2.13 Droptimizer (`/droptimizer`)
- [ ] Page loads with simulation interface
- [ ] Character data populates
- [ ] Submit simulation triggers correctly (or shows proper state)
- [ ] No console errors

#### 2.14 Top Gear (`/top-gear`)
- [ ] Page loads with gear comparison interface
- [ ] Character gear data populates
- [ ] No console errors

#### 2.15 Profile (`/profile`)
- [ ] Profile page renders inside MainLayout (sidebar visible)
- [ ] Battle.net link button renders correctly
- [ ] User info displays
- [ ] No console errors

#### 2.16 Recruitment (`/recruitment`)
- [ ] Recruitment page loads
- [ ] No console errors

#### 2.17 Apply (`/apply`)
- [ ] Public application form renders
- [ ] Fields are interactive
- [ ] Submit button is present
- [ ] No console errors

#### 2.18 Admin Panel (`/admin`)
- [ ] Admin page loads (requires admin role or admin mode)
- [ ] Links to sub-admin pages are visible

#### 2.19 Applications Admin (`/admin/applications`)
- [ ] Application list loads
- [ ] Status filters work (Pending, Approved, Rejected)
- [ ] Click application → detail view opens
- [ ] Accept/Reject/Trial actions work

#### 2.20 Discord Config (`/admin/discord`)
- [ ] Discord bot configuration form renders
- [ ] Settings can be modified and saved
- [ ] No console errors

#### 2.21 Sync History (`/admin/sync`)
- [ ] Sync run history table loads with data from sync_runs
- [ ] Shows duration, status, source for each run
- [ ] "Force Sync" button triggers a new sync
- [ ] Click a sync run → SyncLogViewer modal opens with detailed logs
- [ ] No console errors

#### 2.22 Guild Settings (`/guild-settings`)
- [ ] Settings form loads with current config
- [ ] WoWAudit API Key and Guild URI fields are populated
- [ ] Toggle sync enabled flags
- [ ] Save settings → verify API response is 200
- [ ] Trigger WoWAudit sync from this page
- [ ] Trigger Battle.net sync from this page
- [ ] No console errors

#### 2.23 Sidebar Navigation
- [ ] All links present: Dashboard, Leaderboard, Loot History, Wishlist, Performance, Attendance, Raids, Raid Plans, Droptimizer, Top Gear, Gear, Recruitment
- [ ] Admin section visible (in admin mode): Admin, Sync History, Discord, Guild Settings
- [ ] Every link navigates to the correct page
- [ ] Active link is highlighted

#### 2.24 Forgot Password (`/forgot-password`)
- [ ] Form renders with email input
- [ ] Submit sends request
- [ ] No console errors

#### 2.25 Reset Password (`/reset-password`)
- [ ] Form renders with password fields
- [ ] No console errors

### Phase 3: Sync Data Completeness Verification

After all pages are tested, verify data integrity:

1. **Raiders**: Go to `/leaderboard` — confirm at least 10+ raiders from the guild `dod` are shown. If empty, check `docker logs edgerush-data-sync` for WoWAudit sync errors.
2. **FLPS Scores**: On `/dashboard`, confirm FLPS scores are non-zero for active raiders.
3. **Loot History**: On `/history`, confirm loot awards exist and have item names, raider names, and dates.
4. **Sync Runs**: On `/admin/sync`, confirm at least 1 sync run with status `SUCCESS` or `COMPLETED`.
5. **Raids**: On `/raids`, confirm raid data exists from the sync.
6. **Attendance**: On `/attendance`, confirm attendance records populate.

### Phase 4: Fix-and-Iterate Protocol

For every issue found:

1. **Console Error?** → Check browser dev tools console. Identify the component throwing the error. Fix the `.vue` or `.ts` file. Rebuild frontend (`docker-compose up --build web-dashboard`).
2. **API Error (4xx/5xx)?** → Check `docker logs edgerush-data-sync --tail=200`. Find the Kotlin exception. Fix the code in `data-sync-service/`. Clean and restart: `docker-compose exec data-sync gradle clean && docker-compose restart data-sync`.
3. **Empty Data?** → Verify the sync ran: `GET http://localhost:8080/api/sync-runs/?page=0&size=5`. If no runs, check `.env` has valid `WOWAUDIT_API_KEY` and `WOWAUDIT_GUILD_URI`. Trigger manual sync from Guild Settings page or API.
4. **UI Rendering Bug?** → Screenshot the issue, inspect the DOM, fix the Vue component.
5. **Navigation Broken?** → Check `web-dashboard/src/router/index.ts` for route definitions. Ensure the page component exists and is imported correctly.

**After every fix:**
- Rebuild the affected container
- Re-test the specific feature
- Re-run the full page checklist to confirm no regressions
- Repeat until ALL checkboxes above are ✅

### Phase 5: Final Certification

Once all items pass:

1. Run `docker logs edgerush-data-sync --tail=50` — confirm no ERROR-level logs
2. Open browser console on every page — confirm zero errors
3. Verify sync data is populated across Dashboard, Leaderboard, Loot History, Sync History
4. Create a summary report listing:
   - Pages tested: X/25
   - Issues found: Y
   - Issues fixed: Z
   - Remaining blockers: (list or "None")
   - Final status: **RELEASE READY** or **NEEDS ATTENTION**

### Critical Files Reference

| Area | Key Files |
|------|-----------|
| Router | `web-dashboard/src/router/index.ts` |
| API Layer | `web-dashboard/src/api/*.ts` |
| Pages | `web-dashboard/src/pages/*.vue` |
| Stores | `web-dashboard/src/stores/*.ts` |
| Backend Controllers | `data-sync-service/src/main/kotlin/**/controller/` |
| Backend Services | `data-sync-service/src/main/kotlin/**/service/` |
| Docker | `docker-compose.yml`, `Dockerfile.data-sync`, `web-dashboard/Dockerfile` |
| Nginx | `deploy/nginx/conf.d/lootman.conf` |
| Environment | `.env` |
| Flyway Migrations | `data-sync-service/src/main/resources/db/migration/postgres/` |

## PROMPT END
