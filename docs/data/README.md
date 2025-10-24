# Mock Data Fixtures

These sample exports illustrate the structure expected from external integrations:

- `mock_wowaudit.json` – Roster context, attendance metrics, crest usage, heroic clears, and tier progress snapshots.
- `mock_logs.json` – Encounter-level deaths per attempt and avoidable damage metrics with spec averages.
- `mock_droptimizer.json` – Raidbots upgrade gains normalized by spec baselines.
- `mock_flps_output.json` – Resulting FLPS calculations produced from the mock dataset using the Kotlin `ScoreCalculator`.

Use these fixtures when prototyping the FLPS calculator or building unit tests before real API access is configured. Real guild exports (e.g., `https://wowaudit.com/REGION/REALM/GUILD/profile`) should stay in local `.env`-referenced storage and must not be committed.
