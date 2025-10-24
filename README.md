# EdgeRush LootMan

EdgeRush LootMan (ELM) is a progression-first loot governance blueprint for **World of Warcraft: The War Within** mythic raid teams. It replaces ad‑hoc loot distribution with a transparent, data-backed system tuned to push Cutting Edge kills faster and with fewer roster conflicts. The long-term goal is to deliver an automated scoring platform (see `docs/system-overview.md`) that interfaces with WoWAudit, Warcraft Logs, Wipefest, and Raidbots to calculate Final Loot Priority Scores (FLPS) in real time.

All live data must be collected directly from provider APIs and stored in our own database (PostgreSQL by default, SQLite allowed only for quick local smoke tests); the `wowaudit/` directory contains analysis references only and should never be used as a production data source.

## Why EdgeRush LootMan Exists
- Focus every raid drop on the fastest path to collective progression instead of personal upgrades.
- Flatten loot council drama by tying every decision to public, verifiable metrics.
- Automate the high-friction parts of loot management so officers spend raid nights pulling bosses, not wrangling spreadsheets.

## Core Pillars
- **Accelerated Progression** – Treat each Mythic drop as a raid resource whose job is to remove the next progression wall.
- **Radical Transparency** – Publish the data, the formulas, and the outcomes so raiders can audit every decision.
- **Operational Efficiency** – Lean on automation (WoWAudit, Raidbots Droptimizer, RC Loot Council) to keep the system easy to run week after week.

## Final Loot Priority Score (FLPS)
All contested loot decisions flow through the FLPS. A raider’s score is the product of three components:

```
FLPS = (RMS × IPI) × RDF
```

- **Raider Merit Score (RMS, 40%)** – Attendance commitment, mechanical adherence, and external preparation.
- **Item Priority Index (IPI, 60%)** – Simulated upgrade magnitude, tier set completion urgency, and role multipliers.
- **Recency Decay Factor (RDF)** – Ensures hotly contested loot rotates through top performers instead of funneling indefinitely.

### Raider Merit Score (RMS)
- **Attendance Commitment Score (ACS, 40%)** – Eight-week rolling lookback; unwarned absences lock the raider out of A/B-tier loot for seven days.
- **Mechanical Adherence Score (MAS, 40%)** – Pull-by-pull death rate and avoidable damage from Warcraft Logs and Wipefest. Critical failure caps FLPS until corrected.
- **External Preparation Score (EPS, 20%)** – Weekly Great Vault unlocks, crest usage, and lower-difficulty farming prove the raider has exhausted self-gearing options.

### Item Priority Index (IPI)
- **Upgrade Magnitude (45%)** – Mandatory Raidbots Droptimizer sims, normalized per spec.
- **Tier Set Completion (35%)** – 2-piece and 4-piece bonuses trump marginal stat tweaks; no fifth-piece upgrades until the whole token group finishes 4-piece.
- **Role Multiplier (20%)** – Progression-weighted boost for roles that solve the current wall (e.g., DPS stays at 1.0 baseline, tanks/healers adjust per encounter needs).

### Recency Decay Factor (RDF)
- A-Tier loot (BiS trinkets, high-ilvl weapons) applies a 20% FLPS reduction for two raid weeks; B-Tier applies 10% for one week; C-Tier has no penalty.
- Cooldowns decay linearly and are publicly tracked so everyone can verify rotation fairness.

## Tool Stack
- **RC Loot Council** – Rapid in-game voting with context on current gear and notes.
- **WoWAudit** – Central hub for attendance, sims, wishlists, loot history, and RDF tracking.
- **Raidbots Droptimizer** – Required source for upgrade sims per raider and encounter.
- **Warcraft Logs & Wipefest** – Mechanical execution analytics (deaths, avoidable damage).
- **Raid-Helper / Method Raid Tools / Raid Attendance Tracker** – Attendance automation.
- **Discord (Ticket / Thread System)** – Handles appeals, feedback, and long-form discussions outside raid hours.
- **Automation Targets** – Scheduled scripts hitting the WoWAudit API (`/guild/attendance`, `/guild/loot`, `/guild/sims`, `/guild/characters`) plus Warcraft Logs and Raidbots integrations to feed the future scoring service.
- **Persistence** – PostgreSQL (Docker) as the primary data store; SQLite allowed for local smoke tests. Future ingress consolidation via nginx is planned.

## Operating Workflow
1. **Collect Data Automatically** – Attendance, logs, sims, wishlists feed into WoWAudit.
2. **Pre-Raid Prep** – Officers review flagged penalties, expiring RDF, and priority wishlists.
3. **Post-Kill Process** – Filter applicants for eligibility, compute FLPS, announce recipient with a short data-based rationale.
4. **Record & Decay** – Log the award in WoWAudit, update RDF cooldowns, and highlight any new penalties.
5. **Handle Appeals Off-Raid** – Use Discord tickets referencing the exact RMS/IPI/RDF inputs that determined the outcome.

## Governance Model
- 3–5 council members (Raid Lead, Log Analyst, rotating non-officer) to distribute trust.
- Council members recuse themselves from votes on items where they are candidates.
- Publish all raw inputs (attendance, sims, penalties, loot history) for internal auditing.
- Review the policy after every third Mythic kill or mid-tier to adjust weights, tooling, or penalties based on live progression data.

## Roadmap Ideas
- Build a lightweight dashboard (web or Notion) that surfaces FLPS deltas in real time.
- Automate RDF decay notifications in Discord to remind raiders when they re-enter contention.
- Add testing scripts to validate data imports from WoWAudit or Raidbots after patches.
- Draft onboarding materials (videos or slides) to train new recruits on EdgeRush LootMan expectations.
- Stand up the automated scoring service described in `docs/system-overview.md`, starting with API data ingestion and FLPS calculation scripts.
- Prototype UI flows (e.g., in Canva) for the eventual Flutter + Kotlin + PostgreSQL stack before committing to implementation.

## Contributing
- File issues or proposals in `/docs` with data-backed suggestions.
- Bring encounter-specific exceptions (e.g., healer PM adjustments) with logs ready for review.
- Remember: the system rewards the raid’s fastest path to Cutting Edge. Help us iterate without losing that north star.

## Documentation Map
- `docs/system-overview.md` – End-to-end scoring and automation blueprint.
- `docs/score-model.md` – Detailed formulas and sample values.
- `docs/flps-walkthrough.md` – Worked example using mock data fixtures.
- `docs/onboarding-guide.md`, `docs/onboarding-deck.md`, `docs/onboarding-slides.reveal.md` – Training materials for raiders.
- `docs/project-plan.md` – Phased roadmap with MVP vs extended features.
- `docs/kotlin-data-sync.md`, `docs/automation-notes.md` – Kotlin/Spring Boot data sync design and operational guidance.
- `docs/dashboard-prototype.md` – Interim visualization instructions.
- `docs/discord-templates.md`, `docs/discord-webhook-plan.md` – Messaging templates and rollout plan for automated Discord notifications.
- `docs/wowaudit-spreadsheet.md`, `docs/wowaudit-data-map.md`, `docs/wowaudit-raw-schema.json` – WoWAudit export schema analysis, categorized column inventory, and API mapping for full data parity.
- `assets/onboarding/README.md` – Export instructions for the onboarding slide deck.
- `assets/dashboard/README.md`, `assets/dashboard/mock_flps.csv` – Seed dataset and checklist for the interim FLPS dashboard.
- `wowaudit/War Within Spreadsheet.xlsx` – Analysis-only reference; production data must be fetched via APIs and stored in database (see docs/wowaudit-spreadsheet.md).
