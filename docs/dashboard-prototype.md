# Interim FLPS Dashboard Prototype

Use Google Sheets or Notion databases to visualize mock FLPS outputs while the automated service is under construction.

## Sheet Layout

### Sheet 1 – `FLPS Rankings`
Columns:
1. `Item`
2. `Raider`
3. `Role`
4. `ACS`
5. `MAS`
6. `EPS`
7. `RMS` (formula)
8. `UV`
9. `Tier Bonus`
10. `Role Multiplier`
11. `IPI` (formula)
12. `RDF`
13. `FLPS` (formula)
14. `Eligibility` (TRUE/FALSE)
15. `Award Notes`

Example formulas (Google Sheets):
```
G2 = (D2*0.4)+(E2*0.4)+(F2*0.2)
K2 = (H2*0.45)+(I2*0.35)+(J2*0.2)
M2 = L2*G2*K2
N2 = IF(AND(D2>=0.8,E2>0),TRUE,FALSE)
```

Apply conditional formatting to highlight:
- Eligibility = FALSE (red).
- Highest FLPS per item (green).

### Sheet 2 – `RDF Tracker`
Columns: `Raider`, `Item`, `Tier`, `Award Date`, `Weeks Since`, `Current RDF`.
`Weeks Since` formula: `=INT((TODAY()-D2)/7)`
`Current RDF` formula: `=IF(C2="A",MIN(1,0.8+0.1*E2),IF(C2="B",MIN(1,0.9+0.1*E2),1))`

Set up a filter view to show raiders whose RDF < 0.95 for quick priority checks.

### Sheet 3 – `Data Imports`
- Paste exports from `docs/data/mock_wowaudit.json`, `mock_logs.json`, `mock_droptimizer.json`, and `mock_flps_output.json`.
- Use lookup formulas (`VLOOKUP`, `INDEX/MATCH`) to populate Sheets 1 and 2.

## Notion Alternative
- Create a database with equivalent fields and use formula properties:
  - `RMS = (prop("ACS") * 0.4) + (prop("MAS") * 0.4) + (prop("EPS") * 0.2)`
  - `IPI = (prop("UV") * 0.45) + (prop("Tier Bonus") * 0.35) + (prop("Role Multiplier") * 0.2)`
  - `FLPS = prop("RMS") * prop("IPI") * prop("RDF")`
  - `Eligibility = prop("ACS") >= 0.8 and prop("MAS") > 0`
- Set up filtered views per item and kanban board grouped by `Eligibility` status.

## Usage Tips
- Import mock data to validate formulas before connecting to live exports.
- During raid, duplicate the sheet for each boss to preserve historical decisions.
- Share read-only access with raiders; restrict edit access to council members.
- Note: Replace manual data entry with Kotlin data-sync CSV exports once automation is ready.

### Mock Data Example
- Suggested file name: `EdgeRush_FLPS_Template.xlsx`
- Populate `FLPS Rankings` with values from `docs/flps-walkthrough.md` or `docs/data/mock_flps_output.json` to confirm calculations.
- Store the spreadsheet in a shared drive and link it from Discord (`#loot-announcements`).
