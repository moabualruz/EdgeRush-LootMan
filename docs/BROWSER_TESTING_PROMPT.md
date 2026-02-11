# EdgeRush LootMan — Full Browser Verification & Fix Prompt (Battle.net Login)

> **Copy-paste this entire prompt into a new session.** It will systematically test every feature via the browser, fix what's broken, and iterate until 100%.

---

## PROMPT START

You are performing a **full manual browser verification** of the EdgeRush LootMan web application. Your goal is to visit every page, test every button, dropdown, form, link, and action, verify all sync data is present, and **fix any bugs you find** — iterating until everything works at 100%.

### Environment

- **Project root**: `f:\work\dod\looter`
- **Stack**: Docker Compose (Postgres 18 + Spring Boot backend + Vue 3 frontend + Nginx proxy)
- **Access URL**: `http://localhost` (via Nginx on port 80)
- **Direct Backend**: `http://localhost:8080`
- **Guild ID**: `dod`
- **Guild Realm**: `Twisting Nether` (EU)
- **Sync on startup**: `SYNC_RUN_ON_STARTUP=true` — backend auto-syncs WoWAudit data during boot.
- **WarcraftLogs**: Enabled with client ID/secret in `.env`
- **Battle.net OAuth**: Configured with client ID `3e515afebb29462a97fc0c09c8b48072`, redirect URI `http://localhost/auth/battlenet/callback`

### CRITICAL RULES

1. **Login via Battle.net OAuth** — Do NOT use local username/password login. Click the Battle.net login button on the login page.
2. **Test EVERY interactive element** — every button, every dropdown, every form input, every link, every toggle, every right-click action.
3. **Check console for errors** on every page using `capture_browser_console_logs`.
4. **Take a screenshot** of every page after loading and after every significant action.
5. **If something breaks** — investigate, fix the code, rebuild the container, and re-test. Do NOT skip broken features.
6. **Document everything** — keep a running tally of Pass/Fail for every test item.

---

### Phase 0: Environment Readiness

1. Run `docker ps` to verify these containers are **Up**:
   - `edgerush-postgres`
   - `edgerush-data-sync`
   - `edgerush-web-dashboard`
   - `edgerush-nginx`
2. If any are down, run `docker compose up -d` from `f:\work\dod\looter` and wait.
3. Hit `http://localhost:8080/actuator/health` — wait until status is `UP`. If you see 502/503, keep retrying every 30 seconds (backend takes 2-5 min to compile on first run).
4. Hit `http://localhost` — confirm the Vue app loads (should show login page).

---

### Phase 1: API Health & Data Verification

Before touching the browser UI, verify the backend has data via curl:

```sh
curl http://localhost:8080/api/v1/raiders/guild/dod/all
curl http://localhost:8080/api/v1/loot/guilds/dod/history
curl http://localhost:8080/api/v1/flps/guilds/dod/report
curl http://localhost:8080/api/v1/flps/guilds/dod/leaderboard
curl http://localhost:8080/api/sync-runs/?page=0&size=10
curl http://localhost:8080/api/v1/raids/?page=0&size=10
curl http://localhost:8080/api/v1/attendance/guild/dod/summary
curl http://localhost:8080/api/guild-configurations/guild/dod
curl http://localhost:8080/api/v1/guilds/
curl http://localhost:8080/api/v1/warcraftlogs/guilds/dod/reports
```

**For each endpoint:**

- Returns data → ✅
- Returns empty array or 404 → ⚠️ flag but continue
- Returns 500 → 🔴 investigate `docker logs edgerush-data-sync --tail=100`, fix, rebuild

---

### Phase 2: Login via Battle.net OAuth

1. Open `http://localhost/login`
2. **Screenshot** the login page
3. Click the **Battle.net** login button (blue button with BNet logo)
4. You will be redirected to `https://oauth.battle.net/authorize?...`
5. If a Blizzard login form appears, use valid Battle.net credentials to log in
6. After auth, you'll be redirected back to `http://localhost/auth/battlenet/callback?code=...`
7. **Screenshot** after redirect — you should land on the Dashboard
8. If the callback fails with an error, check:
   - Browser console for errors
   - `docker logs edgerush-data-sync --tail=50` for backend errors
   - The `.env` file has correct `BATTLENET_CLIENT_ID`, `BATTLENET_CLIENT_SECRET`, `BATTLENET_REDIRECT_URI`
9. Verify the auth token is set (check cookies or localStorage for `token`)

---

### Phase 3: Full Page-by-Page Testing

Navigate to each page below. For EVERY page:

1. Navigate to the URL
2. Wait for data to load (watch for loading spinners)
3. Take a screenshot
4. Check browser console for errors
5. Test EVERY interactive element listed

---

#### 3.1 Dashboard (`/dashboard`)

**Data checks:**

- [ ] FLPS score displays (non-zero for active raiders)
- [ ] Score breakdown visualization renders (RMS, IPI, RDF components)
- [ ] Rank badge shows position (#1, #2, etc. or "Unranked")
- [ ] Recent loot widget shows items or "No recent loot" empty state

**Actions to test:**

- [ ] Character selector dropdown (if present) — select different characters
- [ ] Click any raider name → navigates somewhere meaningful
- [ ] All cards/widgets render without "undefined" or "NaN"

---

#### 3.2 Leaderboard (`/leaderboard`)

**Data checks:**

- [ ] Table loads with 10+ raiders (names, scores, classes, roles)
- [ ] Rows are color-coded or tagged by role (Tank/Healer/DPS)
- [ ] FLPS scores are numerical, not "NaN" or "undefined"

**Actions to test:**

- [ ] Click **each column header** — verify sorting toggles (asc/desc)
- [ ] Click a **raider row** — verify it opens detail view or modal
- [ ] If there's a search/filter input — type a name and verify filtering works
- [ ] If pagination exists — click Next/Prev/page numbers

---

#### 3.3 Loot History (`/history`)

**Data checks:**

- [ ] Table loads with loot award records
- [ ] Each row shows: item name, raider name, awarded date, FLPS score
- [ ] RDF badges show "RDF Expired" (green) or "RDF Active"/"RDF: X months" (yellow)
- [ ] Stats summary cards show: Total Items count, Avg FLPS, RDF Cleared count
- [ ] Donut chart (RDF Status) renders with segments
- [ ] Bar chart (Loot by Month) renders with bars

**Actions to test:**

- [ ] **Search box** — type an item name or raider name, verify real-time filtering
- [ ] **Sort buttons** — click Date, Item, FLPS sort buttons, verify order changes
- [ ] **Pagination** — click First/Prev/Next/Last if multiple pages
- [ ] **Right-click** any loot row → verify context menu appears with "Edit" and "Revoke"
- [ ] Click **Edit** → verify EditLootModal opens with pre-filled data
- [ ] In EditLootModal: modify a field, click Save → verify modal closes and data updates
- [ ] Click **Revoke** → verify confirmation dialog appears → confirm → verify item removed
- [ ] **Award Loot** button (top right) → verify AwardLootModal opens
- [ ] In AwardLootModal: fill in raider, item ID, tier → submit → verify new award appears
- [ ] Hover over an item → verify tooltip/preview appears (Wowhead integration)

---

#### 3.4 Wishlist (`/wishlist`)

**Data checks:**

- [ ] Wishlist items load for the current character (or shows empty state)

**Actions to test:**

- [ ] **Add item** button/form — add an item to wishlist
- [ ] **Remove item** — delete an item from wishlist
- [ ] **Priority/ranking** — if reorderable, drag or change priority
- [ ] Any dropdowns or form inputs are interactive

---

#### 3.5 Performance (`/performance`)

**Data checks:**

- [ ] Performance metrics load (MAS, deaths, avoidable damage, healing done)

**Actions to test:**

- [ ] **Time range selector** — change the time period if available
- [ ] **Charts/visualizations** — verify they render and are interactive (hover tooltip)
- [ ] Any toggle or filter controls

---

#### 3.6 Attendance (`/attendance`)

**Data checks:**

- [ ] Attendance records load with dates, percentages, raid names
- [ ] Summary statistics show (overall attendance %)

**Actions to test:**

- [ ] **Date range filter** — change start/end dates, verify table updates
- [ ] **Raid filter** — filter by specific raid if available
- [ ] **Sorting** — click column headers
- [ ] Click a specific attendance entry → verify detail view

---

#### 3.7 Raids (`/raids`)

**Data checks:**

- [ ] Raid list loads with raids from sync data (raid name, date, instance)

**Actions to test:**

- [ ] Click a **raid row** → navigates to `/raids/:id` (Raid Detail page)
- [ ] **Pagination** if multiple pages of raids
- [ ] Any filter/search controls

---

#### 3.8 Raid Detail (`/raids/:id`)

**Data checks:**

- [ ] Encounter/boss list renders for the selected raid
- [ ] Signups/roster displays
- [ ] Loot awarded during this raid shows

**Actions to test:**

- [ ] Click different encounters/bosses
- [ ] Any "Assign Loot" or roster management buttons
- [ ] Back button returns to raid list

---

#### 3.9 Raid Plans (`/raid-plans`)

**Data checks:**

- [ ] Plan list loads (or shows empty state "No plans yet")

**Actions to test:**

- [ ] **Create Plan** button → verify plan creation form/wizard opens
- [ ] Fill in plan name, select encounter → save → verify new plan appears in list
- [ ] Click an existing plan → navigates to `/raid-plans/:id` (Plan Editor)
- [ ] **Delete Plan** action (if available)

---

#### 3.10 Raid Plan Editor (`/raid-plans/:id`)

**Data checks:**

- [ ] SVG canvas renders with encounter background

**Actions to test:**

- [ ] **Zoom controls** — zoom in/out, verify canvas scales
- [ ] **Pan** — drag to move the canvas
- [ ] **Place markers** — click to add position markers on the canvas
- [ ] **Move markers** — drag markers to new positions, verify grid snapping
- [ ] **Delete markers** — remove a marker
- [ ] **Save** button → verify plan saves (API response 200)
- [ ] **Grid overlay toggle** — show/hide grid

---

#### 3.11 Gear (`/gear`)

**Data checks:**

- [ ] Character selector dropdown shows guild characters (for admin/officer)
- [ ] Equipped gear loads for selected character (13 gear slots)
- [ ] Each item shows: name, item level, quality color

**Actions to test:**

- [ ] **Character selector** — change character, verify gear updates
- [ ] **Item tooltips** — hover over items, verify Wowhead tooltip renders
- [ ] **Gear set toggle** (if present) — switch between EQUIPPED and BEST
- [ ] Any "Compare" or "Upgrade" indicators

---

#### 3.12 Droptimizer (`/droptimizer`)

**Data checks:**

- [ ] Character dropdown is populated with raider names
- [ ] Slot type filters show (Head, Neck, Shoulders, etc.)

**Actions to test:**

- [ ] **Character dropdown** — select different characters
- [ ] **Slot filter** — click different slot types, verify items update
- [ ] **Run Simulation** button (if present) — click and verify action
- [ ] Any item upgrades or DPS gain displays

---

#### 3.13 Top Gear (`/top-gear`)

**Data checks:**

- [ ] Character dropdown is populated with raider names
- [ ] Fight profile options show (Single Target, AoE, Custom)
- [ ] Tier Set Bonus section renders

**Actions to test:**

- [ ] **Character dropdown** — select different characters
- [ ] **Fight Profile buttons** — click Single Target, AoE, Custom
- [ ] **Calculate Optimal** button → verify calculation runs
- [ ] **Tier Set pieces** toggle (1, 2, 3, 4) buttons
- [ ] **2pc / 4pc** bonus toggle buttons
- [ ] Current Setup vs Optimal Setup comparison areas

---

#### 3.14 Profile (`/profile`)

**Data checks:**

- [ ] Profile page renders inside MainLayout (sidebar visible)
- [ ] User info displays (username, role, email)
- [ ] Battle.net connection status shows (linked/unlinked)

**Actions to test:**

- [ ] **Battle.net Link/Unlink** button
- [ ] **Edit Profile** fields (if editable)
- [ ] **Logout** button → verify redirect to login page
- [ ] Any notification preferences toggles

---

#### 3.15 Recruitment (`/recruitment`)

**Data checks:**

- [ ] Recruitment page loads with class/role needs or application list

**Actions to test:**

- [ ] Any "Apply" or "View Applications" links
- [ ] Class/role need toggles (if admin)

---

#### 3.16 Apply (`/apply`)

**Data checks:**

- [ ] Public application form renders (no auth required)
- [ ] All form fields present: character name, realm, class, spec, experience, etc.

**Actions to test:**

- [ ] Fill in **every field** in the application form
- [ ] **Class/spec dropdowns** — select options
- [ ] **Experience textarea** — type text
- [ ] **Submit** button → verify application submits (or validation errors appear for empty required fields)
- [ ] Test with **empty required fields** → verify validation messages appear

---

#### 3.17 Admin Panel (`/admin`)

**Data checks:**

- [ ] Admin page loads (requires admin role)
- [ ] Admin navigation links visible

**Actions to test:**

- [ ] Click each admin sub-page link
- [ ] Verify role-based access (if non-admin users can't access)

---

#### 3.18 Applications Admin (`/admin/applications`)

**Data checks:**

- [ ] Application list loads (from applications submitted via `/apply`)

**Actions to test:**

- [ ] **Status filter tabs** — click Pending, Approved, Rejected, All
- [ ] Click an **application** → detail view opens
- [ ] **Accept** button → verify application status changes to Approved
- [ ] **Reject** button → verify application status changes to Rejected
- [ ] **Trial** button → verify status changes to Trial
- [ ] Any notes/comments field

---

#### 3.19 Discord Config (`/admin/discord`)

**Data checks:**

- [ ] Discord bot configuration form renders
- [ ] Current settings pre-filled

**Actions to test:**

- [ ] **Edit** any setting (webhook URL, channel ID, notification toggles)
- [ ] **Save** button → verify settings persist (reload and check)
- [ ] **Test Webhook** button (if present) → verify Discord notification fires

---

#### 3.20 Sync History (`/admin/sync`)

**Data checks:**

- [ ] Sync run history table loads with data
- [ ] Each row shows: timestamp, duration, status (SUCCESS/FAILED), source (WoWAudit/BNet/WCL)

**Actions to test:**

- [ ] **Force Sync** button → click and verify a new sync starts
- [ ] Wait for sync to complete → verify new row appears with status
- [ ] Click a **sync run row** → SyncLogViewer modal opens with detailed logs
- [ ] Close the log viewer modal
- [ ] **Pagination** if multiple pages of sync history
- [ ] **WarcraftLogs sync** trigger → verify it completes without error

---

#### 3.21 Guild Settings (`/guild-settings`)

**Data checks:**

- [ ] Settings form loads with current guild configuration
- [ ] WoWAudit API Key field is populated (masked)
- [ ] WoWAudit Guild URI field is populated
- [ ] Sync enabled toggles show current state

**Actions to test:**

- [ ] **Edit** WoWAudit API Key → Save → verify saved
- [ ] **Edit** Guild URI → Save → verify saved
- [ ] **Toggle** sync enabled (on/off) → Save → verify toggle state persists on reload
- [ ] **Toggle** BNet sync enabled → Save → verify
- [ ] **Trigger WoWAudit Sync** button → click → verify sync starts, observe progress
- [ ] **Trigger Battle.net Sync** button → click → verify sync starts
- [ ] **Save Settings** button → verify API response is 200
- [ ] Verify settings persist after page reload

---

#### 3.22 Sidebar Navigation (test on every page)

- [ ] All links visible and organized into sections:
  - **OPERATIONS**: Mission Control, Raid Operations, Loot & Wishlist, Leaderboards, Analysis, Attendance, Raid Strategy
  - **MY RAIDER**: My Gear, History
  - **TOOLS**: Droptimizer, Top Gear
  - **COMMUNITY**: Recruitment
  - **ADMINISTRATION**: System Admin, Recruitment (admin), Discord Bot, Data Sync
- [ ] Click **every single sidebar link** one by one → verify each navigates correctly
- [ ] Active link is highlighted (different color/background)
- [ ] Guild selector dropdown (top of sidebar) works — shows guild name
- [ ] User avatar/name at bottom of sidebar is visible
- [ ] **View Profile** link works
- [ ] **Logout** icon/button works → redirects to login

---

#### 3.23 Forgot Password (`/forgot-password`)

- [ ] Form renders with email input
- [ ] **Submit** → sends request (verify API call happens, even if it errors)
- [ ] **Back to Login** link works

#### 3.24 Reset Password (`/reset-password`)

- [ ] Form renders with new password + confirm password fields
- [ ] **Submit** → sends request
- [ ] Validation: passwords must match

---

### Phase 4: Cross-Cutting Feature Tests

#### 4.1 Responsive Design

- [ ] Resize browser to mobile width (375px) → verify layout adapts
- [ ] Sidebar collapses or becomes hamburger menu on mobile
- [ ] Data tables scroll horizontally or stack on mobile

#### 4.2 Error Handling

- [ ] Disconnect from network briefly → verify error states appear (not blank screens)
- [ ] Navigate to `/nonexistent-page` → verify 404 page renders

#### 4.3 Data Refresh

- [ ] After performing any mutation (award loot, save settings, trigger sync), verify the data on the page updates WITHOUT a manual page reload

---

### Phase 5: Fix-and-Iterate Protocol

For every issue found:

1. **Console Error?** → Check browser dev tools console. Identify the component throwing the error. Fix the `.vue` or `.ts` file. Rebuild: `docker compose up -d --build web-dashboard`
2. **API Error (4xx/5xx)?** → Check `docker logs edgerush-data-sync --tail=200`. Find the Kotlin exception. Fix the code in `data-sync-service/`. Restart: `docker compose up -d --build data-sync`
3. **Empty Data?** → Verify sync ran: `curl http://localhost:8080/api/sync-runs/?page=0&size=5`. If no runs, trigger from Guild Settings or API. Check `.env` for valid keys.
4. **UI Rendering Bug?** → Screenshot, inspect DOM, fix Vue component.
5. **Button doesn't work?** → Check if the click handler exists, if the API method is called, if the API URL is correct (watch for double `/api/api/` paths).
6. **Navigation Broken?** → Check `web-dashboard/src/router/index.ts` for route definitions.

**After every fix:**

- Rebuild the affected container
- Re-test the specific feature
- Re-run the full page checklist to confirm no regressions
- Repeat until ALL checkboxes above are ✅

---

### Phase 6: Final Certification

Once all items pass:

1. Run `docker logs edgerush-data-sync --tail=50` — confirm no ERROR-level logs
2. Open browser console on every page — confirm zero JS errors
3. Verify sync data populated: Dashboard, Leaderboard, Loot History, Sync History, Attendance, Raids
4. Create a summary report:

```md

## Test Report
- **Pages tested**: X/24
- **Interactive elements tested**: Y
- **Issues found**: Z
- **Issues fixed**: W
- **Remaining blockers**: (list or "None")
- **Final status**: RELEASE READY / NEEDS ATTENTION
```

---

### Critical Files Reference

| Area | Key Files |
| ------ | ----------- |
| Router | `web-dashboard/src/router/index.ts` |
| API Layer | `web-dashboard/src/api/*.ts` |
| Pages | `web-dashboard/src/pages/*.vue` |
| Stores | `web-dashboard/src/stores/*.ts` |
| Types | `web-dashboard/src/types/index.ts` |
| Composables | `web-dashboard/src/composables/*.ts` |
| Backend Controllers | `data-sync-service/src/main/kotlin/**/api/` |
| Backend Services | `data-sync-service/src/main/kotlin/**/application/` |
| Docker | `docker-compose.yml`, `Dockerfile.data-sync`, `web-dashboard/Dockerfile` |
| Nginx | `deploy/nginx/conf.d/lootman.conf` |
| Environment | `.env` |
| Flyway Migrations | `data-sync-service/src/main/resources/db/migration/postgres/` |
| Application Config | `data-sync-service/src/main/resources/application.yaml` |

## PROMPT END
