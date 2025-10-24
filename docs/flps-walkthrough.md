# FLPS Calculation Walkthrough (Mock Data)

This walkthrough demonstrates how EdgeRush LootMan converts data exports into a Final Loot Priority Score (FLPS) for a contested item (Mythic `Fyr'alath, the Dream Render`) using the mock fixtures in `docs/data/`. The computed values are also stored in `docs/data/mock_flps_output.json`.

Reference date: **2025-10-24**

## 1. Source Data Snapshot

- Roster & preparation metrics: `docs/data/mock_wowaudit.json`
- Mechanical performance metrics: `docs/data/mock_logs.json`
- Droptimizer upgrade gains: `docs/data/mock_droptimizer.json`
- Final FLPS results: `docs/data/mock_flps_output.json`

## 2. Raider Merit Score (RMS)

### 2.1 Attendance Commitment Score (ACS)
```
acs = 1.0 if attendance >= 100%
    = 0.9 if 80% <= attendance < 100%
    = 0.0 otherwise
```

| Raider  | Attendance % | ACS |
|---------|--------------|-----|
| MageA   | 98           | 0.9 |
| RogueB  | 92           | 0.9 |
| HunterC | 100          | 1.0 |
| PriestD | 95           | 0.9 |

### 2.2 Mechanical Adherence Score (MAS)
```
if dpa > 1.5 × spec_avg_dpa or adt > 1.5 × spec_avg_adt:
    mas = 0.0
else:
    mas = clamp(1 - ((dpa/spec_avg_dpa - 1) × 0.25
                     + (adt/spec_avg_adt - 1) × 0.25), 0.0, 1.0)
```

| Raider  | DPA | Spec Avg | Ratio | ADT % | Spec Avg | Ratio | MAS    |
|---------|-----|----------|-------|-------|----------|-------|--------|
| MageA   | 0.30| 0.25     | 1.20  | 0.10  | 0.08     | 1.25  | 0.888 |
| RogueB  | 0.18| 0.20     | 0.90  | 0.06  | 0.07     | 0.86  | 1.000 |
| HunterC | 0.35| 0.22     | 1.59  | 0.14  | 0.09     | 1.56  | 0.000 |
| PriestD | 0.22| 0.18     | 1.22  | 0.08  | 0.06     | 1.33  | 0.861 |

> HunterC exceeds the 1.5× threshold on both metrics, triggering the critical penalty.

### 2.3 External Preparation Score (EPS)
```
eps = min(
    (vault_slots / 3) × 0.5 +
    (crest_usage_ratio) × 0.3 +
    (heroic_bosses / 6) × 0.2,
    1.0
)
```

| Raider  | Vault Slots | Crest Usage | Heroic Kills | EPS  |
|---------|-------------|-------------|--------------|------|
| MageA   | 3           | 1.0         | 8            | 1.00 |
| RogueB  | 3           | 0.9         | 7            | 1.00 |
| HunterC | 2           | 0.8         | 6            | 0.77 |
| PriestD | 3           | 1.0         | 8            | 1.00 |

### 2.4 RMS Summary
```
rms = (acs × 0.40) + (mas × 0.40) + (eps × 0.20)
```

| Raider  | ACS | MAS   | EPS  | RMS    |
|---------|-----|-------|------|--------|
| MageA   | 0.9 | 0.888 | 1.00 | 0.915 |
| RogueB  | 0.9 | 1.000 | 1.00 | 0.960 |
| HunterC | 1.0 | 0.000 | 0.77 | 0.554 |
| PriestD | 0.9 | 0.861 | 1.00 | 0.904 |

## 3. Item Priority Index (IPI)

### 3.1 Upgrade Value (UV)
```
uv = simulated_gain / spec_baseline_dps
```

| Raider  | Sim Gain | Baseline DPS | UV    |
|---------|----------|--------------|-------|
| MageA   | 7,750    | 155,000      | 0.050 |
| RogueB  | 7,400    | 148,000      | 0.050 |
| HunterC | 6,100    | 152,000      | 0.040 |
| PriestD | 6,800    | 90,000       | 0.076 |

### 3.2 Tier Bonus
```
if tier_pieces <= 1: tier_bonus = 1.2
elif tier_pieces <= 3: tier_bonus = 1.1
else: tier_bonus = 1.0
```

| Raider  | Tier Pieces | Tier Bonus |
|---------|-------------|------------|
| MageA   | 2           | 1.1        |
| RogueB  | 3           | 1.1        |
| HunterC | 1           | 1.2        |
| PriestD | 3           | 1.1        |

### 3.3 Role Multiplier
```
role_multiplier = {"DPS": 1.0, "Tank": 0.8, "Healer": 0.7}[role]
```

| Raider  | Role   | Multiplier |
|---------|--------|------------|
| MageA   | DPS    | 1.0        |
| RogueB  | DPS    | 1.0        |
| HunterC | DPS    | 1.0        |
| PriestD | Healer | 0.7        |

### 3.4 IPI Summary
```
ipi = (uv × 0.45) + (tier_bonus × 0.35) + (role_multiplier × 0.20)
```

| Raider  | UV    | Tier Bonus | Role Mult. | IPI    |
|---------|-------|------------|------------|--------|
| MageA   | 0.050 | 1.1        | 1.0        | 0.608 |
| RogueB  | 0.050 | 1.1        | 1.0        | 0.608 |
| HunterC | 0.040 | 1.2        | 1.0        | 0.638 |
| PriestD | 0.076 | 1.1        | 0.7        | 0.559 |

> HunterC’s IPI is strong, but MAS = 0.0 will block eligibility later.

## 4. Recency Decay Factor (RDF)
```
if last_award_tier == "A": base = 0.8
elif last_award_tier == "B": base = 0.9
else: base = 1.0
rdf = min(base + 0.1 × weeks_since_award, 1.0)
```

| Raider  | Last Award | Tier | Weeks Since | RDF |
|---------|------------|------|-------------|-----|
| MageA   | 2025-10-10 | A    | 2           | 1.0 |
| RogueB  | 2025-10-17 | B    | 1           | 1.0 |
| HunterC | —          | —    | —           | 1.0 |
| PriestD | 2025-10-03 | C    | 3           | 1.0 |

All RDF penalties have decayed by the reference date.

## 5. FLPS & Eligibility

Eligibility rule:
```
eligible = (acs >= 0.8) and (mas > 0)
```

Final score:
```
flps = (rms × ipi) × rdf
```

| Raider  | RMS   | IPI   | RDF | FLPS  | Eligible? |
|---------|-------|-------|-----|-------|-----------|
| RogueB  | 0.960 | 0.608 | 1.0 | 0.583 | ✅        |
| MageA   | 0.915 | 0.608 | 1.0 | 0.556 | ✅        |
| PriestD | 0.904 | 0.559 | 1.0 | 0.505 | ✅        |
| HunterC | 0.554 | 0.638 | 1.0 | 0.354 | ❌ MAS=0 |

## 6. Award Decision

- Highest eligible FLPS: **RogueB** (0.583).
- Loot council announcement template:
  ```
  Fyr'alath awarded to RogueB — FLPS 0.583 (RMS 0.96, IPI 0.608, RDF 1.0).
  Rationale: Top mechanical adherence (MAS 1.0) and 4pc completion priority.
  ```

## 7. Next Validation Steps
- Import this dataset into the interim dashboard (`docs/dashboard-prototype.md`) to verify formulas.
- Use the same inputs when unit testing the Kotlin scoring service (`ScoreCalculatorTest`).
- Expand mock data with additional roles or active RDF penalties to cover tie-breakers.
