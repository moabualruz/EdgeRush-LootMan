# EdgeRush LootMan Project Plan

## Vision
Deliver a transparent, automated loot governance platform that implements the Progression-Optimized Loot Council Policy (POLCP) end-to-end—data ingestion, scoring, decision support, and accountability dashboards—without sacrificing raid-night efficiency.

## Strategic Goals
- Operationalize the Final Loot Priority Score (FLPS) using live data from WoWAudit, Warcraft Logs, Wipefest, and Raidbots Droptimizer.
- Minimize manual officer workload by automating scoring, eligibility checks, and RDF decay.
- Provide always-on transparency through dashboards, Discord notifications, and auditable decision logs.
- Prototype a future web application (Flutter front end, Kotlin Spring Boot backend, PostgreSQL) once data pipelines and business logic are stable.

## Phase Breakdown

### MVP Feature Set
- Automated RMS, IPI, and RDF calculations driven by WoWAudit + Warcraft Logs + Raidbots data pulls.
- Eligibility filtering and FLPS ranking export (JSON/CSV) for each contested item.
- Basic transparency dashboard (Notion/Sheets) covering FLPS breakdowns and loot history.
- Discord or in-raid messaging templates summarizing award rationales.
- Manual upload workflows for teams without full API access (CSV import supported).
- Kotlin/Spring Boot data-sync service responsible for scheduled API ingestion and score computation (no Python jobs).
- Complete data parity with WoWAudit spreadsheet exports (`raw_data` columns) plus additional metrics required for EDGE (progression, communications).

### Extended Feature Roadmap
- Real-time integrations with RC Loot Council to surface FLPS inside in-game voting frames.
- Automated Discord bot for loot announcements, RDF expiry pings, and penalty alerts.
- Web application (Flutter UI + Kotlin Spring Boot backend + PostgreSQL) with player/admin views.
- OAuth2 authentication via Discord/Battle.net with role-based access control.
- Advanced analytics (loot equity charts, progression correlation, penalty trend reports).
- Visualization dashboards in Data Studio/Power BI and optional mobile companion app.
- Unified ingress layer (e.g., nginx reverse proxy) to consolidate service ports and TLS termination.

### Phase 0 – Foundation (In Progress)
- [x] Document POLCP philosophy, scoring model, and onboarding process (`README.md`, `docs/score-model.md`, `docs/onboarding-guide.md`).
- [x] Capture system overview, pseudocode, and automation roadmap (`docs/system-overview.md`).
- [ ] Define sample data fixtures (mock WoWAudit/Logs exports) for use in demos and testing.

### Phase 1 – Data Infrastructure & Automation
- [ ] Configure WoWAudit API access and document authentication secrets handling.
- [ ] Build Kotlin Spring Boot data-sync service to pull `/guild/attendance`, `/guild/loot`, `/guild/sims`, `/guild/characters`.
- [ ] Map WoWAudit `raw_data` columns to Kotlin domain models ensuring full coverage.
- [ ] Acquire sample WoWAudit API payloads and add deserializer tests for each column group.
- [ ] Provision PostgreSQL via Docker Compose (SQLite fallback profile for quick tests) and establish migration tooling.
- [ ] Normalize attendance, mechanical, and prep metrics into RMS sub-scores.
- [ ] Integrate Warcraft Logs & Wipefest data for MAS calculations.
- [ ] Process Droptimizer exports, normalize upgrade gains, and store tier states.
- [ ] Track loot history and compute RDF decay with automated cooldown alerts.

### Phase 2 – Scoring Engine Prototype
- [ ] Implement FLPS calculator (CLI or service) operating on aggregated datasets.
- [ ] Enforce eligibility filters and tie-breaking logic.
- [ ] Generate transparency outputs (JSON/CSV summaries, Discord embeds, WoWAudit notes).
- [ ] Set up unit tests covering edge cases (critical penalties, RDF boundaries, tie resolutions).

### Phase 3 – Dashboards & Communication
- [ ] Build interim dashboards (Notion, Google Sheets, or lightweight web view) exposing FLPS rankings and history.
- [ ] Automate Discord notifications for loot awards, RDF cooldown expiry, and penalty flags.
- [ ] Produce onboarding slide deck or video aligned with `docs/onboarding-guide.md`.
- [ ] Establish appeals workflow with ticket templates referencing FLPS breakdowns.

### Phase 4 – Web Application Planning (Design Only)
- [ ] Design UI mockups in Canva for player dashboards, admin panels, and loot history views.
- [ ] Draft high-level architecture for Flutter + Kotlin Spring Boot + PostgreSQL stack.
- [ ] Define REST API contracts between frontend and backend (no implementation yet).
- [ ] Outline database schema migrations and data retention policies.

### Phase 5 – Future Implementation (Deferred)
- [ ] Green-light coding framework after validating automation layers and UI designs.
- [ ] Develop backend services, database, and Flutter client in iterative sprints.
- [ ] Integrate OAuth2 authentication (Discord/Battle.net) and role-based access control.
- [ ] Deploy CI/CD pipeline (GitHub Actions) with automated tests and security scanning.

## Risk & Mitigation Log
- **Data Availability** – Confirm API quotas and rate limits for WoWAudit, Warcraft Logs, and Raidbots; cache responses locally.
- **Transparency Trust** – Maintain public dashboards and documented penalties to prevent “black box” perceptions.
- **Officer Bandwidth** – Automate repetitive tasks (RDF decay, data sync) before adding manual review steps.
- **Scope Creep** – Defer coding framework decisions until automation and data confidence are established.

## Next Planning Milestones
- Prepare mock datasets for Phase 1 testing.
- Draft automation script templates (Python/Node) aligned with the step-by-step guide.
- Schedule stakeholder review to prioritize Phase 1 tasks and assign owners.
