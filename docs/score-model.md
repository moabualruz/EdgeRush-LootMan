# EdgeRush LootMan Score Model

This document expands the Final Loot Priority Score (FLPS) into concrete formulas, data pipelines, and sample values officers can reference while auditing decisions.

## 1. Final Loot Priority Score Overview

```
FLPS = (RMS) × (IPI) × (RDF)
```

- `RMS`: Raider Merit Score (0.0 – 1.0 range)
- `IPI`: Item Priority Index (0.0 – 1.25 range depending on encounter needs)
- `RDF`: Recency Decay Factor (0.0 – 1.0 penalty multiplier)

Scores are recalculated per contested item and stored in WoWAudit so raiders can review historic decisions.

---

## 2. Raider Merit Score (RMS)

```
RMS = (ACS × 0.40) + (MAS × 0.40) + (EPS × 0.20)
```

All RMS sub-scores are normalized to 0.0 – 1.0 before weighting.

### 2.1 Attendance Commitment Score (ACS)
- **Data feeds**: Raid-Helper events, Method Raid Tools logs, Warcraft Logs attendance exports.
- **Rolling window**: Eight scheduled raid weeks (e.g., 16 raid nights for a 2-night schedule).
- **Normalization**
  ```
  ACS = min(1.0, Actual Attendance % ÷ 100)
  ```
- **Penalties**
  - Unwarned absence → ACS hard set to 0.0 for seven calendar days.
  - Tardiness > 15 minutes counts as 0.5 attendance for that night.

### 2.2 Mechanical Adherence Score (MAS)
- **Data feeds**: Warcraft Logs (Deaths tab, Avoidable Damage filters), Wipefest summaries.
- **Normalization**
  ```
  DPA Ratio = (Player Deaths per Attempt) ÷ (Spec Average DPA)
  ADT Ratio = (Player Avoidable Damage %) ÷ (Spec Average ADT %)
  MAS = 1 - clamp( (DPA Ratio - 1) × 0.35 + (ADT Ratio - 1) × 0.35 , 0, 1)
  ```
  Values below spec averages raise MAS above 0.9; exceeding 1.5× averages triggers critical failure.
- **Critical failure**
  - If `DPA Ratio > 1.5` **or** `ADT Ratio > 1.5`, set `MAS = 0.2` and flag raider as ineligible for A/B-tier loot until two clean raid nights are logged.

### 2.3 External Preparation Score (EPS)
- **Data feeds**: WoWAudit weekly vault tracker, Raider.IO API, crest spending exports.
- **Normalization**
  ```
  Vault Completion Score = (Unlocked Vault Slots ÷ 3)
  Crest Utilization Score = (Spent Crests ÷ Weekly Cap)
  Off-Night Farming Score = min(1.0, Heroic Bosses Cleared ÷ 8)
  EPS = 0.5 × Vault Completion + 0.3 × Crest Utilization + 0.2 × Off-Night Farming
  ```
- **Penalties**
  - Missing any vault category two weeks in a row halves EPS.
  - Failure to spend capped crests for two resets locks EPS at 0.3 until resolved.

---

## 3. Item Priority Index (IPI)

```
IPI = (Upgrade Magnitude × 0.45) + (Tier Priority × 0.35) + (Role Multiplier × 0.20)
```

All components normalize to 0.0 – 1.0 before weighting, but Role Multiplier may extend above 1.0 when the encounter bottleneck demands it.

### 3.1 Upgrade Magnitude (UM)
- **Data feeds**: Raidbots Droptimizer runs submitted to WoWAudit.
- **Normalization**
  ```
  UM = clamp( Sim Gain ÷ Spec Baseline DPS/HPS , 0, 1.0 )
  ```
- **Example**
  - Fire Mage baseline: 155,000 DPS.
  - Droptimizer upgrade: +7,750 DPS.
  - `UM = 7,750 / 155,000 = 0.05` (5% increase; treated as 0.50 after percentile scaling below).
- **Percentile scaling**
  ```
  UM_scaled = percentile_rank(UM, token_applicants)
  ```
  Officers use percentile rank to compare upgrades across applicants sharing the item.

### 3.2 Tier Priority (TP)
- **Input states**
  - 0.00 → Has 4-piece; item is 5th piece or ilvl swap only.
  - 0.60 → Has 2-piece; item completes 4-piece.
  - 1.00 → Has 0–1 pieces; item grants or completes 2-piece.
- **Override**: Encounter-based adjustments (e.g., healers requested to reach 4-piece before specific boss) can add +0.15.

### 3.3 Role Multiplier (RM)
- **Baseline values**
  - DPS: 1.0
  - Tank: 0.8
  - Healer: 0.7
- **Encounter modifiers**
  - Add up to +0.3 when progression bottleneck favors a role (e.g., healing-intensive boss: Healer RM = 1.0).
  - Apply to entire role cohort for the night and log justification in WoWAudit notes.

---

## 4. Recency Decay Factor (RDF)

- **A-Tier Items**: Initial RDF = 0.80 (20% penalty) for two raid weeks. Decays linearly by 10% per raid reset.
- **B-Tier Items**: Initial RDF = 0.90 (10% penalty) for one raid week. Decays by 10% at next reset.
- **C-Tier Items**: RDF = 1.00 (no penalty).

```
RDF = 1 - (Penalty × Remaining Cooldown ÷ Total Cooldown)
```

### Tracking Guidelines
- Log each award with timestamp and tier classification in WoWAudit.
- Automate reminders in Discord when RDF reaches ≥ 0.95 so raiders know they are back in full contention.
- Officers may manually clear RDF after roster changes (e.g., player moves to bench) but must record the rationale.

---

## 5. Sample Calculation

**Scenario**: Mythic trinket contested by three DPS and one healer.

| Player | RMS | UM_scaled | TP | RM | IPI | RDF | FLPS |
|--------|-----|-----------|----|----|-----|-----|------|
| Mage A | 0.92 | 0.85 | 0.60 | 1.0 | 0.83 | 0.80 | 0.61 |
| Rogue B | 0.88 | 0.70 | 0.00 | 1.0 | 0.59 | 1.00 | 0.52 |
| Hunter C | 0.95 | 0.55 | 0.35 | 1.0 | 0.54 | 1.00 | 0.51 |
| Priest D | 0.90 | 0.75 | 0.60 | 0.7 | 0.55 | 1.00 | 0.49 |

**Outcome**
- Rogue B edges Hunter C slightly due to higher Upgrade Magnitude despite Mage A’s strong RMS; Mage A’s RDF penalty drops them below B.
- Council announces: “Awarded to Rogue B — highest FLPS (0.52) after Mage A’s recent A-tier win applied RDF.”

---

## 6. Audit Checklist
- [ ] All applicants have current Droptimizer links (< 2 resets old).
- [ ] Critical RMS flags resolved or communicated before raid.
- [ ] RDF ledger validated against WoWAudit history.
- [ ] Encounter-specific multipliers posted in raid planning notes.
- [ ] Final award announcement includes RMS/IPI/RDF highlights for transparency.

