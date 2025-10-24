# Discord Announcement Templates

## Loot Award Announcement
Channel: `#loot-announcements`
```
:trophy: **{item_name}** awarded to **{raider}**
FLPS: {flps_value} (RMS {rms_value}, IPI {ipi_value}, RDF {rdf_value})
Rationale: {short_reason}  •  Eligibility: {eligibility_note}
```
Example:
```
:trophy: **Fyr'alath, the Dream Render** awarded to **RogueB**
FLPS: 0.583 (RMS 0.960, IPI 0.608, RDF 1.00)
Rationale: Highest MAS (1.00) and 4pc completion priority  •  Eligibility: All requirements met
```

## RDF Cooldown Expiry
Channel: `#progression-updates`
```
:white_check_mark: RDF cooldown cleared for **{raider}**
Penalty Item: {item_name} ({tier})  •  Awarded: {award_date}
Raider is now at full contention for {tier}-tier loot.
```

## Penalty Flag Notification
Channel: Officer-only (e.g., `#loot-council`)
```
:warning: Eligibility flag detected for **{raider}**
Reason: {penalty_reason} (e.g., MAS critical, attendance < 80%)
Action: Review before next raid • Snapshot: FLPS {flps_value}
```

## Weekly Summary Digest
Channel: `#raid-general`
```
:bar_chart: **EdgeRush Loot Summary – Week of {date}**
- A-tier awards: {countA} • B-tier: {countB} • C-tier: {countC}
- New penalties issued: {penalty_count}
- Raiders returning to full contention: {rdf_cleared_list}
View full dashboard: {dashboard_link}
```

> Hook these templates into the future Kotlin data sync service via webhook payloads once automation milestones are complete.

