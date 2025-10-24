# EdgeRush LootMan System Overview

## Purpose
EdgeRush LootMan (ELM) automates the scoring, eligibility, and loot distribution logic defined in the Progression-Optimized Loot Council Policy (POLCP). The system ingests data from WoWAudit, Warcraft Logs, Wipefest, and Raidbots Droptimizer to deliver transparent, data-driven loot decisions tuned for Cutting Edge progression.

## 1. Core Scoring Model

```
FLPS = (RMS × IPI) × RDF
```

- **Raider Merit Score (RMS)** – Behavioral metrics: attendance, mechanical execution, external preparation.
- **Item Priority Index (IPI)** – Simulated upgrade value, tier completion urgency, and role weighting.
- **Recency Decay Factor (RDF)** – Fairness modifier that rotates contested loot among high performers.

### 1.1 Data Inputs
- **Attendance & Tardiness** – Raid-Helper, Method Raid Tools, Warcraft Logs.
- **Mechanical Execution** – Warcraft Logs death/avoidable damage exports, Wipefest summaries.
- **External Preparation** – Great Vault unlocks, crest usage, heroic farming captured via WoWAudit/Raider.IO.
- **Simulation Data** – Raidbots Droptimizer submissions stored in WoWAudit.
- **Loot History** – RC Loot Council exports synced into WoWAudit for RDF tracking.

### 1.2 Pseudocode Summary
```python
def calculate_RMS(acs, mas, eps):
    return (acs * 0.4) + (mas * 0.4) + (eps * 0.2)

def calculate_RDF(last_award_tier, weeks_since_award):
    if last_award_tier == "A":
        penalty = max(0.8 + 0.1 * weeks_since_award, 1.0)
    elif last_award_tier == "B":
        penalty = max(0.9 + 0.1 * weeks_since_award, 1.0)
    else:
        penalty = 1.0
    return min(penalty, 1.0)

def calculate_FLPS(acs, mas, eps, uv, tier_bonus, role, last_award_tier, weeks_since_award):
    rms = calculate_RMS(acs, mas, eps)
    role_multiplier = {"DPS": 1.0, "Tank": 0.8, "Healer": 0.7}[role]
    ipi = (uv * 0.45) + (tier_bonus * 0.35) + (role_multiplier * 0.20)
    rdf = calculate_RDF(last_award_tier, weeks_since_award)
    return (rms * ipi) * rdf
```

## 2. Raider Merit Score (RMS) Details

### 2.1 Attendance Commitment Score (ACS)
```python
if attendance >= 100:
    acs = 1.0
elif attendance >= 80:
    acs = 0.9
else:
    acs = 0.0  # ineligible for contested loot
```

### 2.2 Mechanical Adherence Score (MAS)
```python
if dpa > 1.5 * spec_avg_dpa or adt > 1.5 * spec_avg_adt:
    mas = 0.0  # critical penalty
else:
    mas = 1 - ((dpa/spec_avg_dpa - 1) * 0.25 + (adt/spec_avg_adt - 1) * 0.25)
```

### 2.3 External Preparation Score (EPS)
```python
eps = (vault_slots / 3 * 0.5) + (crest_usage / 1.0 * 0.3) + (heroic_bosses / 6 * 0.2)
eps = min(eps, 1.0)
```

## 3. Item Priority Index (IPI) Details

### 3.1 Upgrade Value (UV)
```python
uv = simulated_gain / spec_avg_dps
```

### 3.2 Tier Set Completion Modifier
```python
if missing_2pc:
    tier_bonus = 1.2
elif missing_4pc:
    tier_bonus = 1.1
else:
    tier_bonus = 1.0
```

### 3.3 Role Multiplier (PM)
```python
role_multiplier = {"DPS": 1.0, "Tank": 0.8, "Healer": 0.7}[role]
```

### 3.4 Combined IPI Formula
```python
ipi = (uv * 0.45) + (tier_bonus * 0.35) + (role_multiplier * 0.20)
```

## 4. Recency Decay Factor (RDF)

- **A-tier awards**: Apply a 20% penalty, decaying over two raid weeks.
- **B-tier awards**: Apply a 10% penalty, decaying over one raid week.
- **C-tier awards**: No penalty.

```python
def rdf_penalty(last_award_tier, weeks_since_award):
    base = {"A": 0.8, "B": 0.9, "C": 1.0}.get(last_award_tier, 1.0)
    decay = base + 0.1 * weeks_since_award
    return min(decay, 1.0)
```

## 5. Eligibility & Award Logic

```python
eligible = (acs >= 0.8) and (mas > 0.0)
eligible_raiders = [r for r in raiders if r.eligible]
winner = max(eligible_raiders, key=lambda r: r.flps)

if abs(r1.flps - r2.flps) <= 0.05:
    if r1.rms > r2.rms:
        winner = r1
    elif r2.rms > r1.rms:
        winner = r2
    else:
        winner = random.choice([r1, r2])  # /roll fallback
```

## 6. Transparency Outputs
- Raider name and requested item.
- FLPS value with RMS/IPI/RDF breakdowns.
- RDF penalty percentage.
- Award rationale referencing key metrics (e.g., tier completion, MAS).

Example announcement:
```
Item: Fyr’alath, the Dream Render
Awarded To: PlayerX
FLPS: 0.872
Rationale: 4pc completion priority, highest MAS (0.98), RDF -10% (B-tier, 1 week since award)
```

## 7. Implementation Guide (Automation Roadmap)

1. **Establish Data Infrastructure**
   - Use WoWAudit as the source of truth and connect Battle.net/Raider.IO accounts.
   - Sync attendance, sims, logs, and loot via supported integrations.

2. **WoWAudit API Integration**
   - Generate guild API key (`Settings → API Access`).
   - Key endpoints: `/guild/attendance`, `/guild/loot`, `/guild/sims`, `/guild/characters`.
   - Automate pulls with scheduled scripts or Discord bot commands.

3. **External Data Sync**
   - Attendance: Raid-Helper or MRT exports → WoWAudit.
   - Mechanical performance: Warcraft Logs + Wipefest API summaries.
   - External preparation: Great Vault unlocks, crest usage via WoWAudit/Raider.IO.

4. **IPI Automation**
   - Require Droptimizer URL submissions.
   - Normalize upgrade gains per spec baseline.
   - Track tier progress (2pc/4pc) and enforce 5th-piece lockout until token group completes.
   - Apply role multipliers and encounter-specific adjustments.

5. **RDF Management**
   - Record each contested award with tier, date, and RDF expiry.
   - Decay penalties weekly; notify raiders when back in full contention.

6. **FLPS Computation Pipeline**
   - Calculate RMS, IPI, RDF for eligible raiders.
   - Filter out ineligible applicants (attendance < 80% or MAS critical penalties).
   - Rank results and publish to WoWAudit dashboard, Discord embeds, or RCLC notes.

7. **Loot Council Operations**
   - Integrate FLPS outputs with RC Loot Council voting frames.
   - Announce winners with metric-based rationale.
   - Log decisions in WoWAudit or shared sheets for audit.

8. **Governance & Maintenance**
   - Weekly recomputation of RMS/IPI, automatic RDF decay.
   - Optionally review weights after every third Mythic kill.
   - Manage appeals via Discord tickets referencing FLPS breakdown.

9. **Enhancements (Optional)**
   - Discord bot posting loot awards and RDF expirations.
   - Visualization dashboards (Data Studio/Power BI) for attendance, loot share, FLPS trends.
   - Canva prototypes for UI dashboards prior to frontend build.

## 8. Website Vision (Future Discussion)
- **Architecture Recommendation**: Flutter front-end (web/desktop/mobile), Kotlin Spring Boot backend, PostgreSQL database, OAuth2 auth (Discord/Battle.net).
- **Key Features**: Player dashboards, admin panel for council overrides, appeals portal, automation scripts for API sync.
- **Status**: Reserved for later design sessions—framework decisions remain open until coding phase.

Refer back to this document during implementation planning to ensure alignment with POLCP requirements and transparency goals.

